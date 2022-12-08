package org.jkube.gitbeaver.webserver;

import org.jkube.gitbeaver.WebserverPlugin;
import org.jkube.logging.Log;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jkube.logging.Log.log;
import static org.jkube.logging.Log.onException;

public class TriggerQueue {

    private static final long TERMINATION_TIMEOUT_SECONDS = 60;
    private String currentlyExecuted;
    private final Map<String, Runnable> queued = new LinkedHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public synchronized String enqueue(String endpoint, Runnable trigger) {
        if (currentlyExecuted == null) {
            currentlyExecuted = endpoint;
            execute(trigger);
            return "Triggered execution of script endpoint "+endpoint;
        }
        if (queued.containsKey(endpoint)) {
            return "Script for endpoint "+endpoint+" is already enqueued, skipping";
        } else {
            queued.put(endpoint, trigger);
            return "Currently running: "+currentlyExecuted+", script for endpoint "+endpoint+" was enqueued";
        }
    }
    private void execute(Runnable trigger) {
        executor.execute(() -> runAndTriggerNext(trigger));
    }

    private void runAndTriggerNext(Runnable trigger) {
        WebserverPlugin.beginRequestThread();
        onException(trigger::run).warn("Exception executing triggered script "+currentlyExecuted);
        triggerNext();
        WebserverPlugin.endRequestThread();
    }

    private synchronized void triggerNext() {
        Optional<Map.Entry<String, Runnable>> next = queued.entrySet().stream().findFirst();
        if (next.isPresent()) {
            currentlyExecuted = next.get().getKey();
            queued.remove(currentlyExecuted);
            log("{} in queue, triggering next: {}", queued.size(), currentlyExecuted);
            executor.execute(() -> runAndTriggerNext(next.get().getValue()));
        } else {
            currentlyExecuted = null;
            log("No more queued scripts");
        }
    }

    public void drain() {
        Log.log("Shutting down executor");
        executor.shutdown();
    }
}
