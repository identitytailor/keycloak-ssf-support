package com.identitytailor.keycloak.ssf;

import com.google.auto.service.AutoService;
import org.keycloak.provider.Provider;
import org.keycloak.provider.Spi;

@AutoService(Spi.class)
public class SharedSignalsSpi implements Spi {

    @Override
    public String getName() {
        return "ssf";
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SharedSignalsProvider.class;
    }

    @Override
    public Class<? extends SharedSignalsProviderFactory> getProviderFactoryClass() {
        return SharedSignalsProviderFactory.class;
    }
}
