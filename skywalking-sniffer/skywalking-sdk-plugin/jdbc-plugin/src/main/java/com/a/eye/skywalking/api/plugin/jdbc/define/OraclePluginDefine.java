package com.a.eye.skywalking.api.plugin.jdbc.define;

/**
 * Created by xin on 16/8/4.
 */
public class OraclePluginDefine extends AbstractDatabasePluginDefine {
    @Override
    protected String enhanceClassName() {
        return "oracle.jdbc.OracleDriver";
    }
}
