import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// Asked in an interview
public class RemoveStringFromListUsingStream {

    public static void main(String[] args) {
        List<String> str = new ArrayList<>();
        str.add("123");
        str.add("-123");
        str.add("a123");
        str.add("123a");
        str.add("abc");
        str.add(null);
        str.add("");


        List<Integer> res = str.stream()
                .filter(s -> s!=null && s.matches("-?\\d+"))
                .map(Integer::parseInt)
                        .collect(Collectors.toList());

        System.out.println(res);
    }
}
