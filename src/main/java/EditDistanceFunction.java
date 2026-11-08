/** A common signature so all edit-distance implementations can be benchmarked in one loop. */
@FunctionalInterface
public interface EditDistanceFunction {
    int distance(String a, String b);
}
