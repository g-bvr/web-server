package org.jkube.gitbeaver.webserver.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.webserver.WebServer;

import java.util.List;
import java.util.Map;

import static org.jkube.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserveTriggerCommand extends AbstractCommand {

    public WebserveTriggerCommand() {
        super(2, 3, "server", "trigger");
    }

    @Override
    public void execute(Map<String,String> variables, WorkSpace workSpace, List<String> arguments) {
        String endPoint = arguments.get(0);
        String script = arguments.get(1);
        WorkSpace scriptWorkspace = arguments.size() == 2 ? workSpace
                : workSpace.getSubWorkspace(arguments.get(2));
        WebServer.addEndPoint(WebServer.TRIGGER_PREFIX+endPoint, scriptWorkspace, script, variables);
        log("Added trigger "+endPoint+" triggering script "+script+" in workspace "+workSpace.getWorkdir());
    }
}
