package org.keycloak.examples.totp.time;


import org.keycloak.examples.totp.exceptions.TimeProviderException;

public interface TimeProvider {
    /**
     * @return The number of seconds since Jan 1st 1970, 00:00:00 UTC.
     */
    long getTime() throws TimeProviderException;
}
