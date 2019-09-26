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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.ParseException;
import java.util.List;


@Component
@Slf4j
public class AuthProcessor implements Processor {

   @Autowired
   private JWSChecker jwsChecker;

    public void process(Exchange exchange) {
        boolean validCall = false;
        try {
            String jwtKeysEndpoint = exchange.getIn().getHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER).toString();
            String jwtToken = exchange.getIn().getHeader(Constants.AUTHORIZATION_HEADER).toString().substring("Bearer ".length());
            List<String> apiAudienceList = (List<String>) exchange.getIn().getHeader(Constants.AUDIENCE_HEADER);
            exchange.getIn().removeHeader(Constants.JSON_WEB_KEY_SIGNATURE_ENDPOINT_HEADER);
            exchange.getIn().removeHeader(Constants.AUTHORIZATION_HEADER);
            exchange.getIn().removeHeader(Constants.BLOCK_IF_IN_ERROR_HEADER);
            exchange.getIn().removeHeader(Constants.AUDIENCE_HEADER);

            if(jwtKeysEndpoint != null && jwtToken != null) {
                ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
                JWKSource keySource = new RemoteJWKSet(new URL(jwtKeysEndpoint));
                JWSAlgorithm expectedJWSAlg = jwsChecker.getAlgorithm(jwtToken);
                JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
                jwtProcessor.setJWSKeySelector(keySelector);
                JWTClaimsSet claimsSet = jwtProcessor.process(jwtToken, null);
                if(claimsSet != null) {
                    List<String> tokenAudienceList = claimsSet.getAudience();
                    for(String tokenAudience : tokenAudienceList) {
                        if(apiAudienceList.contains(tokenAudience)) {
                            validCall = true;
                        }
                    }
                }
                if(!validCall) {
                    exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
                    exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid audience was provided");
                    exchange.setException(null);
                }
            } else {
                exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.BAD_REQUEST.value());
                exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid token was provided");
                exchange.setException(null);
            }
        } catch(ParseException pex) {
            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.BAD_REQUEST.value());
            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, "Invalid token was provided");
            exchange.setException(null);
        } catch(Exception e) {
            exchange.getIn().setHeader(Constants.REASON_CODE_HEADER, HttpStatus.FORBIDDEN.value());
            exchange.getIn().setHeader(Constants.REASON_MESSAGE_HEADER, e.getMessage());
            exchange.setException(null);
        } finally {
            exchange.getIn().setHeader(Constants.VALID_HEADER, validCall);
        }
    }
}
