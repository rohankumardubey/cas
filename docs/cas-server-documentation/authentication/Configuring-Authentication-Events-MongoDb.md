---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# MongoDb Authentication Events

Stores authentication events into a MongoDb NoSQL database.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-events-mongo" %}

{% include_cached casproperties.html properties="cas.events.mongo" %}

