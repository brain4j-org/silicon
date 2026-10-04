package org.silicon.rocm;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.foreign.ValueLayout.*;

public class Bindings {

    public static final ValueLayout.OfInt HIP_ERROR = JAVA_INT;
    public static final ValueLayout.OfLong HIP_DEVICE_PTR = ValueLayout.JAVA_LONG;
    public static final ValueLayout.OfInt HIP_DEVICE = JAVA_INT;
    public static final AddressLayout HIP_CONTEXT = ValueLayout.ADDRESS;
    public static final AddressLayout HIP_STREAM = ValueLayout.ADDRESS;
    public static final AddressLayout HIP_MODULE = ValueLayout.ADDRESS;
    public static final AddressLayout HIP_FUNCTION = ValueLayout.ADDRESS;

    public static final Map<String, MemoryLayout> TYPES;

    static {
        Map<String, MemoryLayout> map = new HashMap<>();
        map.put("void", null);
        map.put("bool", JAVA_BOOLEAN);
        map.put("char", JAVA_BYTE);
        map.put("signed char", JAVA_BYTE);
        map.put("unsigned char", JAVA_BYTE);
        map.put("short", JAVA_SHORT);
        map.put("unsigned short", JAVA_SHORT);
        map.put("int", JAVA_INT);
        map.put("unsigned int", JAVA_INT);
        map.put("long", JAVA_LONG);
        map.put("unsigned long", JAVA_LONG);
        map.put("long long", JAVA_LONG);
        map.put("unsigned long long", JAVA_LONG);
        map.put("size_t", JAVA_LONG);
        map.put("float", JAVA_FLOAT);
        map.put("double", JAVA_DOUBLE);

        map.put("hipError_t", HIP_ERROR);
        map.put("hipDevice_t", HIP_DEVICE);
        map.put("hipDeviceptr_t", HIP_DEVICE_PTR);
        map.put("hipDeviceAttribute_t", JAVA_INT);
        map.put("hipFunction_attribute", JAVA_INT);
        map.put("hipCtx_t", HIP_CONTEXT);
        map.put("hipStream_t", HIP_STREAM);
        map.put("hipModule_t", HIP_MODULE);
        map.put("hipFunction_t", HIP_FUNCTION);
        map.put("hipEvent_t", ADDRESS);

        TYPES = Collections.unmodifiableMap(map);
    }

    private static final Pattern PROTOTYPE = Pattern.compile(
            "^\\s*(?<ret>.*?)\\s+(?:CUDAAPI\\s+)?(?<name>[A-Za-z_][A-Za-z0-9_]*)\\s*\\((?<params>.*)\\)\\s*;?\\s*$",
            Pattern.DOTALL
    );

    public static final MethodHandle HIP_INIT =
            fromHeader("hipError_t hipInit(unsigned int flags)");

    public static final MethodHandle HIP_DEVICE_GET_COUNT =
            fromHeader("hipError_t hipGetDeviceCount(int* count)");

    public static final MethodHandle HIP_DEVICE_SET =
            fromHeader("hipError_t hipSetDevice(int deviceId)");

    public static final MethodHandle HIP_DEVICE_GET =
            fromHeader("hipError_t hipGetDevice(int *deviceId)");

    public static final MethodHandle HIP_DEVICE_GET_NAME =
            fromHeader("hipError_t hipDeviceGetName(char *name, int len, hipDevice_t device)");

    public static final MethodHandle HIP_DEVICE_TOTAL_MEM =
            fromHeader("hipError_t hipDeviceTotalMem(size_t* bytes, hipDevice_t dev)");

    public static final MethodHandle HIP_DEVICE_GET_ATTRIBUTE =
            fromHeader("hipError_t hipDeviceGetAttribute(int *pi, hipDeviceAttribute_t attr, int deviceId)");

    public static final MethodHandle HIP_CTX_CREATE =
            fromHeader("hipError_t hipCtxCreate(hipCtx_t *ctx, unsigned int flags, hipDevice_t device)");

    public static final MethodHandle HIP_CTX_SYNCHRONIZE =
            fromHeader("hipError_t hipCtxSynchronize()");

    public static final MethodHandle HIP_CTX_SET_CURRENT =
            fromHeader("hipError_t hipCtxSetCurrent(hipCtx_t ctx)");

