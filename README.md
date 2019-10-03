# CAPI Gateway
## Light API Gateway implemented with Apache Camel

## Supports:
* Easy deployment of Swagger and Open API endpoints.
* Authorization (for the moment only supporting Auth0).
* Easy to block your API after N failed attempts. (you can define blocking strategies)
* REST endpoint to manage your API's.
* Automatically creates metrics for Prometheus (Verb granularity)
* Automatically creates zipkin traceability.
* Automatically creates a Grafana dashboard with panels per verb (only count total for the moment)

## What we use:
* Apache Camel
* Spring boot
* Hazelcast distributed cache
* Prometheus
* Grafana
* Zipkin
* Mongo