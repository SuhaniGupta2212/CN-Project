package datalink.mac;

import java.util.*;

/**
 * CSMA/CD (Carrier Sense Multiple Access with Collision Detection) Simulator.
 *
 * Behaviour simulated:
 *  1. Sense the channel before transmitting (Carrier Sense).
 *  2. If channel is BUSY → wait (random backoff).
 *  3. If channel is FREE → begin transmission.
 *  4. While transmitting, detect collision (another station also transmitting).
 *  5. On collision → send JAM signal, stop, apply Binary Exponential Backoff (BEB).
 *  6. After max retries → drop frame.
 *
 * The shared medium is modelled as a static boolean `channelBusy`
 * plus a Set of "currently transmitting" nodes so we can detect collisions.
 */
public class CSMACD {

    private static boolean channelBusy = false;
    private static final Set<String> transmitting = new HashSet<>();
    private static final Random rng = new Random();

    private static final int MAX_RETRIES = 10;
    private static final int TRANSMISSION_TIME = 5; // simulated time units

    public static void transmit(String sender, String data) {

        int attempt = 0;

        while (attempt < MAX_RETRIES) {

            // 🔹 STEP 1: Sense channel
            System.out.println(sender + " sensing channel...");

            while (channelBusy) {
                System.out.println(sender + " found channel BUSY, waiting...");
                sleep(200);
            }

            // 🔹 STEP 2: Start transmission
            System.out.println(sender + " found channel IDLE, starting transmission...");
            channelBusy = true;
            transmitting.add(sender);

            boolean collision = false;

            // 🔹 STEP 3: Monitor DURING transmission
            for (int t = 0; t < TRANSMISSION_TIME; t++) {

                sleep(100); // simulate time passing

                if (transmitting.size() > 1) {
                    collision = true;
                    break;
                }
            }

            // 🔹 STEP 4: Collision handling
            if (collision) {
                System.out.println("⚡ COLLISION detected by " + sender + "! Sending JAM signal...");
               
                // JAM signal simulation
                sleep(100);

                // Abort transmission
                transmitting.remove(sender);
                channelBusy = false;

                attempt++;

                // 🔹 STEP 5: Binary Exponential Backoff
                int k = Math.min(attempt, 10);
                int backoffSlots = (int) Math.pow(2, k);
                int waitTime = rng.nextInt(backoffSlots);

                System.out.println(sender + " backing off for " + waitTime + " slots...");

                for (int i = 0; i < waitTime; i++) {
                    sleep(100);
                }

                // 🔹 STEP 6: Retry (loop continues)
            }
            else {
                // 🔹 SUCCESS
                System.out.println(sender + " successfully transmitted: " + data);

                transmitting.remove(sender);
                channelBusy = false;
                return;
            }
        }

        System.out.println(sender + " failed after max retries.");
    }

    // 🔹 Simulate another device transmitting (to force collision)
    public static void simulateConcurrentTransmitter(String sender) {
        transmitting.add(sender);
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}