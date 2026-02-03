package org.silicon.api.kernel;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an asynchronous kernel dispatch completion handle.
 * <p>
 * Wraps a {@link CompletableFuture} for status checks and synchronization.
 */
public interface ComputeEvent {
    /**
     * @return true if the underlying future is completed
     */
    default boolean isCompleted() {
        return future().isDone();
    }

    /**
     * @return true if the underlying future completed exceptionally
     */
    default boolean isFailed() {
        return future().isCompletedExceptionally();
    }

    /**
     * @return future backing this event
     */
    CompletableFuture<Void> future();

    /**
     * Waits for the dispatch to complete.
     * <p>
     * This method uses native function calls to freeze the current thread.
     */
    void await();
}
