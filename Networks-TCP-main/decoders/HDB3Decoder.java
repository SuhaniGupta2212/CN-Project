package decoders;

public class HDB3Decoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        int zeroCount = 0;

        for (String level : levels) {
            if (level.equals("0")) {
                zeroCount++;
                decoded.append("0");
            } else {
                if (zeroCount == 4) {
                    decoded.replace(decoded.length() - 3, decoded.length(), "0000");
                }
                zeroCount = 0;
                decoded.append("1");
            }
        }
        return decoded.toString();
    }
}
