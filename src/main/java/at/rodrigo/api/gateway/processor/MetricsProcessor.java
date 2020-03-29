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

package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.utils.CamelUtils;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.search.RequiredSearch;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MetricsProcessor implements Processor {

    @Autowired
    private CompositeMeterRegistry meterRegistry;

    @Autowired
    private CamelUtils camelUtils;

    @Override
    public void process(Exchange exchange) {
        if(exchange.getIn().getHeader("CamelServletContextPath") != null && exchange.getIn().getHeader(Exchange.HTTP_METHOD) != null) {
            String metricName = camelUtils.normalizeRouteId(exchange.getIn().getHeader("CamelServletContextPath").toString().substring(1) + "-" + exchange.getIn().getHeader(Exchange.HTTP_METHOD));
            RequiredSearch s = meterRegistry.get(metricName);
            s.counter().increment();
        }
    }
}