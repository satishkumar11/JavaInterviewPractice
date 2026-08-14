import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class productofallotherelements {

    public static void main(String[] args) {
        List<Integer> arr = Arrays.asList(1, 2, 3, 4);

        List<Integer> res = findSolution(arr);
        System.out.println(res);
    }

    static List<Integer> findSolution(List<Integer> arr) {

        List<Integer> result = new ArrayList<>();

        int[] arr1 = new int[arr.size()+1];
        int[] arr2 = new int[arr.size()+1];

        arr1[0] = 1;
        for(int i=1; i<=arr.size(); i++) {
            arr1[i] = arr1[i-1] * arr.get(i-1);
        }


        arr2[arr.size()-1] = 1;
        for(int i=arr.size()-2; i>=0; i--) {
            arr2[i] = arr2[i+1]*arr.get(i+1);
        }

        for(int i=0;i<arr.size();i++) {
            result.add(arr1[i]*arr2[i]);
        }

        return result;
    }
}
