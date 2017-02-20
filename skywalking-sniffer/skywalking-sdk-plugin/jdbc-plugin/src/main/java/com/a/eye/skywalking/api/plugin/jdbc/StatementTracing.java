package com.a.eye.skywalking.api.plugin.jdbc;

import com.a.eye.skywalking.api.plugin.jdbc.define.JDBCBuriedPointType;
import com.a.eye.skywalking.model.Identification;
import com.a.eye.skywalking.invoke.monitor.RPCClientInvokeMonitor;

import java.sql.SQLException;

/**
 * 连接级追踪，用于追踪用于Statement的操作追踪
 *
 * @author wusheng
 */
public class StatementTracing {
    private static RPCClientInvokeMonitor rpcClientInvokeMonitor = new RPCClientInvokeMonitor();

    public static <R> R execute(java.sql.Statement realStatement,
                                String connectInfo, String method, String sql, Executable<R> exec)
            throws SQLException {
        try {
            rpcClientInvokeMonitor.beforeInvoke(Identification
                    .newBuilder()
                    .viewPoint(connectInfo)
                    .businessKey(
                            "statement."
                                    + method
                                    + (sql == null || sql.length() == 0 ? ""
                                    : ":" + sql)).spanType(JDBCBuriedPointType.INSTANCE).build());
            return exec.exe(realStatement, sql);
        } catch (SQLException e) {
            rpcClientInvokeMonitor.occurException(e);
            throw e;
        } finally {
            rpcClientInvokeMonitor.afterInvoke();
        }
    }

    public interface Executable<R> {
        public R exe(java.sql.Statement realStatement, String sql)
                throws SQLException;
    }
}
