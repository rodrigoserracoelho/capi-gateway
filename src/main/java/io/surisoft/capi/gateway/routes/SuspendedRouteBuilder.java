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

package io.surisoft.capi.gateway.routes;

import io.surisoft.capi.gateway.schema.RunningApi;
import io.surisoft.capi.gateway.utils.CamelUtils;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.List;

public class SuspendedRouteBuilder extends RouteBuilder {

    private RunningApi runningApi;

    private CamelUtils camelUtils;

    public SuspendedRouteBuilder(CamelContext context, CamelUtils camelUtils, RunningApi runningApi) {
        super(context);
        this.runningApi = runningApi;
        this.camelUtils = camelUtils;
    }

    @Override
    public void configure() {
        log.info("Starting to publish a suspended route");

        try {
            addRoute(runningApi);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private void addRoute(RunningApi runningApi) throws  Exception {
        RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
        RouteDefinition routeDefinition;
        switch(runningApi.getVerb()) {
            case GET:
                routeDefinition = rest().get("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case POST:
                routeDefinition = rest().post("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case PUT:
                routeDefinition = rest().put("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            case DELETE:
                routeDefinition = rest().delete("/" + runningApi.getContext() + runningApi.getPath()).route();
                break;
            default:
                throw new Exception("No verb available");
        }
        List<String> paramList = camelUtils.evaluatePath(runningApi.getPath());
        if(!paramList.isEmpty()) {
            for(String param : paramList) {
                restParamDefinition.name(param)
                        .type(RestParamType.path)
                        .dataType("String");
            }
            camelUtils.buildSuspendedRoute(routeDefinition, runningApi, true);
        }
        camelUtils.buildSuspendedRoute(routeDefinition, runningApi, false);
    }
}