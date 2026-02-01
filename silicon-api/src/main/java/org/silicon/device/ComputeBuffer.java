package org.silicon.device;

import org.silicon.BitUtils;
import org.silicon.computing.ComputeQueue;
import org.silicon.memory.Freeable;

public interface ComputeBuffer extends Freeable {
    ComputeBuffer copy();

    ComputeBuffer copyInto(ComputeBuffer other);

    ComputeBuffer copyAsync(ComputeQueue queue);

    ComputeBuffer copyIntoAsync(ComputeBuffer other, ComputeQueue queue);

    void get(byte[] data);

    void get(double[] data);
    
    void get(float[] data);

    void get(long[] data);

    void get(int[] data);

    void get(short[] data);
    
    default void getHalf(float[] data) {
        short[] tmp = new short[data.length];
        get(tmp);
        BitUtils.half2Float(tmp, data);
    }
}
