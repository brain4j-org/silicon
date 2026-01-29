import Foundation
import Metal

@_cdecl("metal_create_system_device")
public func metal_create_system_device() -> UnsafeMutableRawPointer? {
    guard let device: MTLDevice = MTLCreateSystemDefaultDevice() else { return nil }
    return objectToPointer(device)
}

@_cdecl("metal_device_name")
public func metal_device_name(ptr: UnsafeMutableRawPointer) -> UnsafeMutablePointer<CChar>? {
    let device: MTLDevice = pointerToObject(ptr)
    return strdup(device.name)
}

@_cdecl("metal_free_native")
public func metal_free_native(ptr: UnsafeMutableRawPointer) {
    free(ptr)
}

@_cdecl("metal_memory_size")
public func metal_memory_size(ptr: UnsafeMutableRawPointer) -> UInt64 {
    let device: MTLDevice = pointerToObject(ptr)
    return device.recommendedMaxWorkingSetSize
}
