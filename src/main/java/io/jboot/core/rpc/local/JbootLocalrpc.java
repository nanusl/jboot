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
package io.jboot.core.rpc.local;

import io.jboot.core.rpc.JbootrpcBase;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JbootLocalrpc extends JbootrpcBase {

    Map<Class, Object> objectMap = new ConcurrentHashMap<>();

    @Override
    public <T> T serviceObtain(Class<T> serviceClass, String group, String version) {
        return (T) objectMap.get(serviceClass);
    }

    @Override
    public <T> boolean serviceExport(Class<T> interfaceClass, Object object, String group, String version, int port) {
        objectMap.put(interfaceClass, object);
        return true;
    }
}
