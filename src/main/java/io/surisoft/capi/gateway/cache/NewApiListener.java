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

package io.surisoft.capi.gateway.cache;

import io.surisoft.capi.gateway.grafana.GrafanaDashboardBuilder;
import io.surisoft.capi.gateway.parser.SwaggerParser;
import io.surisoft.capi.gateway.routes.SimpleRestRouteRepublisher;
import io.surisoft.capi.gateway.routes.SwaggerRoutePublisher;
import io.surisoft.capi.gateway.schema.Api;
import io.surisoft.capi.gateway.schema.RunningApi;
import io.surisoft.capi.gateway.utils.CamelUtils;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NewApiListener implements EntryAddedListener<String, Api>, EntryRemovedListener<String, Api>, EntryUpdatedListener<String, Api> {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private CamelUtils camelUtils;

    @Autowired(required = false)
    private GrafanaDashboardBuilder grafanaDashboardBuilder;

    @Autowired
    private SwaggerParser swaggerParser;

    @Autowired
    private ThrottlingManager throttlingManager;

    @Autowired
    private RunningApiManager runningApiManager;

    @Override
    public void entryAdded( EntryEvent<String, Api> event ) {
        log.info("One new API detected, deploying");
        addRunTimeApi(event.getValue());
    }

    @Override
    public void entryRemoved( EntryEvent<String, Api> event ) {
        log.info("API deleted, undeploying");
        removeRunTimeApi(event.getOldValue());
    }

    @Override
    public void entryUpdated(EntryEvent<String, Api> entryEvent) {
        log.info("API updated, redeploying");
        removeRunTimeApi(entryEvent.getOldValue());
        addRunTimeApi(entryEvent.getValue());
    }

    private void removeRunTimeApi(Api api) {
        try {
            List<RunningApi> runningApis = runningApiManager.getRunningApiForApi(api.getId());
            for(RunningApi runningApi : runningApis) {
                if(api.getThrottlingPolicy() != null) {
                    throttlingManager.removeThrottlingByRouteID(runningApi.getRouteId());
                }
                if(camelContext.getRoute(runningApi.getRouteId()) != null) {
                    camelContext.getRouteController().stopRoute(runningApi.getRouteId());
                    camelContext.removeRoute(runningApi.getRouteId());
                }
                runningApiManager.removeRunningApi(runningApi);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void addRunTimeApi(Api api) {
        try {
            removeRunTimeApi(api);
            if(api.getSwaggerEndpoint() == null) {
                camelContext.addRoutes(new SimpleRestRouteRepublisher(camelContext, camelUtils, grafanaDashboardBuilder, throttlingManager, api));
            } else {
                camelContext.addRoutes(new SwaggerRoutePublisher(camelContext, camelUtils, grafanaDashboardBuilder, throttlingManager, swaggerParser, api));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}