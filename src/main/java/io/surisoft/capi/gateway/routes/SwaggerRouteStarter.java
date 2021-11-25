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

import io.surisoft.capi.gateway.cache.CacheConstants;
import io.surisoft.capi.gateway.repository.ApiRepository;
import io.surisoft.capi.gateway.schema.Api;
import com.hazelcast.core.HazelcastInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SwaggerRouteStarter implements InitializingBean {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public void afterPropertiesSet()  {
        log.info("Starting configuration of Swagger Routes");
        List<Api> apiList = apiRepository.findAllBySwagger(true);
        for(Api api : apiList) {
            try {
                hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
            } catch(Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}