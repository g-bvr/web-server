package org.jkube.gitbeaver.webserver.commands;

import org.jkube.gitbeaver.AbstractCommand;
import org.jkube.gitbeaver.WorkSpace;
import org.jkube.gitbeaver.webserver.WebServer;

import java.util.Map;

import static org.jkube.gitbeaver.logging.Log.log;

/**
 * Usage: resolve source target
 */
public class WebserverEndPointCommand extends AbstractCommand {

    private static final String PATH = "path";
    private static final String SCRIPT = "script";
    private static final String FOLDER = "folder";

    public WebserverEndPointCommand() {
        super("Define an endpoint (endpoints are executed concurrently, the request returns after processing is done, endpoints can have url parameters that are passed as variables to the script)");
        commandlineVariant("SERVER ENDPOINT "+PATH+" "+SCRIPT, "execute script in current workspace");
        commandlineVariant("SERVER ENDPOINT "+PATH+" "+SCRIPT+" IN "+FOLDER, "execute script in sub-workspace specified by given folder");
        argument(PATH, "The url path that shall be mapped to this endpoint (starts with an / character)");
        argument(SCRIPT, "The path to the script (relative to current workspace, not the execution workspace) that gets executed when endpoint receives a GET request");
        argument(FOLDER, "The path to a folder (relative to current workspace) that serves as execution workspace (the executed script can be located outside this folder)");
    }

    @Override
    public void execute(Map<String,String> variables, WorkSpace workSpace, Map<String, String> arguments) {
        String endPoint = arguments.get(PATH);
        String script = arguments.get(SCRIPT);
        WorkSpace scriptWorkspace = arguments.containsKey(FOLDER) ? workSpace.getSubWorkspace(arguments.get(FOLDER)) : workSpace;
        WebServer.addEndPoint("/"+endPoint, scriptWorkspace, script, variables);
        log("Added endpoint "+endPoint+" calling script "+script+" in workspace "+workSpace);
    }
}
