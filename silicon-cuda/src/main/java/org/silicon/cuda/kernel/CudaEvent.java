package org.silicon.cuda.kernel;

import org.silicon.api.SiliconException;
import org.silicon.api.kernel.ComputeEvent;
import org.silicon.cuda.CudaObject;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class CudaEvent implements ComputeEvent {
    
    public static final MethodHandle CUDA_EVENT_CREATE = CudaObject.find(
        "cuda_event_create",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS, // event pointer
            ValueLayout.JAVA_INT // flags
        )
    );
    public static final MethodHandle CUDA_EVENT_RECORD = CudaObject.find(
        "cuda_event_record",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT, // result code
            ValueLayout.ADDRESS, // event pointer
            ValueLayout.ADDRESS // stream pointer
        )
    );
    public static final MethodHandle CUDA_EVENT_QUERY = CudaObject.find(
        "cuda_event_query",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS
        )
    );
    public static final MethodHandle CUDA_EVENT_SYNCHRONIZE = CudaObject.find(
        "cuda_event_synchronize",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS
        )
    );
    public static final MethodHandle CUDA_EVENT_DESTROY = CudaObject.find(
        "cuda_event_destroy",
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS
        )
    );
    
    private final AtomicBoolean destroyed = new AtomicBoolean(false);
    private final CompletableFuture<Void> future;
    private final MemorySegment event;
    
    public CudaEvent(CudaStream stream) {
        this.future = new CompletableFuture<>();
        
        try {
            int flags = Flags.CU_EVENT_DISABLE_TIMING.value();
            this.event = (MemorySegment) CUDA_EVENT_CREATE.invokeExact(flags);
            
            int res = (int) CUDA_EVENT_RECORD.invokeExact(event, stream.handle());
            if (res != 0) {
                throw new RuntimeException("cuEventRecord failed: " + res);
            }
            
            Thread.startVirtualThread(this::waitAndComplete);
        } catch (Throwable t) {
            throw new SiliconException("Failed to create CudaEvent", t);
        }
    }
    
    private void waitAndComplete() {
        try {
            CUDA_EVENT_SYNCHRONIZE.invoke(event);
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
            int result = (int) CUDA_EVENT_SYNCHRONIZE.invokeExact(event);
            if (result != 0) throw new RuntimeException("cuEventSynchronize failed: " + result);
        } catch (Throwable t) {
            throw new SiliconException("await() failed", t);
        }
    }
    
    private void tryDestroy() {
        if (destroyed.compareAndSet(false, true)) {
            try {
                int result = (int) CUDA_EVENT_DESTROY.invokeExact(event);
                if (result != 0) throw new RuntimeException("cuEventDestroy failed: " + result);
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
