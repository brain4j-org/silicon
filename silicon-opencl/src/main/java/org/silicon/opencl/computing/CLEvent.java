package org.silicon.opencl.computing;

import org.lwjgl.opencl.CL10;
import org.silicon.api.kernel.ComputeEvent;

import java.util.concurrent.CompletableFuture;

public class CLEvent implements ComputeEvent {

    private final CompletableFuture<Void> callback;
    private final long eventPtr;
    
    public CLEvent(long eventPtr) {
        this.callback = new CompletableFuture<>();
        this.eventPtr = eventPtr;
        
        Thread.startVirtualThread(() -> {
            try {
                CL10.clWaitForEvents(eventPtr);
                callback.complete(null);
            } catch (Throwable t) {
                callback.completeExceptionally(t);
            } finally {
                CL10.clReleaseEvent(eventPtr);
            }
        });
    }

    @Override
    public boolean isCompleted() {
        return callback.isDone();
    }

    @Override
    public CompletableFuture<Void> future() {
        return callback;
    }
    
    @Override
    public void await() {
        CL10.clWaitForEvents(eventPtr);
    }

    @Override
    public String toString() {
        return "CLEvent{" +
            "completed=" + isCompleted() +
            ", failed=" + isFailed() +
            '}';
    }
}
