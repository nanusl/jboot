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
package io.jboot.component.shiro;

import com.jfinal.core.Controller;
import io.jboot.Jboot;
import io.jboot.component.shiro.processer.AuthorizeResult;
import io.jboot.utils.StringUtils;
import io.jboot.web.fixedinterceptor.FixedInvocation;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 */
public interface JbootShiroInvokeListener {


    /**
     * 通过这个方法，可以用来处理 jwt、sso 等和shiro的整合
     *
     * @param inv
     */
    public void onInvokeBefore(FixedInvocation inv);

    /**
     * 通过这个方法，可以用来自定义shiro 处理结果 和 错误逻辑
     *
     * @param inv
     * @param result
     */
    public void onInvokeAfter(FixedInvocation inv, AuthorizeResult result);


    public static final JbootShiroInvokeListener DEFAULT = new JbootShiroInvokeListener() {


        private JbootShiroConfig config = Jboot.config(JbootShiroConfig.class);


        @Override
        public void onInvokeBefore(FixedInvocation inv) {
            //do nothing
        }

        @Override
        public void onInvokeAfter(FixedInvocation inv, AuthorizeResult result) {
            if (result == null || result.isOk()) {
                inv.invoke();
                return;
            }

            int errorCode = result.getErrorCode();
            switch (errorCode) {
                case AuthorizeResult.ERROR_CODE_UNAUTHENTICATED:
                    doProcessUnauthenticated(inv.getController());
                    break;
                case AuthorizeResult.ERROR_CODE_UNAUTHORIZATION:
                    doProcessuUnauthorization(inv.getController());
                    break;
                default:
                    inv.getController().renderError(404);
            }
        }


        public void doProcessUnauthenticated(Controller controller) {
            if (StringUtils.isBlank(config.getLoginUrl())) {
                controller.renderError(401);
                return;
            }
            controller.redirect(config.getLoginUrl());
        }

        public void doProcessuUnauthorization(Controller controller) {
            if (StringUtils.isBlank(config.getUnauthorizedUrl())) {
                controller.renderError(403);
                return;
            }
            controller.redirect(config.getUnauthorizedUrl());
        }

    };

}
