package at.rodrigo.api.gateway.utils;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.grafana.entity.GrafanaDashboard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class GrafanaUtils {

    @Value("${api.gateway.grafana.create.panels}")
    private boolean autoCreatePanels;

    @Value("${api.gateway.grafana.endpoint}")
    private String grafanaEndpoint;

    @Autowired
    private RestTemplate restTemplate;

    public void addToGrafana(Api api) {
        if(autoCreatePanels) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Api> request = new HttpEntity<>(api, headers);
                restTemplate.exchange(grafanaEndpoint, HttpMethod.POST, request, GrafanaDashboard.class);
            } catch(Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }
}
