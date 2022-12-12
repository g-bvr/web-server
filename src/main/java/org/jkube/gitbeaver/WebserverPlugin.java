package org.jkube.gitbeaver;

import org.jkube.application.Application;
import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.webserver.commands.WebserverEndPointCommand;
import org.jkube.gitbeaver.webserver.commands.WebserverInitCommand;
import org.jkube.gitbeaver.webserver.commands.WebserverShutdownCommand;
import org.jkube.logging.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebserverPlugin extends SimplePlugin {

    public WebserverPlugin() {
        super(
                WebserverInitCommand.class,
                WebserverEndPointCommand.class,
                WebserverShutdownCommand.class
        );
    }

    private static final Set<Thread> requestThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void init() {
        Application.setFailureHandler((message, code) -> {
            Log.exception(new RuntimeException("Failure captured"));
            Log.log("Failure in WebServer failure handler: "+message+" threads: "+requestThreads);
            if (requestThreads.contains(Thread.currentThread())) {
                Log.error("Failure in handling http request {}", message);
            } else {
                Log.error("Failure in main: {}", message);
                Log.error("Terminating VM with error code: {}", code);
                System.exit(code);
            }
        });
        Log.log("Webserver installed request failure handler.");
    }

    public static void beginRequestThread() {
        requestThreads.add(Thread.currentThread());
    }

    public static void endRequestThread() {
        requestThreads.remove(Thread.currentThread());
    }

}
