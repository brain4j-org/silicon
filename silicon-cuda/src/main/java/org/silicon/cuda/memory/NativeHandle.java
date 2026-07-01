package org.silicon.cuda.memory;

import java.lang.foreign.MemorySegment;

public interface NativeHandle {
    MemorySegment handle();
}
