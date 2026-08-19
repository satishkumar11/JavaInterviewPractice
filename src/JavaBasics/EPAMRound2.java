package JavaBasics;

import java.util.*;
import java.util.stream.Collectors;

public class EPAMRound2 {

    public static void main(String[] args) {
        List<String> strArr = Arrays.asList(
                "1,2,3",
                "3,4,5",
                "6,abc,7",
                "8,9,10",
                "10,2,4"
        );

        Set<Integer> result = new TreeSet<>(Collections.reverseOrder());
        Set<Integer> result1 = new HashSet<>();

        for (String str : strArr) {
            String[] parts = str.split(",");

            for (String part : parts) {
                try {
                    int num = Integer.parseInt(part.trim());
                    result.add(num);
                    result1.add(num);
                } catch (NumberFormatException e) {
                    // Ignore "abc"
                }
            }
        }

        System.out.println("TreeSet: " + result);

        List<Integer> op = new ArrayList<>(result1);
        Collections.sort(op, Collections.reverseOrder());

        System.out.println("HashSet + Sort: " + op);


        Set<Integer> set = strArr.stream()
                .flatMap(s->Arrays.stream(s.split(",")))
                .filter(s->s.matches("-?\\d+"))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        System.out.println(set);

        List<Integer> order = set.stream()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
        System.out.println(order);
    }
}