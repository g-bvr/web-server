# Repository g-bvr/web-server

This repository defines a plugin that can be used to enhance the built-in set of commands
to  operate a web server to handle http requests and trigger serialized script execution.

## Activation

This plugin can be integrated into the [core docker image](https://hub.docker.com/r/gitbeaver/core/tags)
by executing the following beaver script:

```
GIT CLONE https://github.com/g-bvr web-server main
PLUGIN COMPILE web-server/src/main/java
PLUGIN ENABLE org.jkube.gitbeaver.WebServerPlugin
```

A more convenient way to build a gitbeaver release with multiple
plugins (based on a tabular selection)
is provided by E. Breuninger GmbH & Co. in the public repository
[e-breuninger/git-beaver](https://github.com/e-breuninger/git-beaver).

## Documentation of defined commands

A list of all commands defined by this plugin can be found in this [automatically generated documentation](https://htmlpreview.github.io/?https://raw.githubusercontent.com/g-bvr/web-server/main/doc/WebserverPlugin.html).

## Operation hints

The web server allows two conceptionally different types of interactions:
 * ***End-Point***: handles GET requests concurrently and returns a result after the script execution has terminated. URL arguments are passed as variables to the invoked script.
 * ***Trigger***: accepts only POST requests, the invoked scripts are executed sequentially, url arguments are not supported. The request returns immediately (usually before 
   the invoked script has terminated). If another (or same) trigger script is currently active, the execution is deferred to a FIFO queue. A trigger (identified by its name)
   can be maximally present once in the queue. The reply to the POST request informs if the invoked script was started immediatly, put into the queue or ignored (because 
   already present in the queue)

When the web server is started, it continupously runs a thread for polling the queue and executing triggered scripts. This prevents the JVM running the gitbeaver core executable 
from terminating. When the web server is stopped (and no other plugins run threads), the JVM will be terminated.

## Hints for operating as a cloud run service

When the gitbeaver docker image is deployed as a cloud run service, the execution of triggered scripts extends beyond the handling of requests.  

It is thus recommended to set the option ```run.googleapis.com/cpu-throttling = false```to allocate cpu also when no requests are processed.
In order to not create unnecessary costs, it is also recommended to set ```autoscaling.knative.dev/minScale=0``` (this will scale down the number of instances
to 0 if no requests are received for some time). 

An example setup of a gitbeaver cloud run service is illustrated by [this terraform file](https://raw.githubusercontent.com/e-breuninger/git-beaver-gcp/main/terraform/main.tf) in the public repository
[e-breuninger/git-beaver-gcp](https://github.com/e-breuninger/git-beaver-gcp) (kindly provided by E. Breuninger GmbH & Co.).



