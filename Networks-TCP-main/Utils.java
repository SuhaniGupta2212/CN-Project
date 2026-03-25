
public class Utils {

    public static String stringToBits(String input) {
        StringBuilder bits = new StringBuilder();
        for (char c : input.toCharArray()) {
            bits.append(Integer.toBinaryString(c)).append(" ");
        }
        return bits.toString();
    }

    public static boolean isPalindrome(String str) {
        int l = 0, r = str.length() - 1;
        while (l < r) {
            if (str.charAt(l++) != str.charAt(r--)) return false;
        }
        return true;
    }

    public static String palindrome(String s) {
        String longest = "";
        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 1; j <= s.length(); j++) {
                String sub = s.substring(i, j);
                if (isPalindrome(sub) && sub.length() > longest.length()) {
                    longest = sub;
                }
            }
        }
        return longest;
    }
}