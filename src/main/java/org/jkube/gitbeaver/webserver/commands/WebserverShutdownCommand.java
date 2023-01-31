package org.jkube.gitbeaver.webserver.commands;

import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.webserver.WebServer;

import java.util.Map;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserverShutdownCommand extends SimpleCommand {

    public WebserverShutdownCommand() {
        super("SERVER SHUTDOWN", "Shutdown webserver (no requests/triggers are accepted any longer, server shuts down after all scheduled trigger runs have terminated)");
    }

    @Override
    public void execute(WorkSpace workSpace, Map<String, String> arguments) {
        log("Shutting down webserver");
        WebServer.shutdown();
    }
}
