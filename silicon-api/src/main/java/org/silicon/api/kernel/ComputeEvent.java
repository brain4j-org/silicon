package org.silicon.api.kernel;

import java.util.concurrent.CompletableFuture;

public interface ComputeEvent {
    default boolean isCompleted() {
        return future().isDone();
    }

    default boolean isFailed() {
        return future().isCompletedExceptionally();
    }

    CompletableFuture<Void> future();

    void await();
}
