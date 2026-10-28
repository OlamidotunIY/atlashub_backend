package com.atlashub.integrations.application;

import com.atlashub.integrations.domain.*;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StripeWebhookServiceTest {
    @Test void signed_deauthorization_revokes_the_matching_authorization_and_replay_is_idempotent() throws Exception {
        ConnectionServiceTest.MemoryRepository repo=new ConnectionServiceTest.MemoryRepository();repo.saveConnection(
                new ProviderConnection(1L,2L,Provider.STRIPE,"US",ProviderConnection.Status.CONNECTED));repo.saveAuthorization(
                new ProviderAuthorization(7L,1L,ApiEnvironment.TEST,"acct_1",ProviderAuthorization.Status.ACTIVE,
                        ProviderAuthorization.Health.HEALTHY,Set.of(Capability.CARD_COLLECTION),"vault"));
        Set<String> seen=new HashSet<>();WebhookInbox inbox=new WebhookInbox(){public boolean begin(String p,Long c,String e,byte[] b){return seen.add(e);}public void markPublished(String p,Long c,String e){}};
        StripeWebhookService service=new StripeWebhookService(repo,inbox,new ObjectMapper(),new ConnectionServiceTest.Vault());set(service,"testSecret","whsec_test");set(service,"liveSecret","whsec_live");
        String payload="{\"id\":\"evt_1\",\"type\":\"account.application.deauthorized\",\"account\":\"acct_1\"}";
        long time=Instant.now().getEpochSecond();String header="t="+time+",v1="+hmac(time+"."+payload,"whsec_test");
        service.receive(payload.getBytes(StandardCharsets.UTF_8),header);service.receive(payload.getBytes(StandardCharsets.UTF_8),header);
        assertEquals(ProviderAuthorization.Status.REVOKED,repo.findAuthorization(7L).orElseThrow().status());
        assertThrows(IllegalArgumentException.class,()->service.receive(payload.getBytes(StandardCharsets.UTF_8),"t="+time+",v1=bad"));
    }
    private void set(Object target,String name,String value)throws Exception{var field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.set(target,value);}
    private String hmac(String value,String secret)throws Exception{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));}
}
