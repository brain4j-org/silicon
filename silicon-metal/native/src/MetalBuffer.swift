import Foundation
import Metal

@_cdecl("metal_new_buffer")
public func metal_new_buffer(devPtr: UnsafeMutableRawPointer, length: Int) -> UnsafeMutableRawPointer? {
    let device: MTLDevice = pointerToObject(devPtr)
    guard let buffer = device.makeBuffer(length: length, options: []) else { return nil }
    return objectToPointer(buffer)
}

@_cdecl("metal_buffer_contents")
public func metal_buffer_contents(bufPtr: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer {
    let buf: MTLBuffer = pointerToObject(bufPtr)
    return buf.contents()
}
