package org.jkube.gitbeaver;

import org.jkube.application.Application;
import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.webserver.commands.WebserverEndPointCommand;
import org.jkube.gitbeaver.webserver.commands.WebserverInitCommand;
import org.jkube.gitbeaver.webserver.commands.WebserverShutdownCommand;
import org.jkube.gitbeaver.webserver.commands.WebserverTriggerCommand;
import org.jkube.logging.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WebserverPlugin extends SimplePlugin {

    public WebserverPlugin() {
        super("""
                        operates a web server to handle http requests and trigger serialized script execution
                        """,
                WebserverInitCommand.class,
                WebserverEndPointCommand.class,
                WebserverTriggerCommand.class,
                WebserverShutdownCommand.class
        );
    }

}
