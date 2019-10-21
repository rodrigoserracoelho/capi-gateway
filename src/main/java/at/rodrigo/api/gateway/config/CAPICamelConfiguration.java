package at.rodrigo.api.gateway.config;

import at.rodrigo.api.gateway.cache.CAPICacheConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

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

    @Value("${server.ssl.key-alias}")
    String serverSslKeyAlias;

    @Value("${server.ssl.key-store-password}")
    String serverSslKeyPassword;

    @Value("${server.ssl.key-store}")
    String serverSslKeyStore;

    @Value("${server.ssl.trust-store}")
    String trustStorePath;

    @Value("${server.ssl.trust-store-password}")
    String trustStorePassword;

    @Autowired
    private CamelContext camelContext;

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public Config hazelCastConfig() {
        return new CAPICacheConfig();
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
            keyStore.load(new FileInputStream(serverSslKeyStore), serverSslKeyPassword.toCharArray());
            RSAPrivateCrtKey key = (RSAPrivateCrtKey) keyStore.getKey(serverSslKeyAlias, serverSslKeyPassword.toCharArray());
            RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            KeyPair keyPair = new KeyPair(publicKey, key);
            RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(serverSslKeyAlias);
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

}