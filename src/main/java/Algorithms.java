import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementations used in Assignment 4.
 * Counters are reset at the beginning of each public algorithm call.
 */
public class Algorithms {
    private long editDistanceOps;
    private long editDistanceAuxMemoryInts;
    private long stringSearchOps;
    private long stringSearchAuxMemoryInts;

    public long getEditDistanceOps() {
        return editDistanceOps;
    }

    /** Number of auxiliary int cells allocated by the last edit-distance call. */
    public long getEditDistanceAuxMemoryInts() {
        return editDistanceAuxMemoryInts;
    }

    public long getStringSearchOps() {
        return stringSearchOps;
    }

    /** Number of auxiliary int cells allocated by the last string-search call. */
    public long getStringSearchAuxMemoryInts() {
        return stringSearchAuxMemoryInts;
    }

    /*
     * State: D(i,j) is the edit distance between prefixes a[0..i-1] and b[0..j-1].
     * Transition: matching last characters use D(i-1,j-1); otherwise use 1 + min(delete, insert, replace).
     * Base case: D(0,j)=j and D(i,0)=i.
     * This version intentionally uses direct recursion without memoization.
     */
    public int distanceRecursive(String a, String b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        editDistanceOps = 0;
        editDistanceAuxMemoryInts = 0; // No DP array is allocated; recursion uses the call stack.
        return recursiveDistance(a, b, a.length(), b.length());
    }

    private int recursiveDistance(String a, String b, int i, int j) {
        editDistanceOps++; // Count every recursive state/call, including base cases.
        if (i == 0) return j;
        if (j == 0) return i;
        if (a.charAt(i - 1) == b.charAt(j - 1)) {
            return recursiveDistance(a, b, i - 1, j - 1);
        }
        int deletion = recursiveDistance(a, b, i - 1, j);
        int insertion = recursiveDistance(a, b, i, j - 1);
        int replacement = recursiveDistance(a, b, i - 1, j - 1);
        return 1 + Math.min(deletion, Math.min(insertion, replacement));
    }

    /*
     * State: D(i,j) is the edit distance between prefixes a[0..i-1] and b[0..j-1].
     * Transition: matching last characters use D(i-1,j-1); otherwise use 1 + min(delete, insert, replace).
     * Base case: D(0,j)=j and D(i,0)=i.
     */
    public int distanceTable(String a, String b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        editDistanceOps = 0;
        int m = a.length();
        int n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        editDistanceAuxMemoryInts = (long) (m + 1) * (n + 1);

        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                editDistanceOps++; // One DP table cell is initialized or computed.
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],
                            Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }
        return dp[m][n];
    }

    /*
     * State: previous[j] and current[j] store two consecutive DP rows.
     * Transition: current[j] = previous[j-1] on a match, otherwise 1 + min(delete, insert, replace).
     * Base case: previous[j]=j for the empty prefix of a; current[0]=i for each new row.
     */
    public int distanceTwoRows(String a, String b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        editDistanceOps = 0;

        // Keep the shorter string on the columns to achieve O(min(m,n)) auxiliary memory.
        if (b.length() > a.length()) {
            String temporary = a;
            a = b;
            b = temporary;
        }
        int m = a.length();
        int n = b.length();
        int[] previous = new int[n + 1];
        int[] current = new int[n + 1];
        editDistanceAuxMemoryInts = 2L * (n + 1);

        for (int j = 0; j <= n; j++) {
            previous[j] = j;
            editDistanceOps++;
        }
        for (int i = 1; i <= m; i++) {
            current[0] = i;
            editDistanceOps++;
            for (int j = 1; j <= n; j++) {
                editDistanceOps++;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    current[j] = previous[j - 1];
                } else {
                    current[j] = 1 + Math.min(
                            previous[j],
                            Math.min(current[j - 1], previous[j - 1]));
                }
            }
            int[] temporary = previous;
            previous = current;
            current = temporary;
        }
        return previous[n];
    }

    /**
     * Naive string search. Counts each text-pattern character comparison.
     * Returns all matches, including overlapping ones, in ascending order.
     */
    public List<Integer> searchNaive(String text, String pattern) {
        stringSearchOps = 0;
        stringSearchAuxMemoryInts = 0;
        validateSearchArguments(text, pattern);

        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m > n) return matches;

        for (int start = 0; start <= n - m; start++) {
            int j = 0;
            while (j < m) {
                stringSearchOps++;
                if (text.charAt(start + j) != pattern.charAt(j)) {
                    break;
                }
                j++;
            }
            if (j == m) matches.add(start);
        }
        return matches;
    }

    /** Public LPS method, as required so that tests can call it directly. */
    public int[] lps(String pattern) {
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern must not be null.");
        }
        stringSearchOps = 0;
        stringSearchAuxMemoryInts = pattern.length();
        return buildLps(pattern);
    }

    private int[] buildLps(String pattern) {
        int[] prefix = new int[pattern.length()];
        int length = 0;
        int i = 1;
        while (i < pattern.length()) {
            stringSearchOps++; // One prefix-function character comparison.
            if (pattern.charAt(i) == pattern.charAt(length)) {
                length++;
                prefix[i] = length;
                i++;
            } else if (length > 0) {
                length = prefix[length - 1];
            } else {
                prefix[i] = 0;
                i++;
            }
        }
        return prefix;
    }

    /**
     * KMP search. Counts character comparisons in both LPS construction and text search.
     * The text index never moves backwards; after a match, LPS preserves overlap support.
     */
    public List<Integer> searchKMP(String text, String pattern) {
        stringSearchOps = 0;
        stringSearchAuxMemoryInts = 0;
        validateSearchArguments(text, pattern);

        List<Integer> matches = new ArrayList<>();
        int n = text.length();
        int m = pattern.length();
        if (m > n) return matches;

        int[] prefix = buildLps(pattern);
        stringSearchAuxMemoryInts = prefix.length;
        int i = 0;
        int j = 0;
        while (i < n) {
            stringSearchOps++; // One text-pattern character comparison.
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    matches.add(i - j);
                    j = prefix[j - 1]; // Continue from the longest proper prefix/suffix.
                }
            } else if (j > 0) {
                j = prefix[j - 1]; // Do not move i backwards.
            } else {
                i++;
            }
        }
        return matches;
    }

    private void validateSearchArguments(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern must not be null or empty.");
        }
        Objects.requireNonNull(text, "Text must not be null.");
    }
}
