package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.cache.RunningApiManager;
import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.entity.Verb;
import at.rodrigo.api.gateway.processor.AuthProcessor;
import at.rodrigo.api.gateway.utils.CamelUtils;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

class TempReloadControllerTest {

    protected CamelContext camelContext = new DefaultCamelContext();

    private AuthProcessor authProcessor = new AuthProcessor();

    private RunningApiManager runningApiManager = new RunningApiManager();

    private CamelUtils camelUtils = new CamelUtils();

    private static TestHazelcastInstanceFactory testInstanceFactory = new TestHazelcastInstanceFactory();

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testPost() throws Exception {

        Api api = new Api();
        api.setEndpoint("localhost:9010");
        api.setName("ROD-UNSAFE-API");
        api.setSecured(false);
        api.setContext("super-unsafe");
        api.setId(UUID.randomUUID().toString());

        Path path = new Path();
        path.setPath("/internal");
        path.setVerb(Verb.GET);
        path.setBlockIfInError(false);
        path.setMaxAllowedFailedCalls(-1);
        List<Path> apiPathList = new ArrayList<>();
        apiPathList.add(path);
        api.setPaths(apiPathList);

        TempReloadController tempReloadControllerUnderTest = new TempReloadController();

        ReflectionTestUtils.setField(runningApiManager, "hazelcastInstance", testInstanceFactory.newHazelcastInstance());
        ReflectionTestUtils.setField(tempReloadControllerUnderTest, "runningApiManager", runningApiManager);
        ReflectionTestUtils.setField(tempReloadControllerUnderTest, "camelContext", camelContext);
        ReflectionTestUtils.setField(tempReloadControllerUnderTest, "authProcessor", authProcessor);
        ReflectionTestUtils.setField(tempReloadControllerUnderTest, "camelUtils", camelUtils);
        ReflectionTestUtils.setField(tempReloadControllerUnderTest, "apiGatewayErrorEndpoint", "localhost:8380/error");

        JSONObject result = new JSONObject();
        result.put("result", "created");
        result.put("api", api);

        final ResponseEntity<String> expectedResult = new ResponseEntity<>(result.toString(), HttpStatus.OK);

        // Run the test
        ResponseEntity<String> responseResult = tempReloadControllerUnderTest.post(api);

        // Verify the results
        assertEquals(expectedResult, responseResult);
    }
}
