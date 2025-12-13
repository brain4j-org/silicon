package org.compute4j.device;

public interface ComputeBuffer extends Cloneable {
    ComputeBuffer copy() throws Throwable;
    void free() throws Throwable;
    void get(byte[] data) throws Throwable;
    void get(double[] data) throws Throwable;
    void get(float[] data) throws Throwable;
    void get(long[] data) throws Throwable;
    void get(int[] data) throws Throwable;
    void get(short[] data) throws Throwable;
}
