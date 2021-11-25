package io.surisoft.capi.gateway.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.apache.curator.shaded.com.google.common.collect.Lists.newArrayList;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket labelApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .produces(new HashSet<>(Arrays.asList("application/json")))
                .consumes(new HashSet<>(Arrays.asList("application/json")))
                .select()
                .apis(RequestHandlerSelectors.basePackage("at.rodrigo.api.gateway.rest"))
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .paths(Predicates.not(PathSelectors.regex("/consumer.*")))
                .paths(Predicates.not(PathSelectors.regex("/grafana.*")))
                .build()
                .securitySchemes(newArrayList(apiKey()))
                .securityContexts(newArrayList(securityContext()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("CAPI Gateway")
                .description("Management Endpoint")
                .version("1.0").contact(new Contact("SURISOFT","","me@rodrigo.at"))
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("Bearer", "Authorization", "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/.*"))
                .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return newArrayList(new SecurityReference("Bearer", authorizationScopes));
    }
}
