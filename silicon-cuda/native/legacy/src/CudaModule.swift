import Foundation
import CUDADriver

final class CudaModuleWrapper {
    let module: CUmodule
    init(module: CUmodule) {
        self.module = module
    }
}

@_cdecl("cuda_module_load")
public func cuda_module_load(pathPtr: UnsafePointer<CChar>) -> UnsafeMutableRawPointer? {
    var module: CUmodule?

    guard cuModuleLoad(&module, pathPtr) == CUDA_SUCCESS else {
        return nil
    }

    let wrapper = CudaModuleWrapper(module: module!)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_module_load_data")
public func cuda_module_load_data(dataPtr: UnsafeRawPointer) -> UnsafeMutableRawPointer? {
    var module: CUmodule?

    guard cuModuleLoadData(&module, dataPtr) == CUDA_SUCCESS else {
        return nil
    }

    let wrapper = CudaModuleWrapper(module: module!)
    return objectToPointer(wrapper)
}

@_cdecl("cuda_module_unload")
public func cuda_module_unload(modulePtr: UnsafeMutableRawPointer) -> Int32 {
    let wrapper: CudaModuleWrapper = pointerToObject(modulePtr)
    let res: CUresult = cuModuleUnload(wrapper.module)
    return Int32(res.rawValue)
}

@_cdecl("cuda_module_get_function")
public func cuda_module_get_function(
    modulePtr: UnsafeMutableRawPointer,
    namePtr: UnsafePointer<CChar>
) -> UnsafeMutableRawPointer? {
    let module: CudaModuleWrapper = pointerToObject(modulePtr)
    var function: CUfunction?

    guard cuModuleGetFunction(&function, module.module, namePtr) == CUDA_SUCCESS else {
        return nil
    }

    let wrapper = CudaFunctionWrapper(function: function!)
    return objectToPointer(wrapper)
}