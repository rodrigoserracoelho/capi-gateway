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

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String APPLICATION_NAME = "CAPI";
    public static final String FAIL_REST_ENDPOINT_OBJECT = "https:%s?throwExceptionOnFailure=false&connectTimeout=1000&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BLOCK_IF_IN_ERROR_HEADER = "block-if-in-error";
    public static final String REASON_CODE_HEADER = "error-reason-code";
    public static final String REASON_MESSAGE_HEADER = "error-reason-message";
    public static final String ROUTE_ID_HEADER = "routeID";
    public static final String HTTP4_PREFIX = "http://";
    public static final String HTTPS4_PREFIX = "https://";
    public static final String HTTP4_CALL_PARAMS = "?throwExceptionOnFailure=false&bridgeEndpoint=true&copyHeaders=true&connectionClose=true";
    public static final String CAPI_CONTEXT_HEADER = "CapiContext";
    public static final String API_ID_HEADER = "api_id";
    public static final String CAPI_INTERNAL_ERROR = "capi-internal-error";
    public static final String CAPI_INTERNAL_ERROR_CLASS_NAME = "capi-internal-error-class-name";
    public static final String HTTP_CONNECT_TIMEOUT = "connectTimeout=";
    public static final String HTTP_SOCKET_TIMEOUT = "socketTimeout=";
    public static final String ERROR_API_SHOW_TRACE_ID = "show-trace-id";
    public static final String ERROR_API_SHOW_INTERNAL_ERROR_MESSAGE = "show-internal-error-message";
    public static final String ERROR_API_SHOW_INTERNAL_ERROR_CLASS = "show-internal-error-class";
    public static final String TRACE_ID_HEADER = "X-B3-TraceId";
    public static final String REST_CALL_BODY = "rest-call-body";
}
