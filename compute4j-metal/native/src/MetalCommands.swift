import Foundation
import Metal

@_cdecl("metal_create_command_buffer")
public func metal_create_command_buffer(queuePtr: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer? {
    let queue: MTLCommandQueue = pointerToObject(queuePtr)
    guard let cmdBuf = queue.makeCommandBuffer() else { return nil }
    return objectToPointer(cmdBuf)
}

@_cdecl("metal_make_encoder")
public func metal_make_encoder(
    cmdBufPtr: UnsafeMutableRawPointer,
    pipelinePtr: UnsafeMutableRawPointer
) -> UnsafeMutableRawPointer? {
    let cmdBuf: MTLCommandBuffer = pointerToObject(cmdBufPtr)
    let encoder = cmdBuf.makeComputeCommandEncoder()!
    let pipeline: MTLComputePipelineState = pointerToObject(pipelinePtr)
    encoder.setComputePipelineState(pipeline)
    return objectToPointer(encoder)
}

@_cdecl("metal_encoder_set_buffer")
public func metal_encoder_set_buffer(
    encPtr: UnsafeMutableRawPointer,
    bufPtr: UnsafeMutableRawPointer,
    index: Int
) {
    let encoder: MTLComputeCommandEncoder = pointerToObject(encPtr)
    let buffer: MTLBuffer = pointerToObject(bufPtr)
    encoder.setBuffer(buffer, offset: 0, index: index)
}

@_cdecl("metal_dispatch")
public func metal_dispatch(
    encPtr: UnsafeMutableRawPointer,
    gridX: Int, gridY: Int, gridZ: Int,
    blockX: Int, blockY: Int, blockZ: Int,
) {
    let encoder: MTLComputeCommandEncoder = pointerToObject(encPtr)

    let gridSize = MTLSize(width: gridX, height: gridY, depth: gridZ)
    let threadgroupSize = MTLSize(width: blockX, height: blockY, depth: blockZ)

    encoder.dispatchThreads(gridSize, threadsPerThreadgroup: threadgroupSize)
}

@_cdecl("metal_end_encoding")
public func metal_end_encoding(encPtr: UnsafeMutableRawPointer) {
    let encoder: MTLComputeCommandEncoder = pointerToObject(encPtr)
    encoder.endEncoding()
}

@_cdecl("metal_commit")
public func metal_commit(
    cmdBufPtr: UnsafeMutableRawPointer
) {
    let cmdBuf: MTLCommandBuffer = pointerToObject(cmdBufPtr)
    cmdBuf.commit()
}

@_cdecl("metal_wait_until_completed")
public func metal_wait_until_completed(
    cmdBufPtr: UnsafeMutableRawPointer
) {
    let cmdBuf: MTLCommandBuffer = pointerToObject(cmdBufPtr)
    cmdBuf.waitUntilCompleted()
}