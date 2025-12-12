import Foundation
import Metal

@_cdecl("metal_create_function")
public func metal_create_function(
    libraryPtr: UnsafeMutableRawPointer,
    functionPtr: UnsafePointer<CChar>?
) -> UnsafeMutableRawPointer? {
    let library: MTLLibrary = pointerToObject(libraryPtr)
    guard let functionPtr = functionPtr else { return nil }

    let functionName = String(cString: functionPtr)
    guard let function = library.makeFunction(name: functionName) else { return nil }

    return objectToPointer(function)
}