package org.metal4j;

import org.metal4j.state.MetalDevice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Metal {
    
    public static final Linker LINKER = Linker.nativeLinker();
    public static final SymbolLookup LOOKUP = loadFromResources("/libmetal4j.dylib");
    public static final MethodHandle METAL_CREATE_SYSTEM_DEVICE = LINKER.downcallHandle(
        LOOKUP.find("metal_create_system_device").orElse(null),
        FunctionDescriptor.of(ValueLayout.ADDRESS)
    );
    
    public static MetalDevice createSystemDevice() throws Throwable {
        MemorySegment ptr = (MemorySegment) METAL_CREATE_SYSTEM_DEVICE.invokeExact();
        return new MetalDevice(ptr);
    }
    
    public static SymbolLookup loadFromResources(String resourceName) {
        try (InputStream in = MetalObject.class.getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            
            String suffix = resourceName.substring(resourceName.lastIndexOf('.'));
            Path tempFile = Files.createTempFile("nativeLib", suffix);
            
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            
            return SymbolLookup.libraryLookup(tempFile.toString(), Arena.global());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
