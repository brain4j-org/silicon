package org.compute4j;

public interface ComputeDevice {
    ComputeContext createContext();
    ComputeBuffer allocateBytes(long size);

    // allocation stream aware
    ComputeBuffer allocateArray(double[] data, long size, ComputeStream stream);
    ComputeBuffer allocateArray(float[] data, long size, ComputeStream stream);
    ComputeBuffer allocateArray(long[] data, long size, ComputeStream stream);
    ComputeBuffer allocateArray(int[] data, long size, ComputeStream stream);
    ComputeBuffer allocateArray(short[] data, long size, ComputeStream stream);

    // allocation on default stream
    ComputeBuffer allocateArray(double[] data, long size);
    ComputeBuffer allocateArray(float[] data, long size);
    ComputeBuffer allocateArray(long[] data, long size);
    ComputeBuffer allocateArray(int[] data, long size);
    ComputeBuffer allocateArray(short[] data, long size);
}
