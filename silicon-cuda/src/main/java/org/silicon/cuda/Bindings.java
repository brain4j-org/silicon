package org.silicon.cuda;

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

    public static final ValueLayout.OfInt CU_RESULT = JAVA_INT;
    public static final ValueLayout.OfLong CU_DEVICE_PTR = ValueLayout.JAVA_LONG;
    public static final ValueLayout.OfInt CU_DEVICE = JAVA_INT;
    public static final AddressLayout CU_CONTEXT = ValueLayout.ADDRESS;
    public static final AddressLayout CU_STREAM = ValueLayout.ADDRESS;
    public static final AddressLayout CU_MODULE = ValueLayout.ADDRESS;
    public static final AddressLayout CU_FUNCTION = ValueLayout.ADDRESS;

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

        map.put("CUresult", CU_RESULT);
        map.put("CUdevice", CU_DEVICE);
        map.put("CUdeviceptr", CU_DEVICE_PTR);
        map.put("CUdevice_attribute", JAVA_INT);
        map.put("CUfunction_attribute", JAVA_INT);

        map.put("CUcontext", CU_CONTEXT);
        map.put("CUstream", CU_STREAM);
        map.put("CUmodule", CU_MODULE);
        map.put("CUfunction", CU_FUNCTION);
        map.put("CUevent", ADDRESS);

        TYPES = Collections.unmodifiableMap(map);
    }

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

    public static final MethodHandle CU_STREAM_DESTROY =
        fromHeader("CUresult cuStreamDestroy_v2(CUstream stream)");

    public static final MethodHandle CU_STREAM_SYNCHRONIZE =
        fromHeader("CUresult cuStreamSynchronize(CUstream stream)");

    public static final MethodHandle CU_MEM_ALLOC =
        fromHeader("CUresult cuMemAlloc_v2(CUdeviceptr* dptr, size_t bytesize)");

    public static final MethodHandle CU_MEM_FREE =
        fromHeader("CUresult cuMemFree_v2(CUdeviceptr dptr)");

    public static final MethodHandle CU_MEMCPY_HTOD =
        fromHeader("CUresult cuMemcpyHtoD_v2(CUdeviceptr dstDevice, const void* srcHost, size_t ByteCount)");

    public static final MethodHandle CU_MEMCPY_DTOH =
        fromHeader("CUresult cuMemcpyDtoH_v2(void* dstHost, CUdeviceptr srcDevice, size_t ByteCount)");

    public static final MethodHandle CU_MEMCPY_DTOD =
        fromHeader("CUresult cuMemcpyDtoD_v2(CUdeviceptr dstDevice, CUdeviceptr srcDevice, size_t ByteCount)");

    public static final MethodHandle CU_MEMCPY_HTOD_ASYNC =
        fromHeader("CUresult cuMemcpyHtoDAsync_v2(CUdeviceptr dstDevice, const void* srcHost, size_t ByteCount, CUstream hStream)");

    public static final MethodHandle CU_MEMCPY_DTOH_ASYNC =
        fromHeader("CUresult cuMemcpyDtoHAsync_v2(void* dstHost, CUdeviceptr srcDevice, size_t ByteCount, CUstream hStream)");

    public static final MethodHandle CU_MEMCPY_DTOD_ASYNC =
        fromHeader("CUresult cuMemcpyDtoDAsync_v2(CUdeviceptr dstDevice, CUdeviceptr srcDevice, size_t ByteCount, CUstream hStream)");

    public static final MethodHandle CU_MODULE_LOAD =
        fromHeader("CUresult cuModuleLoad(CUmodule* module, const char* fname)");

    public static final MethodHandle CU_MODULE_LOAD_DATA =
        fromHeader("CUresult cuModuleLoadData(CUmodule* module, const void* image)");

    public static final MethodHandle CU_MODULE_GET_FUNCTION =
        fromHeader("CUresult cuModuleGetFunction(CUfunction* hfunc, CUmodule hmod, const char* name)");

    public static final MethodHandle CU_FUNC_GET_ATTRIBUTE =
        fromHeader("CUresult cuFuncGetAttribute(int* pi, CUfunction_attribute attrib, CUfunction hfunc)");

    public static final MethodHandle CU_EVENT_CREATE =
        fromHeader("CUresult cuEventCreate(CUevent* phEvent, unsigned int Flags)");

    public static final MethodHandle CU_EVENT_RECORD =
        fromHeader("CUresult cuEventRecord(CUevent hEvent, CUstream hStream)");

    public static final MethodHandle CU_EVENT_QUERY =
        fromHeader("CUresult cuEventQuery(CUevent hEvent)");

    public static final MethodHandle CU_EVENT_SYNCHRONIZE =
        fromHeader("CUresult cuEventSynchronize(CUevent hEvent)");

    public static final MethodHandle CU_EVENT_DESTROY =
        fromHeader("CUresult cuEventDestroy(CUevent hEvent)");

    public static final MethodHandle CU_LAUNCH_KERNEL = CudaObject.find(
        "cuLaunchKernel",
        FunctionDescriptor.of(
            CU_RESULT,
            CU_FUNCTION,
            JAVA_INT, JAVA_INT, JAVA_INT,
            JAVA_INT, JAVA_INT, JAVA_INT,
            JAVA_INT,
            CU_STREAM,
            ADDRESS,
            ADDRESS
        )
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
        t = t.replaceAll("\\s*\\[\\s*]", "*");

        return t.trim();
    }
}
