package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.security.JWSChecker;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.mockito.MockitoAnnotations.initMocks;

class AuthProcessorTest {

    @Autowired
    protected CamelContext camelContext = new DefaultCamelContext();

    private AuthProcessor authProcessorUnderTest = new AuthProcessor();

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testProcess() throws Exception {
        // Setup
        ReflectionTestUtils.setField(authProcessorUnderTest, "jwsChecker", new JWSChecker());

        Exchange exchange = new DefaultExchange(camelContext);

        Message message = new DefaultMessage(camelContext);
        message.setHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik1VUkdSVFEyUmtJMk1qZzJOVE0wTTBFMVJESTJNalpETVRNeVJUTTNRakJEUTBVd09EZ3hSZyJ9.eyJpc3MiOiJodHRwczovL3JvZHJpZ29jb2VsaG8uYXV0aDAuY29tLyIsInN1YiI6IjBQSm5McXJTTWdsMUxsa0hMTkdBcFRRMHpXY0I3ZWRoQGNsaWVudHMiLCJhdWQiOiJodHRwczovL2NhcGkuZ2F0ZXdheS5hcGkiLCJpYXQiOjE1Njg0MTI5ODcsImV4cCI6MTU2ODQ5OTM4NywiYXpwIjoiMFBKbkxxclNNZ2wxTGxrSExOR0FwVFEweldjQjdlZGgiLCJndHkiOiJjbGllbnQtY3JlZGVudGlhbHMifQ.Xv7aVxGDe2aeETasG_5f_-9cKOEvVWdx97R4l0quHYGrtAvX-93laHsDf_k1hE5DIWNcoP8B0BlIEH2X8iaWyy81xYa_1rRg5KheXpmrcMTa5mtkUeeyy57CkfY3cdwEQviMuRdN7uW3KuzrmfuqWgW5zd3MW26O49UmBEUTaTiZwhrnkyMobXWWf6-HANerSZCTp8xhAvmyIxeReMo3-Y25HltYbvWaNqBK3PhNfQiR2vygkJY1bx-Ej2Nsv8uMkPBI-5o7zKxvogZRM4dQiecpxYJQeMgt6waoB4AgDISnBLWNLGGadTH8_3vPrLvor-ftvjt9Q71A9L_MC99uyQ");
        message.setHeader("jwks-endpoint", "https://rodrigocoelho.auth0.com");

        exchange.setIn(message);

        // Run the test
        authProcessorUnderTest.process(exchange);

        // Verify the results
        assertFalse(Boolean.getBoolean(exchange.getIn().getHeader("VALID").toString()));

    }
}
