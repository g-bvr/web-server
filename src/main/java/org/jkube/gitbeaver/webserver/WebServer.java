package org.jkube.gitbeaver.webserver;

import com.sun.net.httpserver.HttpServer;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.logging.Log;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jkube.gitbeaver.logging.Log.onException;

public class WebServer {

    public static final SimpleDateFormat RUN_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final static WebServer SINGLETON = new WebServer();

    public static final String TRIGGER_PREFIX = "/trigger/";

    private static final int STOP_MAX_SECONDS = 5;
    private final DynamicHttpHandler httpHandler = new DynamicHttpHandler();

    private HttpServer server;

    public static void init(int port) {
        SINGLETON.startListening(port);
    }

    public static void addEndPoint(String endPoint, WorkSpace workspace, String script, Map<String, String> variables) {
        // clone the variables twice, first to decouple from input, second to decouple separate runs
        Map<String, String> clonedVariables = clone(variables);
        SINGLETON.httpHandler.addEndpoint(endPoint, urlparams ->
                invokeScript(workspace, script, endPoint, cloneAndCombine(clonedVariables, urlparams)));
    }

    public static void shutdown() {
        SINGLETON.stopListening();
    }

    private void startListening(int port) {
        server = onException(() -> HttpServer.create(new InetSocketAddress(port), 0))
                .fail("Could not create http server");
        server.createContext("/", httpHandler);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private void stopListening() {
        server.stop(STOP_MAX_SECONDS);
        httpHandler.drainQueue();
    }

    private static String invokeScript(WorkSpace workspace, String script, String endPoint, Map<String, String> variables) {
        String runId = createRunId(endPoint);
        GitBeaver.applicationLogHandler().createRun(runId);
        Log.log("Triggering run "+runId+" of script "+script);
        variables.put(GitBeaver.RUN_ID_VARIABLE, runId);
        return GitBeaver.scriptExecutor().execute(script, null, variables, workspace);
    }

    private static Map<String, String> clone(Map<String, String> variables) {
        return new LinkedHashMap<>(variables);
    }

    private static Map<String, String> cloneAndCombine(Map<String, String> variables, Map<String, String> urlparams) {
        Map<String, String> result = clone(variables);
        result.putAll(urlparams);
        return result;
    }

    private static String createRunId(String endpoint) {
        return endpoint.substring(TRIGGER_PREFIX.length())+"-"+RUN_TIME_FORMAT.format(new Date());
    }

}
