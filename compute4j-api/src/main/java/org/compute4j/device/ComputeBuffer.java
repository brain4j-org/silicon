package org.compute4j.device;

public interface ComputeBuffer {
    void get(byte[] data);
    void get(double[] data);
    void get(float[] data);
    void get(long[] data);
    void get(int[] data);
    void get(short[] data);
}
