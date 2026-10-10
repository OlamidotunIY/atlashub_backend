package com.atlashub.paystack.infrastructure.external.paystack.adapters;

import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClientRegistry;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSettlementClient;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.settlement.PaystackSettlementData;
import com.atlashub.shared.application.port.SettlementProviderPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class PaystackSettlementAdapter implements SettlementProviderPort {
    private static final int PAGE_SIZE=100;
    private final ObjectProvider<PaystackClientRegistry> registries;
    public PaystackSettlementAdapter(ObjectProvider<PaystackClientRegistry> registries){this.registries=registries;}
    @Override public List<Batch> fetchSuccessfulSettlements(ApiEnvironment environment,String subaccountCode,String afterId){
        if(environment!=ApiEnvironment.LIVE)return List.of();PaystackClientRegistry registry=registries.getIfAvailable();
        if(registry==null)throw new IllegalStateException("Paystack integration is disabled");
        PaystackSettlementClient client=registry.forEnvironment(PaystackEnvironment.LIVE).settlements();
        List<Batch>batches=new ArrayList<>();int page=1,pageCount=1;
        do{PaystackResponse<List<PaystackSettlementData>> response=client.list(subaccountCode,page,PAGE_SIZE);
            if(response==null||!response.status()||response.data()==null)throw new IllegalStateException("Paystack settlement listing failed");
            for(PaystackSettlementData data:response.data()){String id=String.valueOf(data.id());
                if(!"success".equalsIgnoreCase(data.status())||!isAfter(id,afterId))continue;
                CurrencyCode currency=CurrencyCode.valueOf(data.currency());long fees=Math.max(0,data.fees());
                long net=Math.max(0,data.amount());long gross=data.totalAmount()>0?data.totalAmount():net+fees;
                batches.add(new Batch(id,money(gross,currency),money(net,currency),money(fees,currency),
                        data.settledAt()!=null?data.settledAt():data.settlementDate(),transactions(client,id)));}
            pageCount=response.meta()!=null&&response.meta().pageCount()!=null?response.meta().pageCount():page;page++;
        }while(page<=pageCount);batches.sort(Comparator.comparingLong(b->Long.parseLong(b.providerSettlementId())));return batches;}
    private List<String>transactions(PaystackSettlementClient client,String id){List<String>refs=new ArrayList<>();int page=1,pageCount=1;
        do{var response=client.transactions(id,page,PAGE_SIZE);if(response==null||!response.status()||response.data()==null)
            throw new IllegalStateException("Paystack settlement transaction listing failed");
            response.data().stream().map(t->t.reference()).filter(Objects::nonNull).forEach(refs::add);
            pageCount=response.meta()!=null&&response.meta().pageCount()!=null?response.meta().pageCount():page;page++;
        }while(page<=pageCount);return List.copyOf(refs);}
    private Money money(long minor,CurrencyCode currency){return new Money(BigDecimal.valueOf(minor,2),currency);}
    private boolean isAfter(String id,String after){return after==null||after.isBlank()||Long.parseLong(id)>Long.parseLong(after);}
}
