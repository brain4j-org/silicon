package org.silicon.device;

import org.silicon.computing.ComputeQueue;
import org.silicon.kernel.ComputeModule;

import java.nio.file.Path;

public interface ComputeContext {
    ComputeQueue createQueue() throws Throwable;
    ComputeModule loadModule(Path path) throws Throwable;
    ComputeModule loadModule(byte[] rawSrc) throws Throwable;
    ComputeModule loadModule(String source);
}
