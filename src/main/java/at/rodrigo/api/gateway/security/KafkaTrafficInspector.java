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

package at.rodrigo.api.gateway.security;

import at.rodrigo.api.gateway.processor.TrafficInspectorProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaTrafficInspector extends RouteBuilder {

    @Value("${api.gateway.traffic.inspector.enabled}")
    private boolean trafficInspectorEnabled;

    @Value("${api.gateway.traffic.inspector.kafka.topic}")
    private String trafficInspectorKafkaTopic;

    @Value("${api.gateway.traffic.inspector.kafka.broker}")
    private String trafficInspectorKafkaBroker;

    @Value("${api.gateway.traffic.inspector.kafka.groupId}")
    private String trafficInspectorKafkaGroupId;

    @Autowired
    private TrafficInspectorProcessor trafficInspectorProcessor;

    @Override
    public void configure() {
        if(trafficInspectorEnabled) {
            log.info("Starting Traffic Inspector Route...");
            from("kafka:" + trafficInspectorKafkaTopic + "?brokers=" + trafficInspectorKafkaBroker + "&groupId=" + trafficInspectorKafkaGroupId)
               .process(trafficInspectorProcessor);
        }
    }
}

