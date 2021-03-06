package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.crypto.CipherExecutor;

/**
 * This is {@link OAuth20JwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class OAuth20JwtBuilder extends JwtBuilder {
    public OAuth20JwtBuilder(final CipherExecutor defaultTokenCipherExecutor,
                             final ServicesManager servicesManager,
                             final RegisteredServiceCipherExecutor registeredServiceCipherExecutor,
                             final CasConfigurationProperties casProperties) {
        super(defaultTokenCipherExecutor, servicesManager,
            registeredServiceCipherExecutor, casProperties);
    }

    @Override
    protected RegisteredService locateRegisteredService(final String id) {
        var service = super.locateRegisteredService(id);
        if (service == null) {
            service = OAuth20Utils.getRegisteredOAuthServiceByClientId(getServicesManager(), id);
        }
        return service;
    }
}
