import Foundation
import CUDADriver

final class CudaBufferWrapper {
    let ptr: CUdeviceptr
    let size: Int
    init(ptr: CUdeviceptr, size: Int) {
        self.ptr = ptr
        self.size = size
    }
}

@_cdecl("cuda_buffer_ptr")
public func cuda_buffer_ptr(ptr: UnsafeMutableRawPointer) -> UInt64 {
    let wrapper: CudaBufferWrapper = pointerToObject(ptr)
    return UInt64(wrapper.ptr)
}

@_cdecl("cuda_mem_alloc")
public func cuda_mem_alloc(size: Int) -> UnsafeMutableRawPointer? {
    var dptr: CUdeviceptr = 0
    let res = cuMemAlloc_v2(&dptr, size)

    if res != CUDA_SUCCESS { return nil }

    let wrapper = CudaBufferWrapper(ptr: dptr, size: size)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_mem_free")
public func cuda_mem_free(ptr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper = Unmanaged<CudaBufferWrapper>.fromOpaque(ptr).takeRetainedValue()
    let result: CUresult = cuMemFree_v2(wrapper.ptr)
    return Int32(result.rawValue)
}

@_cdecl("cuda_memcpy_htod")
public func cuda_memcpy_htod(
    bufPtr: UnsafeMutableRawPointer,
    host: UnsafeRawPointer,
    size: Int
) -> Int32 {
    let wrapper: CudaBufferWrapper = pointerToObject(bufPtr)
    let result: CUresult = cuMemcpyHtoD_v2(wrapper.ptr, host, size)
    return Int32(result.rawValue)
}

@_cdecl("cuda_memcpy_dtoh")
public func cuda_memcpy_dtoh(
    host: UnsafeMutableRawPointer,
    bufPtr: UnsafeMutableRawPointer,
    size: Int
) -> Int32 {
    let wrapper: CudaBufferWrapper = pointerToObject(bufPtr)
    let result: CUresult = cuMemcpyDtoH_v2(host, wrapper.ptr, size)
    return Int32(result.rawValue)
}


@_cdecl("cuda_memcpy_dtod")
public func cuda_memcpy_dtod(
    dst: UnsafeMutableRawPointer,
    src: UnsafeMutableRawPointer,
    size: Int
) -> Int32 {
    let dstBuf: CudaBufferWrapper = pointerToObject(dst)
    let srcBuf: CudaBufferWrapper = pointerToObject(src)
    let result: CUresult = cuMemcpyDtoD_v2(dstBuf.ptr, srcBuf.ptr, size)
    return Int32(result.rawValue)
}

@_cdecl("cuda_memcpy_htod_async")
public func cuda_memcpy_htod_async(
    bufPtr: UnsafeMutableRawPointer,
    hostPtr: UnsafeRawPointer,
    size: Int,
    streamPtr: UnsafeMutableRawPointer
) -> Int32 {
    let buf: CudaBufferWrapper = pointerToObject(bufPtr)
    let stream: CudaStreamWrapper = pointerToObject(streamPtr)
    let res: CUresult = cuMemcpyHtoDAsync_v2(buf.ptr, hostPtr, size, stream.stream)
    return Int32(res.rawValue)
}

@_cdecl("cuda_memcpy_dtoh_async")
public func cuda_memcpy_dtoh_async(
    hostPtr: UnsafeMutableRawPointer,
    bufPtr: UnsafeMutableRawPointer,
    size: Int,
    streamPtr: UnsafeMutableRawPointer
) -> Int32 {
    let buf: CudaBufferWrapper = pointerToObject(bufPtr)
    let stream: CudaStreamWrapper = pointerToObject(streamPtr)
    let res: CUresult = cuMemcpyDtoHAsync_v2(hostPtr, buf.ptr, size, stream.stream)
    return Int32(res.rawValue)
}