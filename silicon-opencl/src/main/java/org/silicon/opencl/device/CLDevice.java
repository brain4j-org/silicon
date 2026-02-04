package org.silicon.opencl.device;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.api.SiliconException;
import org.silicon.api.device.ComputeContext;
import org.silicon.api.device.ComputeDevice;
import org.silicon.api.device.DeviceFeature;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;

public record CLDevice(long handle, long platform) implements ComputeDevice {

    @Override
    public ComputeContext createContext() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer properties = stack.mallocPointer(3);
            IntBuffer result = stack.mallocInt(1);

            properties.put(CL10.CL_CONTEXT_PLATFORM).put(platform).put(0);
            properties.flip();

            long context = CL10.clCreateContext(properties, handle, null, 0, result);
            if (result.get(0) != CL10.CL_SUCCESS) throw new SiliconException("clCreateContext failed: " + result.get(0));

            return new CLContext(context, handle);
        }
    }

    private String getString(MemoryStack stack, int paramName) {
        PointerBuffer sizeBuf = stack.mallocPointer(1);
        CL10.clGetDeviceInfo(handle, paramName, (ByteBuffer) null, sizeBuf);

        long size = sizeBuf.get(0);

        ByteBuffer nameBuffer = stack.malloc((int) size);
        CL10.clGetDeviceInfo(handle, paramName, nameBuffer, null);

        return StandardCharsets.UTF_8.decode(nameBuffer).toString().trim();
    }

    @Override
    public String name() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return getString(stack, CL10.CL_DEVICE_NAME);
        }
    }

    @Override
    public String vendor() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return getString(stack, CL10.CL_DEVICE_VENDOR);
        }
    }

    @Override
    public long memorySize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer sizeBuf = stack.mallocLong(1);
            CL10.clGetDeviceInfo(handle, CL10.CL_DEVICE_GLOBAL_MEM_SIZE, sizeBuf, null);

            return sizeBuf.get(0);
        }
    }

    @Override
    public boolean supports(DeviceFeature feature) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return switch (feature) {
                case FP16 -> getString(stack, CL10.CL_DEVICE_EXTENSIONS).contains("cl_khr_fp16");
                case FP64 -> getString(stack, CL10.CL_DEVICE_EXTENSIONS).contains("cl_khr_fp64");
            };
        }
    }
}
