# Elasticsearch Setup

> **Source**: *Elasticsearch: The Definitive Guide* — Clinton Gormley & Zachary Tong; *Designing Data-Intensive Applications* — Kleppmann, Ch. 3 (B-Tree Indexes vs. Log-Structured).

---

## Why Elasticsearch in Addition to PostgreSQL?

PostgreSQL's B-tree indexes support prefix matching and range queries efficiently. They do not support:

- **Full-text relevance search**: "Find products containing 'men shoes leather' across name, description, and tags" — PostgreSQL's `LIKE '%shoes%'` requires a full table scan; B-tree cannot help.
- **Fuzzy matching**: A customer searches "adidass" (typo) and expects to find Adidas products.
- **Multi-field weighted scoring**: Name match scores higher than description match; exact match scores higher than partial match.

For these use cases — product search in Commerce, customer search in Pay, and rider/employee search in HR — Elasticsearch is the right tool.

PostgreSQL remains the system of record. Elasticsearch is a **search index** — a derived read store maintained by processing domain events. It is never written to directly from use cases; it is populated by dedicated `*SearchIndexer` Kafka listeners.

---

## Index Inventory

| Index | Module | Purpose |
|---|---|---|
| `products` | Commerce | Full-text product search, barcode lookup, category filtering |
| `customers` | Pay / Commerce | Customer search by name, email, phone |
| `vendors` | Commerce | Vendor search by business name |
| `employees` | HR | Employee search by name, department, designation |
| `shipments` | Logistics | Shipment search by tracking number, recipient name |

---

## Index Schemas

### Products Index

```json
{
  "settings": {
    "analysis": {
      "analyzer": {
        "product_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "asciifolding", "product_edge_ngram"]
        },
        "product_search_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "asciifolding"]
        }
      },
      "filter": {
        "product_edge_ngram": {
          "type": "edge_ngram",
          "min_gram": 2,
          "max_gram": 20
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id":             { "type": "long" },
      "organizationId": { "type": "long" },
      "vendorId":       { "type": "long" },
      "code":           { "type": "keyword" },
      "name": {
        "type": "text",
        "analyzer": "product_analyzer",
        "search_analyzer": "product_search_analyzer",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "description":    { "type": "text", "analyzer": "product_analyzer" },
      "categoryId":     { "type": "long" },
      "categoryName":   { "type": "keyword" },
      "status":         { "type": "keyword" },
      "sellingPrice":   { "type": "scaled_float", "scaling_factor": 100 },
      "currency":       { "type": "keyword" },
      "hasStock":       { "type": "boolean" },
      "updatedAt":      { "type": "date" }
    }
  }
}
```

`edge_ngram` on `name` enables prefix autocomplete: a search for "leat" matches "leather boots", "leather jacket", etc. The separate `search_analyzer` (without ngram) ensures the query terms are not also expanded — avoids irrelevant matches.

---

## Synchronization Strategy

Elasticsearch documents are maintained by Kafka listeners that consume domain events. This is the **CDC (Change Data Capture) via Domain Events** pattern — preferable to polling the database because:

1. It reacts in real time (seconds), not on a polling interval
2. It does not create read load on the operational PostgreSQL instance
3. It is already aligned with the AtlasHub event architecture

```java
// In atlashub-commerce:adapter/in/messaging
@Component
public class ProductSearchIndexer extends BaseKafkaEventListener {

    private static final String GROUP_ID = "commerce-search-indexer";

    private final ElasticsearchOperations elasticsearchOps;

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void onCommerceEvent(String payload) {

        processEventIfMatches(payload, "ProductCreatedEvent", ProductCreatedEvent.class,
            log, GROUP_ID, event -> {
                ProductSearchDocument doc = buildDocument(event.payload());
                elasticsearchOps.save(doc);
            });

        processEventIfMatches(payload, "ProductUpdatedEvent", ProductUpdatedEvent.class,
            log, GROUP_ID, event -> {
                elasticsearchOps.update(
                    UpdateQuery.builder(String.valueOf(event.payload().productId()))
                        .withDocument(Document.from(buildUpdateMap(event.payload())))
                        .build(),
                    IndexCoordinates.of("products")
                );
            });

        processEventIfMatches(payload, "ProductDeletedEvent", ProductDeletedEvent.class,
            log, GROUP_ID, event ->
                elasticsearchOps.delete(
                    String.valueOf(event.payload().productId()),
                    IndexCoordinates.of("products")
                )
        );
    }
}
```

### Full Reindex (Bootstrap / Recovery)

When the Elasticsearch cluster is rebuilt or an index is corrupted, a full reindex reads from PostgreSQL:

```java
// Admin-triggered only, not part of normal operation
@Service
public class ProductReindexUseCase {

    private final SpringDataProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOps;

    public void reindex(Long orgId) {
        // Stream to avoid loading all rows into memory
        productRepository.streamByOrganizationId(orgId).forEach(entity -> {
            ProductSearchDocument doc = mapToDocument(entity);
            elasticsearchOps.save(doc);
        });
    }
}
```

---

## Search Queries

```java
// Product search — full-text + filters
@Repository
public class ProductSearchRepository {

    private final ElasticsearchOperations elasticsearchOps;

    public SearchPage<ProductSearchDocument> search(Long orgId, String q,
                                                     Long categoryId, Boolean inStock,
                                                     Pageable pageable) {
        BoolQuery.Builder bool = new BoolQuery.Builder()
            .must(TermQuery.of(t -> t.field("organizationId").value(orgId))._toQuery());

        if (q != null && !q.isBlank()) {
            bool.must(MultiMatchQuery.of(m -> m
                .fields("name^3", "description^1", "code^2")  // name weighted 3x
                .query(q)
                .fuzziness("AUTO")                             // handles typos
                .type(TextQueryType.BestFields)
            )._toQuery());
        }

        if (categoryId != null) {
            bool.filter(TermQuery.of(t -> t.field("categoryId").value(categoryId))._toQuery());
        }
        if (Boolean.TRUE.equals(inStock)) {
            bool.filter(TermQuery.of(t -> t.field("hasStock").value(true))._toQuery());
        }

        NativeQuery query = NativeQuery.builder()
            .withQuery(bool.build()._toQuery())
            .withPageable(pageable)
            .build();

        return elasticsearchOps.searchForPage(query, ProductSearchDocument.class);
    }
}
```

---

## Docker Compose

```yaml
elasticsearch:
  image: elasticsearch:8.13.0
  environment:
    - discovery.type=single-node
    - xpack.security.enabled=false          # disable for local dev only
    - ES_JAVA_OPTS=-Xms512m -Xmx512m
    - bootstrap.memory_lock=true
  ulimits:
    memlock:
      soft: -1
      hard: -1
  ports:
    - "9200:9200"
  volumes:
    - es-data:/usr/share/elasticsearch/data
```

---

## Operational Notes

- **Lag tolerance**: Elasticsearch can be a few seconds behind PostgreSQL. This is acceptable — search results showing a just-created product after a few seconds is fine. It is never acceptable for financial balances or inventory counts (those use PostgreSQL directly).
- **Elasticsearch is not a source of truth.** If the index is lost, it can be fully rebuilt by re-consuming Kafka events or reading from PostgreSQL. Nothing is lost.
- **Index aliases**: Use index aliases (`products_v1` aliased to `products`) to enable zero-downtime reindexing. To change the schema, create `products_v2`, populate it, then atomically swap the alias.
