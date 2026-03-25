package decoders;

public class NRZIDecoder {
    public String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        String[] levels = encoded.trim().split("\\s+");

        if (levels.length == 0) return "";

        String prev = levels[0];
        decoded.append("1"); 

        for (int i = 1; i < levels.length; i++) {
            if (levels[i].equals(prev))
                decoded.append("0");
            else
                decoded.append("1");
            prev = levels[i];
        }
        return decoded.toString();
    }
}

// NRZ-I was inversion at 1 and no inversion at 0 
// first we need split it , assumming first bit is 1 , 
// then if it is same as previous then zero , if it is changing then 1 
