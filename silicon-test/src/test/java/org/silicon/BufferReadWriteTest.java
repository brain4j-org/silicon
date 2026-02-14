package org.silicon;

import org.silicon.api.Silicon;
import org.silicon.api.backend.BackendType;
import org.silicon.api.device.ComputeArena;
import org.silicon.api.device.ComputeBuffer;
import org.silicon.api.device.ComputeContext;
import org.silicon.api.device.ComputeDevice;

import java.util.Arrays;

public class BufferReadWriteTest {

    public static void main(String[] args) {
        int failures = 0;
        int skipped = 0;

        for (BackendType backend : BackendType.values()) {
            try {
                Silicon.chooseBackend(backend);
                ComputeDevice device = Silicon.createDevice();
                ComputeContext context = device.createContext();

                System.out.println("=== Backend " + backend.formalName() + " (" + device.name() + ") ===");
                runAllTypeTests(context);
                System.out.println("OK " + backend.formalName());
            } catch (IllegalStateException e) {
                skipped++;
                System.out.println("SKIP " + backend.formalName() + ": " + e.getMessage());
            } catch (Throwable t) {
                failures++;
                System.err.println("FAIL " + backend.formalName() + ": " + t.getMessage());
                t.printStackTrace(System.err);
            }
        }

        if (failures > 0) {
            throw new AssertionError("BufferReadWriteTest failed. failures=" + failures + ", skipped=" + skipped);
        }

        System.out.println("All buffer write/get tests passed. skipped=" + skipped);
    }

    private static void runAllTypeTests(ComputeContext context) {
        testByte(context);
        testDouble(context);
        testFloat(context);
        testLong(context);
        testInt(context);
        testShort(context);
    }

