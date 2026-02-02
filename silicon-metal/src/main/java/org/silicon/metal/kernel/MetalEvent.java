package org.silicon.metal.kernel;

import org.silicon.api.kernel.ComputeEvent;

import java.util.concurrent.CompletableFuture;

public class MetalEvent implements ComputeEvent {

    private final CompletableFuture<Void> callback;
    private final MetalCommandBuffer buffer;
    
    public MetalEvent(MetalCommandBuffer buffer) {
        this.callback = new CompletableFuture<>();
        this.buffer = buffer;
        
        Thread.startVirtualThread(() -> {
            try {
                buffer.waitUntilCompleted();
                callback.complete(null);
            } catch (Throwable t) {
                callback.completeExceptionally(t);
            } finally {
                buffer.free();
            }
        });
    }

    @Override
    public CompletableFuture<Void> future() {
        return callback;
    }
    
    @Override
    public void await() {
        buffer.waitUntilCompleted();
    }

    @Override
    public String toString() {
        return "MetalEvent{" +
            "completed=" + isCompleted() +
            ", failed=" + isFailed() +
            '}';
    }
}
