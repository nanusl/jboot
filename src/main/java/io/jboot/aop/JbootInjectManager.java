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
package io.jboot.aop;


import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.jfinal.aop.Before;
import io.jboot.aop.annotation.Bean;
import io.jboot.aop.annotation.BeanExclude;
import io.jboot.aop.injector.JbootrpcMembersInjector;
import io.jboot.aop.interceptor.JFinalBeforeInterceptor;
import io.jboot.aop.interceptor.JbootHystrixCommandInterceptor;
import io.jboot.aop.interceptor.cache.JbootCacheEvictInterceptor;
import io.jboot.aop.interceptor.cache.JbootCacheInterceptor;
import io.jboot.aop.interceptor.cache.JbootCachePutInterceptor;
import io.jboot.aop.interceptor.metric.*;
import io.jboot.component.hystrix.annotation.EnableHystrixCommand;
import io.jboot.component.metric.annotation.*;
import io.jboot.core.cache.annotation.CacheEvict;
import io.jboot.core.cache.annotation.CachePut;
import io.jboot.core.cache.annotation.Cacheable;
import io.jboot.core.mq.JbootmqMessageListener;
import io.jboot.core.rpc.annotation.JbootrpcService;
import io.jboot.event.JbootEventListener;
import io.jboot.server.listener.JbootAppListenerManager;
import io.jboot.utils.ArrayUtils;
import io.jboot.utils.ClassScanner;
import io.jboot.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Inject管理器
 */
public class JbootInjectManager implements com.google.inject.Module, TypeListener {

    private static Class[] default_excludes = new Class[]{JbootEventListener.class, JbootmqMessageListener.class, Serializable.class};

    /**
     * 这个manager的创建不能来之ClassNewer
     * 因为 ClassKits 需要 JbootInjectManager，会造成循环调用。
     */
    private static JbootInjectManager manager = new JbootInjectManager();

    public static JbootInjectManager me() {
        return manager;
    }


    private Injector injector;

    private JbootInjectManager() {
        injector = Guice.createInjector(this);
    }


    public Injector getInjector() {
        return injector;
    }


    /**
     * module implements
     *
     * @param binder
     */
    @Override
    public void configure(Binder binder) {


        // 设置 TypeListener
        binder.bindListener(Matchers.any(), this);


        // 设置 Metrics 相关的统计拦截
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(EnableMetricCounter.class), new JbootMetricCounterAopInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(EnableMetricConcurrency.class), new JbootMetricConcurrencyAopInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(EnableMetricHistogram.class), new JbootMetricHistogramAopInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(EnableMetricMeter.class), new JbootMetricMeterAopInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(EnableMetricTimer.class), new JbootMetricTimerAopInterceptor());


        // 设置 hystricx 的拦截器
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(EnableHystrixCommand.class), new JbootHystrixCommandInterceptor());

        // 设置 Jfinal AOP 相关的拦截器
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Before.class), new JFinalBeforeInterceptor());
        binder.bindInterceptor(Matchers.annotatedWith(Before.class), Matchers.any(), new JFinalBeforeInterceptor());

        // 设置缓存相关的拦截器
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(Cacheable.class), new JbootCacheInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(CacheEvict.class), new JbootCacheEvictInterceptor());
        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(CachePut.class), new JbootCachePutInterceptor());

        /**
         * Bean 注解
         */
        beanBind(binder);

        //自定义aop configure
        JbootAppListenerManager.me().onGuiceConfigure(binder);
    }


    /**
     * auto bind interface impl
     *
     * @param binder
     */
    private void beanBind(Binder binder) {

        List<Class> classes = ClassScanner.scanClassByAnnotation(Bean.class, true);
        for (Class impl : classes) {
            Class<?>[] interfaceClasses = impl.getInterfaces();
            Bean bean = (Bean) impl.getAnnotation(Bean.class);
            String name = bean.name();

            BeanExclude beanExclude = (BeanExclude) impl.getAnnotation(BeanExclude.class);

            //对某些系统的类 进行排除，例如：Serializable 等
            Class[] excludes = beanExclude == null ? default_excludes : ArrayUtils.concat(default_excludes, beanExclude.value());

            for (Class interfaceClass : interfaceClasses) {
                boolean isContinue = false;
                for (Class ex : excludes) {
                    if (ex.isAssignableFrom(interfaceClass)) {
                        isContinue = true;
                        break;
                    }
                }
                if (isContinue) {
                    continue;
                }
                try {
                    if (StringUtils.isBlank(name)) {
                        binder.bind(interfaceClass).to(impl);
                    } else {
                        binder.bind(interfaceClass).annotatedWith(Names.named(name)).to(impl);
                    }
                } catch (Throwable ex) {
                    System.err.println(String.format("can not bind [%s] to [%s]", interfaceClass, impl));
                }
            }
        }
    }

    /**
     * TypeListener  implements
     *
     * @param type
     * @param encounter
     * @param <I>
     */
    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        Class clazz = type.getRawType();
        if (clazz == null) return;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(JbootrpcService.class)) {
                encounter.register(new JbootrpcMembersInjector(field));
            }
        }
    }
}