    private static void testByte(ComputeContext context) {
        byte[] data = new byte[] {0, 1, -1, 42, Byte.MIN_VALUE, Byte.MAX_VALUE};

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer exact = arena.allocateBytes(data.length);
            exact.write(data);
            byte[] out = new byte[data.length];
            exact.get(out);
            assertArrayEquals(data, out, "byte roundtrip exact");

            ComputeBuffer partial = arena.allocateBytes(data.length + 4L);
            partial.write(data);
            byte[] partialOut = new byte[data.length];
            partial.get(partialOut);
            assertArrayEquals(data, partialOut, "byte roundtrip underflow");

            expectIllegalArgument(() -> arena.allocateBytes(data.length - 1L).write(data), "byte write overflow");
            expectIllegalArgument(() -> arena.allocateBytes(data.length - 1L).get(new byte[data.length]), "byte read overflow");
        }
    }

    private static void testDouble(ComputeContext context) {
        double[] data = new double[] {0.0, -0.0, 1.5, -7.25, Double.MIN_VALUE, Double.MAX_VALUE};
        long bytes = (long) data.length * Double.BYTES;

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer exact = arena.allocateBytes(bytes);
            exact.write(data);
            double[] out = new double[data.length];
            exact.get(out);
            assertArrayEquals(data, out, "double roundtrip exact");

            ComputeBuffer partial = arena.allocateBytes(bytes + Double.BYTES);
            partial.write(data);
            double[] partialOut = new double[data.length];
            partial.get(partialOut);
            assertArrayEquals(data, partialOut, "double roundtrip underflow");

            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).write(data), "double write overflow");
            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).get(new double[data.length]), "double read overflow");
        }
    }

    private static void testFloat(ComputeContext context) {
        float[] data = new float[] {0.0f, -0.0f, 1.25f, -13.5f, Float.MIN_VALUE, Float.MAX_VALUE};
        long bytes = (long) data.length * Float.BYTES;

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer exact = arena.allocateBytes(bytes);
            exact.write(data);
            float[] out = new float[data.length];
            exact.get(out);
            assertArrayEquals(data, out, "float roundtrip exact");

            ComputeBuffer partial = arena.allocateBytes(bytes + Float.BYTES);
            partial.write(data);
            float[] partialOut = new float[data.length];
            partial.get(partialOut);
            assertArrayEquals(data, partialOut, "float roundtrip underflow");

            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).write(data), "float write overflow");
            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).get(new float[data.length]), "float read overflow");
        }
    }

    private static void testLong(ComputeContext context) {
        long[] data = new long[] {0L, -1L, 42L, -999_999_999L, Long.MIN_VALUE, Long.MAX_VALUE};
        long bytes = (long) data.length * Long.BYTES;

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer exact = arena.allocateBytes(bytes);
            exact.write(data);
            long[] out = new long[data.length];
            exact.get(out);
            assertArrayEquals(data, out, "long roundtrip exact");

            ComputeBuffer partial = arena.allocateBytes(bytes + Long.BYTES);
            partial.write(data);
            long[] partialOut = new long[data.length];
            partial.get(partialOut);
            assertArrayEquals(data, partialOut, "long roundtrip underflow");

            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).write(data), "long write overflow");
            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).get(new long[data.length]), "long read overflow");
        }
    }

    private static void testInt(ComputeContext context) {
        int[] data = new int[] {0, -1, 42, -1337, Integer.MIN_VALUE, Integer.MAX_VALUE};
        long bytes = (long) data.length * Integer.BYTES;

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer exact = arena.allocateBytes(bytes);
            exact.write(data);
            int[] out = new int[data.length];
            exact.get(out);
            assertArrayEquals(data, out, "int roundtrip exact");

            ComputeBuffer partial = arena.allocateBytes(bytes + Integer.BYTES);
            partial.write(data);
            int[] partialOut = new int[data.length];
            partial.get(partialOut);
            assertArrayEquals(data, partialOut, "int roundtrip underflow");

            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).write(data), "int write overflow");
            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).get(new int[data.length]), "int read overflow");
        }
    }

    private static void testShort(ComputeContext context) {
        short[] data = new short[] {0, -1, 42, -1234, Short.MIN_VALUE, Short.MAX_VALUE};
        long bytes = (long) data.length * Short.BYTES;

        try (ComputeArena arena = context.createArena()) {
            ComputeBuffer exact = arena.allocateBytes(bytes);
            exact.write(data);
            short[] out = new short[data.length];
            exact.get(out);
            assertArrayEquals(data, out, "short roundtrip exact");

            ComputeBuffer partial = arena.allocateBytes(bytes + Short.BYTES);
            partial.write(data);
            short[] partialOut = new short[data.length];
            partial.get(partialOut);
            assertArrayEquals(data, partialOut, "short roundtrip underflow");

            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).write(data), "short write overflow");
            expectIllegalArgument(() -> arena.allocateBytes(bytes - 1L).get(new short[data.length]), "short read overflow");
        }
    }

    private static void expectIllegalArgument(Runnable action, String message) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException: " + message);
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(message + " | expected=" + Arrays.toString(expected) + " actual=" + Arrays.toString(actual));
        }
    }

    private static void assertArrayEquals(double[] expected, double[] actual, String message) {
        if (expected.length != actual.length) {
            throw new AssertionError(message + " | different lengths");
        }
        for (int i = 0; i < expected.length; i++) {
            if (Double.doubleToLongBits(expected[i]) != Double.doubleToLongBits(actual[i])) {
                throw new AssertionError(message + " | index=" + i + " expected=" + expected[i] + " actual=" + actual[i]);
            }
        }
    }

    private static void assertArrayEquals(float[] expected, float[] actual, String message) {
        if (expected.length != actual.length) {
            throw new AssertionError(message + " | different lengths");
        }
        for (int i = 0; i < expected.length; i++) {
            if (Float.floatToIntBits(expected[i]) != Float.floatToIntBits(actual[i])) {
                throw new AssertionError(message + " | index=" + i + " expected=" + expected[i] + " actual=" + actual[i]);
            }
        }
    }

    private static void assertArrayEquals(long[] expected, long[] actual, String message) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(message + " | expected=" + Arrays.toString(expected) + " actual=" + Arrays.toString(actual));
        }
    }

    private static void assertArrayEquals(int[] expected, int[] actual, String message) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(message + " | expected=" + Arrays.toString(expected) + " actual=" + Arrays.toString(actual));
        }
    }

    private static void assertArrayEquals(short[] expected, short[] actual, String message) {
        if (!Arrays.equals(expected, actual)) {
            throw new AssertionError(message + " | expected=" + Arrays.toString(expected) + " actual=" + Arrays.toString(actual));
        }
    }
}
