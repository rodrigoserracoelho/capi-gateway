/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.EndpointType;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.processor.MetricsProcessor;
import at.rodrigo.api.gateway.processor.PathVariableProcessor;
import at.rodrigo.api.gateway.processor.RouteErrorProcessor;
import at.rodrigo.api.gateway.routes.PathRouteRepublisher;
import at.rodrigo.api.gateway.routes.SuspendedRouteBuilder;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.zipkin.ZipkinTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.language.constant.ConstantLanguage.constant;


@Component
@Slf4j
public class CamelUtils {

    @Value("${api.gateway.traffic.inspector.enabled}")
    private boolean trafficInspectorEnabled;

    @Value("${api.gateway.traffic.inspector.kafka.topic}")
    private String trafficInspectorKafkaTopic;

    @Value("${api.gateway.traffic.inspector.kafka.broker}")
    private String trafficInspectorKafkaBroker;

    @Value("${api.gateway.error.endpoint}")
    private String apiGatewayErrorEndpoint;

    @Autowired
    private AuthProcessor authProcessor;

    @Autowired
    private PathVariableProcessor pathProcessor;

    @Autowired
    private MetricsProcessor metricsProcessor;

    @Autowired
    private CompositeMeterRegistry meterRegistry;

    @Autowired
    private ZipkinTracer zipkinTracer;

    @Autowired
    private RunningApiManager runningApiManager;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private RouteErrorProcessor routeErrorProcessor;

    private void registerMetric(String routeID) {
        meterRegistry.counter(routeID);
    }

    public List<String> evaluatePath(String fullPath) {
        List<String> paramList = new ArrayList<>();
        if(fullPath.contains("{")) {
            String[] splittedPath = fullPath.split("/");
            for(String path : splittedPath) {
                if(path.contains("{")) {
                    String name = path.substring(1, path.length()-1);
                    paramList.add(name);
                }
            }
        }
        return paramList;
    }

    public void buildOnExceptionDefinition(RouteDefinition routeDefinition, boolean isZipkinTraceIdVisible, boolean isInternalExceptionMessageVisible, boolean isInternalExceptionVisible, String routeID) {
        routeDefinition
                .onException(Exception.class)
                .handled(true)
                .setHeader(Constants.ERROR_API_SHOW_TRACE_ID, constant(isZipkinTraceIdVisible))
                .setHeader(Constants.ERROR_API_SHOW_INTERNAL_ERROR_MESSAGE, constant(isInternalExceptionMessageVisible))
                .setHeader(Constants.ERROR_API_SHOW_INTERNAL_ERROR_CLASS, constant(isInternalExceptionVisible))
                .process(routeErrorProcessor)
                .setHeader(Constants.ROUTE_ID_HEADER, constant(routeID))
                .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                .removeHeader(Constants.ERROR_API_SHOW_TRACE_ID)
                .removeHeader(Constants.ERROR_API_SHOW_INTERNAL_ERROR_MESSAGE)
                .removeHeader(Constants.ERROR_API_SHOW_INTERNAL_ERROR_CLASS)
                .removeHeader(Constants.ROUTE_ID_HEADER)
                .end();
    }

    public void buildRoute(RouteDefinition routeDefinition, String routeID, Api api, Path path, boolean pathHasParams) {

        String protocol = api.getEndpointType().equals(EndpointType.HTTP) ? Constants.HTTP4_PREFIX : Constants.HTTPS4_PREFIX;
        String[] endpointArray = buildEndpoints(pathHasParams, protocol, path.getPath(), api.getEndpoints(), api.getConnectTimeout(), api.getSocketTimeout());

        if(pathHasParams) {
            routeDefinition.setHeader(Constants.CAPI_CONTEXT_HEADER, constant(api.getContext()));
        }

        if(trafficInspectorEnabled) {
            routeDefinition
                    .setBody(constant(routeID))
                    .to("kafka:" + trafficInspectorKafkaTopic + "?brokers=" + trafficInspectorKafkaBroker);
        }

        if(api.isSecured()) {
            routeDefinition
                    .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(api.isBlockIfInError()))
                    .setHeader(Constants.API_CLIENT_ID_HEADER, constant(api.getClientID()))
                    .process(pathProcessor)
                    .process(authProcessor)
                    .process(metricsProcessor)
                    .loadBalance()
                    .roundRobin()
                    .to(endpointArray)
                    .end()
                    .setId(routeID);
        } else {
            routeDefinition.streamCaching()
                    .process(pathProcessor)
                    .process(metricsProcessor)
                    .loadBalance()
                    .roundRobin()
                    .to(endpointArray)
                    .end()
                    .setId(routeID);
        }

