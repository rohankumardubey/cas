package org.apereo.cas;

import org.apereo.cas.monitor.SessionHealthIndicatorJpaTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.JpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.MySQLJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.OracleJpaTicketRegistryTests;
import org.apereo.cas.ticket.registry.PostgresJpaTicketRegistryCleanerTests;
import org.apereo.cas.ticket.registry.PostgresJpaTicketRegistryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    SessionHealthIndicatorJpaTests.class,
    PostgresJpaTicketRegistryTests.class,
    JpaTicketRegistryTests.class,
    MySQLJpaTicketRegistryTests.class,
    JpaTicketRegistryCleanerTests.class,
    PostgresJpaTicketRegistryCleanerTests.class,
    OracleJpaTicketRegistryTests.class
})
@Suite
public class AllTestsSuite {
}
