package org.silicon.api.slang;

import org.silicon.api.SiliconException;
import org.silicon.api.backend.BackendType;
import org.silicon.api.device.ComputeContext;
import org.silicon.api.function.ComputeModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Compiles Slang source files to backend-specific modules.
 * <p>
 * Invokes the {@code slangc} tool and can cache compiled outputs on disk
 * for classpath resources.
 */
public class SlangCompiler {

    private final ComputeContext context;
    private final boolean noCache;

    /**
     * Creates a compiler using the given context and cache enabled.
     * @param context compute context used to load modules
     */
    public SlangCompiler(ComputeContext context) {
        this(context, false);
    }

    /**
     * Creates a compiler using the given context.
     * @param context compute context used to load modules
     * @param noCache if true, disables cache hits for resource compilation
     */
    public SlangCompiler(ComputeContext context, boolean noCache) {
        this.context = context;
        this.noCache = noCache;
    }

    /**
     * Compiles a Slang file on disk and loads the resulting module.
     * The target format is derived from {@link ComputeContext#getBackendType()}.
     * @param path path to the Slang source file
     * @return loaded compute module
     */
    public ComputeModule compile(Path path) {
        BackendType backendType = context.getBackendType();
        String target = backendType.compileTarget();

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
                    System.err.println("[slangc] " + line);
                }
            }

            int exit = process.waitFor();
            if (exit != 0) {
                throw new SiliconException("Slang compilation failed with exit code " + exit);
            }

            return context.loadModule(outPath);
        } catch (SiliconException e) {
            throw e;
        } catch (Throwable e) {
            throw new SiliconException("compile(Path) failed", e);
        }
    }
    
    /**
     * Compiles a Slang resource from the classpath and loads the resulting module.
     * Uses a SHA-256 based cache directory unless disabled via {@code noCache}.
     * @param resourcePath classpath resource path (must be relative)
     * @return loaded compute module
     */
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
            
            String target = backendType.compileTarget();
            
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
            throw new SiliconException("Failed to compile resource: " + resourcePath, e);
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
            throw new SiliconException("sha256() failed", e);
        }
    }
}