    public static final MethodHandle HIP_STREAM_CREATE =
            fromHeader("hipError_t hipStreamCreate(hipStream_t *stream)");

    public static final MethodHandle HIP_STREAM_DESTROY =
            fromHeader("hipError_t hipStreamDestroy(hipStream_t stream)");

    public static final MethodHandle HIP_STREAM_SYNCHRONIZE =
            fromHeader("hipError_t hipStreamSynchronize(hipStream_t stream)");

    public static final MethodHandle HIP_MEM_ALLOC =
            fromHeader("hipError_t hipMalloc(void** ptr, size_t size)");

    public static final MethodHandle HIP_MEM_FREE =
            fromHeader("hipError_t hipFree(void* ptr)");

    public static final MethodHandle HIP_MEMCPY_HTOD =
            fromHeader("hipError_t hipMemcpyHtoD(hipDeviceptr_t dst, const void *src, size_t sizeBytes)");

    public static final MethodHandle HIP_MEMCPY_DTOH =
            fromHeader("hipError_t hipMemcpyDtoH(void *dst, hipDeviceptr_t src, size_t sizeBytes)");

    public static final MethodHandle HIP_MEMCPY_DTOD =
            fromHeader("hipError_t hipMemcpyDtoD(hipDeviceptr_t dst, hipDeviceptr_t src, size_t sizeBytes)");

    public static final MethodHandle HIP_MEMCPY_HTOD_ASYNC =
            fromHeader("hipError_t hipMemcpyHtoDAsync(hipDeviceptr_t dst, const void *src, size_t sizeBytes, hipStream_t stream)");

    public static final MethodHandle HIP_MEMCPY_DTOH_ASYNC =
            fromHeader("hipError_t hipMemcpyDtoHAsync(void *dst, hipDeviceptr_t src, size_t sizeBytes, hipStream_t stream)");

    public static final MethodHandle HIP_MEMCPY_DTOD_ASYNC =
            fromHeader("hipError_t hipMemcpyDtoDAsync(hipDeviceptr_t dst, hipDeviceptr_t src, size_t sizeBytes, hipStream_t stream)");

    public static final MethodHandle HIP_MODULE_LOAD =
            fromHeader("hipError_t hipModuleLoad(hipModule_t* module, const char* fname)");

    public static final MethodHandle HIP_MODULE_LOAD_DATA =
            fromHeader("hipError_t hipModuleLoadData(hipModule_t* module, const void* image)");

    public static final MethodHandle HIP_MODULE_GET_FUNCTION =
            fromHeader("hipError_t hipModuleGetFunction(hipFunction_t* function, hipModule_t module, const char* kname)");

    public static final MethodHandle HIP_FUNC_GET_ATTRIBUTE =
            fromHeader("hipError_t hipFuncGetAttribute(int* value, hipFunction_attribute attrib, hipFunction_t hfunc)");

    public static final MethodHandle HIP_EVENT_CREATE =
            fromHeader("hipError_t hipEventCreate(hipEvent_t* event)");

    public static final MethodHandle HIP_EVENT_CREATE_WITH_FLAGS =
            fromHeader("hipError_t hipEventCreateWithFlags(hipEvent_t* event, unsigned int flags)");

    public static final MethodHandle HIP_EVENT_RECORD =
            fromHeader("hipError_t hipEventRecord(hipEvent_t event, hipStream_t stream)");

    public static final MethodHandle HIP_EVENT_QUERY =
            fromHeader("hipError_t hipEventQuery(hipEvent_t event)");

    public static final MethodHandle HIP_EVENT_SYNCHRONIZE =
            fromHeader("hipError_t hipEventSynchronize(hipEvent_t event)");

    public static final MethodHandle HIP_EVENT_DESTROY =
            fromHeader("hipError_t hipEventDestroy(hipEvent_t event)");

    public static final MethodHandle HIP_LAUNCH_KERNEL =
            fromHeader("hipError_t hipModuleLaunchKernel(hipFunction_t f, unsigned int gridDimX, unsigned int gridDimY, unsigned int gridDimZ, unsigned int blockDimX, unsigned int blockDimY, unsigned int blockDimZ, unsigned int sharedMemBytes, hipStream_t stream, void** kernelParams, void** extra)");

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

        return RocmObject.find(name, descriptor);
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
        t = t.replaceAll("\\s*\\[\\s*]", "*");

        return t.trim();
    }
}
