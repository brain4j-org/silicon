import Foundation
import CUDADriver

final class CudaStreamWrapper {
    let stream: CUstream
    init(stream: CUstream) {
        self.stream = stream
    }
}

@_cdecl("cuda_stream_create")
public func cuda_stream_create() -> UnsafeMutableRawPointer? {
    var stream: CUstream?
    let res = cuStreamCreate(&stream, 0)
    if res != CUDA_SUCCESS || stream == nil { return nil }
    let wrapper = CudaStreamWrapper(stream: stream!)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_stream_destroy")
public func cuda_stream_destroy(ptr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper = Unmanaged<CudaStreamWrapper>.fromOpaque(ptr).takeRetainedValue()
    let result: CUresult = cuStreamDestroy_v2(wrapper.stream);
    return Int32(result.rawValue)
}

@_cdecl("cuda_stream_sync")
public func cuda_stream_sync(ptr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper: CudaStreamWrapper = pointerToObject(ptr)
    let result: CUresult = cuStreamSynchronize(wrapper.stream)
    return Int32(result.rawValue)
}

@_cdecl("cuda_stream_query")
public func cuda_stream_query(ptr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper: CudaStreamWrapper = pointerToObject(ptr)
    let result: CUresult = cuStreamQuery(wrapper.stream)
    return Int32(result.rawValue)
}