package com.a.eye.skywalking.api.logging.api;

/**
 * {@link LogResolver} just do only one thing: return the {@link ILog} implementation.
 * <p>
 * Created by xin on 2016/11/10.
 */
public interface LogResolver {
    /**
     * @param clazz, the class is showed in log message.
     * @return {@link ILog} implementation.
     */
    ILog getLogger(Class<?> clazz);
}
