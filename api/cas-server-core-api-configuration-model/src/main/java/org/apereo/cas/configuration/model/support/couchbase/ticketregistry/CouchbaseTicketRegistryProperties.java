package org.apereo.cas.configuration.model.support.couchbase.ticketregistry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link CouchbaseTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.6
 */
@RequiresModule(name = "cas-server-support-couchbase-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
@Deprecated(since = "6.6")
public class CouchbaseTicketRegistryProperties extends BaseCouchbaseProperties {

    private static final long serialVersionUID = 2123040809519673836L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    public CouchbaseTicketRegistryProperties() {
        this.crypto.setEnabled(false);
    }
}
