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

package at.rodrigo.api.gateway.scheduler;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.entity.SuspensionType;
import at.rodrigo.api.gateway.utils.CamelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class RunningApiInspector {

    @Autowired
    RunningApiManager runningApiManager;

    @Autowired
    CamelUtils camelUtils;

    @Scheduled(fixedRateString = "${api.gateway.api.running.inspector.period}")
    public void checkDisabledRunningApis() {
        List<RunningApi> disabledRunningApis = runningApiManager.getDisabledRunningApis();
        for(RunningApi runningApi : disabledRunningApis) {
            camelUtils.suspendRoute(runningApi);
            runningApi.setRemoved(true);
            runningApi.setSuspensionType(SuspensionType.ERROR);
            runningApi.setSuspensionMessage("Your route was suspended due to errors calling the backend");
            camelUtils.addSuspendedRoute(runningApi);
            runningApiManager.saveRunningApi(runningApi);
        }
    }

    @Scheduled(fixedRateString = "${api.gateway.api.running.inspector.period}")
    public void checkRunningApisToUnblock() {
        List<RunningApi> removeddRunningApis = runningApiManager.getRemovedRunningApis();
        for(RunningApi runningApi : removeddRunningApis) {
            if(runningApi.getCountBlockChecks() == runningApi.getUnblockAfterMinutes()) {
                try {
                    //Remove suspended route
                    camelUtils.suspendRoute(runningApi);
                    runningApi.setRemoved(false);
                    runningApi.setDisabled(false);
                    runningApi.setCountBlockChecks(0);
                    runningApi.setSuspensionType(null);
                    runningApi.setSuspensionMessage(null);
                    camelUtils.addActiveRoute(runningApi);
                    runningApiManager.saveRunningApi(runningApi);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                runningApi.setCountBlockChecks(runningApi.getCountBlockChecks() + 1);
                runningApiManager.saveRunningApi(runningApi);
            }
        }
    }
}