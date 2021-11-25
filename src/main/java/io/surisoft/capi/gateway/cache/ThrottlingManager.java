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

import io.surisoft.capi.gateway.schema.Api;
import io.surisoft.capi.gateway.schema.Path;
import io.surisoft.capi.gateway.schema.ThrottlingPolicy;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ThrottlingManager {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    public void applyThrottling(Api api) {
        if(api.getThrottlingPolicy() != null) {
            ThrottlingPolicy memoryThrottlingPolicy = cloneThrottling(api.getThrottlingPolicy());
            List<Path> pathList = api.getPaths();
            List<String> apiRouteList = new ArrayList<>();
            for(Path path : pathList) {
                if(api.getThrottlingPolicy().isApplyPerPath()) {
                    List<String> routeList = new ArrayList<>();
                    routeList.add(path.getRouteID());
                    memoryThrottlingPolicy.setRouteID(routeList);
                    getThrottlingPolicies().put(UUID.randomUUID().toString(), memoryThrottlingPolicy);
                } else {
                    apiRouteList.add(path.getRouteID());
                }
            }
            if(!api.getThrottlingPolicy().isApplyPerPath()) {
                memoryThrottlingPolicy.setRouteID(apiRouteList);
                getThrottlingPolicies().put(UUID.randomUUID().toString(), memoryThrottlingPolicy);
            }
        }
    }

    public IMap<String, ThrottlingPolicy> getThrottlingPolicies() {
        return hazelcastInstance.getMap(CacheConstants.THROTTLING_POLICIES_IMAP_NAME);
    }

    public void saveThrottlingPolicy(String id, ThrottlingPolicy throttlingPolicy) {
        this.getThrottlingPolicies().put(id, throttlingPolicy);
    }

    public void incrementThrottlingByRouteID(String routeID, int incrementBy) {
        IMap<String, ThrottlingPolicy> entries = getThrottlingPolicies();
        Iterator<String> iterator = entries.keySet().iterator();
        while(iterator.hasNext()) {
            String id = iterator.next();
            ThrottlingPolicy throttlingPolicy = entries.get(id);
            if(throttlingPolicy.getRouteID().contains(routeID)) {
                throttlingPolicy.setTotalCalls(throttlingPolicy.getTotalCalls() + incrementBy);
                getThrottlingPolicies().put(id, throttlingPolicy);
            }
        }
    }

    public void removeThrottlingByRouteID(String routeID) {
        IMap<String, ThrottlingPolicy> entries = getThrottlingPolicies();
        Iterator<String> iterator = entries.keySet().iterator();
        while(iterator.hasNext()) {
            String id = iterator.next();
            ThrottlingPolicy throttlingPolicy = entries.get(id);
            if(throttlingPolicy.getRouteID().contains(routeID)) {
                getThrottlingPolicies().remove(id);
            }
        }
    }

    ThrottlingPolicy cloneThrottling(ThrottlingPolicy throttlingPolicy) {
        ThrottlingPolicy localThrottlingPolicy = new ThrottlingPolicy();
        localThrottlingPolicy.setStartCountingAt(throttlingPolicy.getStartCountingAt());
        localThrottlingPolicy.setTotalCalls(throttlingPolicy.getTotalCalls());
        localThrottlingPolicy.setThrottlingExpiration(null);
        localThrottlingPolicy.setMaxCallsAllowed(throttlingPolicy.getMaxCallsAllowed());
        localThrottlingPolicy.setApplyPerPath(throttlingPolicy.isApplyPerPath());
        localThrottlingPolicy.setPeriodForMaxCalls(throttlingPolicy.getPeriodForMaxCalls());
        return localThrottlingPolicy;
    }
}