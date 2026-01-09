package org.silicon.cuda.device;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import static java.lang.foreign.ValueLayout.*;

public record CudaPointer(MemorySegment segment) {
    
    public static final Arena GLOBAL = Arena.ofShared();
    
    public static CudaPointer from(CudaPointer... pointers) {
        MemorySegment seg = GLOBAL.allocate(ADDRESS, pointers.length);
        
        for (int i = 0; i < pointers.length; i++) {
            MemorySegment pointerSeg = pointers[i].segment;
            seg.setAtIndex(ADDRESS, i, pointerSeg);
        }
        
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromByte(byte value) {
        MemorySegment seg = GLOBAL.allocate(JAVA_BYTE);
        seg.set(JAVA_BYTE, 0, value);
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromDouble(double value) {
        MemorySegment seg = GLOBAL.allocate(JAVA_DOUBLE);
        seg.set(JAVA_DOUBLE, 0, value);
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromFloat(float value) {
        MemorySegment seg = GLOBAL.allocate(JAVA_FLOAT);
        seg.set(JAVA_FLOAT, 0, value);
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromLong(long value) {
        MemorySegment seg = GLOBAL.allocate(JAVA_LONG);
        seg.set(JAVA_LONG, 0, value);
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromInt(int value) {
        MemorySegment seg = GLOBAL.allocate(JAVA_INT);
        seg.set(JAVA_INT, 0, value);
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromShort(short value) {
        MemorySegment seg = GLOBAL.allocate(JAVA_SHORT);
        seg.set(JAVA_SHORT, 0, value);
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromBuffer(CudaBuffer buf) {
        MemorySegment seg = GLOBAL.allocate(JAVA_LONG);
        seg.set(JAVA_LONG, 0, buf.getNativePointer());
        return new CudaPointer(seg);
    }
    
    public static CudaPointer fromString(String value) {
        byte[] bytes = (value + "\0").getBytes(StandardCharsets.UTF_8);
        MemorySegment seg = GLOBAL.allocateFrom(JAVA_BYTE, bytes);
        return new CudaPointer(seg);
    }
}
