package org.silicon.computing;

import org.silicon.device.ComputeBuffer;

import java.util.ArrayList;
import java.util.List;

public class ComputeArgs {

    private final List<Object> args;

    private ComputeArgs(Object[] args) {
        this.args = new ArrayList<>(List.of(args));
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
        args.add(buffer);
        return this;
    }

    public ComputeArgs doubleVal(double value) {
        args.add(value);
        return this;
    }

    public ComputeArgs floatVal(int value) {
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
}
