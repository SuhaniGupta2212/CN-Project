package datalink.flowcontrol;

import datalink.error.ChecksumControl;
import datalink.frame.Frame;
import java.util.*;

/**
 * FINAL Go-Back-N Sliding Window Protocol (Correct + Demonstration Ready)
 */
public class GoBackN {

    private final int windowSize;
    private final double errorRate;
    private final Random rng;

    public GoBackN(int windowSize, double errorRate) {
        this.windowSize = windowSize;
        this.errorRate  = errorRate;
        this.rng        = new Random(99); // fixed seed for reproducible output
    }

    public void send(List<String> chunks,
                     String srcName,  String srcMAC,
                     String destName, String destMAC) {

        System.out.printf("\n  ┌─────────────────────────────────────────────────────────┐\n");
        System.out.printf("  │  GO-BACK-N  |  Window Size = %-3d  |  Error Rate = %.0f%% │\n",
                windowSize, errorRate * 100);
        System.out.printf("  │  Sender: %-10s   Receiver: %-10s             │\n", srcName, destName);
        System.out.printf("  └─────────────────────────────────────────────────────────┘\n");

        int total = chunks.size();

        int base = 0;        // first unACKed frame
        int nextSeq = 0;     // next frame to send
        int expected = 0;    // receiver expected seq

        Frame[] frames = new Frame[total];

        // Create all frames
        for (int i = 0; i < total; i++) {
            frames[i] = new Frame(destMAC, srcMAC, i, chunks.get(i));
        }

        while (base < total) {

            // =========================
            // SENDER: SEND WINDOW
            // =========================
            while (nextSeq < base + windowSize && nextSeq < total) {
                Frame f = frames[nextSeq];

                boolean corrupt = rng.nextDouble() < errorRate;

                if (corrupt) {
                    f.corrupt();
                    System.out.printf("  [SENDER ] ❌ Sending CORRUPTED %s\n", f);
                } else {
                    System.out.printf("  [SENDER ] 📤 Sending %s\n", f);
                }

                nextSeq++;
            }

            System.out.println();

            // =========================
            // RECEIVER: PROCESS ONE BY ONE
            // =========================
            boolean errorOccurred = false;

            for (int i = base; i < nextSeq; i++) {

                Frame f = frames[i];

                System.out.printf("  [RECEIVER] 📥 Received %s\n", f);
                ChecksumControl.printVerification(f);

                // ✅ Correct frame
                if (!f.isCorrupted() && f.seqNum == expected) {

                    System.out.printf("  [RECEIVER] ✅ Accepted seq=%d -> ACK %d\n",
                            f.seqNum, f.seqNum);

                    Frame ack = new Frame(srcMAC, destMAC, f.seqNum, true, false);
                    System.out.printf("  [RECEIVER] 📤 %s\n\n", ack);

                    expected++;
                    base++;
                }

                // ❌ Error or out-of-order
                else {

                    System.out.printf("  [RECEIVER] ❌ Error at seq=%d -> sending ACK %d\n",
                            f.seqNum, expected - 1);

                    Frame ack = new Frame(srcMAC, destMAC, expected - 1, true, false);
                    System.out.printf("  [RECEIVER] 📤 %s\n", ack);

                    System.out.printf("  [SENDER ] ⏳ Timeout -> Go-Back to seq=%d\n\n", base);

                    // Reset frames from base (simulate retransmission)
                    for (int j = base; j < total; j++) {
                        frames[j] = new Frame(destMAC, srcMAC, j, chunks.get(j));
                    }

                    nextSeq = base;
                    errorOccurred = true;
                    break;
                }
            }

            if (!errorOccurred) {
                System.out.println("  [WINDOW ] Sliding window forward...\n");
            }
        }

        System.out.printf("\n  [GBN] ✅ All %d frames successfully delivered to %s\n",
                total, destName);
    }
}