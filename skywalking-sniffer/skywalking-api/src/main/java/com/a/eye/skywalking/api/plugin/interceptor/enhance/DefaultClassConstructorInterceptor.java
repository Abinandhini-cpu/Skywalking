package com.a.eye.skywalking.api.plugin.interceptor.enhance;

import com.a.eye.skywalking.api.logging.api.ILog;
import com.a.eye.skywalking.api.logging.api.LogManager;
import com.a.eye.skywalking.api.plugin.interceptor.EnhancedClassInstanceContext;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldProxy;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Created by xin on 2016/12/6.
 */
public class DefaultClassConstructorInterceptor {
    private static ILog logger = LogManager
            .getLogger(ClassConstructorInterceptor.class);

    @RuntimeType
    public void intercept(
            @This Object obj,
            @FieldProxy(ClassEnhancePluginDefine.contextAttrName) FieldSetter accessor,
            @AllArguments Object[] allArguments) {
        try {
            EnhancedClassInstanceContext context = new EnhancedClassInstanceContext();
            accessor.setValue(context);
        } catch (Throwable t) {
            logger.error("ClassConstructorInterceptor failure.", t);
        }

    }
}
