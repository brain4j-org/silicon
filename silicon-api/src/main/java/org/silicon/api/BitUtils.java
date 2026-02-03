package org.silicon.api;

/**
 * Utilities for converting between FP16 (half) and FP32 (float).
 * <p>
 * These helpers perform bit-level conversions and array-wide transforms.
 */
public class BitUtils {
    /**
     * Converts a half-precision value to float.
     * @param h half-precision bits
     * @return float value
     */
    public static float half2Float(short h) {
        int bits = h & 0xFFFF;
        
        int sign = (bits & 0x8000) << 16;
        int exp  = (bits >>> 10) & 0x1F;
        int mant = bits & 0x03FF;
        
        if (exp == 0) {
            if (mant == 0) {
                // zero
                return Float.intBitsToFloat(sign);
            }
            // subnormal
            while ((mant & 0x0400) == 0) {
                mant <<= 1;
                exp--;
            }
            exp++;
            mant &= ~0x0400;
        } else if (exp == 31) {
            // Inf / NaN
            return Float.intBitsToFloat(sign | 0x7F800000 | (mant << 13));
        }
        
        exp = exp - 15 + 127;
        mant <<= 13;
        
        return Float.intBitsToFloat(sign | (exp << 23) | mant);
    }
    
    /**
     * Converts a float to half-precision bits.
     * @param f float value
     * @return half-precision bits
     */
    public static short float2Half(float f) {
        int bits = Float.floatToIntBits(f);
        
        int sign = (bits >>> 16) & 0x8000;
        int exp  = ((bits >>> 23) & 0xFF) - 127 + 15;
        int mant = bits & 0x7FFFFF;
        
        if (exp <= 0) {
            if (exp < -10) return (short) sign;
            mant = (mant | 0x800000) >> (1 - exp);
            return (short) (sign | (mant >> 13));
        }
        
        if (exp >= 31) {
            return (short) (sign | 0x7C00); // Inf
        }
        
        return (short) (sign | (exp << 10) | (mant >> 13));
    }
    
    /**
     * Converts an array of half-precision values to float in-place.
     * @param input half-precision input
     * @param output float output (same length as input)
     */
    public static void half2Float(short[] input, float[] output) {
        for (int i = 0; i < input.length; i++) {
            output[i] = BitUtils.half2Float(input[i]);
        }
    }
    
    /**
     * Converts an array of half-precision values to a new float array.
     * @param values half-precision input
     * @return new float array
     */
    public static float[] half2Float(short[] values) {
        float[] result = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = BitUtils.half2Float(values[i]);
        }
        return result;
    }
    
    /**
     * Converts an array of float values to half-precision bits in-place.
     * @param input float input
     * @param output half-precision output (same length as input)
     */
    public static void float2Half(float[] input, short[] output) {
        for (int i = 0; i < input.length; i++) {
            output[i] = BitUtils.float2Half(input[i]);
        }
    }
    
    /**
     * Converts an array of float values to a new half-precision array.
     * @param values float input
     * @return new half-precision array
     */
    public static short[] float2Half(float[] values) {
        short[] result = new short[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = BitUtils.float2Half(values[i]);
        }
        return result;
    }
}
