import Foundation
import CUDADriver

final class CudaFunctionWrapper {
    let function: CUfunction
    init(function: CUfunction) {
        self.function = function
    }
}

@_cdecl("cuda_launch_kernel")
public func cuda_launch_kernel(
    funcPtr: UnsafeMutableRawPointer,
    gridX: UInt32,
    gridY: UInt32,
    gridZ: UInt32,
    blockX: UInt32,
    blockY: UInt32,
    blockZ: UInt32,
    sharedMemBytes: UInt32,
    streamPtr: UnsafeMutableRawPointer?,
    kernelParams: UnsafeMutablePointer<UnsafeMutableRawPointer?>?
) -> Int32 {
    let function: CudaFunctionWrapper = pointerToObject(funcPtr)
    var stream: CUstream? = nil

    if let sPtr = streamPtr {
        let sWrapper: CudaStreamWrapper = pointerToObject(sPtr)
        stream = sWrapper.stream
    }

    let res = cuLaunchKernel(
        function.function,
        gridX, gridY, gridZ,
        blockX, blockY, blockZ,
        sharedMemBytes,
        stream,
        kernelParams,
        nil
    )
    return Int32(res.rawValue)
}