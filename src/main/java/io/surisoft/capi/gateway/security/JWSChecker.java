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

package io.surisoft.capi.gateway.security;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Slf4j
@Component
public class JWSChecker {

    public JWSAlgorithm getAlgorithm(String token) throws ParseException {
        JWT jwtToken = JWTParser.parse(token);
        Header tokenHeader = jwtToken.getHeader();
        Algorithm algorithm = tokenHeader.getAlgorithm();
        if(algorithm.getName().equals(JWSAlgorithm.RS256.getName())) {
            return JWSAlgorithm.RS256;
        } else if(algorithm.getName().equals(JWSAlgorithm.EdDSA.getName())) {
            return JWSAlgorithm.EdDSA;
        } else {
            throw new ParseException("Unsupported Algorithm", 0);
        }
    }
}