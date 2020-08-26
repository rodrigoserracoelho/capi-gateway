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

package at.rodrigo.api.gateway.config;

import at.rodrigo.api.gateway.cache.CAPICacheConfig;
import at.rodrigo.api.gateway.grafana.http.GrafanaDashboardBuilder;
import at.rodrigo.api.gateway.utils.Constants;
import com.hazelcast.config.Config;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.component.micrometer.DistributionStatisticConfigFilter;
import org.apache.camel.component.micrometer.messagehistory.MicrometerMessageHistoryFactory;
import org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyFactory;
import org.apache.camel.zipkin.ZipkinTracer;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;

import static org.apache.camel.component.micrometer.MicrometerConstants.DISTRIBUTION_SUMMARIES;
import static org.apache.camel.component.micrometer.messagehistory.MicrometerMessageHistoryNamingStrategy.MESSAGE_HISTORIES;
import static org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyNamingStrategy.ROUTE_POLICIES;

@Slf4j
@Configuration
public class CAPICamelConfiguration {

    @Value("${api.gateway.zipkin.endpoint}")
    private String zipkinEndpoint;

    @Value("${token.provider.key-alias}")
    private String tokenProviderKeyAlias;

    @Value("${token.provider.key-store-password}")
    private String tokenProviderKeyPassword;

    @Value("${token.provider.key-store}")
    private String tokenProviderKeyStore;

    @Value("${server.ssl.trust-store}")
    private String trustStorePath;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Autowired
    private CamelContext camelContext;

    @Value("${gateway.environment}")
    private String gatewayEnvironment;

    @Value("${gateway.cache.zookeeper.discovery}")
    private boolean zookeeperDiscovery;

    @Value("${gateway.cache.zookeeper.host}")
    private String zookeeperHost;

    @Value("${gateway.cache.zookeeper.path}")
    private String zookeeperPath;

    @Value("${gateway.cache.zookeeper.group.key}")
    private String zookeeperGroupKey;

    @Value("${api.gateway.grafana.create.dashboard}")
    private boolean createGrafanaDashboard;

    @Value("${api.gateway.grafana.endpoint}")
    private String grafanaEndpoint;

    @Value("${api.gateway.grafana.user}")
    private String grafanaUser;

    @Value("${api.gateway.grafana.password}")
    private String grafanaPassword;

    @Value("${api.gateway.grafana.token}")
    private String grafanaToken;

    @Value("${api.gateway.grafana.basic.auth}")
    private boolean grafanaBasicAuth;

    @Value("${api.gateway.grafana.datasource}")
    private String grafanaDataSource;


    @Bean
    public Config hazelCastConfig() {
        return new CAPICacheConfig(gatewayEnvironment, zookeeperDiscovery, zookeeperHost, zookeeperPath, zookeeperGroupKey);
    }

    @Bean
    void contextConfig() {
        camelContext.addRoutePolicyFactory(new MicrometerRoutePolicyFactory());
        camelContext.setMessageHistoryFactory(new MicrometerMessageHistoryFactory());
        ZipkinTracer zipkinTracer = zipkinTracer();
        zipkinTracer.init(camelContext);

    }

    @Bean
    ZipkinTracer zipkinTracer() {
        ZipkinTracer zipkin = new ZipkinTracer();
        OkHttpSender sender = OkHttpSender.create(zipkinEndpoint);
        zipkin.setSpanReporter(AsyncReporter.create(sender));
        return zipkin;
    }

    @Bean
    public CompositeMeterRegistry metrics() {
        DistributionStatisticConfigFilter timerMeterFilter = new DistributionStatisticConfigFilter()
                .andAppliesTo(ROUTE_POLICIES)
                .orAppliesTo(MESSAGE_HISTORIES)
                .setPublishPercentileHistogram(true)
                .setMinimumExpectedDuration(Duration.ofMillis(1L))
                .setMaximumExpectedDuration(Duration.ofMillis(150L));

        DistributionStatisticConfigFilter summaryMeterFilter = new DistributionStatisticConfigFilter()
                .andAppliesTo(DISTRIBUTION_SUMMARIES)
                .setPublishPercentileHistogram(true)
                .setMinimumExpectedValue(1L)
                .setMaximumExpectedValue(100L);


        CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
        compositeMeterRegistry.config().commonTags(Tags.of("application", Constants.APPLICATION_NAME))
        .meterFilter(timerMeterFilter)
        .meterFilter(summaryMeterFilter).namingConvention().tagKey(Constants.APPLICATION_NAME);
        return compositeMeterRegistry;
    }

    @Bean
    public JWKSet jwkSet() {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(tokenProviderKeyStore), tokenProviderKeyPassword.toCharArray());
            RSAPrivateCrtKey key = (RSAPrivateCrtKey) keyStore.getKey(tokenProviderKeyAlias, tokenProviderKeyPassword.toCharArray());
            RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            KeyPair keyPair = new KeyPair(publicKey, key);
            RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(tokenProviderKeyAlias);
            return new JWKSet(builder.build());
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Bean
    void setTrustStoreParams() {
        File filePath = new File(trustStorePath);
        String absolutePath = filePath.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", absolutePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
    }

    @Bean
    RestTemplate restTemplate() {

        final SSLContext sslContext;
        try {
            File trustStore = new File(trustStorePath);
            sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(trustStore, trustStorePassword.toCharArray())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to setup client SSL context", e);
        }

        final HttpClient httpClient = HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .build();

        final ClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean
    GrafanaDashboardBuilder grafanaDashboardBuilder() {
        if(createGrafanaDashboard) {
            return new GrafanaDashboardBuilder(grafanaEndpoint, grafanaBasicAuth, grafanaUser, grafanaPassword, grafanaToken, grafanaDataSource);
        } else {
            return null;
        }
    }
}