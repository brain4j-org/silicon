package org.silicon.slang;

import org.silicon.backend.BackendType;
import org.silicon.device.ComputeContext;
import org.silicon.kernel.ComputeModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

public class SlangCompiler {

    private final ComputeContext context;
    private final boolean noCache;

    public SlangCompiler(ComputeContext context) {
        this(context, false);
    }

    public SlangCompiler(ComputeContext context, boolean noCache) {
        this.context = context;
        this.noCache = noCache;
    }

    public ComputeModule compile(Path path) {
        BackendType backendType = context.getBackendType();
        String target = switch (backendType) {
            case CUDA -> "ptx";
            case METAL -> "metal";
            case OPENCL -> throw new IllegalStateException("OpenCL is not yet supported");
        };

        String fileName = path.getFileName().toString();
        String out = fileName.replaceAll("\\.", "_") + "." + target;

        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path source = path.isAbsolute()
            ? path.normalize()
            : cwd.resolve(path).normalize();

        Path workDir = source.getParent();
        Path outPath = workDir.resolve(out);

        try {
            ProcessBuilder builder = new ProcessBuilder(
                "slangc", fileName,
                "-target", target,
                "-o", out
            );

            builder.directory(workDir.toFile());
            builder.redirectErrorStream(true);

            Process process = builder.start();

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("[slangc] " + line); // debugging, TODO: improve debugging and error logging
                }
            }

            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Slang compilation failed with exit code " + exit);
            }

            return context.loadModule(outPath);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    
    public ComputeModule compileFromResource(String resourcePath) {
        BackendType backendType = context.getBackendType();
        
        if (resourcePath.startsWith("/")) {
            throw new IllegalArgumentException("Resource path must be relative (no leading '/')");
        }
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        if (cl == null) {
            throw new IllegalStateException("No context ClassLoader available");
        }
        
        try (InputStream in = cl.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            
            byte[] sourceBytes = in.readAllBytes();
            String hash = sha256((backendType.name() + "\0").getBytes(StandardCharsets.UTF_8));
            
            hash = sha256(concat(hash.getBytes(), sourceBytes));
            
            Path cacheDir = cacheRoot().resolve(hash);
            Files.createDirectories(cacheDir);
            
            String fileName = Path.of(resourcePath).getFileName().toString();
            Path sourcePath = cacheDir.resolve(fileName);
            
            String target = switch (backendType) {
                case CUDA -> "ptx";
                case METAL -> "metal";
                case OPENCL -> throw new IllegalStateException("OpenCL is not yet supported");
            };
            
            Path modulePath = cacheDir.resolve(
                fileName.replaceAll("\\.", "_") + "." + target
            );
            
            // Cache hit
            if (Files.exists(modulePath) && !noCache) {
                return context.loadModule(modulePath);
            }
            
            // Cache miss -> compile
            Files.write(sourcePath, sourceBytes);
            return compile(sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compile resource: " + resourcePath, e);
        }
    }

    
    private static Path cacheRoot() {
        return Paths.get(
            System.getProperty("user.home"),
            ".cache",
            "silicon",
            "slang"
        );
    }
    
    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }
    
    private static String sha256(byte[] data) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            
            StringBuilder sb = new StringBuilder();
            
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
