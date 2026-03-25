package datalink.error;

import datalink.frame.Frame;

/**
 * Error Control using Internet Checksum (XOR-based 8-bit).
 *
 * On send  : compute checksum, embed in frame.
 * On receive: recompute and compare; flag corrupted frames.
 */
public class ChecksumControl {

    public static void attachChecksum(Frame f) {
        f.checksum = Frame.computeChecksum(f.data);
    }

    public static boolean verify(Frame f) {
        return !f.isCorrupted();
    }

    public static void printVerification(Frame f) {
        int recomputed = Frame.computeChecksum(f.data);
        System.out.printf("  [CHECKSUM] Frame seq=%-2d | Embedded=0x%02X | Computed=0x%02X | %s%n",
                f.seqNum, f.checksum, recomputed,
                f.isCorrupted() ? "NO CORRUPTED" : " OK");
    }
}
