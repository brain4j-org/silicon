[shader("compute")]
void add(
    uint3 tid : SV_DispatchThreadID,
    StructuredBuffer<half> a,
    StructuredBuffer<half> b,
    RWStructuredBuffer<half> c
)
{
    uint i = tid.x;
    c[i] = a[i] + b[i];
}
