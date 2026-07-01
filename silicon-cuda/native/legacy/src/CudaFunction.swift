import Foundation
import CUDADriver

final class CudaFunctionWrapper {
    let function: CUfunction
    init(function: CUfunction) {
        self.function = function
    }
}

@_cdecl("cuda_function_max_threads_per_block")
public func cuda_function_max_threads_per_block(ptr: UnsafeMutableRawPointer) -> Int32 {
    let functionWrap: CudaFunctionWrapper = pointerToObject(ptr)
    let function: CUfunction = functionWrap.function

    var value: Int32 = 0
    let res = cuFuncGetAttribute(
        &value,
        CU_FUNC_ATTRIBUTE_MAX_THREADS_PER_BLOCK,
        function
    )

    guard res == CUDA_SUCCESS else {
        return -1
    }

    return value
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