package org.compute4j.kernel;

public interface ComputeModule {
    ComputeFunction getFunction(String name) throws Throwable;
}
