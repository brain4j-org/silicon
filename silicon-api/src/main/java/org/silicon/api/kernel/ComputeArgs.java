package org.silicon.api.kernel;

import org.silicon.api.device.ComputeBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loose-typed container for kernel arguments.
 * <p>
 * Supports {@link ComputeBuffer} and boxed primitives. The argument order is
 * preserved and used during {@link ComputeQueue} dispatch. Buffers are validated
 * as alive when added.
 */
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

    /**
     * Creates an argument list with immediate validation.
     * @param args initial arguments (buffers or boxed primitives)
     * @return a new argument container
     */
    public static ComputeArgs of(Object... args) {
        return new ComputeArgs(args);
    }

    /**
     * @return current argument count
     */
    public int size() {
        return args.size();
    }
    
    /**
     * @return the underlying argument list (in order)
     */
    public List<Object> getArgs() {
        return Collections.unmodifiableList(args);
    }

    /**
     * Adds a buffer argument, validating it is alive.
     * @param buffer buffer to add
     * @return this, for chaining
     */
    public ComputeArgs buffer(ComputeBuffer buffer) {
        if (!buffer.isAlive()) throw new IllegalArgumentException("Buffer is not alive");

        args.add(buffer);
        return this;
    }

    /**
     * Adds a double argument.
     * @param value value to add
     * @return this, for chaining
     */
    public ComputeArgs doubleVal(double value) {
        args.add(value);
        return this;
    }

    /**
     * Adds a float argument.
     * @param value value to add
     * @return this, for chaining
     */
    public ComputeArgs floatVal(float value) {
        args.add(value);
        return this;
    }

    /**
     * Adds a long argument.
     * @param value value to add
     * @return this, for chaining
     */
    public ComputeArgs longVal(long value) {
        args.add(value);
        return this;
    }

    /**
     * Adds an int argument.
     * @param value value to add
     * @return this, for chaining
     */
    public ComputeArgs intVal(int value) {
        args.add(value);
        return this;
    }

    /**
     * Adds a short argument.
     * @param value value to add
     * @return this, for chaining
     */
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
