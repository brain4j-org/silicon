import CUDADriver

@_cdecl("cuda_release_object")
public func cuda_release_object(ptr: UnsafeMutableRawPointer) {
    Unmanaged<AnyObject>.fromOpaque(ptr).release()
}

func objectToPointer<T: AnyObject>(_ obj: T) -> UnsafeMutableRawPointer {
    return Unmanaged.passRetained(obj).toOpaque()
}

func pointerToObject<T: AnyObject>(_ ptr: UnsafeMutableRawPointer) -> T {
    return Unmanaged<T>.fromOpaque(ptr).takeUnretainedValue()
}