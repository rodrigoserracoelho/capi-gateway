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

import at.rodrigo.api.gateway.exception.InvalidTokenException;
import at.rodrigo.api.gateway.exception.NoSubscriptionException;
import at.rodrigo.api.gateway.utils.Constants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

@Component
@Slf4j
public class AuthProcessor implements Processor {

   @Value("${capi.authorization.keys.endpoint}")
   private String capiAuthorizationKeysEndpoint;

   @Autowired
   private RestTemplate restTemplate;

    public void process(Exchange exchange) {
        boolean validCall = false;
        try {
            if(exchange.getIn().getHeader(Constants.AUTHORIZATION_HEADER) == null) {
                exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
                exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "No authorization (Bearer) token provided.");
                exchange.setException(new NoSubscriptionException());
            } else {
                String jwtToken = exchange.getIn().getHeader(Constants.AUTHORIZATION_HEADER).toString().substring("Bearer ".length());
                String apiClientID = exchange.getIn().getHeader(Constants.API_CLIENT_ID_HEADER).toString();
                exchange.getIn().removeHeader(Constants.AUTHORIZATION_HEADER);
                exchange.getIn().removeHeader(Constants.BLOCK_IF_IN_ERROR_HEADER);
                exchange.getIn().removeHeader(Constants.API_CLIENT_ID_HEADER);
                if(apiClientID != null && jwtToken != null) {
                    ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
                    ResponseEntity<String> publicKeyEndpoint = restTemplate.getForEntity(capiAuthorizationKeysEndpoint, String.class);
                    if(publicKeyEndpoint.getStatusCode().is2xxSuccessful()) {
                        JWKSet jwkSet = JWKSet.parse(publicKeyEndpoint.getBody());
                        JWKSource keySource = new ImmutableJWKSet(jwkSet);
                        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
                        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
                        jwtProcessor.setJWSKeySelector(keySelector);
                        JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);


                        Map<String, Object> claimSetMap = claimsSet.getJSONObjectClaim("realm_access");
                        log.info("------------------->");
                        claimSetMap.forEach((k, v) -> log.info((k + ":" + v)));


                        if(!claimSetMap.isEmpty() && claimSetMap.entrySet().stream().findFirst().isPresent()) {
                            JSONObject realmAccessClaimSet = (JSONObject) claimSetMap.entrySet().stream().findFirst().get().getValue();
                            JSONArray rolesObject = (JSONArray) realmAccessClaimSet.get("roles");
                            if(rolesObject.contains(apiClientID)) {
                                validCall = true;
                            }
                            if(!validCall) {
                                exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
                                exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "You are not subscribed to this API");
                                exchange.setException(new NoSubscriptionException());
                            }
                        } else {
                            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
                            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Problem reading realm access");
                            exchange.setException(new NoSubscriptionException());
                        }
                    } else {
                        exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
                        exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Problem loading Public Keys");
                        exchange.setException(new NoSubscriptionException());
                    }
                } else {
                    exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.BAD_REQUEST.value());
                    exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid token was provided");
                    exchange.setException(new InvalidTokenException());
                }
            }
        } catch(ParseException exception) {
            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.BAD_REQUEST.value());
            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid token was provided");
            exchange.setException(exception);
        } catch(Exception exception) {
            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid Keys");
            exchange.setException(exception);
        }
    }
}