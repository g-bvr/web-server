package org.jkube.gitbeaver.webserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jkube.gitbeaver.WebserverPlugin;
import org.jkube.logging.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jkube.logging.Log.onException;

public class DynamicHttpHandler implements HttpHandler {

    public static final String POST = "POST";
    private final Map<String, Runnable> endPoints = new ConcurrentHashMap<>();

    private final RequestQueue queue = new RequestQueue();
    public void addEndpoint(String endPoint, Runnable trigger) {
        if (endPoints.put(endPoint, trigger) != null) {
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
        String endpoint = extractEndpointName(he.getRequestURI().getPath());
        Runnable trigger = endPoints.get(endpoint);
        String responseMessage;
        int responseCode;
        if (trigger == null) {
            responseMessage = "No such endpoint: "+endpoint;
            responseCode = 400;
        } else if (!he.getRequestMethod().equals(POST)) {
            responseMessage = "trigger request must use POST method";
            responseCode = 405;
        } else {
            responseMessage = queue.enqueue(endpoint, trigger);
            responseCode = 200;
        }
        onException(() -> sendResponse(he, responseCode, responseMessage)).warn("could not send http response");
    }


    private void sendResponse(HttpExchange he, int responseCode, String responseMessage) throws IOException {
        he.sendResponseHeaders(responseCode, responseMessage.length());
        try(OutputStream os = he.getResponseBody()) {
            os.write(responseMessage.getBytes());
        }
    }

    private String extractEndpointName(String path) {
        if (!path.startsWith(WebServer.TRIGGER)) {
            Log.warn("Illegal path received: "+path);
            return null;
        }
        return path.substring(WebServer.TRIGGER.length());
    }

    public void drainQueue() {
        queue.drain();
    }
}
