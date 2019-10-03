package at.rodrigo.api.gateway.config;

import at.rodrigo.api.gateway.cache.CAPICacheConfig;
import at.rodrigo.api.gateway.utils.Constants;
import com.hazelcast.config.Config;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.component.micrometer.DistributionStatisticConfigFilter;
import org.apache.camel.component.micrometer.messagehistory.MicrometerMessageHistoryFactory;
import org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyFactory;
import org.apache.camel.zipkin.ZipkinTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.time.Duration;

import static org.apache.camel.component.micrometer.MicrometerConstants.DISTRIBUTION_SUMMARIES;
import static org.apache.camel.component.micrometer.messagehistory.MicrometerMessageHistoryNamingStrategy.MESSAGE_HISTORIES;
import static org.apache.camel.component.micrometer.routepolicy.MicrometerRoutePolicyNamingStrategy.ROUTE_POLICIES;

@Configuration
public class CAPICamelConfiguration {

    @Value("${api.gateway.zipkin.endpoint}")
    private String zipkinEndpoint;

    @Autowired
    private CamelContext camelContext;

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
}