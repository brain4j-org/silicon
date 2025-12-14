package org.silicon.device;

import org.silicon.computing.ComputeQueue;

public interface ComputeBuffer extends Cloneable {
    ComputeBuffer copy() throws Throwable;
    ComputeBuffer copyInto(ComputeBuffer other) throws Throwable;
    ComputeBuffer copyAsync(ComputeQueue queue) throws Throwable;
    ComputeBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue) throws Throwable;
    void free() throws Throwable;
    void get(byte[] data) throws Throwable;
    void get(double[] data) throws Throwable;
    void get(float[] data) throws Throwable;
    void get(long[] data) throws Throwable;
    void get(int[] data) throws Throwable;
    void get(short[] data) throws Throwable;
}
