package datalink.flowcontrol;

import datalink.error.ChecksumControl;
import datalink.frame.Frame;
import java.util.*;

/**
 * Go-Back-N (GBN) Sliding Window Protocol.
 *
 * Sender:
 *   - Maintains a window of size W.
 *   - Sends frames up to window size without waiting for individual ACKs.
 *   - On receiving NAK(n) or timeout: retransmits from frame n onwards.
 *
 * Receiver:
 *   - Only accepts in-order frames.
 *   - Sends ACK for the last correctly received in-order frame.
 *   - Sends NAK if a frame is received out-of-order or corrupted.
 *
 * The entire exchange is simulated synchronously and printed step-by-step.
 */
public class GoBackN {

    private final int windowSize;
    private final double errorRate;   // probability 0.0–1.0 that a frame gets corrupted
    private final Random rng;

    public GoBackN(int windowSize, double errorRate) {
        this.windowSize = windowSize;
        this.errorRate  = errorRate;
        this.rng        = new Random(99);
    }

    /**
     * Simulate sending all chunks from srcMAC → destMAC.
     *
     * @param chunks     list of data strings to send (each = one frame payload)
     * @param srcName    sender display name
     * @param srcMAC     sender MAC
     * @param destName   receiver display name
     * @param destMAC    receiver MAC
     */
    public void send(List<String> chunks,
                     String srcName,  String srcMAC,
                     String destName, String destMAC) {

        System.out.printf("%n  ┌─────────────────────────────────────────────────────────┐%n");
        System.out.printf("  │  GO-BACK-N  |  Window Size = %-3d  |  Error Rate = %.0f%%%s│%n",
                windowSize, errorRate * 100, "  ");
        System.out.printf("  │  Sender: %-10s   Receiver: %-10s             │%n", srcName, destName);
        System.out.printf("  └─────────────────────────────────────────────────────────┘%n");

        int totalFrames = chunks.size();
        int base     = 0;   // first unACKed frame
        int nextSeq  = 0;   // next frame to send
        int expectedSeq = 0; // receiver's expected seq

        // Build all frames up front
        Frame[] frames = new Frame[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            frames[i] = new Frame(destMAC, srcMAC, i, chunks.get(i));
        }

        Map<Integer, Boolean> acked = new HashMap<>();

        while (base < totalFrames) {

            // === SENDER: Fill the window ===
            while (nextSeq < totalFrames && nextSeq < base + windowSize) {
                Frame f = frames[nextSeq];

                // Maybe corrupt
                boolean corrupt = rng.nextDouble() < errorRate;
                if (corrupt) {
                    f.corrupt();
                    System.out.printf("  [GBN SENDER ] 📤 Sending (CORRUPTED) %s%n", f);
                } else {
                    System.out.printf("  [GBN SENDER ] 📤 Sending             %s%n", f);
                }
                nextSeq++;
            }

            // === RECEIVER: Process frames base..nextSeq-1 ===
            boolean needRetransmit = false;
            int retransmitFrom = base;

            for (int i = base; i < nextSeq; i++) {
                Frame f = frames[i];
                System.out.printf("  [GBN RECEIVER] 📥 Received %s%n", f);
                ChecksumControl.printVerification(f);

                if (f.isCorrupted()) {
                    System.out.printf("  [GBN RECEIVER] ❌ Frame seq=%d corrupted → sending NAK%n", f.seqNum);
                    Frame nak = new Frame(srcMAC, destMAC, expectedSeq, false, true);
                    System.out.printf("  [GBN RECEIVER] 📤 %s%n", nak);
                    needRetransmit = true;
                    retransmitFrom = expectedSeq;
                    break;
                }

                if (f.seqNum != expectedSeq) {
                    System.out.printf("  [GBN RECEIVER] ⚠️  Out-of-order: expected seq=%d, got seq=%d → NAK%n",
                            expectedSeq, f.seqNum);
                    Frame nak = new Frame(srcMAC, destMAC, expectedSeq, false, true);
                    System.out.printf("  [GBN RECEIVER] 📤 %s%n", nak);
                    needRetransmit = true;
                    retransmitFrom = expectedSeq;
                    break;
                }

                // Frame accepted
                System.out.printf("  [GBN RECEIVER] ✅ Frame seq=%d accepted → sending ACK%n", f.seqNum);
                Frame ack = new Frame(srcMAC, destMAC, f.seqNum, true, false);
                System.out.printf("  [GBN RECEIVER] 📤 %s%n", ack);
                acked.put(f.seqNum, true);
                expectedSeq++;
            }

            if (needRetransmit) {
                System.out.printf("  [GBN SENDER ] 🔁 GO-BACK to seq=%d, retransmitting window...%n%n",
                        retransmitFrom);
                // Reset: re-build frames from retransmitFrom (they may have been corrupted)
                for (int i = retransmitFrom; i < totalFrames; i++) {
                    frames[i] = new Frame(destMAC, srcMAC, i, chunks.get(i));
                }
                nextSeq = retransmitFrom;
            } else {
                // Advance base past all ACKed frames
                while (base < totalFrames && acked.getOrDefault(base, false)) {
                    base++;
                }
            }
        }

        System.out.printf("%n  [GBN] ✅ All %d frames successfully delivered to %s%n", totalFrames, destName);
    }
}
