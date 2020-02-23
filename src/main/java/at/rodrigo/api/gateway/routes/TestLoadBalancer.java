package at.rodrigo.api.gateway.routes;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.processor.RouteErrorProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.zipkin.ZipkinTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Component
@Slf4j
public class TestLoadBalancer extends RouteBuilder {

    @Autowired
    private RouteErrorProcessor routeErrorProcessor;

    @Autowired
    private ZipkinTracer zipkinTracer;

    @Override
    public void configure() throws Exception {



        boolean zipkinTraceIDVisible = true;
        boolean internalMessageVisible = true;


        String context = "/lb/test";
        String routeID = UUID.randomUUID().toString();
        Collection<String> hostList = new ArrayList<>();

        hostList.add("http://capi.ecdevops.eu:9011/exposed?bridgeEndpoint=true&throwExceptionOnFailure=false&connectTimeout=5000&socketTimeout=10000&sleepValue=0&lb=2&connectionClose=true");
        hostList.add("http://capi.ecdevops.eu:9010/exposed?bridgeEndpoint=true&throwExceptionOnFailure=false&connectTimeout=10000&socketTimeout=10000&sleepValue=5000&lb=1&connectionClose=true");

        RouteDefinition routeDefinition = rest().get(context).route();

        routeDefinition
                .onException(Exception.class)
                .handled(true)
                .setHeader("showTraceID", constant(zipkinTraceIDVisible))
                .setHeader("showInternal", constant(internalMessageVisible))
                .process(routeErrorProcessor)
                .to("https://localhost:8380/error?bridgeEndpoint=true&throwExceptionOnFailure=false");
        String hostsCommaSeparated = String.join(",", hostList);
        routeDefinition
                .loadBalance().roundRobin()
                .to("http://capi.ecdevops.eu:9011/exposed?bridgeEndpoint=true&throwExceptionOnFailure=false&connectTimeout=5000&socketTimeout=10000&sleepValue=0&lb=2&connectionClose=true")
                .to("http://capi.ecdevops.eu:9010/exposed?bridgeEndpoint=true&throwExceptionOnFailure=false&connectTimeout=10000&socketTimeout=10000&sleepValue=5000&lb=1&connectionClose=true")
                //.roundRobin()
                //.to(hostsCommaSeparated)

                //.to("http://capi.ecdevops.eu:9011/exposed?bridgeEndpoint=true&throwExceptionOnFailure=false&connectTimeout=5000&socketTimeout=10000&sleepValue=0&lb=2&connectionClose=true")
                //.to("http://capi.ecdevops.eu:9010/exposed?bridgeEndpoint=true&throwExceptionOnFailure=false&connectTimeout=10000&socketTimeout=10000&sleepValue=5000&lb=1&connectionClose=true")
        .setId(routeID);
        zipkinTracer.addServerServiceMapping(context, routeID);

        //put connectTimeout as global in properties and in the api definition
        //put socketTimeout as global in properties and in the api definition
    }
}
