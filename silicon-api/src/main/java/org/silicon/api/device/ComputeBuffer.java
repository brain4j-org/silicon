package org.silicon.api.device;

import org.silicon.api.BitUtils;
import org.silicon.api.memory.Freeable;

/**
 * Device-managed memory buffer.
 * <p>
 * Used to move data between host and device and as kernel arguments. Instances are
 * allocated by a {@link ComputeContext} and must be released via {@link #free()}.
 * The concrete implementation depends on the backend and allocation type.
 */
public interface ComputeBuffer extends Freeable {
    /**
     * Creates a device-side copy of this buffer.
     * @return a new buffer with the same contents
     */
    ComputeBuffer copy();

    /**
     * Copies this buffer into another buffer.
     * Both buffers must be {@link #isAlive() alive}.
     * @param other destination buffer
     * @return the destination buffer (for chaining)
     */
    ComputeBuffer copyInto(ComputeBuffer other);

    /**
     * Reads the buffer contents into a byte array.
     * @param data destination array
     */
    void get(byte[] data);

    /**
     * Reads the buffer contents into a double array.
     * @param data destination array
     */
    void get(double[] data);
    
    /**
     * Reads the buffer contents into a float array.
     * @param data destination array
     */
    void get(float[] data);

    /**
     * Reads the buffer contents into a long array.
     * @param data destination array
     */
    void get(long[] data);

    /**
     * Reads the buffer contents into an int array.
     * @param data destination array
     */
    void get(int[] data);

    /**
     * Reads the buffer contents into a short array.
     * @param data destination array
     */
    void get(short[] data);
    
    /**
     * Reads FP16 (half) data and converts it to float.
     * <p>
     * Internally reads into a short array and converts via {@link BitUtils}.
     * @param data destination float array
     */
    default void getHalf(float[] data) {
        short[] tmp = new short[data.length];
        get(tmp);
        BitUtils.half2Float(tmp, data);
    }
}
