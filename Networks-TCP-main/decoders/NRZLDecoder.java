package decoders;

public class NRZLDecoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        for (String level : levels) {
            if (level.equals("+V"))
                decoded.append("1");
            else if (level.equals("-V"))
                decoded.append("0");
            else
                decoded.append("?");
        }
        return decoded.toString();
    }
}
// nrz-l is simple 
// 0 -> -V
// 1 -> +V 
//  so decoding it is also simple , if it is +V replace and append 1 and if it is -V replace and append 0.