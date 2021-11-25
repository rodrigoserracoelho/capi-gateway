package io.surisoft.capi.gateway.controller;

import io.surisoft.capi.gateway.cache.CacheConstants;
import io.surisoft.capi.gateway.repository.ApiRepository;
import io.surisoft.capi.gateway.schema.Api;
import io.surisoft.capi.gateway.utils.ApiValidator;
import com.hazelcast.core.HazelcastInstance;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/route")
@Slf4j
@io.swagger.annotations.Api(value = "API's Management", tags = {"API's Management"})
public class ManagerController {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiValidator apiValidator;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @ApiOperation(value = "Get all API's (swagger)")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Info", response = Api.class),
            @ApiResponse(code = 400, message = "Bad request")
    })
    @GetMapping
    public ResponseEntity<List<Api>> getSwaggerRestRoutes(HttpServletRequest request) {
        return new ResponseEntity<>(apiRepository.findAll(), HttpStatus.OK);
    }

    @ApiOperation(value = "Publish an API with an exposed swagger endpoint")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Created"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 412, message = "Pre Condition failed")
    })
    @PostMapping
    public ResponseEntity<String> postSwaggerEndpoints(@RequestBody Api api, HttpServletRequest request) {
        if(apiValidator.isApiValid(api)) {
            apiRepository.save(api);
            hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).put(api.getId(), api);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @ApiOperation(value = "Delete an API")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "API Deleted"),
            @ApiResponse(code = 400, message = "Bad request"),
            @ApiResponse(code = 412, message = "Pre Condition failed")
    })
    @DeleteMapping( path="/{apiId}" )
    public ResponseEntity<String> deleteApi(@PathVariable String apiId, HttpServletRequest request) {
        boolean canDelete = true;
        Optional<Api> api = apiRepository.findById(apiId);
        if(canDelete) {
            apiRepository.delete(api.get());
            hazelcastInstance.getMap(CacheConstants.API_IMAP_NAME).remove(api.get().getId());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>("API Contains active subscribers", HttpStatus.FORBIDDEN);
    }
}