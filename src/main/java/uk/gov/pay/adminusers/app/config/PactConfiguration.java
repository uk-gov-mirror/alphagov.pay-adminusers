package uk.gov.pay.adminusers.app.config;

import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PactConfiguration extends Configuration {

    @Valid
    @NotNull
    private String host;

    @Valid
    @NotNull
    private String port;

    @Valid
    @NotNull
    private String protocol;

    @Valid
    @NotNull
    private String[] tags;


    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String[] getTags() {
        return tags;
    }
}
