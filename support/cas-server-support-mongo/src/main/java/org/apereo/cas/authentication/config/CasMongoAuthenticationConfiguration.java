package org.apereo.cas.authentication.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MongoDbAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CasMongoAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casMongoAuthenticationConfiguration", proxyBeanMethods = false)
public class CasMongoAuthenticationConfiguration {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "mongoPrincipalFactory")
    @Bean
    public PrincipalFactory mongoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "mongoAuthenticationHandler")
    @Autowired
    public AuthenticationHandler mongoAuthenticationHandler(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                            @Qualifier("mongoPrincipalFactory")
                                                            final PrincipalFactory mongoPrincipalFactory,
                                                            @Qualifier("servicesManager")
                                                            final ServicesManager servicesManager,
                                                            @Qualifier("sslContext")
                                                            final SSLContext sslContext) {
        val mongo = casProperties.getAuthn().getMongo();
        val factory = new MongoDbConnectionFactory(sslContext);
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        val handler = new MongoDbAuthenticationHandler(mongo.getName(), servicesManager, mongoPrincipalFactory, mongo, mongoTemplate);
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(mongo.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer mongoAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("mongoAuthenticationHandler")
        final AuthenticationHandler mongoAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(mongoAuthenticationHandler, defaultPrincipalResolver);
    }
}
