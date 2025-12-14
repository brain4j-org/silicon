import Foundation
import Metal

@_cdecl("metal_make_pipeline")
public func metal_make_pipeline(fnPtr: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer? {
    let fn: MTLFunction = pointerToObject(fnPtr)
    let device = fn.device

    guard let pipeline = try? device.makeComputePipelineState(function: fn) else { return nil }

    return objectToPointer(pipeline)
}
