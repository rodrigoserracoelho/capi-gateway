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
package at.rodrigo.api.gateway.grafana;

public class GrafanaConstants {
    public static final String BROWSER = "browser";
    public static final String DEFAULT_TIME_FROM = "now-5m";
    public static final String DEFAULT_TIME_TO = "now";
    public static final int DEFAULT_DECIMAL_VALUE = 0;
    public static final int SCHEMA_VERSION = 16;
    public static final int DASHBOARD_VERSION = 0;
    public static final int FOLDER_ID = 0;
    public static final String PLUGIN_VERSION = "7.0.0";
    public static final String PANEL_TYPE = "graph";
    public static final String REFRESH = "5s";

    public static final int DEFAULT_GRIS_POS_H = 9;
    public static final int DEFAULT_GRIS_POS_W = 12;
    public static final int DEFAULT_GRIS_POS_X = 0;
    public static final int DEFAULT_GRIS_POS_Y = 0;

    public static final String COLOR_MODE = "value";
    public static final String GRAPH_MODE = "area";
    public static final String JUSTIFY_MODE = "auto";
    public static final String ORIENTATION = "auto";

    public static final String DEFAULT_REF_ID = "A";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String APPLICATION_TYPE_JSON = "application/json";
    public static final String GRAFANA_API_CALL_ERROR = "Error creating Grafana Dashboard. Error: {}";
}