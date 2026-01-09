package org.silicon.computing;

import org.silicon.kernel.ComputeFunction;

public interface ComputeQueue {
    void dispatch(ComputeFunction function, ComputeSize globalSize, ComputeSize groupSize, ComputeArgs args);
    void awaitCompletion();
    void release();
}
