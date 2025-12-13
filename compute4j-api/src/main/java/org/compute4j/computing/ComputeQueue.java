package org.compute4j.computing;

import org.compute4j.kernel.ComputeFunction;

public interface ComputeQueue {
    void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args) throws Throwable;
    void awaitCompletion() throws Throwable;
    boolean isCompleted() throws Throwable;
}
