package org.jkube.gitbeaver.webserver;

import com.sun.net.httpserver.HttpExchange;
import org.jkube.gitbeaver.WebserverPlugin;
import org.jkube.logging.Log;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class RequestExecutors {

    private static final int NUM_THREADS = 10;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
    public void submit(HttpExchange he,  Function<Map<String, String>, String> method) {
        threadPool.submit(wrap(he, method));
    }

    private Runnable wrap(HttpExchange he, Function<Map<String, String>, String> method) {
        return () -> {
            WebserverPlugin.beginRequestThread();
            String responseMessage;
            int responseCode;
            try {
                responseMessage = method.apply(queryToMap(he.getRequestURI().getQuery()));
                responseCode = 200;
            } catch (RuntimeException e) {
                Log.exception(e);
                responseMessage = "Exception occured: "+e;
                responseCode = 500;
            }
            DynamicHttpHandler.sendResponse(he, responseCode, responseMessage);
            WebserverPlugin.endRequestThread();
        };
    }

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=", 2);
                result.put(entry[0], entry.length == 2 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : "");
            }
        }
        return result;
    }

}
