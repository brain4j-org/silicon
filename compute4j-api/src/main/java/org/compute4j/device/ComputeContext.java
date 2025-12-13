package org.compute4j.device;

import org.compute4j.kernel.ComputeModule;
import org.compute4j.computing.ComputeQueue;

import java.nio.file.Path;

public interface ComputeContext {
    ComputeQueue createQueue() throws Throwable;
    ComputeModule loadModule(Path path) throws Throwable;
    ComputeModule loadModule(byte[] rawSrc) throws Throwable;
    ComputeModule loadModule(String source);
}
