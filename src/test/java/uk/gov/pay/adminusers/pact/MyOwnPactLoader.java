package uk.gov.pay.adminusers.pact;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.provider.junit.loader.PactBrokerLoader;
import au.com.dius.pact.provider.junit.loader.PactFolderLoader;
import au.com.dius.pact.provider.junit.loader.PactLoader;
import com.google.inject.Inject;
import uk.gov.pay.adminusers.app.config.AdminUsersConfig;
import uk.gov.pay.adminusers.app.config.PactConfiguration;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyOwnPactLoader implements PactLoader {

    @Inject
    private static AdminUsersConfig configuration;

    @Override
    public List<Pact> load(String providerName) throws IOException {
        PactConfiguration pactConfiguration = configuration.getPactConfiguration();
        PactBrokerLoader pbl = new PactBrokerLoader(pactConfiguration.getHost(), pactConfiguration.getPort(),
                pactConfiguration.getProtocol(), Arrays.asList(pactConfiguration.getTags()));
        return pbl.load(providerName);
    }

}
