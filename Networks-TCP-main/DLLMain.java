import datalink.*;
import datalink.error.*;
import datalink.flowcontrol.*;
import datalink.frame.*;
import datalink.mac.*;

import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║       ITL351 Network Simulator — Data Link Layer                 ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║  Layer-2 features implemented:                                   ║
 * ║  • Layer-2 devices   : Switch (with MAC learning), Bridge        ║
 * ║  • Error Control     : Checksum (XOR-based Internet Checksum)    ║
 * ║  • Access Control    : CSMA/CD with Binary Exponential Backoff   ║
 * ║  • Flow Control      : Go-Back-N sliding window                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class DLLMain {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        printBanner();

        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║         MAIN MENU — Layer 2          ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. Single Switch — 5 Devices        ║");
            System.out.println("║     (access + flow + error demo)     ║");
            System.out.println("║  2. Two Hubs + Switch (10 Devices)   ║");
            System.out.println("║  3. Bridge Demo                      ║");
            System.out.println("║  4. Custom Topology (you design it)  ║");
            System.out.println("║  5. Exit                             ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Choose option: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> scenarioSingleSwitch();
                case 2 -> scenarioTwoHubsSwitch();
                case 3 -> scenarioBridge();
                case 4 -> scenarioCustom();
                case 5 -> running = false;
                default -> System.out.println("Invalid choice.");
            }
        }
        System.out.println("\nSimulator exited. Goodbye!");
    }

    // ─────────────────────────────────────────────────────────────
    // SCENARIO 1: One Switch, 5 End Devices
    // ─────────────────────────────────────────────────────────────
    static void scenarioSingleSwitch() {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  Scenario: Single Switch, 5 Devices");
        System.out.println("════════════════════════════════════════");

        // Create nodes
        DLLNode[] nodes = createNodes(5);
        Switch sw = new Switch("SW1");
        for (DLLNode n : nodes) sw.connect(n);

        printTopology(sw, nodes);

        boolean go = true;
        while (go) {
            System.out.println("\n  ┌──────────────────────────────────┐");
            System.out.println("  │  Switch Demo Menu                │");
            System.out.println("  ├──────────────────────────────────┤");
            System.out.println("  │  1. Send unicast frame           │");
            System.out.println("  │  2. Send broadcast               │");
            System.out.println("  │  3. CSMA/CD collision demo       │");
            System.out.println("  │  4. Go-Back-N flow control demo  │");
            System.out.println("  │  5. Show MAC table               │");
            System.out.println("  │  6. Show domain summary          │");
            System.out.println("  │  7. Back to main menu            │");
            System.out.println("  └──────────────────────────────────┘");
            System.out.print("  Choose: ");

            switch (readInt()) {
                case 1 -> sendUnicast(nodes, sw);
                case 2 -> sendBroadcast(nodes, sw);
                case 3 -> csmacdDemo(nodes);
                case 4 -> goBackNDemo(nodes);
                case 5 -> sw.printMACTable();
                case 6 -> sw.printDomainSummary();
                case 7 -> go = false;
                default -> System.out.println("  Invalid.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SCENARIO 2: Two Hubs + One Switch → 10 devices
    // ─────────────────────────────────────────────────────────────
    static void scenarioTwoHubsSwitch() {
        System.out.println("\n════════════════════════════════════════════════");
        System.out.println("  Scenario: 2 Hubs + Switch (10 end devices)");
        System.out.println("════════════════════════════════════════════════");

        System.out.println("\n  Creating Hub-1 segment (5 devices)...");
        DLLNode[] seg1 = createNodesBatch(1, 5);

        System.out.println("\n  Creating Hub-2 segment (5 devices)...");
        DLLNode[] seg2 = createNodesBatch(6, 5);

        // Hub proxies — we model each hub as one DLLNode port on the switch
        // (physically a hub floods within its segment; we simulate that)
        DLLNode hub1Port = new DLLNode("Hub1-Port", "AA:AA:AA:AA:AA:01");
        DLLNode hub2Port = new DLLNode("Hub2-Port", "BB:BB:BB:BB:BB:01");

        Switch sw = new Switch("SW-Core");
        sw.connect(hub1Port);
        sw.connect(hub2Port);

        System.out.println("\n  ╔═══════════════════════════════════════════════════╗");
        System.out.println("  ║         Two-Hub + Switch Topology                 ║");
        System.out.println("  ╠═══════════════════════════════════════════════════╣");
        System.out.println("  ║  D1─┐                         ┌─D6               ║");
        System.out.println("  ║  D2─┤                         ├─D7               ║");
        System.out.println("  ║  D3─┤HUB1────[SW-Core]────HUB2├─D8               ║");
        System.out.println("  ║  D4─┤                         ├─D9               ║");
        System.out.println("  ║  D5─┘                         └─D10              ║");
        System.out.println("  ╚═══════════════════════════════════════════════════╝");

        // Domain info
        System.out.println("\n  ╔═══════════════════════════════════════╗");
        System.out.println("  ║   Domain Analysis                     ║");
        System.out.println("  ╠═══════════════════════════════════════╣");
        System.out.println("  ║  Broadcast Domains : 1 (switch = 1)  ║");
        System.out.println("  ║  Collision Domains :                  ║");
        System.out.println("  ║    Hub-1 segment   = 1 collision dom  ║");
        System.out.println("  ║    Hub-2 segment   = 1 collision dom  ║");
        System.out.println("  ║    Total           = 2 collision doms ║");
        System.out.println("  ║  (Each hub = shared medium = 1 CD)   ║");
        System.out.println("  ╚═══════════════════════════════════════╝");

        boolean go = true;
        while (go) {
            printAllNodes(seg1, seg2);
            System.out.println("\n  1. Send frame from Seg-1 to Seg-2");
            System.out.println("  2. Send frame within Seg-1");
            System.out.println("  3. CSMA/CD demo within Hub segment");
            System.out.println("  4. Go-Back-N between two nodes");
            System.out.println("  5. Show switch MAC table");
            System.out.println("  6. Back");
            System.out.print("  Choose: ");

            switch (readInt()) {
                case 1 -> crossSegmentSend(seg1, seg2, sw, hub1Port, hub2Port);
                case 2 -> withinSegmentSend(seg1);
                case 3 -> csmacdDemo(seg1);
                case 4 -> goBackNDemoNodes(seg1[0], seg2[0]);
                case 5 -> sw.printMACTable();
                case 6 -> go = false;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SCENARIO 3: Bridge Demo
    // ─────────────────────────────────────────────────────────────
    static void scenarioBridge() {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  Scenario: Bridge Demo");
        System.out.println("════════════════════════════════════════");

        System.out.println("\n  Creating Side-A (3 devices)...");
        DLLNode[] sideA = createNodesBatch(1, 3);
        System.out.println("  Creating Side-B (3 devices)...");
        DLLNode[] sideB = createNodesBatch(4, 3);

        Bridge bridge = new Bridge("BR1");
        for (DLLNode n : sideA) bridge.connectToSideA(n);
        for (DLLNode n : sideB) bridge.connectToSideB(n);

        System.out.println("\n  Side-A: D1 — D2 — D3   [BR1]   D4 — D5 — D6 :Side-B");
        bridge.printDomainSummary();

        boolean go = true;
        while (go) {
            System.out.println("\n  1. Send frame (cross bridge)");
            System.out.println("  2. Send frame (same side)");
            System.out.println("  3. Show MAC table");
            System.out.println("  4. Back");
            System.out.print("  Choose: ");
            switch (readInt()) {
                case 1 -> {
                    DLLNode src = pickNode(concat(sideA, sideB), "source");
                    DLLNode dst = pickNode(concat(sideA, sideB), "destination");
                    System.out.print("  Enter data: ");
                    String data = sc.nextLine().trim();
                    if (data.isEmpty()) data = "Hello!";
                    Frame f = new Frame(dst.mac, src.mac, 0, data);
                    ChecksumControl.printVerification(f);
                    bridge.processFrame(f, src);
                    drainAndPrint(concat(sideA, sideB));
                }
                case 2 -> {
                    System.out.println("  (Send within Side-A)");
                    Frame f = new Frame(sideA[1].mac, sideA[0].mac, 0, "intra-segment");
                    bridge.processFrame(f, sideA[0]);
                }
                case 3 -> bridge.printMACTable();
                case 4 -> go = false;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SCENARIO 4: Custom topology
    // ─────────────────────────────────────────────────────────────
    static void scenarioCustom() {
        System.out.println("\n════════════════════════════════════════");
        System.out.println("  Custom Topology Builder");
        System.out.println("════════════════════════════════════════");

        System.out.print("\n  How many end devices? ");
        int n = readInt();
        DLLNode[] nodes = createNodes(n);

        System.out.println("\n  Connect to:");
        System.out.println("  1. Switch");
        System.out.println("  2. Bridge (splits into two halves)");
        System.out.print("  Choose: ");
        int dev = readInt();

        if (dev == 1) {
            System.out.print("  Switch name: ");
            String swName = sc.nextLine().trim();
            if (swName.isEmpty()) swName = "SW1";
            Switch sw = new Switch(swName);
            for (DLLNode node : nodes) sw.connect(node);
            printTopology(sw, nodes);

            System.out.print("\n  Go-Back-N window size: ");
            int ws = readInt();
            System.out.print("  Error rate (0-100)%: ");
            int er = readInt();

            DLLNode src = pickNode(nodes, "source");
            DLLNode dst = pickNode(nodes, "destination");

            System.out.print("  Enter message to send: ");
            String msg = sc.nextLine().trim();
            if (msg.isEmpty()) msg = "Hello Network!";

            // CSMA/CD first
            CSMACD.reset();
            boolean ok = CSMACD.transmit(src.name, src.mac);
            if (!ok) { System.out.println("  Frame dropped by CSMA/CD."); return; }

            // Split message into frames
            List<String> chunks = splitIntoChunks(msg, 4);
            System.out.printf("\n  Message split into %d frame(s)%n", chunks.size());

            GoBackN gbn = new GoBackN(ws, er / 100.0);
            gbn.send(chunks, src.name, src.mac, dst.name, dst.mac);

            // Send final frame via switch
            Frame final_f = new Frame(dst.mac, src.mac, 0, msg);
            sw.processFrame(final_f, src);
            sw.printMACTable();
            sw.printDomainSummary();

        } else {
            int half = n / 2;
            Bridge br = new Bridge("BR1");
            for (int i = 0; i < half; i++) br.connectToSideA(nodes[i]);
            for (int i = half; i < n; i++) br.connectToSideB(nodes[i]);
            br.printMACTable();
            br.printDomainSummary();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helper: interactive unicast
    // ─────────────────────────────────────────────────────────────
    static void sendUnicast(DLLNode[] nodes, Switch sw) {
        DLLNode src = pickNode(nodes, "source");
        DLLNode dst = pickNode(nodes, "destination");
        System.out.print("  Enter data to send: ");
        String data = sc.nextLine().trim();
        if (data.isEmpty()) data = "Hello!";

        // CSMA/CD channel access
        CSMACD.reset();
        boolean ok = CSMACD.transmit(src.name, src.mac);
        if (!ok) { System.out.println("  ❌ Frame dropped (CSMA/CD max retries)."); return; }

        Frame f = new Frame(dst.mac, src.mac, 0, data);
        System.out.println("\n  [CHECKSUM] Verifying frame before transmission:");
        ChecksumControl.printVerification(f);

        sw.processFrame(f, src);
        drainAndPrint(nodes);
    }

    static void sendBroadcast(DLLNode[] nodes, Switch sw) {
        DLLNode src = pickNode(nodes, "source");
        System.out.print("  Enter broadcast data: ");
        String data = sc.nextLine().trim();
        if (data.isEmpty()) data = "BROADCAST!";

        CSMACD.reset();
        CSMACD.transmit(src.name, src.mac);

        Frame f = new Frame("FF:FF:FF:FF:FF:FF", src.mac, 0, data);
        ChecksumControl.printVerification(f);
        sw.processFrame(f, src);
        drainAndPrint(nodes);
    }

    static void csmacdDemo(DLLNode[] nodes) {
        System.out.println("\n  ── CSMA/CD Collision Demo ──");
        System.out.println("  Two nodes will try to transmit simultaneously.\n");

        DLLNode a = pickNode(nodes, "first transmitter");
        DLLNode b = pickNode(nodes, "second transmitter (will collide)");

        CSMACD.reset();
        // Simulate b grabbing the channel at same time
        CSMACD.simulateConcurrentTransmitter(b.mac);
        System.out.printf("  [SIM] %s and %s start transmitting simultaneously...%n", a.name, b.name);
        boolean okA = CSMACD.transmit(a.name, a.mac);
        CSMACD.releaseConcurrentTransmitter(b.mac);

        System.out.println("\n  Now " + b.name + " attempts alone:");
        CSMACD.reset();
        CSMACD.transmit(b.name, b.mac);
    }

    static void goBackNDemo(DLLNode[] nodes) {
        System.out.println("\n  ── Go-Back-N Demo ──");
        DLLNode src = pickNode(nodes, "sender");
        DLLNode dst = pickNode(nodes, "receiver");
        System.out.print("  Enter message to send (will be split into frames): ");
        String msg = sc.nextLine().trim();
        if (msg.isEmpty()) msg = "Hello from GBN Protocol";

        System.out.print("  Window size (1-8): ");
        int ws = Math.max(1, Math.min(8, readInt()));
        System.out.print("  Simulated error rate (0-100)%: ");
        int er = Math.max(0, Math.min(100, readInt()));

        List<String> chunks = splitIntoChunks(msg, 5);
        System.out.printf("\n  Message → %d frame(s) of max 5 chars each%n", chunks.size());

        GoBackN gbn = new GoBackN(ws, er / 100.0);
        gbn.send(chunks, src.name, src.mac, dst.name, dst.mac);
    }

    static void goBackNDemoNodes(DLLNode src, DLLNode dst) {
        System.out.printf("\n  Go-Back-N between %s and %s%n", src.name, dst.name);
        System.out.print("  Message: ");
        String msg = sc.nextLine().trim();
        if (msg.isEmpty()) msg = "Cross-segment data";
        System.out.print("  Window size: ");
        int ws = readInt();
        System.out.print("  Error rate %: ");
        int er = readInt();

        GoBackN gbn = new GoBackN(ws, er / 100.0);
        gbn.send(splitIntoChunks(msg, 5), src.name, src.mac, dst.name, dst.mac);
    }

    static void crossSegmentSend(DLLNode[] seg1, DLLNode[] seg2, Switch sw,
                                  DLLNode hub1Port, DLLNode hub2Port) {
        System.out.println("\n  Sending from Seg-1 node to Seg-2 node...");
        DLLNode src = pickNode(seg1, "source (Seg-1)");
        DLLNode dst = pickNode(seg2, "destination (Seg-2)");
        System.out.print("  Data: ");
        String data = sc.nextLine().trim();
        if (data.isEmpty()) data = "Inter-segment packet";

        CSMACD.reset();
        CSMACD.transmit(src.name, src.mac);

        // Hub floods within seg1 → arrives at hub1Port
        System.out.printf("\n  [HUB-1] Flooding within segment-1 (received from %s)%n", src.name);
        Frame frame = new Frame(dst.mac, src.mac, 0, data);
        ChecksumControl.printVerification(frame);

        // Switch learns hub1Port side
        sw.processFrame(frame, hub1Port);

        // Frame arrives at hub2Port side, hub2 floods to seg2
        System.out.printf("\n  [HUB-2] Flooding within segment-2...%n");
        for (DLLNode n : seg2) {
            n.receive(frame);
            System.out.printf("  [HUB-2] → %s%n", n.name);
        }
        drainAndPrint(seg2);
    }

    static void withinSegmentSend(DLLNode[] seg) {
        DLLNode src = pickNode(seg, "source");
        DLLNode dst = pickNode(seg, "destination");
        System.out.print("  Data: ");
        String data = sc.nextLine().trim();
        if (data.isEmpty()) data = "Intra-segment";
        System.out.printf("\n  [HUB] Broadcasting within segment: \"%s\"%n", data);
        Frame f = new Frame(dst.mac, src.mac, 0, data);
        for (DLLNode n : seg) {
            if (n != src) { n.receive(f); System.out.printf("  [HUB] → %s%n", n.name); }
        }
        drainAndPrint(seg);
    }

    // ─────────────────────────────────────────────────────────────
    // Utility methods
    // ─────────────────────────────────────────────────────────────
    static DLLNode[] createNodes(int count) {
        DLLNode[] nodes = new DLLNode[count];
        System.out.println("\n  Creating " + count + " end devices:");
        for (int i = 0; i < count; i++) {
            System.out.printf("  Device %d — name: ", i + 1);
            String name = sc.nextLine().trim();
            if (name.isEmpty()) name = "D" + (i + 1);
            String mac = generateMAC(i + 1);
            nodes[i] = new DLLNode(name, mac);
            System.out.printf("  → %s  MAC: %s%n", name, mac);
        }
        return nodes;
    }

    static DLLNode[] createNodesBatch(int startIdx, int count) {
        DLLNode[] nodes = new DLLNode[count];
        for (int i = 0; i < count; i++) {
            String name = "D" + (startIdx + i);
            String mac  = generateMAC(startIdx + i);
            nodes[i] = new DLLNode(name, mac);
            System.out.printf("  Created %s  MAC: %s%n", name, mac);
        }
        return nodes;
    }

    static String generateMAC(int idx) {
        return String.format("00:0A:00:00:00:%02X", idx);
    }

    static DLLNode pickNode(DLLNode[] nodes, String role) {
        System.out.printf("\n  Select %s:%n", role);
        for (int i = 0; i < nodes.length; i++) {
            System.out.printf("  %d. %s (%s)%n", i + 1, nodes[i].name, nodes[i].mac);
        }
        System.out.print("  Enter number: ");
        int idx = Math.max(1, Math.min(nodes.length, readInt())) - 1;
        return nodes[idx];
    }

    static void drainAndPrint(DLLNode[] nodes) {
        System.out.println("\n  --- Frame Delivery Report ---");
        for (DLLNode n : nodes) {
            List<Frame> frames = n.drainInbox();
            if (!frames.isEmpty()) {
                for (Frame f : frames) {
                    System.out.printf("  📬 %-12s received: \"%s\"%n", n.name, f.data);
                }
            }
        }
    }

    static void printTopology(Switch sw, DLLNode[] nodes) {
        System.out.println("\n  ── Topology ──");
        System.out.printf("  [ Switch: %s ]%n", sw.name);
        for (DLLNode n : nodes) {
            System.out.printf("       │%n");
            System.out.printf("  [ %-10s  %s ]%n", n.name, n.mac);
        }
    }

    static void printAllNodes(DLLNode[] seg1, DLLNode[] seg2) {
        System.out.println("\n  Seg-1 nodes: ");
        for (DLLNode n : seg1) System.out.printf("    %s (%s)%n", n.name, n.mac);
        System.out.println("  Seg-2 nodes: ");
        for (DLLNode n : seg2) System.out.printf("    %s (%s)%n", n.name, n.mac);
    }

    static List<String> splitIntoChunks(String s, int size) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < s.length(); i += size) {
            chunks.add(s.substring(i, Math.min(i + size, s.length())));
        }
        return chunks;
    }

    static DLLNode[] concat(DLLNode[] a, DLLNode[] b) {
        DLLNode[] result = new DLLNode[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    static int readInt() {
        while (true) {
            try {
                String line = sc.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("  Please enter a number: ");
            }
        }
    }

    static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         ITL351 — Network Simulator                           ║");
        System.out.println("║         Data Link Layer (Layer 2)                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Error Control  : Checksum (XOR Internet Checksum)           ║");
        System.out.println("║  Access Control : CSMA/CD + Binary Exponential Backoff       ║");
        System.out.println("║  Flow Control   : Go-Back-N Sliding Window                   ║");
        System.out.println("║  Devices        : Switch (MAC learning), Bridge              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
