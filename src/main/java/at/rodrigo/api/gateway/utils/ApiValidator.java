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

import at.rodrigo.api.gateway.repository.ApiRepository;
import at.rodrigo.api.gateway.schema.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class ApiValidator {

    @Autowired
    private ApiRepository apiRepository;

    public boolean isApiValid(Api newApi) {
        if(newApi.getId() != null) {
            Optional<Api> existingApi = apiRepository.findById(newApi.getId());
            if(existingApi.isPresent()) {
                return validateExistingApi(newApi, existingApi.get());
            } else {
                return false;
            }
        } else {
            return validateNewApi(newApi);
        }
    }

    private boolean validateExistingApi(Api newApi, Api existingApi) {
        if(!newApi.getName().equalsIgnoreCase(existingApi.getName())) {
            return false;
        }
        if(!newApi.getContext().equalsIgnoreCase(existingApi.getContext())) {
            return false;
        }
        return validateFields(newApi);
    }

    private boolean validateNewApi(Api newApi) {
        Api existingWithCondition = apiRepository.findByName(newApi.getName());
        if(existingWithCondition != null) {
            return false;
        }
        existingWithCondition = apiRepository.findByContext(newApi.getContext());
        if(existingWithCondition != null) {
            return false;
        }
        newApi.setId(UUID.randomUUID().toString());
        return validateFields(newApi);
    }

    private boolean validateFields(Api newApi) {
        if(newApi.getEndpoints().isEmpty()) {
            return false;
        }
        if(newApi.getSwaggerEndpoint() == null) {
            return false;
        }
        if(newApi.isBlockIfInError() && newApi.getMaxAllowedFailedCalls() < 1) {
            return false;
        }

        return true;
    }
}