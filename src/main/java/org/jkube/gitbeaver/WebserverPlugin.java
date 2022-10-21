package org.jkube.gitbeaver;

import org.jkube.gitbeaver.plugin.SimplePlugin;
import org.jkube.gitbeaver.webserver.WebServer;
import org.jkube.gitbeaver.webserver.WebserverEndPointCommand;
import org.jkube.gitbeaver.webserver.WebserverInitCommand;

public class WebserverPlugin extends SimplePlugin {

    public WebserverPlugin() {
        super(
                WebserverInitCommand.class,
                WebserverEndPointCommand.class
        );
    }

}
