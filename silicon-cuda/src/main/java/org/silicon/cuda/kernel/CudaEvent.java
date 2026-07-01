package org.silicon.cuda.kernel;

import org.silicon.api.SiliconException;
import org.silicon.api.kernel.ComputeEvent;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.silicon.cuda.Bindings.*;

public class CudaEvent implements ComputeEvent {

    private final AtomicBoolean destroyed = new AtomicBoolean(false);
    private final CompletableFuture<Void> future;
    private final MemorySegment event;

    public CudaEvent(CudaStream stream) {
        this.future = new CompletableFuture<>();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment eventPtr = arena.allocate(ValueLayout.ADDRESS);
            int res = (int) CU_EVENT_CREATE.invokeExact(eventPtr, Flags.CU_EVENT_DISABLE_TIMING.value());

            if (res != 0) {
                throw new SiliconException("cuEventCreate failed: " + res);
            }

            this.event = eventPtr.get(ValueLayout.ADDRESS, 0);

            res = (int) CU_EVENT_RECORD.invokeExact(this.event, stream.handle());
            if (res != 0) {
                throw new SiliconException("cuEventRecord failed: " + res);
            }

            Thread.startVirtualThread(this::waitAndComplete);
        } catch (Throwable t) {
            throw new SiliconException("Failed to create CudaEvent", t);
        }
    }

    private void waitAndComplete() {
        try {
            CU_EVENT_SYNCHRONIZE.invokeExact(event);
            future.complete(null);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        } finally {
            tryDestroy();
        }
    }

    @Override
    public CompletableFuture<Void> future() {
        return future;
    }

    @Override
    public void await() {
        try {
            int result = (int) CU_EVENT_SYNCHRONIZE.invokeExact(event);
            if (result != 0) throw new SiliconException("cuEventSynchronize failed: " + result);
        } catch (Throwable t) {
            throw new SiliconException("await() failed", t);
        }
    }

    private void tryDestroy() {
        if (destroyed.compareAndSet(false, true)) {
            try {
                int result = (int) CU_EVENT_DESTROY.invokeExact(event);
                if (result != 0) throw new SiliconException("cuEventDestroy failed: " + result);
            } catch (Throwable t) {
                throw new SiliconException("destroy() failed", t);
            }
        }
    }

    public enum Flags {

        CU_EVENT_DEFAULT(0x0),
        CU_EVENT_BLOCKING_SYNC(0x1),
        CU_EVENT_DISABLE_TIMING(0x2),
        CU_EVENT_INTERPROCESS(0x4);

        private final int value;

        Flags(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    @Override
    public String toString() {
        return "CudaEvent{" +
            "completed=" + isCompleted() +
            ", failed=" + isFailed() +
            '}';
    }
}
