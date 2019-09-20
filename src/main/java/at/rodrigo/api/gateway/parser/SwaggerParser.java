package at.rodrigo.api.gateway.parser;

import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.Verb;
import io.swagger.models.Swagger;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SwaggerParser {


    public List<Path> parse(String endpoint) {

        List<Path> capiPathList = new ArrayList<>();

        try {
            //Check OpenAPI 3
            OpenAPI openAPI = new OpenAPIV3Parser().read(endpoint);
            if (openAPI != null && openAPI.getPaths() != null) {
                io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();
                Iterator<String> iterator = paths.keySet().iterator();
                while(iterator.hasNext()) {
                    Path capiPath = new Path();

                    String key = iterator.next();
                    PathItem pathItem = paths.get(key);
                    capiPath.setPath(key);

                    if(pathItem.getGet() != null) {
                        capiPath.setVerb(Verb.GET);
                    }
                    if(pathItem.getPost() != null) {
                        capiPath.setVerb(Verb.POST);
                    }
                    if(pathItem.getPut() != null) {
                        capiPath.setVerb(Verb.PUT);
                    }
                    if(pathItem.getDelete() != null) {
                        capiPath.setVerb(Verb.DELETE);
                    }

                    capiPathList.add(capiPath);
                }
            } else {
                //Swagger 2
                io.swagger.parser.SwaggerParser swaggerParser = new io.swagger.parser.SwaggerParser();
                Swagger swaggerObject = swaggerParser.read(endpoint);
                if (swaggerObject != null) {
                    Map<String, io.swagger.models.Path> paths = swaggerObject.getPaths();
                    Iterator<String> iterator = paths.keySet().iterator();
                    while(iterator.hasNext()) {
                        Path capiPath = new Path();

                        String key = iterator.next();
                        capiPath.setPath(key);
                        io.swagger.models.Path pathItem = paths.get(key);

                        if(pathItem.getGet() != null) {
                            capiPath.setVerb(Verb.GET);
                        }
                        if(pathItem.getPost() != null) {
                            capiPath.setVerb(Verb.POST);
                        }
                        if(pathItem.getPut() != null) {
                            capiPath.setVerb(Verb.PUT);
                        }
                        if(pathItem.getDelete() != null) {
                            capiPath.setVerb(Verb.DELETE);
                        }

                        capiPathList.add(capiPath);

                    }
                }
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return capiPathList;
    }
}