#include <metal_stdlib>
#include <metal_math>
#include <metal_texture>
using namespace metal;

#line 10 "example_kernel.slang"
struct KernelContext_0
{
    float device* result_0;
    float device* buffer0_0;
    float device* buffer1_0;
};


#line 7
[[kernel]] void add(uint3 threadId_0 [[thread_position_in_grid]], float device* result_1 [[buffer(2)]], float device* buffer0_1 [[buffer(0)]], float device* buffer1_1 [[buffer(1)]])
{

#line 7
    KernelContext_0 kernelContext_0;

#line 7
    (&kernelContext_0)->result_0 = result_1;

#line 7
    (&kernelContext_0)->buffer0_0 = buffer0_1;

#line 7
    (&kernelContext_0)->buffer1_0 = buffer1_1;

    uint index_0 = threadId_0.x;
    *((&kernelContext_0)->result_0+index_0) = *((&kernelContext_0)->buffer0_0+index_0) + *(buffer1_1+index_0);
    return;
}

