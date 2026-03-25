package decoders;

public class AMIDecoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        for (String level : levels) {
            if (level.equals("0"))
                decoded.append("0");
            else if (level.equals("+V") || level.equals("-V"))
                decoded.append("1");
            else
                decoded.append("?");
        }
        return decoded.toString();
    }
}

// AMI is simple 0 pe 0 
// 1 pe inversion
// first i split , then decode , then convert it into string 