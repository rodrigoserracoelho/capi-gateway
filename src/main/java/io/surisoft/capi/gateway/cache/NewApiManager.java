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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class NewApiManager {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private NewApiListener newApiListener;

    @PostConstruct
    public void addListener() {
        getCachedApi().addEntryListener( newApiListener, true );
    }

    private IMap<String, Api> getCachedApi() {
        return hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME);
    }

    public Api getApiByContext(String context) {
        IMap<String, Api> iMap = getCachedApi();
        for(String id : iMap.keySet()) {
            Api api = iMap.get(id);
            if(api.getContext().equals(context)) {
                return api;
            }
        }
        return null;
    }
}