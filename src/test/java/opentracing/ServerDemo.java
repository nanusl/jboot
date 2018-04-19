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
package opentracing;

import io.jboot.Jboot;
import io.jboot.core.rpc.Jbootrpc;
import service.CategoryService;
import service.CategoryServiceImpl;
import service.UserService;
import service.UserServiceImpl;


public class ServerDemo {


    /**
     * 在启用之前，请先提起启动zipkin
     * 启动zipkin的步骤：
     * 1、下载 zipkin 的jar包：https://search.maven.org/remote_content?g=io.zipkin.java&a=zipkin-server&v=LATEST&c=exec
     * 2、执行 java -jar 下载的jar包路径
     * 3、执行后，浏览器可以访问 http://127.0.0.1:9411 来查看zipkin收集的数据
     *
     * @param args
     */
    public static void main(String[] args) {


        Jboot.setBootArg("jboot.server.port","8083");

        Jboot.setBootArg("jboot.rpc.type", "motan");
        Jboot.setBootArg("jboot.rpc.callMode", "redirect");//直连模式，默认为注册中心
        Jboot.setBootArg("jboot.rpc.directUrl", "localhost:8002");//直连模式的url地址


        Jboot.setBootArg("jboot.tracing.type", "zipkin");
        Jboot.setBootArg("jboot.tracing.serviceName", "ServerDemo");
        Jboot.setBootArg("jboot.tracing.url", "http://127.0.0.1:9411/api/v2/spans");


        Jboot.run(args);

        Jbootrpc factory = Jboot.me().getRpc();

        factory.serviceExport(UserService.class, new UserServiceImpl(), "jboot", "1.0", 8002);
        factory.serviceExport(CategoryService.class, new CategoryServiceImpl(), "jboot", "1.0", 8002);

        System.out.println("ServerDemo started...");


    }
}
