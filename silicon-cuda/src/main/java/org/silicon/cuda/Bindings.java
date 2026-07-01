package org.silicon.cuda;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.foreign.ValueLayout.*;

public class Bindings {

    public static final ValueLayout.OfInt CU_RESULT = JAVA_INT;
    public static final ValueLayout.OfLong CU_DEVICE_PTR = ValueLayout.JAVA_LONG;
    public static final ValueLayout.OfInt CU_DEVICE = JAVA_INT;
    public static final AddressLayout CU_CONTEXT = ValueLayout.ADDRESS;
    public static final AddressLayout CU_STREAM = ValueLayout.ADDRESS;
    public static final AddressLayout CU_MODULE = ValueLayout.ADDRESS;
    public static final AddressLayout CU_FUNCTION = ValueLayout.ADDRESS;

    public static Map<String, MemoryLayout> TYPES = Map.ofEntries(
        Map.entry("void", null),
        Map.entry("bool", JAVA_BOOLEAN),
        Map.entry("char", JAVA_BYTE),
        Map.entry("signed char", JAVA_BYTE),
        Map.entry("unsigned char", JAVA_BYTE),
        Map.entry("short", JAVA_SHORT),
        Map.entry("unsigned short", JAVA_SHORT),
        Map.entry("int", JAVA_INT),
        Map.entry("unsigned int", JAVA_INT),
        Map.entry("long", JAVA_LONG),
        Map.entry("unsigned long", JAVA_LONG),
        Map.entry("long long", JAVA_LONG),
        Map.entry("unsigned long long", JAVA_LONG),
        Map.entry("size_t", JAVA_LONG),
        Map.entry("float", JAVA_FLOAT),
        Map.entry("double", JAVA_DOUBLE),

        // CUDA typedefs / handles
        Map.entry("CUresult", CU_RESULT),
        Map.entry("CUdevice", CU_DEVICE),
        Map.entry("CUdeviceptr", CU_DEVICE_PTR),
        Map.entry("CUdevice_attribute", JAVA_INT),

        // Opaque pointer-like handles
        Map.entry("CUcontext", CU_CONTEXT),
        Map.entry("CUstream", CU_STREAM),
        Map.entry("CUmodule", CU_MODULE),
        Map.entry("CUfunction", CU_FUNCTION),
        Map.entry("CUevent", ADDRESS)
    );

    private static final Pattern PROTOTYPE = Pattern.compile(
        "^\\s*(?<ret>.*?)\\s+(?:CUDAAPI\\s+)?(?<name>[A-Za-z_][A-Za-z0-9_]*)\\s*\\((?<params>.*)\\)\\s*;?\\s*$",
        Pattern.DOTALL
    );

    public static final MethodHandle CU_INIT =
        fromHeader("CUresult cuInit(unsigned int Flags)");

    public static final MethodHandle CU_DEVICE_GET_COUNT =
        fromHeader("CUresult cuDeviceGetCount(int* count)");

    public static final MethodHandle CU_DEVICE_GET =
        fromHeader("CUresult cuDeviceGet(CUdevice* device, int ordinal)");

    public static final MethodHandle CU_DEVICE_GET_NAME =
        fromHeader("CUresult cuDeviceGetName(char* name, int len, CUdevice dev)");

    public static final MethodHandle CU_CTX_CREATE =
        fromHeader("CUresult cuCtxCreate_v2(CUcontext* pctx, unsigned int flags, CUdevice dev)");

    public static final MethodHandle CU_DEVICE_TOTAL_MEM =
        fromHeader("CUresult cuDeviceTotalMem(size_t* bytes, CUdevice dev)");

    public static final MethodHandle CU_DEVICE_GET_ATTRIBUTE =
        fromHeader("CUresult cuDeviceGetAttribute(int* pi, CUdevice_attribute attrib, CUdevice dev)");

    public static final MethodHandle CU_CTX_SYNCHRONIZE =
        fromHeader("CUresult cuCtxSynchronize()");

    public static final MethodHandle CU_CTX_SET_CURRENT =
        fromHeader("CUresult cuCtxSetCurrent(CUcontext ctx)");

    public static final MethodHandle CU_STREAM_CREATE =
        fromHeader("CUresult cuStreamCreate(CUstream* phStream, unsigned int flags)");


    public static final MethodHandle CUDA_MODULE_LOAD = CudaObject.find(
        "cuda_module_load",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MODULE_LOAD_DATA = CudaObject.find(
        "cuda_module_load_data",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    );
    public static final MethodHandle CUDA_MEM_ALLOC = CudaObject.find(
        "cuda_mem_alloc",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );

    public static MethodHandle fromHeader(String header) {
        Matcher result = PROTOTYPE.matcher(header);

        if (!result.matches()) {
            throw new IllegalArgumentException("Invalid prototype: " + header);
        }

        String returnType = normalize(result.group("ret"));
        String name = result.group("name");
        String params = result.group("params");

        List<MemoryLayout> argLayouts = parseParams(params);

        MemoryLayout[] args = argLayouts.toArray(new MemoryLayout[0]);
        MemoryLayout ret = cToLayout(returnType);

        FunctionDescriptor descriptor = ret == null
            ? FunctionDescriptor.ofVoid(args)
            : FunctionDescriptor.of(ret, args);

        return CudaObject.find(name, descriptor);
    }

    private static List<MemoryLayout> parseParams(String params) {
        List<MemoryLayout> argLayouts = new ArrayList<>();

        if (params.isBlank() || params.equals("void")) {
            return argLayouts;
        }

        for (String param : params.split(",")) {
            param = param.trim();

            int paramNameIndex = param.lastIndexOf(' ');

            String type = (paramNameIndex >= 0)
                ? param.substring(0, paramNameIndex)
                : param;

            argLayouts.add(cToLayout(type));
        }

        return argLayouts;
    }

    private static MemoryLayout cToLayout(String type) {
        String normalized = normalize(type);

        if (normalized.endsWith("*")) {
            return ValueLayout.ADDRESS;
        }

        MemoryLayout layout = TYPES.get(normalized);
        if (layout == null && !TYPES.containsKey(normalized)) {
            throw new IllegalArgumentException("Unsupported type: " + normalized);
        }

        return layout;
    }

    private static String normalize(String raw) {
        String t = raw.trim();

        t = t.replaceAll("\\b(const|volatile|restrict|__restrict__|__restrict|CUDAAPI)\\b", " ");
        t = t.replaceAll("\\s+", " ").trim();

        t = t.replaceAll("\\s*\\*\\s*", "*");
        t = t.replaceAll("\\s*\\[\\s*]", "*"); // arrays -> pointers, if they ever appear

        return t.trim();
    }
}
