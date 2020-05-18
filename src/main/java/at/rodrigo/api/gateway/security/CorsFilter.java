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

import at.rodrigo.api.gateway.cache.NewApiManager;
import at.rodrigo.api.gateway.entity.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter implements Filter {

    @Value("${camel.component.servlet.mapping.context-path}")
    private String capiRootContext;

    @Autowired
    private NewApiManager newApiManager;

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String routeContext =  getRouteContext(request);

        if(routeContext != null) {
            Api api = newApiManager.getApiByContext(routeContext);
            if(api != null) {
                if(api.isCorsEnabled()) {
                    if(api.getAllowedOrigins().contains(request.getHeader("Origin"))) {
                        response.setHeader("Access-Control-Allow-Credentials", Boolean.toString(api.isCredentialsAllowed()));
                        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
                        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
                        response.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
                        response.setHeader("Access-Control-Max-Age", "1728000");
                    }
                }
            }
        }
    }

    private String getRouteContext(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if(capiRootContext != null) {
            String[] splittedContext = requestUri.split("/");
            return splittedContext[2];
        }
        return null;
    }
}