package decoders;

public class DiffManchesterDecoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        if (levels.length < 2) return "";

        String prev = levels[0];

        for (int i = 1; i < levels.length; i += 2) {
            String current = levels[i];
            if (current.equals(prev))
                decoded.append("1");
            else
                decoded.append("0");
            prev = current;
        }
        return decoded.toString();
    }
}
// differential manchester is RZ + NRZ-i ka inversion 
// so if i have it equals to prev , then 1 
// otherwise , i have 0 