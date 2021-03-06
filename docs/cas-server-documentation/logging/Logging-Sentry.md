---
layout: default
title: CAS - Sentry Monitoring Integration
category: Logs & Audits
---

{% include variables.html %}

# Overview

[Sentry](https://sentry.io) allows you to track logs and error in real time. It provides insight into production deployments and information to reproduce and fix crashes.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-sentry" %}

The [Logging](../logging/Logging.html) configuration file must be adjusted to match the following:

```xml
<Configuration packages="...,org.apache.logging.log4j.core,com.getsentry.raven.log4j2">
    <Appenders>
        <Raven name="Sentry">
          <dsn><!-- provided by sentry --></dsn>
          <tags>tag1:value1,tag2:value2</tags>
        </Raven>
    ...
    </Appenders>
    ...
    <Loggers>
        ...
        <Logger name="org.apereo" level="info" additivity="false" includeLocation="true">
            <AppenderRef ref="casConsole"/>
            <AppenderRef ref="casFile"/>
            <AppenderRef ref="Sentry"/>
        </Logger>
        ...
    </Loggers>
...
</Configuration>
```

The `Sentry` appender can be mapped to any of the available logger elements defined.
