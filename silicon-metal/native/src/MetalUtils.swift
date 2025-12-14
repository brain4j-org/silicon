import Foundation

@_cdecl("metal_release_object")
public func metal_release_object(ptr: UnsafeMutableRawPointer) {
    Unmanaged<AnyObject>.fromOpaque(ptr).release()
}

public func pointerToObject<T: AnyObject>(_ ptr: UnsafeMutableRawPointer) -> T {
    return Unmanaged<T>.fromOpaque(ptr).takeUnretainedValue()
}

public func objectToPointer<T: AnyObject>(_ object: T) -> UnsafeMutableRawPointer {
    return Unmanaged.passRetained(object).toOpaque()
}