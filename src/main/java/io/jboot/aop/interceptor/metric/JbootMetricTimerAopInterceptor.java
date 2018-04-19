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
package io.jboot.aop.interceptor.metric;


import com.codahale.metrics.Timer;
import io.jboot.Jboot;
import io.jboot.component.metric.annotation.EnableMetricTimer;
import io.jboot.utils.StringUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 用于在AOP拦截，并通过Metrics的Timer进行统计
 */
public class JbootMetricTimerAopInterceptor implements MethodInterceptor {

    private static final String suffix = ".timer";

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        EnableMetricTimer annotation = methodInvocation.getThis().getClass().getAnnotation(EnableMetricTimer.class);

        String name = StringUtils.isBlank(annotation.value())
                ? methodInvocation.getThis().getClass().getName() + "." + methodInvocation.getMethod().getName() + suffix
                : annotation.value();

        Timer meter = Jboot.me().getMetric().timer(name);
        Timer.Context timerContext = meter.time();
        try {
            return methodInvocation.proceed();
        } finally {
            timerContext.stop();
        }

    }
}
