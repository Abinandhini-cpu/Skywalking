package com.a.eye.skywalking.api.logging;

import com.a.eye.skywalking.api.logging.api.ILog;
import com.a.eye.skywalking.api.logging.api.LogResolver;

/**
 * Created by wusheng on 2016/11/26.
 */
public class EasyLogResolver implements LogResolver {
    @Override
    public ILog getLogger(Class<?> clazz) {
        return new EasyLogger(clazz);
    }
}
