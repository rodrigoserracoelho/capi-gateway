package at.rodrigo.api.gateway.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HazelcastConfigurationTest {



    private HazelcastConfiguration hazelcastConfigurationUnderTest;

    @BeforeEach
    void setUp() {
        hazelcastConfigurationUnderTest = new HazelcastConfiguration();
    }

    @Test
    void testHazelCastConfig() {
        assertEquals("running-apis-instance", hazelcastConfigurationUnderTest.hazelCastConfig().getInstanceName());
    }
}
