package at.rodrigo.api.gateway.grafana;

import at.rodrigo.api.gateway.grafana.entity.GrafanaDashboard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class GrafanaClient {

    //@Autowired
    //private RestTemplate restTemplate;

    public void getApiKey() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("eyJrIjoic1RsVjNOdFNhUDQyUU9kVjJteG5HcGxuT0lMQmVPZDciLCJuIjoiY2FwaSIsImlkIjoxfQ==");
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<GrafanaDashboard> response = restTemplate.exchange("http://localhost:3000/api/dashboards/uid/dT_dSspWk", HttpMethod.GET, entity, GrafanaDashboard.class);
        log.info(response.getBody().getDashboard().getUid());
    }

/*
* curl -H "Authorization: Bearer eyJrIjoialdhUEpGVEl5bVhZWHl6QTl3Smw3VDB0SjBuV2lDN1MiLCJuIjoiY2FwaSIsImlkIjoxfQ==" http://localhost:3000/api/dashboards/home
* */
}
