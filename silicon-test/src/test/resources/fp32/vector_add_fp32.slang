[shader("compute")]
void add(
    uint3 tid : SV_DispatchThreadID,
    StructuredBuffer<float> a : register(t0),
    StructuredBuffer<float> b : register(t1),
    RWStructuredBuffer<float> c : register(t2)
)
{
    uint i = tid.x;
    c[i] = a[i] + b[i];
}
