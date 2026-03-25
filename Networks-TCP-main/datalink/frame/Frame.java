package datalink.frame;

/**
 * Represents a Data Link Layer Frame.
 * Structure: [DEST_MAC | SRC_MAC | SEQ_NUM | DATA | CHECKSUM]
 */
public class Frame {
    public String destMAC;
    public String srcMAC;
    public int seqNum;
    public String data;
    public int checksum;
    public boolean isACK;
    public boolean isNAK;
    public int ackNum;

    // Data frame constructor
    public Frame(String destMAC, String srcMAC, int seqNum, String data) {
        this.destMAC = destMAC;
        this.srcMAC  = srcMAC;
        this.seqNum  = seqNum;
        this.data    = data;
        this.checksum = computeChecksum(data);
        this.isACK = false;
        this.isNAK = false;
    }

    // ACK/NAK constructor
    public Frame(String destMAC, String srcMAC, int ackNum, boolean isACK, boolean isNAK) {
        this.destMAC = destMAC;
        this.srcMAC  = srcMAC;
        this.ackNum  = ackNum;
        this.isACK   = isACK;
        this.isNAK   = isNAK;
        this.data    = "";
        this.checksum = 0;
    }

    // Simple 8-bit checksum: XOR of all bytes in the data string
    public static int computeChecksum(String data) {
        int cs = 0;
        for (char c : data.toCharArray()) cs ^= (int) c;
        return cs & 0xFF;
    }

    public boolean isCorrupted() {
        return computeChecksum(this.data) != this.checksum;
    }

    // Simulate corruption by flipping one bit in checksum
    public void corrupt() {
        this.checksum ^= 0xFF;
    }

    @Override
    public String toString() {
        if (isACK) return String.format("[ACK  | seq=%d | %s -> %s]", ackNum, srcMAC, destMAC);
        if (isNAK) return String.format("[NAK  | seq=%d | %s -> %s]", ackNum, srcMAC, destMAC);
        return String.format("[FRAME| seq=%-2d | %s -> %s | data=\"%s\" | chk=0x%02X]",
                seqNum, srcMAC, destMAC, data, checksum);
    }
}
