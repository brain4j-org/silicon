package org.silicon.api.device;

import org.silicon.api.BitUtils;
import org.silicon.api.kernel.ComputeQueue;
import org.silicon.api.memory.Freeable;

public interface ComputeBuffer extends Freeable {
    ComputeBuffer copy();

    ComputeBuffer copyInto(ComputeBuffer other);

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
