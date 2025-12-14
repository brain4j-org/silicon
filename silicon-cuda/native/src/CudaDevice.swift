import Foundation
import CUDADriver

final class CudaDeviceWrapper {
    let device: CUdevice
    init(device: CUdevice) {
        self.device = device
    }
}

@_cdecl("cuda_init")
public func cuda_init() {
    cuInit(0)
}

@_cdecl("cuda_create_system_device")
public func cuda_create_system_device(index: Int32) -> UnsafeMutableRawPointer? {
    var count: Int32 = 0
    guard cuDeviceGetCount(&count) == CUDA_SUCCESS, index < count else {
        return nil
    }

    var dev: CUdevice = 0
    guard cuDeviceGet(&dev, index) == CUDA_SUCCESS else {
        return nil
    }

    let wrapper = CudaDeviceWrapper(device: dev)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_device_name")
public func cuda_device_name(ptr: UnsafeMutableRawPointer) -> UnsafeMutablePointer<CChar>? {
    let wrapper: CudaDeviceWrapper = pointerToObject(ptr)
    let buffer = UnsafeMutablePointer<CChar>.allocate(capacity: 256)
    cuDeviceGetName(buffer, 256, wrapper.device)
    return buffer
}

@_cdecl("cuda_device_count")
public func cuda_device_count() -> Int32 {
    var count: Int32 = 0
    cuDeviceGetCount(&count)
    return count
}