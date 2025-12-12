package org.compute4j;

import java.nio.file.Path;

public interface ComputeContext {
    ComputeQueue createQueue();
    ComputeModule loadModule(Path path);
    ComputeModule loadModule(byte[] rawSrc);
    ComputeModule loadModule(String source);
}
