import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LongestWordsUppercased {


    public static void main(String[] args) {
        List<String> input = Arrays.asList("apple", null, "banana", "kiwi", "cherry", "", "fig");

        int maxLength = input.stream().filter(Objects::nonNull)
                .mapToInt(String::length).max().orElse(0);

        List<String> res = input.stream().filter(i -> i!=null && i.length() == maxLength)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        System.out.println(res);
    }

}
