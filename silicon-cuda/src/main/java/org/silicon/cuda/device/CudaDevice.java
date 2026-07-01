package org.silicon.cuda.device;

import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeDevice;
import org.silicon.api.device.DeviceFeature;
import org.silicon.cuda.Bindings;
import org.silicon.cuda.CUResult;
import org.silicon.cuda.CudaObject;
import org.silicon.cuda.memory.NativeValueHandle;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import static org.silicon.cuda.Bindings.*;

public record CudaDevice(int handle, int index) implements CudaObject, NativeValueHandle, ComputeDevice {
    
    @Override
    public CudaContext createContext() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment context = arena.allocate(Bindings.CU_CONTEXT);
            int res = (int) CU_CTX_CREATE.invokeExact(context, 0, handle());

            if (res != 0) {
                throw new SiliconException("cuCtxCreate failed: " + CUResult.fromCode(res));
            }

            MemorySegment ctx = context.get(Bindings.CU_CONTEXT, 0);
            return new CudaContext(ctx, this).setCurrent();
        } catch (Throwable e) {
            throw new SiliconException("createContext() failed", e);
        }
    }

    @Override
    public String name() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment name = arena.allocate(256);

            int res = (int) CU_DEVICE_GET_NAME.invokeExact(name, 256, handle);

            if (res != 0) {
                throw new SiliconException("cuDeviceGetName failed: " + CUResult.fromCode(res));
            }

            return name.getUtf8String(0);
        } catch (Throwable e) {
            throw new SiliconException("name() failed", e);
        }
    }

    @Override
    public String vendor() {
        return "NVIDIA";
    }
    
    @Override
    public long memorySize() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment size = arena.allocate(ValueLayout.JAVA_LONG);

            int res = (int) CU_DEVICE_TOTAL_MEM.invokeExact(size, handle);

            if (res != 0) {
                throw new SiliconException("cuDeviceTotalMem failed: " + CUResult.fromCode(res));
            }

            return size.get(ValueLayout.JAVA_LONG, 0);
        } catch (Throwable e) {
            throw new SiliconException("memorySize() failed", e);
        }
    }
    
    @Override
    public boolean supports(DeviceFeature feature) {
        return true; // TODO: implement
//        try {
//            return (boolean) CUDA_DEVICE_SUPPORTS.invokeExact(handle, feature.ordinal());
//        } catch (Throwable e) {
//            throw new SiliconException("supports(" + feature + ") failed", e);
//        }
    }
}
