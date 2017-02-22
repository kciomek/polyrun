package polyrun;

/***
 * Consumer for samples generated with {@link SamplerRunner}.
 */
public interface SampleConsumer {
    void consume(double[] sample);
}
