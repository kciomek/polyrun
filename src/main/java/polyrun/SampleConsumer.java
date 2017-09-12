package polyrun;

/***
 * Consumer for samples generated with {@link SamplerRunner}, {@link PolytopeRunner#neighborhood} or {@link PolytopeRunner#chain}.
 */
public interface SampleConsumer {
    void consume(double[] sample);
}
