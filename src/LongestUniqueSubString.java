public class LongestUniqueSubString {

    public static void main(String[] args) {
        int result = longestString("geeksforgeeks");
        System.out.println(result);

        System.out.println("YO");

        int result1 = optimized("geeksforgeeks");
        System.out.println(result1);
    }

    static int longestString(String str) {
        int n = str.length();
        int maxLength = 1;
        for(int i=0; i<n; i++) {

            boolean[] visited = new boolean[26];
            for(int j=i; j<n; j++) {
                if(visited[str.charAt(j) - 'a']) {
                    break;
                } else {
                    maxLength = Math.max(maxLength, j-i+1);
                    visited[str.charAt(j) - 'a'] = true;
                }
            }
        }
        return maxLength;
    }

    static int optimized(String s) {

        int left = 0;
        int right = 0;
        int maxLength = 1;
        boolean[] visited = new boolean[26];

        while(right < s.length()) {

            while(visited[s.charAt(right)-'a']) {
                visited[s.charAt(left)-'a'] = false;
                left++;
            }

            visited[s.charAt(right)-'a'] = true;
            maxLength = Math.max(maxLength, right-left+1);
            right++;
        }
        return maxLength;
    }
}
