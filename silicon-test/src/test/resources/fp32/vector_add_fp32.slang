[shader("compute")]
void add(
    uint3 tid : SV_DispatchThreadID,
    StructuredBuffer<float> a,
    StructuredBuffer<float> b,
    RWStructuredBuffer<float> c
)
{
    uint i = tid.x;
    c[i] = a[i] + b[i];
}
