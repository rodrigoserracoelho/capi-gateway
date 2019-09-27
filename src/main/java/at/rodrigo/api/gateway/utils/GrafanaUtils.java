package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.grafana.entity.GrafanaDashboard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GrafanaUtils {

    @Autowired
    private RestTemplate restTemplate;

    public void addToGrafana(Api api) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Api> request = new HttpEntity<>(api, headers);
            ResponseEntity<GrafanaDashboard> response = restTemplate.exchange("http://localhost:8080/grafana", HttpMethod.POST, request, GrafanaDashboard.class);
            log.info(response.getStatusCode()+"");
        } catch(Exception e) {
            log.info(e.getMessage(), e);
        }
    }
}
