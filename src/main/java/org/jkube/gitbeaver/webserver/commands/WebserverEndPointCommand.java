package org.jkube.gitbeaver.webserver.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.SimpleCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.webserver.WebServer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserverEndPointCommand extends AbstractCommand {

    public WebserverEndPointCommand() {
        super(2, 3, "server", "endpoint");
    }

    @Override
    public void execute(Map<String,String> variables, WorkSpace workSpace, List<String> arguments) {
        String endPoint = arguments.get(0);
        String script = arguments.get(1);
        WorkSpace scriptWorkspace = arguments.size() == 2 ? workSpace
                : workSpace.getSubWorkspace(arguments.get(2));
        WebServer.addEndPoint(endPoint, scriptWorkspace, script, variables);
        log("Added endpoint "+endPoint+" calling script "+script+" in workspace "+workSpace);
    }
}
