package org.silicon.api;

import java.util.Locale;
import java.util.List;
import java.util.Optional;

public final class Platform {

    private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    private static final String ARCH = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);

    private Platform() {
    }

    public static OperativeSystem current() {
        if (isWindows()) return OperativeSystem.WINDOWS;
        if (isLinux()) return OperativeSystem.LINUX;
        if (isMacOS()) return OperativeSystem.MACOS;

        throw new UnsupportedOperationException();
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isLinux() {
        return OS.contains("linux");
    }

    public static boolean isMacOS() {
        return OS.contains("mac") || OS.contains("darwin");
    }

    public static Optional<String> normalizedArch() {
        if (ARCH.equals("amd64") || ARCH.equals("x86_64")) {
            return Optional.of("x64");
        }
        if (ARCH.equals("aarch64") || ARCH.equals("arm64")) {
            return Optional.of("arm64");
        }
        return Optional.empty();
    }

    public static Optional<String> normalizedOs() {
        if (isWindows()) {
            return Optional.of("windows");
        }
        if (isLinux()) {
            return Optional.of("linux");
        }
        if (isMacOS()) {
            return Optional.of("macos");
        }
        return Optional.empty();
    }

    public static Optional<String> platformClassifier() {
        return normalizedOs().flatMap(os -> normalizedArch().map(arch -> os + "-" + arch));
    }

    public static Optional<String> nativeLibraryName(String baseName) {
        List<String> names = nativeLibraryNames(baseName);

        if (names.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(names.getFirst());
    }

    public static List<String> nativeLibraryNames(String baseName) {
        if (isWindows()) {
            return List.of("lib" + baseName + ".dll");
        }

        if (isLinux()) {
            return List.of("lib" + baseName + ".so");
        }

        if (isMacOS()) {
            return List.of("lib" + baseName + ".dylib");
        }

        return List.of();
    }

    public static String platformDescription() {
        return OS + " (" + ARCH + ")";
    }

    public enum OperativeSystem {
        WINDOWS, LINUX, MACOS
    }
}
