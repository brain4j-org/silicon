swiftc -emit-library "src\CudaDevice.swift src\CudaUtils.swift src\CudaContext.swift src\CudaBuffer.swift src\CudaStream.swift src\CudaModule.swift src\CudaFunction.swift" \
  -I Modules \
  -I "%CUDA_PATH%\include" \
  -L "%CUDA_PATH%\lib\x64" \
  -lcuda \
  -o out/libcuda4j.dll
