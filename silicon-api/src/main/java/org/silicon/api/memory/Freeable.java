package org.silicon.api.memory;

/**
 * Represents a resource with an explicit lifetime managed by the backend.
 * <p>
 * Implementations must track their {@link MemoryState} and release resources in {@link #free()}.
 * Typical implementors include buffers, queues, and other backend handles.
 */
public interface Freeable {

    /**
     * @return true if the resource is still alive
     */
    default boolean isAlive() {
        return state() == MemoryState.ALIVE;
    }

    /**
     * Ensures the resource is alive or throws.
     * @throws IllegalStateException if not alive
     */
    default void ensureAlive() {
        if (state() == MemoryState.ALIVE) return;

        throw new IllegalStateException(getClass().getSimpleName() + " is not ALIVE! State: " + state());
    }

    /**
     * Ensures another resource is alive or throws.
     * @param freeable the other resource to validate
     * @throws IllegalStateException if the other resource is not alive
     */
    default void ensureOther(Freeable freeable) {
        if (freeable.state() == MemoryState.ALIVE) return;

        throw new IllegalStateException("Other " + freeable.getClass().getSimpleName() + " is not ALIVE! State: " + freeable.state());
    }

    /**
     * @return current memory state
     */
    MemoryState state();

    /**
     * Releases the underlying resource.
     */
    void free();
}
