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
        map.put("CUdeviceptr", HIP_DEVICE_PTR);
        map.put("hipDeviceAttribute_t", JAVA_INT);
        map.put("CUfunction_attribute", JAVA_INT);

        map.put("CUcontext", HIP_CONTEXT);
        map.put("CUstream", HIP_STREAM);
        map.put("CUmodule", HIP_MODULE);
        map.put("CUfunction", HIP_FUNCTION);
        map.put("CUevent", ADDRESS);

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

    // TODO
    public static final MethodHandle HIP_CTX_CREATE =
            fromHeader("hipError_t cuCtxCreate_v2(CUcontext* pctx, unsigned int flags, CUdevice dev)");

    public static final MethodHandle HIP_DEVICE_TOTAL_MEM =
            fromHeader("hipError_t cuDeviceTotalMem(size_t* bytes, CUdevice dev)");

    public static final MethodHandle HIP_DEVICE_GET_ATTRIBUTE =
            fromHeader("hipError_t hipDeviceGetAttribute(int *pi, hipDeviceAttribute_t attr, int deviceId)");

    public static final MethodHandle HIP_CTX_SYNCHRONIZE =
            fromHeader("hipError_t cuCtxSynchronize()");

    public static final MethodHandle HIP_CTX_SET_CURRENT =
            fromHeader("hipError_t cuCtxSetCurrent(CUcontext ctx)");

    public static final MethodHandle HIP_STREAM_CREATE =
            fromHeader("hipError_t cuStreamCreate(CUstream* phStream, unsigned int flags)");

    public static final MethodHandle HIP_STREAM_DESTROY =
            fromHeader("hipError_t cuStreamDestroy_v2(CUstream stream)");

    public static final MethodHandle HIP_STREAM_SYNCHRONIZE =
            fromHeader("hipError_t cuStreamSynchronize(CUstream stream)");

    public static final MethodHandle HIP_MEM_ALLOC =
            fromHeader("hipError_t cuMemAlloc_v2(CUdeviceptr* dptr, size_t bytesize)");

    public static final MethodHandle HIP_MEM_FREE =
            fromHeader("hipError_t cuMemFree_v2(CUdeviceptr dptr)");

    public static final MethodHandle HIP_MEMCPY_HTOD =
            fromHeader("hipError_t cuMemcpyHtoD_v2(CUdeviceptr dstDevice, const void* srcHost, size_t ByteCount)");

    public static final MethodHandle HIP_MEMCPY_DTOH =
            fromHeader("hipError_t cuMemcpyDtoH_v2(void* dstHost, CUdeviceptr srcDevice, size_t ByteCount)");

    public static final MethodHandle HIP_MEMCPY_DTOD =
            fromHeader("hipError_t cuMemcpyDtoD_v2(CUdeviceptr dstDevice, CUdeviceptr srcDevice, size_t ByteCount)");

    public static final MethodHandle HIP_MEMCPY_HTOD_ASYNC =
            fromHeader("hipError_t cuMemcpyHtoDAsync_v2(CUdeviceptr dstDevice, const void* srcHost, size_t ByteCount, CUstream hStream)");

    public static final MethodHandle HIP_MEMCPY_DTOH_ASYNC =
            fromHeader("hipError_t cuMemcpyDtoHAsync_v2(void* dstHost, CUdeviceptr srcDevice, size_t ByteCount, CUstream hStream)");

    public static final MethodHandle HIP_MEMCPY_DTOD_ASYNC =
            fromHeader("hipError_t cuMemcpyDtoDAsync_v2(CUdeviceptr dstDevice, CUdeviceptr srcDevice, size_t ByteCount, CUstream hStream)");

    public static final MethodHandle HIP_MODULE_LOAD =
            fromHeader("hipError_t cuModuleLoad(CUmodule* module, const char* fname)");

    public static final MethodHandle HIP_MODULE_LOAD_DATA =
            fromHeader("hipError_t cuModuleLoadData(CUmodule* module, const void* image)");

    public static final MethodHandle HIP_MODULE_GET_FUNCTION =
            fromHeader("hipError_t cuModuleGetFunction(CUfunction* hfunc, CUmodule hmod, const char* name)");

    public static final MethodHandle HIP_FUNC_GET_ATTRIBUTE =
            fromHeader("hipError_t cuFuncGetAttribute(int* pi, CUfunction_attribute attrib, CUfunction hfunc)");

    public static final MethodHandle HIP_EVENT_CREATE =
            fromHeader("hipError_t cuEventCreate(CUevent* phEvent, unsigned int Flags)");

    public static final MethodHandle HIP_EVENT_RECORD =
            fromHeader("hipError_t cuEventRecord(CUevent hEvent, CUstream hStream)");

    public static final MethodHandle HIP_EVENT_QUERY =
            fromHeader("hipError_t cuEventQuery(CUevent hEvent)");

    public static final MethodHandle HIP_EVENT_SYNCHRONIZE =
            fromHeader("hipError_t cuEventSynchronize(CUevent hEvent)");

    public static final MethodHandle HIP_EVENT_DESTROY =
            fromHeader("hipError_t cuEventDestroy(CUevent hEvent)");

    public static final MethodHandle HIP_LAUNCH_KERNEL =
            fromHeader("hipError_t cuLaunchKernel(CUfunction f, unsigned int gridDimX, unsigned int gridDimY, unsigned int gridDimZ, unsigned int blockDimX, unsigned int blockDimY, unsigned int blockDimZ, unsigned int sharedMemBytes, CUstream hStream, void** kernelParams, void** extra)");

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
