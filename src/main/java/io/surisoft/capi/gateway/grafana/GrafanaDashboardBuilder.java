/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package io.surisoft.capi.gateway.grafana;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GrafanaDashboardBuilder {

    private String grafanaEndpoint;
    private boolean basicAuthorization;
    private String grafanaUser;
    private String grafanaPassword;
    private String grafanaToken;
    private String grafanaDataSource;

    public GrafanaDashboardBuilder(String grafanaEndpoint, boolean basicAuthorization, String grafanaUser, String grafanaPassword, String grafanaToken, String grafanaDataSource) {
        this.grafanaEndpoint = grafanaEndpoint;
        this.basicAuthorization = basicAuthorization;
        this.grafanaUser = grafanaUser;
        this.grafanaPassword = grafanaPassword;
        this.grafanaToken = grafanaToken;
        this.grafanaDataSource = grafanaDataSource;
    }

    public void buildDashboardObject(String title,
                                     List<String> tags,
                                     List<Panel> panels) {

        Dashboard dashboard = new Dashboard();
        dashboard.setTitle(title);
        dashboard.setTags(tags);
        dashboard.setTimezone(GrafanaConstants.BROWSER);
        dashboard.setSchemaVersion(GrafanaConstants.SCHEMA_VERSION);
        dashboard.setVersion(GrafanaConstants.DASHBOARD_VERSION);
        dashboard.setRefresh(GrafanaConstants.REFRESH);

        Time time = new Time();
        time.setFrom(GrafanaConstants.DEFAULT_TIME_FROM);
        time.setTo(GrafanaConstants.DEFAULT_TIME_TO);

        dashboard.setTime(time);
        dashboard.setFolderId(GrafanaConstants.FOLDER_ID);
        dashboard.setOverwrite(false);
        dashboard.setPanels(panels);

        for(int i=0; i<dashboard.getPanels().size(); i++) {
            dashboard.getPanels().get(i).setId(i);
        }

        GrafanaDashboard grafanaDashboard = new GrafanaDashboard();
        grafanaDashboard.setDashboard(dashboard);
        try {
            Response response = createDashboard(grafanaDashboard);
            if(!response.isSuccessful()) {
                log.info(GrafanaConstants.GRAFANA_API_CALL_ERROR, response.body().string());
            }
            response.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Panel buildPanelObject(String routeID, String panelTitle) {
        Panel panel = new Panel();
        panel.setDatasource(grafanaDataSource);

        Defaults defaults = new Defaults();
        defaults.setDecimals(GrafanaConstants.DEFAULT_DECIMAL_VALUE);

        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.setDefaults(defaults);

        panel.setFieldConfig(fieldConfig);

        GridPos gridPos = new GridPos();
        gridPos.setH(GrafanaConstants.DEFAULT_GRIS_POS_H);
        gridPos.setW(GrafanaConstants.DEFAULT_GRIS_POS_W);
        gridPos.setX(GrafanaConstants.DEFAULT_GRIS_POS_X);
        gridPos.setY(GrafanaConstants.DEFAULT_GRIS_POS_Y);

        panel.setGridPos(gridPos);

        Options options = new Options();
        options.setColorMode(GrafanaConstants.COLOR_MODE);
        options.setGraphMode(GrafanaConstants.GRAPH_MODE);
        options.setJustifyMode(GrafanaConstants.JUSTIFY_MODE);
        options.setOrientation(GrafanaConstants.ORIENTATION);

        panel.setOptions(options);
        panel.setPluginVersion(GrafanaConstants.PLUGIN_VERSION);

        Target target = new Target();
        target.setExpr(buildExpression(routeID));
        target.setInterval("");
        target.setLegendFormat("");
        target.setRefId(GrafanaConstants.DEFAULT_REF_ID);

        List<Target> targets = new ArrayList<>();
        targets.add(target);

        panel.setTargets(targets);
        panel.setTitle(panelTitle);
        panel.setType(GrafanaConstants.PANEL_TYPE);

        return panel;
    }

    private String buildExpression(String routeID) {
        return "increase(" + routeID + "_total[24h])";
    }

    private Response createDashboard(GrafanaDashboard grafanaDashboard) throws IOException {
        OkHttpClient client = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        RequestBody requestBody = RequestBody.create(MediaType.parse(GrafanaConstants.APPLICATION_TYPE_JSON), objectMapper.writeValueAsString(grafanaDashboard));

        Request.Builder requestBuilder = new Request.Builder().url(grafanaEndpoint);
        if(basicAuthorization && grafanaUser != null && grafanaPassword != null) {
            requestBuilder.addHeader(GrafanaConstants.AUTHORIZATION_HEADER, Credentials.basic(grafanaUser, grafanaPassword));
        } else if(grafanaToken != null) {
            requestBuilder.addHeader(GrafanaConstants.AUTHORIZATION_HEADER, GrafanaConstants.BEARER + grafanaToken);
        }

        requestBuilder.post(requestBody);
        Request request = requestBuilder.build();

        Call call = client.newCall(request);
        return call.execute();
    }
}