import Foundation
import Metal

@_cdecl("metal_create_command_queue")
public func metal_create_command_queue(ptr: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer? {
    let device: MTLDevice = pointerToObject(ptr)
    guard let commandQueue = device.makeCommandQueue() else { return nil }
    return objectToPointer(commandQueue);
}