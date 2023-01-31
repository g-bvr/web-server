package org.jkube.gitbeaver.webserver.commands;

import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.webserver.WebServer;

import java.util.Map;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserverInitCommand extends SimpleCommand {

    private static final String PORT = "port";

    public WebserverInitCommand() {
        super("SERVER LISTEN PORT ", "Start web server, listening to specified port");
        argument(PORT, "the port to which the server will be listening");
    }

    @Override
    public void execute(WorkSpace workSpace, Map<String, String> arguments) {
        int port = Integer.parseInt(arguments.get(PORT));
        WebServer.init(port);
        log("Started webserver listening on port "+port);
    }
}
