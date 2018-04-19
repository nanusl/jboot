/**
 * Copyright (c) 2015-2018, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jboot.db.model;

import io.jboot.Jboot;
import io.jboot.config.annotation.PropertyConfig;
import io.jboot.db.JbootDbHystrixFallbackListenerDefault;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Package io.jboot.db.model
 */
@PropertyConfig(prefix = "jboot.model")
public class JbootModelConfig {

    private boolean cacheEnable = true;
    private int cacheTime = 60 * 60 * 24; // 1day
    private String scan;

    private boolean hystrixEnable = true;
    private int hystrixTimeout = 1000 * 10; //单位：毫秒
    private String hystrixFallbackListener = JbootDbHystrixFallbackListenerDefault.class.getName();

    private String columnCreated = "created";
    private String columnModified = "modified";

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    public void setCacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public int getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(int cacheTime) {
        this.cacheTime = cacheTime;
    }

    public String getScan() {
        return scan;
    }

    public void setScan(String scan) {
        this.scan = scan;
    }

    public boolean isHystrixEnable() {
        return hystrixEnable;
    }

    public void setHystrixEnable(boolean hystrixEnable) {
        this.hystrixEnable = hystrixEnable;
    }

    public int getHystrixTimeout() {
        return hystrixTimeout;
    }

    public void setHystrixTimeout(int hystrixTimeout) {
        this.hystrixTimeout = hystrixTimeout;
    }

    public String getHystrixFallbackListener() {
        return hystrixFallbackListener;
    }

    public String getColumnCreated() {
        return columnCreated;
    }

    public void setColumnCreated(String columnCreated) {
        this.columnCreated = columnCreated;
    }

    public String getColumnModified() {
        return columnModified;
    }

    public void setColumnModified(String columnModified) {
        this.columnModified = columnModified;
    }

    public void setHystrixFallbackListener(String hystrixFallbackListener) {
        this.hystrixFallbackListener = hystrixFallbackListener;
    }

    private static JbootModelConfig config;

    public static JbootModelConfig getConfig() {
        if (config == null) {
            config = Jboot.config(JbootModelConfig.class);
        }
        return config;
    }

}
