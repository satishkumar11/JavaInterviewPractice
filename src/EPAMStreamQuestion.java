import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EPAMStreamQuestion {




    public static void main(String[] args) {

        List<String> strArr = Arrays.asList("1,2,3", "3,4,5", "6,abc,7", "8,9,10", "10,2,4");

//        Output:  [10, 9, 8, 7, 6, 5, 4, 3, 2, 1];

        List<String[]> res = strArr.stream()
                .map(obj -> obj.split(","))
                .collect(Collectors.toList());







//        Move Zero to last of the Array from given array: int[] arr = {0,9,0,8,0,6,5,0,10};
//        Output:  9 8 6 5 10 0 0 0 0




//        int[] arr = {0,9,0,8,0,6,5,0,10};
//
//
//        int i = 0;boardName
//        int j = 1;
//        int n = arr.length;
//
//        while(j<n) {
//            if(arr[i]!=0) {
//                i++;
//                if(i>=j) {
//                    j++;
//                }
//            }
//            else if(arr[j] == 0){
//                j++;
//            } else {
//                int temp = arr[i];
//                arr[i] = arr[j];
//                arr[j] = temp;
//                i++; j++;
//            }
//        }
//
//        for(int k=0;k<n; k++) {
//            System.out.println(arr[k]);
//        }
    }



}
