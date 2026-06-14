@echo off
set "OUT_DIR=out"

if not "%TARGET_CLASSIFIER%"=="" (
    set "CLASSIFIER=%TARGET_CLASSIFIER%"
) else if /i "%PROCESSOR_ARCHITECTURE%"=="ARM64" (
    set "CLASSIFIER=windows-arm64"
) else (
    set "CLASSIFIER=windows-x64"
)

if "%SWIFTC%"=="" set "SWIFTC=swiftc"
set "SWIFTC_CMD=%SWIFTC:"=%"
set "OUT_DIR=%OUT_DIR%\%CLASSIFIER%"
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

set "CUDA_LIB_DIR=%CUDA_PATH%\lib\x64"
if "%CLASSIFIER%"=="windows-arm64" set "CUDA_LIB_DIR=%CUDA_PATH%\lib\arm64"

"%SWIFTC_CMD%" -emit-library ^
  src\CudaDevice.swift src\CudaUtils.swift src\CudaContext.swift src\CudaBuffer.swift src\CudaStream.swift src\CudaModule.swift src\CudaFunction.swift ^
  -I Modules ^
  -I "%CUDA_PATH%\include" ^
  -L "%CUDA_LIB_DIR%" ^
  -lcuda ^
  -o "%OUT_DIR%\libcuda4j.dll"
