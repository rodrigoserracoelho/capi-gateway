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

package at.rodrigo.api.gateway.cache;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.SuspensionType;
import at.rodrigo.api.gateway.entity.Verb;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class RunningApiManager {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @PostConstruct
    public void addListener() {
        getCachedApi().addEntryListener(new RunningApiListener(), true );
    }

    public void runApi(String routeId, Api api, String path, Verb verb) {
        RunningApi runningApi = new RunningApi();
        runningApi.setId(api.getId());
        runningApi.setRouteId(routeId);
        runningApi.setDisabled(false);
        runningApi.setFailedCalls(0);

        runningApi.setContext(api.getContext());
        runningApi.setAudience(api.getAudience());
        runningApi.setEndpoints(api.getEndpoints());
        runningApi.setJwsEndpoint(api.getJwsEndpoint());
        runningApi.setEndpointType(api.getEndpointType());
        runningApi.setSecured(api.isSecured());
        runningApi.setPath(path);
        runningApi.setVerb(verb);
        runningApi.setBlockIfInError(api.isBlockIfInError());
        runningApi.setZipkinTraceIdVisible(api.isZipkinTraceIdVisible());
        runningApi.setInternalExceptionMessageVisible(api.isInternalExceptionMessageVisible());
        runningApi.setInternalExceptionVisible(api.isInternalExceptionVisible());
        runningApi.setConnectTimeout(api.getConnectTimeout());
        runningApi.setSocketTimeout(api.getSocketTimeout());

        if(api.isBlockIfInError()) {
            if(api.isUnblockAfter()) {
                runningApi.setUnblockAfterMinutes(api.getUnblockAfterMinutes());
            } else {
                runningApi.setUnblockAfterMinutes(-1);
            }
            runningApi.setUnblockAfter(api.isUnblockAfter());
            runningApi.setMaxAllowedFailedCalls(api.getMaxAllowedFailedCalls());
        } else {
            runningApi.setUnblockAfterMinutes(-1);
            runningApi.setMaxAllowedFailedCalls(-1);
        }
        getCachedApi().put(routeId, runningApi);
    }

    public RunningApi getRunningApi(String routeId) {
        if(getCachedApi().containsKey(routeId)) {
            return getCachedApi().get(routeId);
        } else {
            return null;
        }
    }

    public boolean blockApi(String routeId) {
        RunningApi runningApi = getRunningApi(routeId);
        if(runningApi != null) {
            if(runningApi.getFailedCalls() == runningApi.getMaxAllowedFailedCalls()) {
                runningApi.setDisabled(true);
                getCachedApi().put(routeId, runningApi);
                return true;
            } else {
                runningApi.setFailedCalls(runningApi.getFailedCalls() + 1);
                getCachedApi().put(routeId, runningApi);
                return false;
            }
        } else {
            return false;
        }
    }

    private IMap<String, RunningApi> getCachedApi() {
        return hazelcastInstance.getMap(CacheConstants.RUNNING_API_IMAP_NAME);
    }

    public List<RunningApi> getDisabledRunningApis() {
        List<RunningApi> disabledRunningApis = new ArrayList<>();
        IMap<String, RunningApi> runningApis = getCachedApi();
        Iterator<String> i = runningApis.keySet().iterator();
        while(i.hasNext()) {
            String routeId = i.next();
            if(runningApis.get(routeId).isDisabled() && !runningApis.get(routeId).isRemoved()) {
                disabledRunningApis.add(runningApis.get(routeId));
            }
        }
        return disabledRunningApis;
    }

    public List<RunningApi> getRemovedRunningApis() {
        List<RunningApi> removedRunningApis = new ArrayList<>();
        IMap<String, RunningApi> runningApis = getCachedApi();
        Iterator<String> i = runningApis.keySet().iterator();
        while(i.hasNext()) {
            String routeId = i.next();
            if(runningApis.get(routeId).isDisabled() &&
                    runningApis.get(routeId).isRemoved() &&
                    runningApis.get(routeId).isUnblockAfter() &&
                    runningApis.get(routeId).getSuspensionType().equals(SuspensionType.ERROR)) {
                removedRunningApis.add(runningApis.get(routeId));
            }
        }
        return removedRunningApis;
    }

    public void saveRunningApi(RunningApi runningApi) {
        this.getCachedApi().put(runningApi.getRouteId(), runningApi);
    }

    public List<RunningApi> getRunningApiForApi(String apiID) {
        List<RunningApi> runningApis = new ArrayList<>();
        Iterator<String> iterator = getCachedApi().keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            if(getCachedApi().get(key).getId().equals(apiID)) {
                runningApis.add(getCachedApi().get(key));
            }
        }
        return runningApis;
    }

    public void removeRunningApi(RunningApi runningApi) {
        getCachedApi().remove(runningApi.getRouteId());
    }

    public int count() {
        return getCachedApi().size();
    }

}