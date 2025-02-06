package com.identitytailor.keycloak.ssf.receiver;

import com.google.auto.service.AutoService;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

@AutoService(Spi.class)
public class SharedSignalsReceiverSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "shared-signals-receiver";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return SharedSignalsReceiver.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SharedSignalsReceiverFactory.class;
    }
}
