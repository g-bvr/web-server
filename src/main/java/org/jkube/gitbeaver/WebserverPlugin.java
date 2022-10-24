package org.jkube.gitbeaver;

import org.jkube.application.Application;
import org.jkube.application.FailureHandler;
import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.webserver.WebServer;
import org.jkube.gitbeaver.webserver.WebserverEndPointCommand;
import org.jkube.gitbeaver.webserver.WebserverInitCommand;
import org.jkube.logging.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class WebserverPlugin extends SimplePlugin {

    public WebserverPlugin() {
        super(
                WebserverInitCommand.class,
                WebserverEndPointCommand.class
        );
    }

    private static final Set<Thread> requestThreads = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void init() {
        Application.setFailureHandler((message, code) -> {
            if (requestThreads.contains(Thread.currentThread())) {
                Log.error("Failure in handling http request {}", message);
            } else {
                Log.error("Failure in main: {}", message);
                Log.error("Terminating VM with error code: {}", code);
                System.exit(code);
            }
        });
    }

    public static void startRequest() {
        requestThreads.add(Thread.currentThread());
    }

    public static void endRequest() {
        requestThreads.remove(Thread.currentThread());
    }

}
