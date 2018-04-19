/**
 * Copyright (c) 2015-2018, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.server;

import io.jboot.Jboot;
import io.jboot.server.jetty.JettyServer;
import io.jboot.server.tomcat.TomcatServer;
import io.jboot.server.undertow.UnderTowServer;


public class JbootServerFactory {


    private static JbootServerFactory me = new JbootServerFactory();

    public static JbootServerFactory me() {
        return me;
    }


    public JbootServer buildServer() {

        JbootServerConfig jbootServerConfig = Jboot.config(JbootServerConfig.class);


        switch (jbootServerConfig.getType()) {
            case JbootServerConfig.TYPE_UNDERTOW:
                return new UnderTowServer();
            case JbootServerConfig.TYPE_TOMCAT:
                return new TomcatServer();
            case JbootServerConfig.TYPE_JETTY:
                return new JettyServer();
            default:
                return new UnderTowServer();
        }
    }

}
