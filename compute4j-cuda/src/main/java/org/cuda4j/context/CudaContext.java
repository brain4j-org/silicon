package org.cuda4j.context;

import org.compute4j.device.ComputeContext;
import org.cuda4j.CudaObject;
import org.cuda4j.device.CudaModule;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

public record CudaContext(MemorySegment handle) implements CudaObject, ComputeContext {
    
    public static final MethodHandle CUDA_DESTROY_CONTEXT = LINKER.downcallHandle(
        LOOKUP.find("cuda_destroy_context").orElse(null),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_SYNC_CONTEXT = LINKER.downcallHandle(
        LOOKUP.find("cuda_sync_context").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT)
    );
    public static final MethodHandle CUDA_CONTEXT_SET_CURRENT = LINKER.downcallHandle(
        LOOKUP.find("cuda_context_set_current").orElse(null),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_STREAM_CREATE = LINKER.downcallHandle(
        LOOKUP.find("cuda_stream_create").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MODULE_LOAD = LINKER.downcallHandle(
        LOOKUP.find("cuda_module_load").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MODULE_LOAD_DATA = LINKER.downcallHandle(
        LOOKUP.find("cuda_module_load_data").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    
    public CudaContext setCurrent() throws Throwable {
        int res = (int) CUDA_CONTEXT_SET_CURRENT.invoke(handle);
        
        if (res != 0) {
            throw new RuntimeException("cuCtxSetCurrent failed: " + res);
        }
        
        return this;
    }
    
    public CudaContext synchronize() throws Throwable {
        int res = (int) CUDA_SYNC_CONTEXT.invoke();
        
        if (res != 0) {
            throw new RuntimeException("cuCtxSynchronize failed, error " + res);
        }
        
        return this;
    }
    
    @Override
    public CudaStream createQueue() throws Throwable {
        MemorySegment ptr = (MemorySegment) CUDA_STREAM_CREATE.invoke();
        
        if (ptr == null || ptr.address() == 0) {
            throw new RuntimeException("Failed to create CUDA stream");
        }
        
        return new CudaStream(ptr);
    }
    
    @Override
    public CudaModule loadModule(Path path) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cPath = arena.allocateFrom(path.toString());
            MemorySegment moduleHandle = (MemorySegment) CUDA_MODULE_LOAD.invoke(cPath);
            
            if (moduleHandle == null || moduleHandle.address() == 0) {
                throw new RuntimeException("cuModuleLoad failed for: " + path);
            }
            
            return new CudaModule(moduleHandle);
        }
    }
    
    @Override
    public CudaModule loadModule(byte[] rawSrc) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment data = arena.allocateFrom(ValueLayout.JAVA_BYTE, rawSrc);
            MemorySegment moduleHandle = (MemorySegment) CUDA_MODULE_LOAD_DATA.invoke(data);
            
            if (moduleHandle == null || moduleHandle.address() == 0) {
                throw new RuntimeException("cuModuleLoadData failed");
            }
            
            return new CudaModule(moduleHandle);
        }
    }
    
    @Override
    public CudaModule loadModule(String source) {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public void release() throws Throwable {
        CUDA_DESTROY_CONTEXT.invoke(handle);
        CudaObject.super.release();
    }
}
