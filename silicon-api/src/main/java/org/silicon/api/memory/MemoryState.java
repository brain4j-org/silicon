package org.silicon.api.memory;

/**
 * Lifecycle state for {@link Freeable} resources.
 * <p>
 * {@link #ALIVE} indicates valid usage, {@link #FREE} indicates released.
 */
public enum MemoryState {
    ALIVE,
    FREE
}
