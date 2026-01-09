package org.silicon.opencl;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;
import org.silicon.backend.BackendType;
import org.silicon.backend.ComputeBackend;
import org.silicon.device.ComputeDevice;
import org.silicon.opencl.device.CLDevice;

import java.nio.IntBuffer;

public class OpenCL implements ComputeBackend {
    
    private int getPlatformCount() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            
            int result = CL10.clGetPlatformIDs(null, buffer);
            if (result != CL10.CL_SUCCESS) throw new RuntimeException("clGetPlatformIDs failed: " + result);
            
            return buffer.get(0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private long getPlatform(int platformCount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer platforms = stack.mallocPointer(platformCount);
            
            int result = CL10.clGetPlatformIDs(platforms, (IntBuffer) null);
            if (result != CL10.CL_SUCCESS) throw new RuntimeException("clGetPlatformIDs failed: " + result);
            
            return platforms.get(0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public int getDeviceCount() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int platformCount = getPlatformCount();
            long platform = getPlatform(platformCount);
            
            IntBuffer numDevices = stack.mallocInt(1);
            
            int result = CL10.clGetDeviceIDs(platform, CL10.CL_DEVICE_TYPE_GPU, null, numDevices);
            if (result != CL10.CL_SUCCESS) throw new RuntimeException("clGetDeviceIDs failed: " + result);
            
            return numDevices.get(0);
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public boolean isAvailable() {
        return getPlatformCount() > 0;
    }
    
    @Override
    public BackendType getType() {
        return BackendType.OPENCL;
    }
    
    @Override
    public ComputeDevice createSystemDevice(int index) {
        int deviceCount = getDeviceCount();
        
        if (index < 0) throw new IllegalArgumentException("Device index must be greater than 0!");
        if (index >= deviceCount) {
            throw new IllegalArgumentException("Specified device index (" + index + ") but only " + deviceCount + " devices available!");
        }
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int platformCount = getPlatformCount();
            long platform = getPlatform(platformCount);
            
            PointerBuffer devices = stack.mallocPointer(deviceCount);
            
            int result = CL10.clGetDeviceIDs(platform, CL10.CL_DEVICE_TYPE_GPU, devices, (IntBuffer)null);
            if (result != CL10.CL_SUCCESS) throw new RuntimeException("clGetDeviceIDs failed: " + result);
            
            long device = devices.get(index);
            return new CLDevice(device, platform);
        }
    }
}
