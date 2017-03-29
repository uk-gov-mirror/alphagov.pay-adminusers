package uk.gov.pay.adminusers.pact;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.provider.junit.loader.PactBrokerLoader;
import au.com.dius.pact.provider.junit.loader.PactLoader;
import com.google.inject.Inject;
import uk.gov.pay.adminusers.app.config.AdminUsersConfig;
import uk.gov.pay.adminusers.app.config.PactConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.c;

public class MyOwnPactLoader implements PactLoader {

    @Inject
    private static AdminUsersConfig configuration;

    public MyOwnPactLoader() {

    }

    @Override
    public List<Pact> load(String providerName) throws IOException {
        PactConfiguration pactConfiguration = configuration.getPactConfiguration();
        PactBrokerLoader pbl = new PactBrokerLoader(pactConfiguration.getHost(), pactConfiguration.getPort(),
                pactConfiguration.getProtocol(), Arrays.asList(pactConfiguration.getTags()));
        return pbl.load(providerName);
    }

}
