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

package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.processor.KafkaRouteProcessor;
import at.rodrigo.api.gateway.repository.KafkaRouteRepository;
import at.rodrigo.api.gateway.schema.KafkaRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KafkaConsumerRoutePublisher extends RouteBuilder {

    @Autowired
    private KafkaRouteRepository kafkaRouteRepository;

    @Autowired
    private KafkaRouteProcessor kafkaRouteProcessor;

    @Override
    public void configure() {

        List<KafkaRoute> kafkaRoutes = kafkaRouteRepository.findAll();

        for(KafkaRoute kafkaRoute : kafkaRoutes) {
            from("kafka:" + kafkaRoute.getTopicName() + "?brokers=" + kafkaRoute.getBrokerEndpoint())
                    .process(kafkaRouteProcessor)
                    .to(kafkaRoute.getHttpEndpoint());
        }
    }
}