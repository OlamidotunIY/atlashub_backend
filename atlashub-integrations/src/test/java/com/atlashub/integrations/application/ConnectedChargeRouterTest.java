package com.atlashub.integrations.application;

import com.atlashub.integrations.domain.*;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ConnectedChargeRouterTest {
    @Test void resolves_authorization_secret_without_exposing_it(){
        var repo=repository(ProviderAuthorization.Health.HEALTHY);ConnectedChargeAdapter adapter=new ConnectedChargeAdapter(){public Provider provider(){return Provider.PAYSTACK;}
            public ChargeProviderPort.InitializationResult initialize(ChargeProviderPort.InitializationRequest r,String secret){assertEquals("secret",secret);return new ChargeProviderPort.InitializationResult("ref","url","access");}
            public ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest r,String s){throw new UnsupportedOperationException();}
            public ChargeProviderPort.ProviderStatus fetchStatus(String r,String s){throw new UnsupportedOperationException();}};
        ConnectedChargeRouter router=new ConnectedChargeRouter(repo,new CredentialVaultPort(){public String store(String s){return s;}public String read(String r){return "secret";}},List.of(adapter));
        assertEquals("ref",router.initialize(new ChargeProviderPort.InitializationRequest(ApiEnvironment.TEST,"a@b.com",BigDecimal.TEN,CurrencyCode.NGN,
                ChargeProviderPort.ChargeChannel.CARD,"order",null,Map.of(),7L)).providerReference());
    }
    @Test void rejects_unhealthy_authorization(){ConnectedChargeRouter router=new ConnectedChargeRouter(repository(ProviderAuthorization.Health.DOWN),
            new CredentialVaultPort(){public String store(String s){return s;}public String read(String r){return "secret";}},List.of());
        assertThrows(IllegalStateException.class,()->router.initialize(new ChargeProviderPort.InitializationRequest(ApiEnvironment.TEST,"a@b.com",BigDecimal.TEN,
                CurrencyCode.NGN,ChargeProviderPort.ChargeChannel.CARD,"order",null,Map.of(),7L)));}
    private ConnectionRepository repository(ProviderAuthorization.Health health){return new ConnectionServiceTest.MemoryRepository(){{
        saveConnection(new ProviderConnection(3L,2L,Provider.PAYSTACK,"NG",ProviderConnection.Status.CONNECTED));
        saveAuthorization(new ProviderAuthorization(7L,3L,ApiEnvironment.TEST,"merchant",ProviderAuthorization.Status.ACTIVE,health,Set.of(Capability.CARD_COLLECTION),"vault"));
    }};}
}
