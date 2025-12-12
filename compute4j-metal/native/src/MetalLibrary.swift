import Foundation
import Metal

@_cdecl("metal_create_library")
public func metal_create_library(
    devicePtr: UnsafeMutableRawPointer,
    sourcePtr: UnsafePointer<CChar>?
) -> UnsafeMutableRawPointer? {
    let device: MTLDevice = pointerToObject(devicePtr)

    guard let sourcePtr = sourcePtr else { return nil }

    let source = String(cString: sourcePtr)

    do {
        let library = try device.makeLibrary(source: source, options: nil)
        return objectToPointer(library)
    } catch {
        return nil
    }
}