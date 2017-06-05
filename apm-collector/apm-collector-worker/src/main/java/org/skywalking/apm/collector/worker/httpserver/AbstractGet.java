package org.skywalking.apm.collector.worker.httpserver;

import com.google.gson.JsonObject;
import org.skywalking.apm.collector.actor.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author pengys5
 */
public abstract class AbstractGet extends AbstractLocalSyncWorker {

    protected AbstractGet(Role role, ClusterWorkerContext clusterContext, LocalWorkerContext selfContext) {
        super(role, clusterContext, selfContext);
    }

    @Override
    final public void onWork(Object request, Object response) throws Exception {
        Map<String, String[]> parameterMap = (Map<String, String[]>) request;
        try {
            onSearch(parameterMap, (JsonObject) response);
        } catch (Exception e) {
            ((JsonObject) response).addProperty("isSuccess", false);
            ((JsonObject) response).addProperty("reason", e.getMessage());
        }
    }

    protected abstract void onSearch(Map<String, String[]> request, JsonObject response) throws Exception;

    static class GetWithHttpServlet extends AbstractHttpServlet {

        private final LocalSyncWorkerRef ownerWorkerRef;

        GetWithHttpServlet(LocalSyncWorkerRef ownerWorkerRef) {
            this.ownerWorkerRef = ownerWorkerRef;
        }

        @Override
        final protected void doGet(HttpServletRequest request,
                                   HttpServletResponse response) throws ServletException, IOException {
            Map<String, String[]> parameterMap = request.getParameterMap();

            JsonObject resJson = new JsonObject();
            try {
                ownerWorkerRef.ask(parameterMap, resJson);
                reply(response, resJson, HttpServletResponse.SC_OK);
            } catch (Exception e) {
                resJson.addProperty("error", e.getMessage());
                reply(response, resJson, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
