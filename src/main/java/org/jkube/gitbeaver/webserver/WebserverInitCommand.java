package org.jkube.gitbeaver.webserver;

import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;

import java.nio.file.Path;
import java.util.List;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserverInitCommand extends SimpleCommand {

    public WebserverInitCommand() {
        super(1, "server", "listen", "port");
    }

    @Override
    public void execute(WorkSpace workSpace, List<String> arguments) {
        int port = Integer.parseInt(arguments.get(0));
        WebServer.init(port);
        log("Started webserver listening on port "+port);
    }
}
