import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Problem:
 *
 * Return a Map<String, Long> containing the frequency of each word.
 *
 * Requirements:
 * 1. Comparison should be case-insensitive.
 *    Example: "java", "JAVA", "Java" should be treated as the same word.
 *
 * 2. Include only words that appear more than once.
 *
 * 3. Sort the result by frequency in descending order.
 *
 * Expected output:
 *
 * {java=3, python=2, go=2}
 */
public class FrequencyMapFilteredStream {

    public static void main(String[] args) {

        List<String> input = Arrays.asList(
                "java",
                "Python",
                "JAVA",
                "go",
                "python",
                "Go",
                "java",
                "rust"
        );

        /*
         * STEP 1: Convert every word to lowercase.
         *
         * This makes the comparison case-insensitive.
         *
         * Example:
         *
         * "java"   -> "java"
         * "JAVA"   -> "java"
         * "Python" -> "python"
         * "Go"     -> "go"
         *
         * After this step:
         *
         * [java, python, java, go, python, go, java, rust]
         */
        Map<String, Long> res = input.stream()
                .map(String::toLowerCase)

                /*
                 * STEP 2: Count the occurrences of each word.
                 *
                 * groupingBy() groups identical words together.
                 *
                 * counting() counts how many elements are present
                 * in each group.
                 *
                 * IMPORTANT:
                 * Collectors.counting() returns Long, not Integer.
                 *
                 * Result at this stage:
                 *
                 * {
                 *     java=3,
                 *     python=2,
                 *     go=2,
                 *     rust=1
                 * }
                 */
                .collect(Collectors.groupingBy(
                        s -> s,
                        Collectors.counting()
                ))

                /*
                 * STEP 3: Convert the Map into a Stream of Map.Entry.
                 *
                 * A Map cannot directly be streamed.
                 *
                 * entrySet() gives us:
                 *
                 * java   -> 3
                 * python -> 2
                 * go     -> 2
                 * rust   -> 1
                 *
                 * Each element is:
                 *
                 * Map.Entry<String, Long>
                 */
                .entrySet()
                .stream()

                /*
                 * STEP 4: Keep only words whose count is greater than 1.
                 *
                 * This removes "rust" because it appears only once.
                 *
                 * Remaining:
                 *
                 * java   -> 3
                 * python -> 2
                 * go     -> 2
                 */
                .filter(entry -> entry.getValue() > 1)

                /*
                 * STEP 5: Sort the entries by their value (count).
                 *
                 * comparingByValue() means:
                 *     Compare the frequency/count.
                 *
                 * reversed() means:
                 *     Sort in descending order.
                 *
                 * So:
                 *
                 * java   -> 3
                 * python -> 2
                 * go     -> 2
                 *
                 * instead of ascending:
                 *
                 * python -> 2
                 * go     -> 2
                 * java   -> 3
                 */
                .sorted(
                        Map.Entry.<String, Long>
                                        comparingByValue()
                                .reversed()
                )

                /*
                 * STEP 6: Convert the sorted Stream<Entry> back into a Map.
                 *
                 * toMap() takes four arguments:
                 *
                 * 1. Map.Entry::getKey
                 *    -> What should be the Map key?
                 *
                 * 2. Map.Entry::getValue
                 *    -> What should be the Map value?
                 *
                 * 3. (a, b) -> a
                 *    -> What should happen if duplicate keys are found?
                 *       Keep the first value.
                 *
                 * 4. LinkedHashMap::new
                 *    -> Use LinkedHashMap so that insertion order
                 *       (our sorted order) is preserved.
                 *
                 * A normal HashMap does not guarantee insertion order.
                 */
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        /*
         * Final result:
         *
         * {java=3, python=2, go=2}
         */
        System.out.println(res);
    }
}