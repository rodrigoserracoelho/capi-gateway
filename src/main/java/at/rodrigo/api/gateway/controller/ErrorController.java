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

package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.RunningApi;
import at.rodrigo.api.gateway.exception.CapiRestException;
import at.rodrigo.api.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class ErrorController {

    @Autowired
    private RunningApiManager runningApiManager;

    @GetMapping(path = Constants.CAPI_INTERNAL_REST_ERROR_PATH)
    public ResponseEntity<CapiRestException> get(HttpServletRequest request) {
        return buildResponse(request);
    }

    @PostMapping(path = Constants.CAPI_INTERNAL_REST_ERROR_PATH)
    public ResponseEntity<CapiRestException> post(HttpServletRequest request) {
        return buildResponse(request);
    }

    @PutMapping(path = Constants.CAPI_INTERNAL_REST_ERROR_PATH)
    public ResponseEntity<CapiRestException> put(HttpServletRequest request) {
        return buildResponse(request);
    }

    @DeleteMapping(path = Constants.CAPI_INTERNAL_REST_ERROR_PATH)
    public ResponseEntity<CapiRestException> delete(HttpServletRequest request) {
        return buildResponse(request);
    }

    private ResponseEntity<CapiRestException> buildResponse(HttpServletRequest request) {

        String routeId = request.getHeader(Constants.ROUTE_ID_HEADER);
        CapiRestException capiRestException = new CapiRestException();

        String errorMessage =  request.getHeader(Constants.REASON_MESSAGE_HEADER);

        if(routeId != null) {
            RunningApi runningApi = runningApiManager.getRunningApi(routeId);
            if(runningApi.getSuspensionMessage() != null) {
                errorMessage = runningApi.getSuspensionMessage();
            }
            runningApiManager.blockApi(routeId);
        }

        if(Boolean.parseBoolean(request.getHeader(Constants.ERROR_API_SHOW_TRACE_ID))) {
            capiRestException.setZipkinTraceID(request.getHeader(Constants.TRACE_ID_HEADER));
        }
        if(Boolean.parseBoolean(request.getHeader(Constants.ERROR_API_SHOW_INTERNAL_ERROR_MESSAGE))) {
            capiRestException.setInternalExceptionMessage(request.getHeader(Constants.CAPI_INTERNAL_ERROR));
            capiRestException.setException(request.getHeader(Constants.CAPI_INTERNAL_ERROR_CLASS_NAME));
        }

        if(errorMessage != null) {
            capiRestException.setErrorMessage(errorMessage);
        } else {
            capiRestException.setErrorMessage("There was an exception connecting to your api");
        }

        if(request.getHeader(Constants.REASON_CODE_HEADER) != null) {
            int returnedCode = Integer.parseInt(request.getHeader(Constants.REASON_CODE_HEADER));
            capiRestException.setErrorCode(returnedCode);
        } else {
            capiRestException.setErrorCode(HttpStatus.SERVICE_UNAVAILABLE.value());
        }

        capiRestException.setRouteID(request.getHeader(Constants.ROUTE_ID_HEADER));
        return new ResponseEntity<>(capiRestException, HttpStatus.valueOf(capiRestException.getErrorCode()));
    }
}
