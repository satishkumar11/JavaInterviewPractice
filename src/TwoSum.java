import java.util.Arrays;

public class TwoSum {

    public static void main(String[] args) {
        int [] arr = {0, -1, 2, -3, 1};
        int target = -5;

        Arrays.sort(arr);
        if(checkValueExists(arr, target)){
            System.out.println("Exists");
        } else {
            System.out.println("Not Exists");
        }
    }

    static boolean checkValueExists(int[] arr, int target) {

        for(int i=0; i<arr.length; i++) {
            int tempTarget = target - arr[i];
            boolean exists = binarySearch(arr, tempTarget);

            if(exists) return true;
        }
        return false;
    }

    static boolean binarySearch(int []arr, int target) {
        int left = 0;
        int right = arr.length-1;

        while(left<=right) {
            int mid = (left+right)/2;

            if(arr[mid] == target) return true;
            if(arr[mid] > target) right = mid-1;
            else left = mid+1;
        }
        return false;
    }
}
