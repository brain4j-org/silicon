package org.silicon.api.kernel;

import org.silicon.api.device.ComputeBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComputeArgs {

    private final List<Object> args;

    private ComputeArgs(Object[] args) {
        this.args = new ArrayList<>(List.of(args));
        
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            
            if (!(arg instanceof ComputeBuffer buffer)) continue;
            
            if (!buffer.isAlive()) {
                throw new IllegalArgumentException("Buffer at %s is not alive".formatted(i));
            }
        }
    }

    public static ComputeArgs of(Object... args) {
        return new ComputeArgs(args);
    }

    public int size() {
        return args.size();
    }
    
    public List<Object> getArgs() {
        return args;
    }

    public ComputeArgs buffer(ComputeBuffer buffer) {
        if (!buffer.isAlive()) throw new IllegalArgumentException("Buffer is not alive");

        args.add(buffer);
        return this;
    }

    public ComputeArgs doubleVal(double value) {
        args.add(value);
        return this;
    }

    public ComputeArgs floatVal(float value) {
        args.add(value);
        return this;
    }

    public ComputeArgs longVal(long value) {
        args.add(value);
        return this;
    }

    public ComputeArgs intVal(int value) {
        args.add(value);
        return this;
    }

    public ComputeArgs shortVal(short value) {
        args.add(value);
        return this;
    }

    @Override
    public String toString() {
        return "ComputeArgs{" +
            "size=" + args.size() +
            ", args=" + args +
            '}';
    }
}
