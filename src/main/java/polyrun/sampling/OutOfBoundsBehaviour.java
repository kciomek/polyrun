package polyrun.sampling;

/**
 * Describes the behaviour of random walk when it tries to make a step that exceeds bounds of the polytope.
 * It applies only to some random walks.
 * <p>
 * Stay - does not make a step, i.e., next sample will be the same as previous one (the default behaviour)
 * Crop - crops the step to the bound (useful for sampling neighborhood)
 * Reflect - reflects from the bound (or stays if reflection also exceeds bounds)
 */
public enum OutOfBoundsBehaviour {
    Stay,
    Crop
}
