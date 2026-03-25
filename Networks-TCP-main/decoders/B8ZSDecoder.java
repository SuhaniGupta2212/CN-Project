package decoders;

public class B8ZSDecoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        int zeroCount = 0;

        for (String level : levels) {
            if (level.equals("0")) {
                zeroCount++;
                decoded.append("0");
            } else {
                zeroCount = 0;
                decoded.append("1");
            }

            if (decoded.length() >= 8) {
                String last8 = decoded.substring(decoded.length() - 8);
                if (last8.equals("00000000")) {
                    decoded.replace(decoded.length() - 8, decoded.length(), "00000000");
                }
            }
        }
        return decoded.toString();
    }
}
