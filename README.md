# DAA Assignment 4 — Dynamic Programming and String Algorithms

**Student:** Alikhan Temirkhanov  
**Group:** SE-2516  
**Instructor:** Taubakabyl Nurlybek

## Requirements

- JDK 17 or newer
- Apache Maven 3.8 or newer
- Python 3.9 or newer with `matplotlib` (only needed to regenerate plots)

## Project structure

```text
src/main/java/       Java algorithms and experiment runner
src/test/java/       JUnit 5 tests
results/results.csv  One row for every measured run
results/plots/       PNG charts generated from results.csv
scripts/             Plot-generation script
REPORT.md            Experiment design, results and conclusions
```

## Build and test

Run all JUnit 5 tests:

```bash
mvn clean test
```

## Re-run the experiment

Build the project and run the deterministic experiment:

```bash
mvn -q -DskipTests package
java -cp target/classes ExperimentRunner
```

The runner uses the fixed seed `20261108` and writes five measured runs for every algorithm/input/size combination to `results/results.csv`. It also performs two unrecorded JVM warm-up calls per combination. The operation counts and generated inputs are reproducible; wall-clock times naturally vary between machines and runs.

Regenerate all charts from the CSV:

```bash
python scripts/plot_results.py
```

On Windows, `py scripts\\plot_results.py` can be used instead of `python`.

## Algorithms and counters

- Edit Distance: direct recursion, full DP table, and two-row DP. All three can be called through the common `EditDistanceFunction` signature.
- Naive Search and KMP return all match positions, including overlaps. `lps(String)` is public.
- `getEditDistanceOps()` counts recursive calls or DP cells initialized/computed, depending on the implementation.
- `getStringSearchOps()` counts character comparisons. For KMP, this includes comparisons during LPS construction and during text search.
- `ExtraMemoryInts` counts auxiliary integer cells allocated by the implementation: the full DP table, two DP rows, or KMP's LPS array. It excludes the returned list of match positions and Java object/array headers.

## Git submission

The GitHub repository is:

`https://github.com/AlikhanTemirkhanov777/DAA_Assignment4_Temirkhanov_Alikhan`

The required branch is `main`. A GitHub Actions workflow runs the JUnit 5 tests, regenerates the PNG plots from `results/results.csv`, and creates the `v1.0` tag only after the checks succeed. Verify that the workflow is green and that `v1.0` appears on the repository's **Tags** page before submitting.

Paste the repository URL into Moodle together with the ZIP archive.

## AI-use acknowledgement

AI assistance was used to review the assignment requirements, help improve the implementation and tests, and prepare the experiment/report structure. The student should review the final source, understand the counters and results, and be able to explain the algorithms independently during the defense.
