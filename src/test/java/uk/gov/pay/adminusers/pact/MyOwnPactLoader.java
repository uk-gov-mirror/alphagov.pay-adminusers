package uk.gov.pay.adminusers.pact;

import au.com.dius.pact.model.Pact;
import au.com.dius.pact.provider.junit.loader.PactLoader;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyOwnPactLoader implements PactLoader {
    @Override
    public List<Pact> load(String s) throws IOException {
        // http://192.168.99.100/pacts/provider/AdminUsers/latest/expecting_bobs
        String url = String.format(Locale.ENGLISH, "http://192.168.99.100/pacts/provider/%s/latest/%s", s, tag);
        return null;
    }
}
