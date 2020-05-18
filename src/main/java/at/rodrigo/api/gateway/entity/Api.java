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

package at.rodrigo.api.gateway.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

@Data
public class Api implements Serializable {
    @Id
    private String id;
    private List<String> endpoints;
    private EndpointType endpointType;
    private int connectTimeout;
    private int socketTimeout;
    private String name;
    private boolean secured;
    private String context;
    private String jwsEndpoint;
    private List<Path> paths;
    private boolean swagger;
    private String swaggerEndpoint;
    private List<String> audience;
    private boolean blockIfInError;
    private int maxAllowedFailedCalls;
    private boolean zipkinTraceIdVisible;
    private boolean internalExceptionMessageVisible;
    private boolean internalExceptionVisible;
    private boolean returnAPIError;
    private boolean unblockAfter;
    private int unblockAfterMinutes;
    private ThrottlingPolicy throttlingPolicy;
    private String clientID;
    private boolean corsEnabled;
    private List<String> allowedOrigins;
    private boolean credentialsAllowed;
}
