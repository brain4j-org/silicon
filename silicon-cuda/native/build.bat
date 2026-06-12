@echo off
set "OUT_DIR=out\windows-x64"
if "%SWIFTC%"=="" set "SWIFTC=swiftc"
set "SWIFTC_CMD=%SWIFTC:"=%"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

"%SWIFTC_CMD%" -emit-library ^
  src\CudaDevice.swift src\CudaUtils.swift src\CudaContext.swift src\CudaBuffer.swift src\CudaStream.swift src\CudaModule.swift src\CudaFunction.swift ^
  -I Modules ^
  -I "%CUDA_PATH%\include" ^
  -L "%CUDA_PATH%\lib\x64" ^
  -lcuda ^
  -o "%OUT_DIR%\libcuda4j.dll"
