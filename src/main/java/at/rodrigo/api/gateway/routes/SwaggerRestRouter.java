package at.rodrigo.api.gateway.routes;


import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SwaggerRestRouter extends RouteBuilder {

    @Autowired
    SwaggerParser swaggerParser;

    @Override
    public void configure() {

        log.info("Starting configuration of Swagger Routes");

        List<Path> pathList = swaggerParser.parse("http://localhost:9010/v2/api-docs");
        addRoutes(pathList);

    }

    public void addRoutes(List<Path> pathList) {

        for(Path path : pathList) {
            if(!path.getPath().equals("/error")) {
                RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
                List<String> paramList = evaluatePath(path.getPath());
                switch(path.getVerb()) {
                    case GET:
                        if(paramList.isEmpty()) {
                            rest()
                                    .get(path.getPath()).route()
                                    .toF("http4://localhost:9010" + path.getPath() + "?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();
                        } else {
                            for(String param : paramList) {
                                restParamDefinition.name(param)
                                        .type(RestParamType.path)
                                        .dataType("String");
                            }
                            rest()
                                    .get(path.getPath())
                                    .param(restParamDefinition)
                                    .route()
                                    .toF("http4://localhost:9010?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();

                        }
                        break;
                    case POST:
                        if(paramList.isEmpty()) {
                            rest()
                                    .post(path.getPath()).route()
                                    .toF("http4://localhost:9010" + path.getPath() + "?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();
                        } else {
                            for(String param : paramList) {
                                restParamDefinition.name(param)
                                        .type(RestParamType.path)
                                        .dataType("String");
                            }
                            rest()
                                    .post(path.getPath())
                                    .param(restParamDefinition)
                                    .route()
                                    .toF("http4://localhost:9010?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();

                        }
                        break;
                    case PUT:
                        if(paramList.isEmpty()) {
                            rest()
                                    .put(path.getPath()).route()
                                    .toF("http4://localhost:9010" + path.getPath() + "?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();
                        } else {
                            for(String param : paramList) {
                                restParamDefinition.name(param)
                                        .type(RestParamType.path)
                                        .dataType("String");
                            }
                            rest()
                                    .put(path.getPath())
                                    .param(restParamDefinition)
                                    .route()
                                    .toF("http4://localhost:9010?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();

                        }
                        break;
                    case DELETE:
                        if(paramList.isEmpty()) {
                            rest()
                                    .delete(path.getPath()).route()
                                    .toF("http4://localhost:9010" + path.getPath() + "?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();
                        } else {
                            for(String param : paramList) {
                                restParamDefinition.name(param)
                                        .type(RestParamType.path)
                                        .dataType("String");
                            }
                            rest()
                                    .delete(path.getPath())
                                    .param(restParamDefinition)
                                    .route()
                                    .toF("http4://localhost:9010?bridgeEndpoint=true&copyHeaders=true&connectionClose=true")
                                    .end();

                        }
                        break;
                        default:
                            log.info("No implementation");
                            break;


                }

                }
            }

        }





    public List<String> evaluatePath(String fullPath) {
        List<String> paramList = new ArrayList<>();
        if(fullPath.contains("{")) {
            String[] splittedPath = fullPath.split("/");
            for(String path : splittedPath) {
                if(path.contains("{")) {
                    String name = path.substring(1, path.length()-1);
                    paramList.add(name);
                }
            }
        }
        return paramList;
    }



}