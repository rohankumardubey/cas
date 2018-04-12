package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.consent.ConsentProperties.Ldap;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link LdapConsentRepository}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Slf4j
public class LdapConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 8561763114482490L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final transient ConnectionFactory connectionFactory;
    private final Ldap ldap;
    private final String searchFilter;

    public LdapConsentRepository(final ConnectionFactory connectionFactory, final Ldap ldap) {
        this.connectionFactory = connectionFactory;
        this.ldap = ldap;
        this.searchFilter = '(' + this.ldap.getSearchFilter() + ')';
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        final var principal = authentication.getPrincipal().getId();
        final var entry = readConsentEntry(principal);
        if (entry != null) {
            final var consentDecisions = entry.getAttribute(this.ldap.getConsentAttributeName());
            if (consentDecisions != null) {
                final var values = consentDecisions.getStringValues();
                LOGGER.debug("Locating consent decision(s) for [{}] and service [{}]", principal, service.getId());
                return values
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .filter(d -> d.getService().equals(service.getId()))
                    .findFirst()
                    .orElse(null);
            }
        }
        return null;
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        final var entry = readConsentEntry(principal);
        if (entry != null) {
            final var consentDecisions = entry.getAttribute(this.ldap.getConsentAttributeName());
            if (consentDecisions != null) {
                LOGGER.debug("Located consent decision for [{}] at attribute [{}]", principal, this.ldap.getConsentAttributeName());
                return consentDecisions.getStringValues()
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .collect(Collectors.toSet());
            }
        }
        return new HashSet<>(0);
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        final var entries = readConsentEntries();
        if (entries != null && !entries.isEmpty()) {
            final Set<ConsentDecision> decisions = new HashSet<>();
            entries
                .stream()
                .map(e -> e.getAttribute(this.ldap.getConsentAttributeName()))
                .filter(Objects::nonNull)
                .map(attr -> attr.getStringValues()
                    .stream()
                    .map(LdapConsentRepository::mapFromJson)
                    .collect(Collectors.toSet()))
                .forEach(decisions::addAll);
            return CollectionUtils.wrap(decisions);
        }
        LOGGER.debug("No consent decision could be found");
        return new HashSet<>(0);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        final var entry = readConsentEntry(decision.getPrincipal());
        if (entry != null) {
            final var newConsent = mergeDecision(entry.getAttribute(this.ldap.getConsentAttributeName()), decision);
            return executeModifyOperation(newConsent, entry);
        }
        return false;
    }

    @Override
    public boolean deleteConsentDecision(final long id) {
        LOGGER.debug("Deleting consent decision [{}]", id);
        final var entries = readConsentEntries();
        entries.forEach(entry -> {
            final var newConsent = removeDecisionById(entry.getAttribute(this.ldap.getConsentAttributeName()), id);
            executeModifyOperation(newConsent, entry);
        });
        return !entries.isEmpty();
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        LOGGER.debug("Deleting consent decisions for principal [{}]", principal);
        final var entry = readConsentEntry(principal);
        if (entry != null) {
            final var newConsent = removeDecision(entry.getAttribute(this.ldap.getConsentAttributeName()),
                Predicates.alwaysFalse());
            return executeModifyOperation(newConsent, entry);
        }
        return false;
    }

    /**
     * Modifies the consent decisions attribute on the entry.
     *
     * @param newConsent new set of consent decisions
     * @param entry      entry of consent decisions
     * @return true / false
     */
    private boolean executeModifyOperation(final Set<String> newConsent, final LdapEntry entry) {
        final Map<String, Set<String>> attrMap = new HashMap<>();
        attrMap.put(this.ldap.getConsentAttributeName(), newConsent);

        LOGGER.debug("Storing consent decisions [{}] at LDAP attribute [{}] for [{}]", newConsent, attrMap.keySet(), entry.getDn());
        return LdapUtils.executeModifyOperation(entry.getDn(), this.connectionFactory, CollectionUtils.wrap(attrMap));
    }

    /**
     * Merges a new decision into existing decisions.
     * Decisions are matched by ID.
     *
     * @param ldapConsent existing consent decisions
     * @param decision    new decision
     * @return new decision set
     */
    private Set<String> mergeDecision(final LdapAttribute ldapConsent, final ConsentDecision decision) {
        if (decision.getId() < 0) {
            decision.setId(System.currentTimeMillis());
        }

        if (ldapConsent != null) {
            final var result = removeDecisionById(ldapConsent, decision.getId());
            final var json = mapToJson(decision);
            if (StringUtils.isBlank(json)) {
                throw new IllegalArgumentException("Could not map consent decision to JSON");
            }
            result.add(json);
            LOGGER.debug("Merged consent decision [{}] with LDAP attribute [{}]", decision, ldapConsent.getName());
            return CollectionUtils.wrap(result);
        }
        final Set<String> result = new HashSet<>();
        final var json = mapToJson(decision);
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException("Could not map consent decision to JSON");
        }
        result.add(json);
        return result;
    }

    private Set<String> removeDecisionById(final LdapAttribute ldapConsent, final long decisionId) {
        return removeDecision(ldapConsent, d -> d.getId() != decisionId);
    }

    /**
     * Removes decision from ldap attribute set.
     *
     * @param ldapConsent the ldap attribute holding consent decisions
     * @param decisionId  the decision Id
     * @return the new decision set
     */
    private Set<String> removeDecision(final LdapAttribute ldapConsent, final Predicate<ConsentDecision> filter) {
        final Set<String> result = new HashSet<>();
        if (ldapConsent.size() != 0) {
            ldapConsent.getStringValues()
                .stream()
                .map(LdapConsentRepository::mapFromJson)
                .filter(filter)
                .map(LdapConsentRepository::mapToJson)
                .filter(Objects::nonNull)
                .forEach(result::add);
        }
        return result;
    }

    /**
     * Fetches a user entry along with its consent attributes.
     *
     * @param principal user name
     * @return the user's LDAP entry
     */
    private LdapEntry readConsentEntry(final String principal) {
        try {
            final var filter = LdapUtils.newLdaptiveSearchFilter(this.searchFilter, CollectionUtils.wrap(Arrays.asList(principal)));
            LOGGER.debug("Locating consent LDAP entry via filter [{}] based on attribute [{}]", filter, this.ldap.getConsentAttributeName());
            final var response =
                LdapUtils.executeSearchOperation(this.connectionFactory, this.ldap.getBaseDn(), filter, this.ldap.getConsentAttributeName());
            if (LdapUtils.containsResultEntry(response)) {
                final var entry = response.getResult().getEntry();
                LOGGER.debug("Locating consent LDAP entry [{}]", entry);
                return entry;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Fetches all user entries that contain consent attributes along with these.
     *
     * @return the collection of user entries
     */
    private Collection<LdapEntry> readConsentEntries() {
        try {
            final var att = this.ldap.getConsentAttributeName();
            final var filter = LdapUtils.newLdaptiveSearchFilter('(' + att + "=*)");

            LOGGER.debug("Locating consent LDAP entries via filter [{}] based on attribute [{}]", filter, att);
            final var response = LdapUtils
                .executeSearchOperation(this.connectionFactory, this.ldap.getBaseDn(), filter, att);
            if (LdapUtils.containsResultEntry(response)) {

                final var results = response.getResult().getEntries();
                LOGGER.debug("Locating [{}] consent LDAP entries", results.size());
                return results;
            }
        } catch (final LdapException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return new HashSet<>(0);
    }

    private static ConsentDecision mapFromJson(final String json) {
        try {
            LOGGER.trace("Mapping JSON value [{}] to consent object", json);
            return MAPPER.readValue(json, ConsentDecision.class);
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static String mapToJson(final ConsentDecision consent) {
        try {
            final var json = MAPPER.writeValueAsString(consent);
            LOGGER.trace("Transformed consent object [{}] as JSON value [{}]", consent, json);
            return json;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
