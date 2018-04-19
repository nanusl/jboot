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
package io.jboot.aop.interceptor.cache;

import com.jfinal.template.Engine;
import io.jboot.exception.JbootException;
import io.jboot.utils.ArrayUtils;
import io.jboot.utils.ClassKits;
import io.jboot.utils.StringUtils;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

class Kits {

    static final Engine ENGINE = new Engine("JbootCacheRender");

    /**
     * use jfinal engine render text
     *
     * @param template
     * @param method
     * @param arguments
     * @return
     */
    static String engineRender(String template, Method method, Object[] arguments) {

        Map<String, Object> datas = new HashMap();
        int x = 0;
        /**
         * 在java8下，通过添加 -parameters 进行编译，可以获取 Parameter 的编译前的名字
         * 否则 只能获取 编译后的名字
         */
        for (Parameter p : method.getParameters()) {
            if (!p.isNamePresent()) {
                break;
            }
            datas.put(p.getName(), arguments[x++]);
        }


        /**
         * 保证在java8没有添加 -parameters 的时候，可以通过注解的方式获取参数，保证兼容。
         * 同时，可以通过注解的方式覆盖 默认名称。
         */
        Annotation[][] annotationss = method.getParameterAnnotations();
        for (int i = 0; i < annotationss.length; i++) {
            for (int j = 0; j < annotationss[i].length; j++) {
                Annotation annotation = annotationss[i][j];
                if (annotation.annotationType() == Named.class) {
                    Named named = (Named) annotation;
                    datas.put(named.value(), arguments[i]);
                } else if (annotation.annotationType() == com.google.inject.name.Named.class) {
                    com.google.inject.name.Named named = (com.google.inject.name.Named) annotation;
                    datas.put(named.value(), arguments[i]);
                }
            }
        }

        try {
            return ENGINE.getTemplateByString(template).renderToString(datas);
        } catch (Throwable throwable) {
            throw new JbootException("render template is error! template is " + template, throwable);
        }

    }

    static String buildCacheKey(String key, Class clazz, Method method, Object[] arguments) {

        clazz = ClassKits.getUsefulClass(clazz);

        if (StringUtils.isBlank(key)) {

            if (ArrayUtils.isNullOrEmpty(arguments)) {
                return String.format("%s#%s", clazz.getName(), method.getName());
            }

            Class[] paramTypes = method.getParameterTypes();
            StringBuilder argumentTag = new StringBuilder();
            int index = 0;
            for (Object argument : arguments) {
                String argumentString = converteToString(argument);
                if (argumentString == null) {
                    throw new JbootException("not support empty key for annotation @Cacheable,@CacheEvict or @CachePut " +
                            "at method[" + clazz.getName() + "." + method.getName() + "()] " +
                            "with argument class:" + argument.getClass().getName() + ", " +
                            "please config key properties in @Cacheable, @CacheEvict or @CachePut annotation.");
                }
                argumentTag.append(paramTypes[index++].getClass().getName()).append(":").append(argumentString).append("-");
            }

            //remove last chat '-'
            argumentTag.deleteCharAt(argumentTag.length() - 1);
            return String.format("%s#%s#%s", clazz.getName(), method.getName(), argumentTag);
        }

        if (!key.contains("#(") || !key.contains(")")) {
            return key;
        }

        return Kits.engineRender(key, method, arguments);
    }


    static boolean isPrimitive(Class clazz) {
        return clazz == String.class
                || clazz == Integer.class
                || clazz == int.class
                || clazz == Long.class
                || clazz == long.class
                || clazz == Double.class
                || clazz == double.class
                || clazz == Float.class
                || clazz == float.class
                || clazz == Boolean.class
                || clazz == boolean.class
                || clazz == BigDecimal.class
                || clazz == BigInteger.class
                || clazz == java.util.Date.class
                || clazz == java.sql.Date.class
                || clazz == java.sql.Timestamp.class
                || clazz == java.sql.Time.class;

    }

    static String converteToString(Object object) {
        if (object == null) {
            return "null";
        }
        if (!isPrimitive(object.getClass())) {
            return null;
        }

        if (object instanceof java.util.Date) {
            return String.valueOf(((java.util.Date) object).getTime());
        }

        if (object instanceof java.sql.Date) {
            return String.valueOf(((java.sql.Date) object).getTime());
        }
        if (object instanceof java.sql.Timestamp) {
            return String.valueOf(((java.sql.Timestamp) object).getTime());
        }
        if (object instanceof java.sql.Time) {
            return String.valueOf(((java.sql.Time) object).getTime());
        }

        return String.valueOf(object);

    }


    static boolean isUnless(String unlessString, Method method, Object[] arguments) throws Throwable {

        if (StringUtils.isBlank(unlessString)) {
            return false;
        }

        unlessString = String.format("#(%s)", unlessString);
        String unlessBoolString = engineRender(unlessString, method, arguments);
        return "true".equals(unlessBoolString);
    }

}
