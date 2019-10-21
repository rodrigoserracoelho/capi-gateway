# CAPI Gateway
###### Light API Gateway implemented with Apache Camel

## Supports:
* Easy deployment of Swagger and Open API endpoints.
* Authorization (for the moment only access_token and password grant_types).
* Easy to block your API after N failed attempts. (you can define blocking strategies)
* REST endpoint to manage your API's.
* Automatically creates metrics for Prometheus (Verb granularity)
* Automatically creates zipkin traceability.
* Automatically creates a Grafana dashboard with panels per verb (only count total for the moment)
* Throttling (You can apply per API or per API Path)
* Easy Installation (See Play with CAPI Gateway)
* Easy enable HTTPS by adding your own certificate (read docker compose file)

## What we want to support:
* Implicit and Authorization code grant type.
* Decent UI for management (Need time, any front enders out there?)

## Example of an API definition

    {
        "_id" : "02c04c49-3304-4dec-b5d4-446327f688f0",
        "endpoint" : "localhost:8080",
        "endpointType" : "HTTP",
        "name" : "YOUR-API",
        "secured" : true,
        "context" : "your-api",
        "swagger" : true,
        "swaggerEndpoint" : "http://localhost:8080/v2/api-docs",
        "blockIfInError" : true,
        "maxAllowedFailedCalls" : 10,
        "unblockAfter" : true,
        "unblockAfterMinutes" : 2,
        "throttlingPolicy" : {
            "maxCallsAllowed" : "100",
            "periodForMaxCalls" : "60000",
            "applyPerPath" : true
        }
    }

With the following configuration your service will be available at: http://localhost:8380/gateway/your-api/

The following configuration will be applied:
* secured: true - Meaning, that the CAPI Gateway expects a Bearer token sign by your account at auth0 with your audience in the token claims.
* blockIfInError: true - Means that for instance if you send more than 10 times (maxAllowedFailedCalls) the wrong token your API will be suspended for 2 minutes (unblockAfterMinutes).
* throttlingPolicy.maxCallsAllowed: 100 / throttlingPolicy.periodForMaxCalls - You can only call your API/Path 100 times per minute.
* throttlingPolicy.applyPerPath: true - If true the policy will be applied by path and NOT the total amount for the API.

## API Object
You can define your own paths, in case you dont have a Swagger Endpoint (Swagger 2/Open API), so if swagger: false, then CAPI will look for a list of PATH like the below example:

    {
        "_id" : "91ab7422-7d37-454b-9b33-6f3e345c8b66",
        "endpoint" : "localhost:8080",
        "endpointType" : "HTTPS",
        "name" : "YOUR-CUSTOM-API",
        "secured" : false,
        "context" : "your-custom-api",
        "blockIfInError" : false
        "paths" : [ 
            {
            "verb" : "GET",
            "path" : "/services/path"
            },
            {
            "verb" : "POST",
            "path" : "/services/path"
            }
        ],
        "swagger" : false
    }

## Capi Client Object
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
* Clone the project or just copy the docker-compose file.
* Execute
    $ sudo docker-compose up -d
* If you are starting a fresh Mongo instance you need to create a default client, in order to be able to request your first access token. Create a collection call "capi_client" and insert the document like defined in the Capi Client Object
* Request your first access token: curl -X POST https://localhost:8080/oauth/token -H 'Authorization: Basic d2ViLXB1Ymxpc2hlcjp3ZWItY2xpZW50LXNlY3JldA==' -H 'Content-Type: multipart/form-data;' -F grant_type=client_credentials -F 'response_type=access_token'
* Go to: http://localhost:8080/swagger-ui.html
* Authenticate with the token you obtained from the previous step. (Don't forget to specify: Bearer <the token>)
* Publish your first API: 
    curl -X POST "http://localhost:8080/route/simple-rest" -H "accept: application/json" -H "Content-Type: application/json" -d "<your-api>" (see Example of an API definition)
* Imagine that your context was: test and one of your GET path was /user you can then test: http://localhost:8380/gateway/test/user

Docker compose will create instances of Grafana, Prometheus and Zipkin, but if you wish to use already existing instances you just need to change this environment variables:

* api.gateway.prometheus.endpoint=http://prometheus:9090
* api.gateway.zipkin.endpoint=http://zipkin:9411/api/v2/spans
* api.gateway.grafana.endpoint=http://localhost:8080/grafana

## What we use:
* Apache Camel
* Spring boot
* Hazelcast distributed cache
* Prometheus
* Grafana
* Zipkin
* Mongo