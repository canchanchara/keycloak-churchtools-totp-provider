package org.keycloak.examples.totp.time;


import org.keycloak.examples.totp.exceptions.TimeProviderException;

import java.time.Instant;

public class SystemTimeProvider implements TimeProvider {
    @Override
    public long getTime() throws TimeProviderException {
        return Instant.now().getEpochSecond();
    }
}
