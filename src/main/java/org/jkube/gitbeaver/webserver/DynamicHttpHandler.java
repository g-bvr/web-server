package org.jkube.gitbeaver.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jkube.gitbeaver.WebserverPlugin;
import org.jkube.logging.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.jkube.logging.Log.onException;

public class DynamicHttpHandler implements HttpHandler {

    public static final String POST = "POST";
    public static final String GET = "GET";
    private final Map<String,  Function<Map<String, String>, String>> endPoints = new ConcurrentHashMap<>();
    private final TriggerQueue queue = new TriggerQueue();

    private final RequestExecutors executors = new RequestExecutors();
    public void addEndpoint(String endPoint, Function<Map<String, String>, String> endpointMethod) {
        if (endPoints.put(endPoint, endpointMethod) != null) {
            Log.warn("Existing endpoint "+endPoint+" was overwritten");
        }
    }

    @Override
    public void handle(HttpExchange he) {
        WebserverPlugin.beginRequestThread();
        try {
            tryHandle(he);
        } catch (Throwable e) {
            Log.exception(e);
        }
        WebserverPlugin.endRequestThread();
    }

    public void tryHandle(HttpExchange he) {
        String endpoint = he.getRequestURI().getPath();
        Function<Map<String, String>, String> method = endPoints.get(endpoint);
        String responseMessage = "error occurred";
        int responseCode = 500;
        boolean sendResponse = true;
        if (method == null) {
            responseMessage = "No such endpoint: "+endpoint;
            responseCode = 400;
        } else if (isTrigger(endpoint)) {
            if (!he.getRequestMethod().equals(POST)) {
                responseMessage = "trigger request must use POST method";
                responseCode = 405;
            } else {
                responseMessage = queue.enqueue(endpoint, () -> method.apply(Map.of()));
                responseCode = 200;
            }
        } else {
            if (!he.getRequestMethod().equals(GET)) {
                responseMessage = "end point request must use GET method";
                responseCode = 405;
            } else {
                executors.submit(he, method);
                sendResponse = false;
            }
        }
        if (sendResponse) {
            sendResponse(he, responseCode, responseMessage);
        }
    }

    public static void sendResponse(HttpExchange he, int responseCode, String responseMessage) {
        onException(() -> trySendResponse(he, responseCode, responseMessage == null ? "no message" : responseMessage))
                .warn("could not send http response");
    }
    private static void trySendResponse(HttpExchange he, int responseCode, String responseMessage) throws IOException {
        he.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        responseMessage = responseMessage.replaceAll("\\n", "\n");
        he.sendResponseHeaders(responseCode, responseMessage.length());
        try(OutputStream os = he.getResponseBody()) {
            os.write(responseMessage.getBytes());
        }
    }

    private boolean isTrigger(String path) {
        Log.log("request for path received: "+path);
        return path.startsWith(WebServer.TRIGGER_PREFIX);
    }

    public void drainQueue() {
        queue.drain();
    }
}
