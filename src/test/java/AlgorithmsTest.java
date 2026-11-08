import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {
    private Algorithms algorithms;

    @BeforeEach
    void setUp() {
        algorithms = new Algorithms();
    }

    @Test
    void editDistanceReferenceValuesForAllThreeVersions() {
        assertAllVersions("kitten", "sitting", 3);
        assertAllVersions("flaw", "lawn", 2);
        assertAllVersions("intention", "execution", 5);
        assertAllVersions("", "abc", 3);
        assertAllVersions("abc", "", 3);
        assertAllVersions("abc", "abc", 0);
    }

    @Test
    void editDistanceHandlesBothEmptyStringsAndAsymmetricLengths() {
        assertAllVersions("", "", 0);
        assertAllVersions("a", "longer", 6);
        assertAllVersions("longer", "a", 6);
    }

    @Test
    void allEditDistanceVersionsAgreeOnOneHundredRandomShortPairs() {
        Random random = new Random(20260404L);
        String alphabet = "ABCDE";
        for (int sample = 0; sample < 100; sample++) {
            String a = randomString(random, alphabet, random.nextInt(9));
            String b = randomString(random, alphabet, random.nextInt(9));
            int recursive = algorithms.distanceRecursive(a, b);
            int table = algorithms.distanceTable(a, b);
            int twoRows = algorithms.distanceTwoRows(a, b);
            assertEquals(recursive, table, "Table differs for sample " + sample);
            assertEquals(table, twoRows, "TwoRows differs for sample " + sample);
        }
    }

    @Test
    void lpsReferenceArrays() {
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, algorithms.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, algorithms.lps("ABABCABAB"));
        assertArrayEquals(new int[0], algorithms.lps(""));
    }

    @Test
    void searchReferenceValuesForBothAlgorithmsIncludingOverlaps() {
        String text = "ABABDABACDABABCABAB";
        String pattern = "ABABCABAB";
        assertEquals(List.of(10), algorithms.searchNaive(text, pattern));
        assertEquals(List.of(10), algorithms.searchKMP(text, pattern));

        assertEquals(List.of(0, 1, 2, 3), algorithms.searchNaive("AAAAA", "AA"));
        assertEquals(List.of(0, 1, 2, 3), algorithms.searchKMP("AAAAA", "AA"));
        assertEquals(List.of(0, 2, 4), algorithms.searchNaive("ABABABA", "ABA"));
        assertEquals(List.of(0, 2, 4), algorithms.searchKMP("ABABABA", "ABA"));
    }

    @Test
    void naiveAndKmpAgreeOnOneHundredRandomTexts() {
        Random random = new Random(20260911L);
        String alphabet = "ABC";
        for (int sample = 0; sample < 100; sample++) {
            String text = randomString(random, alphabet, random.nextInt(31));
            String pattern = randomString(random, alphabet, 1 + random.nextInt(8));
            assertEquals(algorithms.searchNaive(text, pattern), algorithms.searchKMP(text, pattern),
                    "Search algorithms differ for sample " + sample);
        }
    }

    @Test
    void searchEdgeCases() {
        assertThrows(IllegalArgumentException.class, () -> algorithms.searchNaive("A", ""));
        assertThrows(IllegalArgumentException.class, () -> algorithms.searchKMP("A", ""));
        assertThrows(IllegalArgumentException.class, () -> algorithms.searchNaive("A", null));
        assertThrows(IllegalArgumentException.class, () -> algorithms.searchKMP("A", null));
        assertTrue(algorithms.searchNaive("A", "AA").isEmpty());
        assertTrue(algorithms.searchKMP("A", "AA").isEmpty());
        assertTrue(algorithms.searchNaive("", "A").isEmpty());
        assertTrue(algorithms.searchKMP("", "A").isEmpty());
        assertEquals(List.of(0), algorithms.searchNaive("A", "A"));
        assertEquals(List.of(0), algorithms.searchKMP("A", "A"));
    }

    @Test
    void countersAreResetAndMemoryMetricsReflectImplementation() {
        algorithms.distanceTable("AB", "CD");
        assertEquals(9, algorithms.getEditDistanceOps());
        assertEquals(9, algorithms.getEditDistanceAuxMemoryInts());

        algorithms.distanceTwoRows("AB", "CD");
        assertEquals(9, algorithms.getEditDistanceOps());
        assertEquals(6, algorithms.getEditDistanceAuxMemoryInts());

        algorithms.searchKMP("AAAAA", "AA");
        assertTrue(algorithms.getStringSearchOps() > 0);
        assertEquals(2, algorithms.getStringSearchAuxMemoryInts());
    }

    private void assertAllVersions(String a, String b, int expected) {
        assertEquals(expected, algorithms.distanceRecursive(a, b), "Recursive");
        assertEquals(expected, algorithms.distanceTable(a, b), "Table");
        assertEquals(expected, algorithms.distanceTwoRows(a, b), "TwoRows");
    }

    private String randomString(Random random, String alphabet, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return builder.toString();
    }
}
