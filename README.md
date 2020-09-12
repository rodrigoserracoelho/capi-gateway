![CAPI-CICD](https://github.com/rodrigoserracoelho/capi-gateway/workflows/CAPI-CICD/badge.svg?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# CAPI Gateway
###### Light API Gateway implemented with Apache Camel

![CAPI-Gateway-Diagram](https://surisoft.io/assets/images/CAPI-Gateway-white.png)

## Supports:
* Easy deployment of Swagger and Open API endpoints.
* Authorization (Built-in authorization server).
* Easy to block your API after N failed attempts. (you can define blocking strategies)
* REST endpoint to manage your API's.
* Automatically creates metrics for Prometheus (Verb granularity)
* Automatically creates zipkin traceability.
* Automatically creates a Grafana dashboard with panels per verb (only count total for the moment)
* Throttling (You can apply per API or per API Path)
* Easy Installation (See Play with CAPI Gateway)

## Soon available:
* Angular API Manager Interface.
* Keycloak integration (for replacing the authorization server)

## Example of an API definition

    {
        "id": "<UUID generated by the API Manager>",
        "endpoints": [
            "yourapidomain:8080",
            "yourapidomain:8081"
        ],
        "endpointType": "HTTP",
        "connectTimeout": -1,
        "socketTimeout": 10000,
        "name": "YOUR HUMAN UNIQUE NAME",
        "secured": true,
        "context": "yourcontext",
        "swaggerEndpoint": "http://yourapidomain:8080/v2/api-docs",
        "blockIfInError": true,
        "maxAllowedFailedCalls": 10,
        "zipkinTraceIdVisible": true,
        "internalExceptionMessageVisible": true,
        "internalExceptionVisible": true,
        "returnAPIError": false,
        "unblockAfter": true,
        "unblockAfterMinutes": 1,
        "throttlingPolicy": {
            "maxCallsAllowed": 1000,
            "periodForMaxCalls": 60000
        }
    }
### Field Description

 * endpoints (Array) - If you specify more than one endpoint, then CAPI will load balance (round robin fashion)
 * endpointType (HTTP, HTTPS) - If you are exposing on HTTPS its important to add your certificate to CAPI trust store. The API Manager exposes an API for managing your certificates.
 * connectTimeout (default 2 minutes) - You can specify the timeout for CAPI to try to connect to your endpoint.
 * socketTimeout (default 2 minutes) - You can specify the timeout for CAPI to wait for a response from your endpoint.
 * name (string) 
 * secured (boolean) - If true, CAPI will check if the token was signed by the authorization server (API Manager), and if your client is subscribed to the API. (For the moment we only support oauth2 bearer tokens).
 * context (string) - Context will be the path of your API in the CAPI environment. Ex.: https://localhost/gateway/yourcontext/
* swaggerEndpoint (string) - Your swagger endpoint
 * blockIfInError (boolean) - If true, and if your api retuns an error (!200) CAPI will suspend the route to the faulty path, for the amount of time defined in the field: unblockAfterMinutes and after the number of attempts defined in the field: maxAllowedFailedCalls
 

## Capi Client Object 
##### Will be replaced by Keycloak clients

Example of a capi client (with the password: web-client-secret)

    {
        "_id" : ObjectId("5dad5972f14df639474b4669"),
        "clientId" : "web-publisher",
        "resourceIds" : [],
        "secretRequired" : true,
        "clientSecret" : "$2a$10$oQBqS4ZOEiIGVNiZnB0nMuFw/n/Od57IG/uy4nFuOJxLtHE/Z5jDC",
        "scoped" : false,
        "scope" : [ 
            "read-foo"
        ],
        "authorizedGrantTypes" : [ 
            "refresh_token", 
            "password", 
            "client_credentials", 
            "authorization_code"
        ],
        "registeredRedirectUri" : [],
        "authorities" : [ 
            {
                "role" : "ROLE_USER",
                "_class" : "org.springframework.security.core.authority.SimpleGrantedAuthority"
            }, 
            {
                "role" : "ROLE_PUBLISHER",
                "_class" : "org.springframework.security.core.authority.SimpleGrantedAuthority"
            }
        ],
        "accessTokenValiditySeconds" : 60,
        "refreshTokenValiditySeconds" : 14400,
        "autoApprove" : false
    }

## Consuming your API
If you wish to enable security for your API (api.secured = true), then you will need to subscribe your API with a "capi client".
Your API ID will be added as an authority in the authorities list of your client.

    "authorities" : [ 
        {
            "role" : "ROLE_USER",
            "_class" : "org.springframework.security.core.authority.SimpleGrantedAuthority"
        }, 
        {
            "role" : "ROLE_PUBLISHER",
            "_class" : "org.springframework.security.core.authority.SimpleGrantedAuthority"
        }, 
        {
            "role" : "YOUR API ID",
            "_class" : "org.springframework.security.core.authority.SimpleGrantedAuthority"
        }
    ]

Don't forget to request a new token, after subscribing.


## Play with CAPI Gateway

#### Prerequisites
 - Kubernetes 1.9.2+ or Docker with docker-compose

For kubernetes, please visit: https://github.com/rodrigoserracoelho/capi-gateway-k8s

For Docker, please visit: https://github.com/rodrigoserracoelho/capi-docker

## What we use:
* Apache Camel
* Spring boot
* Hazelcast distributed cache
* Prometheus
* Grafana
* Zipkin
* Mongo
* Kafka
* Zookeeper
* Keycloak (under implementation)

## Some load results (Calling a protected service)
#### Using apache benchmark on a 1 node docker container with SSL 
    
    Results for 20k calls 1000 concurrency level:
    Server Hostname:        localhost
    Server Port:            8380
    SSL/TLS Protocol:       TLSv1.2,ECDHE-RSA-AES256-GCM-SHA384,2048,256
    Server Temp Key:        ECDH P-256 256 bits
    TLS Server Name:        localhost

    Document Path:          /gateway/myctx/internal/12345
    Document Length:        33 bytes

    Concurrency Level:      1000
    Time taken for tests:   65.563 seconds
    Complete requests:      20000
    Failed requests:        0
    Total transferred:      6560000 bytes
    HTML transferred:       660000 bytes
    Requests per second:    305.05 [#/sec] (mean)
    Time per request:       3278.129 [ms] (mean)
    Time per request:       3.278 [ms] (mean, across all concurrent requests)
    Transfer rate:          97.71 [Kbytes/sec] received

    Connection Times (ms)
                  min  mean[+/-sd] median   max
    Connect:        7 2431 1388.0   2260   18381
    Processing:     5  798 883.7    684   13862
    Waiting:        3  796 883.3    681   13862
    Total:         58 3229 1683.6   3091   18639
