---
layout: default
title: CAS - Apache Cassandra Authentication
category: Authentication
---
{% include variables.html %}


# Apache Cassandra Authentication

Verify and authenticate credentials using [Apache Cassandra](http://cassandra.apache.org/).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-cassandra-authentication" %}

{% include_cached casproperties.html properties="cas.authn.cassandra" %}

