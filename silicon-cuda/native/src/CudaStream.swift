import Foundation
import CUDADriver

final class CudaStreamWrapper {
    let stream: CUstream
    init(stream: CUstream) {
        self.stream = stream
    }
}

final class CudaEventWrapper {
    let event: CUevent
    init(event: CUevent) {
        self.event = event
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

@_cdecl("cuda_event_create")
public func cuda_event_create(flags: UInt32) -> UnsafeMutableRawPointer? {
    var event: CUevent?
    let result = cuEventCreate(&event, flags)

    if result != CUDA_SUCCESS || event == nil { return nil }

    let wrapper = CudaEventWrapper(event: event!)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_event_record")
public func cuda_event_record(
    eventPtr: UnsafeMutableRawPointer,
    streamPtr: UnsafeMutableRawPointer
) -> Int32 {
    let eventWrap: CudaEventWrapper = pointerToObject(eventPtr)
    let streamWrap: CudaStreamWrapper = pointerToObject(streamPtr)

    let result: CUresult = cuEventRecord(eventWrap.event, streamWrap.stream)
    return Int32(result.rawValue)
}


@_cdecl("cuda_event_synchronize")
public func cuda_event_synchronize(
    eventPtr: UnsafeMutableRawPointer
) -> Int32 {
    let eventWrap: CudaEventWrapper = pointerToObject(eventPtr)
    let result: CUresult = cuEventSynchronize(eventWrap.event)
    return Int32(result.rawValue)
}

@_cdecl("cuda_event_query")
public func cuda_event_query(
    eventPtr: UnsafeMutableRawPointer
) -> Int32 {
    let eventWrap: CudaEventWrapper = pointerToObject(eventPtr)
    let result: CUresult = cuEventQuery(eventWrap.event)
    return Int32(result.rawValue)
}

@_cdecl("cuda_event_destroy")
public func cuda_event_destroy(ptr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper: CudaEventWrapper = pointerToObject(ptr)
    let result: CUresult = cuEventDestroy_v2(wrapper.event)
    return Int32(result.rawValue)
}
