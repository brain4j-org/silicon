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

@_cdecl("cuda_device_memory_size")
public func cuda_device_memory_size(ptr: UnsafeMutableRawPointer) -> UInt64 {
    let wrapper: CudaDeviceWrapper = pointerToObject(ptr)

    var bytes: Int = 0
    let res = cuDeviceTotalMem_v2(&bytes, wrapper.device)

    guard res == CUDA_SUCCESS else {
        return 0
    }

    return UInt64(bytes)
}


@_cdecl("cuda_device_supports")
public func cuda_device_supports(ptr: UnsafeMutableRawPointer, feature: Int32) -> Bool {
    let wrapper: CudaDeviceWrapper = pointerToObject(ptr)

    var major: Int32 = 0
    var minor: Int32 = 0

    cuDeviceGetAttribute(&major, CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MAJOR, wrapper.device)
    cuDeviceGetAttribute(&minor, CU_DEVICE_ATTRIBUTE_COMPUTE_CAPABILITY_MINOR, wrapper.device)

    switch feature {
    case 0: // FP16
        // native half arithmetic: >= 5.3
        return major > 5 || (major == 5 && minor >= 3)

    case 1: // FP64
        // >= 1.3
        return major > 1 || (major == 1 && minor >= 3)

    default:
        return false
    }
}
