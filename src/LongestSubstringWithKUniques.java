public class LongestSubstringWithKUniques {

    public static void main(String[] args) {
        int ans = findResult("aabacbebebe", 3);
        System.out.println(ans);
    }

    static int findResult(String str, int k) {

        int maxLength = -1;
        int left = 0;
        int right = 0;
        int[] visited = new int[26];
        for(int i=0; i<26; i++) visited[i] = 0;

        int distinct = 0;

        while(right < str.length()) {

            if(visited[str.charAt(right)-'a'] != 0) {
                visited[str.charAt(right)-'a'] += 1;
            } else {
                visited[str.charAt(right)-'a'] = 1;
                distinct++;
            }

            while (distinct > k) {
                visited[str.charAt(left)-'a'] -=1;
                left++;

                distinct = 0;
                for(int i=0;i<26;i++) {
                    if(visited[i] > 0) distinct++;
                }
            }

            if(distinct == k) maxLength = Math.max(maxLength, right-left+1);
            right++;
        }



        return maxLength;
    }
}
