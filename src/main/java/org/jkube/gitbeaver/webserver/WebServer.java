package org.jkube.gitbeaver.webserver;

import com.sun.net.httpserver.HttpServer;
import org.jkube.gitbeaver.GitBeaver;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.logging.Log;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jkube.logging.Log.onException;

public class WebServer {

    public static final SimpleDateFormat RUN_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final static WebServer SINGLETON = new WebServer();
    public static final String TRIGGER = "/trigger/";
    private static final int STOP_MAX_SECONDS = 5;
    private final DynamicHttpHandler httpHandler = new DynamicHttpHandler();

    private HttpServer server;

    public static void init(int port) {
        SINGLETON.startListening(port);
    }

    public static void addEndPoint(String endPoint, WorkSpace workspace, String script, Map<String, String> variables) {
        // clone the variables twice, first to decouple from input
        SINGLETON.httpHandler.addEndpoint(endPoint, () -> triggerScript(workspace, script, clone(variables)));
    }

    public static void shutdown() {
        SINGLETON.stopListening();
    }

    private void startListening(int port) {
        server = onException(() -> HttpServer.create(new InetSocketAddress(port), 0))
                .fail("Could not create http server");
        server.createContext(TRIGGER, httpHandler);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private void stopListening() {
        server.stop(STOP_MAX_SECONDS);
        httpHandler.drainQueue();
    }

    private static void triggerScript(WorkSpace workspace, String script, Map<String, String> variables) {
        String runId = createRunId(script);
        GitBeaver.applicationLogHandler().createRun(runId);
        Log.log("Triggering run "+runId+" of script "+script);
        // clone the variables twice, second to decouple from other runs
        GitBeaver.scriptExecutor().execute(script, null, clone(variables), workspace);
    }

    private static Map<String, String> clone(Map<String, String> variables) {
        return new LinkedHashMap<>(variables);
    }

    private static String createRunId(String script) {
        return script+"-"+RUN_TIME_FORMAT.format(new Date());
    }

}
