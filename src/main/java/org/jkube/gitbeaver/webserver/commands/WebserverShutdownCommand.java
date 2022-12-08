package org.jkube.gitbeaver.webserver.commands;

import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.webserver.WebServer;

import java.util.List;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserverShutdownCommand extends SimpleCommand {

    public WebserverShutdownCommand() {
        super(0, "server", "shutdown");
    }

    @Override
    public void execute(WorkSpace workSpace, List<String> arguments) {
        log("Shutting down webserver");
        WebServer.shutdown();
    }
}
