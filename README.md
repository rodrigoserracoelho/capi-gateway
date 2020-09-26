![CAPI-CICD](https://github.com/rodrigoserracoelho/capi-gateway/workflows/CAPI-CICD/badge.svg?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![Docker Image Version (latest by date)](https://img.shields.io/docker/v/surisoft/capi-gateway)

# CAPI Gateway
###### Light API Gateway implemented with Apache Camel

![CAPI-Gateway-Diagram](https://surisoft.io/assets/images/CAPI-Gateway-white.png)

## Supports:
* Light API Gateway powered by Apache Camel dynamics routes.
* Customizable processors
* Easy deployment of Swagger and Open API endpoints.
* Keycloak integration, for API Manager and Deployed API's
* REST endpoint to manage your API's.
* Distributed tracing system (Zipkin)
* Metrics (Prometheus)
* Hawtio (JVM management console)
* API Subscription Engine (Keycloak)
* Traffic management (Apache Camel Kafka)
* Analytics for the metrics (Grafana) 
* Error/Blocking strategies.

## Soon available:
* Angular API Manager Interface. (Demo version available for Helm users: https://github.com/rodrigoserracoelho/capi-charts)
* Web Socket Gateway

## Example of an API definition

    {
    	"endpoints": [
    		"your.app.node1:8080",
    		"your.app.node2:8080"
    	],
    	"endpointType": "HTTP",
    	"connectTimeout": 0,
    	"socketTimeout": 0,
    	"name": "api-name",
    	"secured": true,
    	"context": "api-context",
    	"swagger": true,
    	"swaggerEndpoint": "http://your.server/v2/api-docs",
    	"blockIfInError": false,
    	"maxAllowedFailedCalls": 0,
    	"zipkinTraceIdVisible": false,
    	"internalExceptionMessageVisible": false,
    	"internalExceptionVisible": false,
    	"returnAPIError": false,
    	"unblockAfter": false,
    	"unblockAfterMinutes": 0,
    	"clientID": "your-app-role",
    	"corsEnabled": true,
    	"credentialsAllowed": true,
    	"allowedOrigins": [
    		"http://your.web.app:4300",
    		"http://another.web.app:4400"
    	],
    	"throttlingPolicy": {
    		"maxCallsAllowed": 10,
    		"periodForMaxCalls": 60000,
    		"applyPerPath": true
    	}
    }
### Field Description

 * endpoints (Array) - If you specify more than one endpoint, then CAPI will load balance (round robin fashion)
 * endpointType (HTTP, HTTPS) - If you are exposing on HTTPS it is important to add your certificate to CAPI trust store. The API Manager exposes an API for managing your certificates.
 * connectTimeout (default 2 minutes) - You can specify the timeout for CAPI to try to connect to your endpoint.
 * socketTimeout (default 2 minutes) - You can specify the timeout for CAPI to wait for a response from your endpoint.
 * name (string) 
 * context (string) - Context will be the path of your API in the CAPI environment. Ex.: https://localhost/gateway/yourcontext/
 * secured (boolean) (CAPI only supports Bearer JWT Tokens) - If true, CAPI will check:
    * If a token is provided.
    * It the provided token was signed by the Keycloak instance.
    * If the client and/or user subscribed to the role. (check clientID)
 * clientID (string) The API role on Keycloak.
 * swaggerEndpoint (string) - Your swagger endpoint
 * blockIfInError (boolean) - If true, and if your api retuns an error (!200) CAPI will suspend the route to the faulty path, for the amount of time defined in the field: unblockAfterMinutes and after the number of attempts defined in the field: maxAllowedFailedCalls
 * corsEnabled  If true CAPI will allow pre-flight requests from browsers (AJAX calls). 
 * allowedOrigins (Array) - If CORS is enabled then calls will be allowed to the declared origins.
 
## Play with CAPI Gateway

#### Prerequisites
  * Kubernetes 1.9.2+
  * Helm v3.3.1
  * Clone CAPI Helm Charts: https://github.com/rodrigoserracoelho/capi-charts
  * Follow the instructions.
  
For Docker, please visit: https://github.com/rodrigoserracoelho/capi-docker

## What we use:
* Apache Camel
* Spring Boot
* Hazelcast
* Prometheus
* Grafana
* Zipkin
* Mongo
* Kafka
* Zookeeper
* Keycloak
* Hawtio