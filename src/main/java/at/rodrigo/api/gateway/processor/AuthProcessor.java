package at.rodrigo.api.gateway.processor;

import at.rodrigo.api.gateway.security.JWSChecker;
import at.rodrigo.api.gateway.utils.Constants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;


@Component
@Slf4j
public class AuthProcessor implements Processor {

   @Autowired
   private JWSChecker jwsChecker;

    public void process(Exchange exchange) throws Exception {
        exchange.getIn().setHeader("VALID", false);
        try {
            String jwtKeysEndpoint = exchange.getIn().getHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER).toString();
            String jwtToken = exchange.getIn().getHeader(Constants.AUTHORIZATION_HEADER).toString().substring("Bearer ".length());
            exchange.getIn().removeHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER);
            exchange.getIn().removeHeader(Constants.AUTHORIZATION_HEADER);
//authorizationHeader.substring("Bearer ".length()) : null;
            if(jwtKeysEndpoint != null && jwtToken != null) {
            //try {
                ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
                JWKSource keySource = new RemoteJWKSet(new URL(jwtKeysEndpoint));
                JWSAlgorithm expectedJWSAlg = jwsChecker.getAlgorithm(jwtToken);
                JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);
                if(claimsSet != null) {
                    exchange.getIn().setHeader("VALID", true);
                }
            }
        } catch(Exception e) {
            log.info(e.getClass().getCanonicalName());
            log.info("--------------------| "+ e.getMessage());
exchange.setException(null);

        }
    }
}
