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

package io.surisoft.capi.gateway.processor;

import io.surisoft.capi.gateway.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PathVariableProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {

        processEndpointCallBody(exchange);

        String capiContext = getCapiContext(exchange);
        if(capiContext != null) {
            String httpPath = exchange.getIn().getHeader(Exchange.HTTP_PATH).toString();
            exchange.getIn().setHeader(Exchange.HTTP_PATH, httpPath.substring(capiContext.length()));
        }
    }

    private String getCapiContext(Exchange exchange) {
        if(exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER) != null) {
            String capiContext = exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER).toString().startsWith("/") ?
                    exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER).toString() :
                    "/" + exchange.getIn().getHeader(Constants.CAPI_CONTEXT_HEADER).toString();
            exchange.getIn().removeHeader(Constants.CAPI_CONTEXT_HEADER);
            return capiContext;
        }
        return null;
    }

    private void processEndpointCallBody(Exchange exchange) {
        exchange.getIn().setBody(exchange.getProperty(Constants.REST_CALL_BODY));
    }
}