        registerMetric(normalizeRouteId(api, path));
        zipkinTracer.addServerServiceMapping(api.getContext() + path.getPath(), normalizeRouteId(api, path));
        runningApiManager.runApi(routeID, api, path.getPath(), path.getVerb());

    }

    public void buildRoute(RouteDefinition routeDefinition, String routeID, RunningApi runningApi, boolean pathHasParams) {

        String protocol = runningApi.getEndpointType().equals(EndpointType.HTTP) ? Constants.HTTP4_PREFIX : Constants.HTTPS4_PREFIX;
        String[] endpointArray = buildEndpoints(pathHasParams, protocol, runningApi.getPath(), runningApi.getEndpoints(), runningApi.getConnectTimeout(), runningApi.getSocketTimeout());

        if(pathHasParams) {
            routeDefinition.setHeader(Constants.CAPI_CONTEXT_HEADER, constant(runningApi.getContext()));
        }

        if(trafficInspectorEnabled) {
            log.info("Enabling Traffic Inspector --------------------------------------");
            routeDefinition
                    .setBody(constant(routeID))
                    .to("kafka:" + trafficInspectorKafkaTopic + "?brokers=" + trafficInspectorKafkaBroker);
        }

        if(runningApi.isSecured()) {
            routeDefinition
                    .setHeader(Constants.BLOCK_IF_IN_ERROR_HEADER, constant(runningApi.isBlockIfInError()))
                    .setHeader(Constants.API_CLIENT_ID_HEADER, constant(runningApi.getClientID()))
                    .process(authProcessor)
                    .process(pathProcessor)
                    .process(metricsProcessor)
                    .loadBalance()
                    .roundRobin()
                    .to(endpointArray)
                    .end()
                    .setId(routeID);
        } else {
            routeDefinition
                    .streamCaching()
                    .process(pathProcessor)
                    .process(metricsProcessor)
                    .loadBalance()
                    .roundRobin()
                    .to(endpointArray)
                    .end()
                    .setId(routeID);
        }
        registerMetric(runningApi.getRouteId());
        zipkinTracer.addServerServiceMapping(runningApi.getContext() + runningApi.getPath(), routeID);
    }

    public void buildSuspendedRoute(RouteDefinition routeDefinition, RunningApi runningApi, boolean hasParams) {
        if(hasParams) {
            routeDefinition.setHeader(Constants.CAPI_CONTEXT_HEADER, constant(runningApi.getContext()));
        }
        routeDefinition
                .setHeader(Constants.ROUTE_ID_HEADER, constant(runningApi.getRouteId()))
                .process(routeErrorProcessor)
                .toF(Constants.FAIL_REST_ENDPOINT_OBJECT, apiGatewayErrorEndpoint)
                .end()
                .setId(runningApi.getRouteId());
    }

    public String normalizeRouteId(Api api, Path path) {
        return (api.getContext() + path.getPath() + "_" + path.getVerb()).replaceAll("/", "_").replaceAll("-", "_").replaceAll("[{}]", "");
    }

    public String normalizeRouteId(String route) {
        return route.replaceAll("/", "_").replaceAll("-", "_").replaceAll("[{}]", "");
    }

    public void suspendRoute(RunningApi runningApi) {
        Route routeToRemove = camelContext.getRoute(runningApi.getRouteId());
        if(routeToRemove != null) {
            try {
                log.info("Removing route: {}", routeToRemove.getId());
                camelContext.getRouteController().stopRoute(runningApi.getRouteId());
                camelContext.removeRoute(runningApi.getRouteId());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            log.info("Route does not exist: {}" , runningApi.getRouteId());
        }
    }

    public void addSuspendedRoute(RunningApi runningApi) {
        try {
            log.info("Add suspended route: {}", runningApi.getRouteId());
            camelContext.addRoutes(new SuspendedRouteBuilder(camelContext, this, runningApi));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void addActiveRoute(RunningApi runningApi) {
        try {
            log.info("Add route: {}", runningApi.getRouteId());
            camelContext.addRoutes(new PathRouteRepublisher(camelContext, this, runningApi));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String[] buildEndpoints(boolean pathHasParams, String protocol, String path, List<String> endpointList, int connectTimeout, int socketTimeout) {
        List<String> transformedEndpointList = new ArrayList<>();
        for(String endpoint : endpointList) {
            endpoint = pathHasParams ? protocol + endpoint + Constants.HTTP4_CALL_PARAMS : protocol + endpoint + httpUtils.setPath(path) + Constants.HTTP4_CALL_PARAMS;
            if(connectTimeout > -1) {
                endpoint = httpUtils.setHttpConnectTimeout(endpoint, connectTimeout);
            }
            if(socketTimeout > -1) {
                endpoint = httpUtils.setHttpSocketTimeout(endpoint, socketTimeout);
            }
            transformedEndpointList.add(endpoint);
        }
        return transformedEndpointList.stream().toArray(n -> new String[n]);
    }
}