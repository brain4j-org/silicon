package org.silicon.api.kernel;

/**
 * Three-dimensional size for grids and work-groups.
 * <p>
 * Used for global and group sizes in dispatch. All components must be positive (> 0).
 */
public record ComputeSize(int x, int y, int z) {

    /**
     * Validates that all components are positive.
     * @throws IllegalArgumentException if any component is <= 0
     */
    public ComputeSize {
        if (x <= 0) throw new IllegalArgumentException("X component of compute size must not be 0 or negative");
        if (y <= 0) throw new IllegalArgumentException("Y component of compute size must not be 0 or negative");
        if (z <= 0) throw new IllegalArgumentException("Z component of compute size must not be 0 or negative");
    }
    
    /**
     * Determines the effective work dimension.
     * @return 1, 2, or 3 depending on non-unit components
     */
    public int workDim() {
        if (z > 1) return 3;
        if (y > 1) return 2;
        return 1;
    }
    
    /**
     * @return total element count (x * y * z)
     */
    public int total() {
        return x * y * z;
    }
}
