package datalink;

import datalink.frame.Frame;
import java.util.*;

/**
 * Layer-2 Switch with Address Learning.
 */
public class Switch {

    public final String name;

    // port → node
    private final Map<Integer, DLLNode> ports = new LinkedHashMap<>();

    // MAC → port (learning table)
    private final Map<String, Integer> macTable = new LinkedHashMap<>();

    private int totalFramesForwarded = 0;
    private int totalBroadcasts = 0;

    public Switch(String name) {
        this.name = name;
    }

    // ================================
    // CONNECT DEVICE
    // ================================
    public int connect(DLLNode node) {
        int port = ports.size() + 1;
        ports.put(port, node);

        System.out.printf("  [SWITCH %s] Port %-2d ← connected to %s%n",
                name, port, node.name);

        return port;
    }

    // ================================
    // MAC TABLE
    // ================================
    public void printMACTable() {
        System.out.println("\n=== MAC TABLE ===");

        if (macTable.isEmpty()) {
            System.out.println("(empty)");
            return;
        }

        for (Map.Entry<String, Integer> e : macTable.entrySet()) {
            DLLNode node = ports.get(e.getValue());
            System.out.println(e.getKey() + " → Port " + e.getValue()
                    + " (" + node.name + ")");
        }
    }

    // ================================
    // DOMAIN SUMMARY
    // ================================
    public void printDomainSummary() {
        System.out.println("\n=== DOMAIN SUMMARY ===");
        System.out.println("Broadcast Domains: 1");
        System.out.println("Collision Domains: " + ports.size());
    }

    // ================================
    // CORE LOGIC
    // ================================
    public void processFrame(Frame f, DLLNode sender) {

        int inPort = getPort(sender);

        System.out.printf("\n  [SWITCH %s] 📥 Frame received on Port %d: %s%n",
                name, inPort, f);

        // ================================
        // LEARNING
        // ================================
        if (!macTable.containsKey(f.srcMAC)) {
            macTable.put(f.srcMAC, inPort);

            System.out.printf("  [SWITCH %s] 📚 Learned: %s → Port %d%n",
                    name, f.srcMAC, inPort);
        }

        // ================================
        // BROADCAST
        // ================================
        if (f.destMAC.equals("FF:FF:FF:FF:FF:FF")) {

            System.out.printf("  [SWITCH %s] 📢 Broadcast → flooding%n", name);

            for (Map.Entry<Integer, DLLNode> e : ports.entrySet()) {
                if (e.getKey() != inPort) {

                    System.out.printf("  [SWITCH %s] ➡️  Forwarded to Port %d (%s)%n",
                            name, e.getKey(), e.getValue().name);

                    e.getValue().receive(f);   // ✅ AFTER print
                }
            }

            totalBroadcasts++;
            return;
        }

        // ================================
        // UNICAST
        // ================================
        Integer destPort = macTable.get(f.destMAC);

        // UNKNOWN → FLOOD
        if (destPort == null) {

            System.out.printf("  [SWITCH %s] ❓ Unknown dest %s → flooding%n",
                    name, f.destMAC);

            for (Map.Entry<Integer, DLLNode> e : ports.entrySet()) {
                if (e.getKey() != inPort) {

                    System.out.printf("  [SWITCH %s] ➡️  Forwarded (flood) to Port %d (%s)%n",
                            name, e.getKey(), e.getValue().name);

                    e.getValue().receive(f);   // ✅ AFTER print
                }
            }
        }

        // SAME PORT → DROP
        else if (destPort == inPort) {

            System.out.printf("  [SWITCH %s] 🚫 Same port → frame dropped%n", name);
        }

        // KNOWN → DIRECT UNICAST
        else {

            DLLNode destNode = ports.get(destPort);

            System.out.printf("  [SWITCH %s] ✅ Forwarded to Port %d (%s)%n",
                    name, destPort, destNode.name);

            destNode.receive(f);   // ✅ AFTER print

            totalFramesForwarded++;
        }
    }

    // ================================
    // HELPER
    // ================================
    private int getPort(DLLNode node) {
        for (Map.Entry<Integer, DLLNode> e : ports.entrySet()) {
            if (e.getValue() == node) return e.getKey();
        }
        return -1;
    }
}