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

package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.cache.ThrottlingManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.grafana.Panel;
import at.rodrigo.api.gateway.grafana.GrafanaDashboardBuilder;
import at.rodrigo.api.gateway.utils.CamelUtils;
import at.rodrigo.api.gateway.utils.GrafanaUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.ArrayList;
import java.util.List;

public class SimpleRestRouteRepublisher extends RouteBuilder {

    private Api api;

    private CamelUtils camelUtils;

    private GrafanaDashboardBuilder grafanaDashboardBuilder;

    private ThrottlingManager throttlingManager;

    public SimpleRestRouteRepublisher(CamelContext context, CamelUtils camelUtils, GrafanaDashboardBuilder grafanaDashboardBuilder, ThrottlingManager throttlingManager, Api api) {
        super(context);
        this.api = api;
        this.camelUtils = camelUtils;
        this.grafanaDashboardBuilder = grafanaDashboardBuilder;
        this.throttlingManager = throttlingManager;
    }

    @Override
    public void configure() {
        log.info("Starting configuration of a Simple Route");

        try {
            addRoutes(api);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    void addRoutes(Api api) throws  Exception {

        List<Panel> grafanaPanels;

        if(grafanaDashboardBuilder != null) {
            grafanaPanels = new ArrayList<>();
        }

        for(Path path : api.getPaths()) {
            if(!path.getPath().equals("/error")) {
                RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
                List<String> paramList = camelUtils.evaluatePath(path.getPath());

                String routeID = camelUtils.normalizeRouteId(api, path);
                path.setRouteID(routeID);
                RouteDefinition routeDefinition;

                switch(path.getVerb()) {
                    case GET:
                        routeDefinition = rest().get("/" + api.getContext() + path.getPath()).route();
                        break;
                    case POST:
                        routeDefinition = rest().post("/" + api.getContext() + path.getPath()).route();
                        break;
                    case PUT:
                        routeDefinition = rest().put("/" + api.getContext() + path.getPath()).route();
                        break;
                    case DELETE:
                        routeDefinition = rest().delete("/" + api.getContext() + path.getPath()).route();
                        break;
                    default:
                        throw new Exception("No verb available");
                }
                camelUtils.buildOnExceptionDefinition(routeDefinition, api.isZipkinTraceIdVisible(), api.isInternalExceptionMessageVisible(), api.isInternalExceptionVisible(), routeID);
                if(paramList.isEmpty()) {
                    camelUtils.buildRoute(routeDefinition, routeID, api, path, false);
                } else {
                    for(String param : paramList) {
                        restParamDefinition.name(param)
                                .type(RestParamType.path)
                                .dataType("String");
                    }
                    camelUtils.buildRoute(routeDefinition, routeID, api, path, true);
                }
            }
        }
        throttlingManager.applyThrottling(api);

        if(grafanaDashboardBuilder != null) {
            //grafanaUtils.addToGrafana(api);
        }

    }
}