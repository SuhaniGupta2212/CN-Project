package decoders;

public class ManchesterDecoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        for (int i = 0; i < levels.length - 1; i += 2) {
            String first = levels[i];
            String second = levels[i + 1];

            if (first.equals("+V") && second.equals("-V"))
                decoded.append("1");
            else if (first.equals("-V") && second.equals("+V"))
                decoded.append("0");
            else
                decoded.append("?");
        }
        return decoded.toString();
    }
}
// manchester is RZ and NRZ-l 
// we take two transitions , is transition is +Vthen-V , replace it with 1 
// and if it si -Vand+V , then replace it with 0