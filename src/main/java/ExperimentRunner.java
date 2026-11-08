import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/** Reproducible experiment runner. Every measured run is written as one CSV row. */
public class ExperimentRunner {
    private static final long SEED = 20261108L;
    private static final int MEASURED_RUNS = 5;
    private static final int[] RECURSIVE_SIZES = {2, 4, 6, 8};
    private static final int[] DP_SIZES = {32, 64, 128, 256, 512};
    private static final int[] SEARCH_SIZES = {1000, 2000, 4000, 8000};
    private static final char[] ALPHABET = "ABCDE".toCharArray();

    private final Algorithms algorithms = new Algorithms();
    private final Random random = new Random(SEED);

    public static void main(String[] args) throws IOException {
        new ExperimentRunner().run();
    }

    private void run() throws IOException {
        Path output = Path.of("results", "results.csv");
        Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write("algorithm,input,n,m,run,time_ms,ops,extra_memory_ints");
            writer.newLine();
            runRecursiveEditDistance(writer);
            runDynamicProgramming(writer);
            runStringSearch(writer);
        }
        System.out.println("Experiment completed. Results written to " + output.toAbsolutePath());
        System.out.println("Seed=" + SEED + ", measured runs per case=" + MEASURED_RUNS);
    }

    private void runRecursiveEditDistance(BufferedWriter writer) throws IOException {
        Map<String, EditDistanceFunction> methods = new LinkedHashMap<>();
        methods.put("Recursive", algorithms::distanceRecursive);
        for (int n : RECURSIVE_SIZES) {
            for (String inputType : new String[]{"Random", "WorstCase"}) {
                String a = inputType.equals("WorstCase") ? "A".repeat(n) : randomString(n);
                String b = inputType.equals("WorstCase") ? "B".repeat(n) : randomString(n);
                for (Map.Entry<String, EditDistanceFunction> entry : methods.entrySet()) {
                    measureEdit(writer, entry.getKey(), inputType, n, n, entry.getValue(), a, b);
                }
            }
        }
    }

    private void runDynamicProgramming(BufferedWriter writer) throws IOException {
        Map<String, EditDistanceFunction> methods = new LinkedHashMap<>();
        methods.put("Table", algorithms::distanceTable);
        methods.put("TwoRows", algorithms::distanceTwoRows);
        for (int n : DP_SIZES) {
            for (String inputType : new String[]{"Random", "WorstCase"}) {
                String a = inputType.equals("WorstCase") ? "A".repeat(n) : randomString(n);
                String b = inputType.equals("WorstCase") ? "B".repeat(n) : randomString(n);
                for (Map.Entry<String, EditDistanceFunction> entry : methods.entrySet()) {
                    measureEdit(writer, entry.getKey(), inputType, n, n, entry.getValue(), a, b);
                }
            }
        }
    }

    private void measureEdit(BufferedWriter writer, String algorithm, String inputType,
                            int n, int m, EditDistanceFunction method, String a, String b)
            throws IOException {
        for (int warmup = 0; warmup < 2; warmup++) method.distance(a, b); // Excluded from CSV.
        for (int run = 1; run <= MEASURED_RUNS; run++) {
            long start = System.nanoTime();
            method.distance(a, b);
            double timeMs = (System.nanoTime() - start) / 1_000_000.0;
            writeRow(writer, algorithm, inputType, n, m, run, timeMs,
                    algorithms.getEditDistanceOps(), algorithms.getEditDistanceAuxMemoryInts());
        }
    }

    private void runStringSearch(BufferedWriter writer) throws IOException {
        for (int n : SEARCH_SIZES) {
            int m = Math.max(2, n / 4);
            for (String inputType : new String[]{"BestCase", "WorstCase"}) {
                String text;
                String pattern;
                if (inputType.equals("BestCase")) {
                    // Every candidate fails at its first character: one comparison per shift.
                    text = "A".repeat(n);
                    pattern = "B" + "A".repeat(m - 1);
                } else {
                    // Every candidate shares m-1 characters, then fails at the last character.
                    text = "A".repeat(n);
                    pattern = "A".repeat(m - 1) + "B";
                }
                measureSearch(writer, "Naive", inputType, n, m, text, pattern);
                measureSearch(writer, "KMP", inputType, n, m, text, pattern);
            }
        }
    }

    private void measureSearch(BufferedWriter writer, String algorithm, String inputType,
                               int n, int m, String text, String pattern) throws IOException {
        if (algorithm.equals("Naive")) {
            for (int warmup = 0; warmup < 2; warmup++) algorithms.searchNaive(text, pattern); // Warm-up.
        } else {
            for (int warmup = 0; warmup < 2; warmup++) algorithms.searchKMP(text, pattern); // Warm-up.
        }
        for (int run = 1; run <= MEASURED_RUNS; run++) {
            long start = System.nanoTime();
            if (algorithm.equals("Naive")) {
                algorithms.searchNaive(text, pattern);
            } else {
                algorithms.searchKMP(text, pattern);
            }
            double timeMs = (System.nanoTime() - start) / 1_000_000.0;
            writeRow(writer, algorithm, inputType, n, m, run, timeMs,
                    algorithms.getStringSearchOps(), algorithms.getStringSearchAuxMemoryInts());
        }
    }

    private String randomString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }

    private void writeRow(BufferedWriter writer, String algorithm, String inputType,
                          int n, int m, int run, double timeMs, long operations,
                          long extraMemoryInts) throws IOException {
        writer.write(String.format(java.util.Locale.ROOT,
                "%s,%s,%d,%d,%d,%.6f,%d,%d",
                algorithm, inputType, n, m, run, timeMs, operations, extraMemoryInts));
        writer.newLine();
    }
}
