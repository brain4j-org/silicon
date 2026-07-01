import Foundation
import CUDADriver

final class CudaContextWrapper {
    let ctx: CUcontext
    let dev: CUdevice

    init(ctx: CUcontext, dev: CUdevice) {
        self.ctx = ctx
        self.dev = dev
    }
}

@_cdecl("cuda_context_set_current")
public func cuda_context_set_current(ptr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper = Unmanaged<CudaContextWrapper>.fromOpaque(ptr).takeUnretainedValue()
    let result: CUresult = cuCtxSetCurrent(wrapper.ctx)
    return Int32(result.rawValue)
}

@_cdecl("cuda_create_context")
public func cuda_create_context(devPtr: UnsafeMutableRawPointer) -> UnsafeMutableRawPointer? {
    let deviceWrap: CudaDeviceWrapper = pointerToObject(devPtr)

    var ctx: CUcontext?
    guard cuDevicePrimaryCtxRetain(&ctx, deviceWrap.device) == CUDA_SUCCESS,
          let realCtx = ctx else {
        return nil
    }

    _ = cuCtxSetCurrent(realCtx)
    let wrapper = CudaContextWrapper(ctx: realCtx, dev: deviceWrap.device)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_destroy_context")
public func cuda_destroy_context(ptr: UnsafeMutableRawPointer) {
    let wrapper = Unmanaged<CudaContextWrapper>.fromOpaque(ptr).takeRetainedValue()
    _ = cuDevicePrimaryCtxRelease_v2(wrapper.dev)
}

@_cdecl("cuda_sync_context")
public func cuda_context_synchronize() -> Int32 {
    let result: CUresult = cuCtxSynchronize()
    return Int32(result.rawValue)
}