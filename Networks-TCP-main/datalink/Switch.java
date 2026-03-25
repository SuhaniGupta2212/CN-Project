package datalink;

import datalink.frame.Frame;
import java.util.*;

/**
 * Layer-2 Switch with Address Learning.
 *
 * Behaviours:
 *  • Learns source MAC → port mapping on every received frame.
 *  • Forwards unicast frames only to the known destination port.
 *  • Floods frames whose destination MAC is unknown (or broadcast FF:FF:FF:FF:FF:FF).
 *  • Tracks broadcast domains and collision domains.
 */
public class Switch {

    public final String name;
    // port number → connected DLLNode
    private final Map<Integer, DLLNode> ports = new LinkedHashMap<>();
    // MAC address → port number  (the learning table)
    private final Map<String, Integer> macTable = new LinkedHashMap<>();

    private int totalFramesForwarded = 0;
    private int totalBroadcasts      = 0;
    private int totalCollisionsDomains = 0; // one per port

    public Switch(String name) {
        this.name = name;
    }

    /** Connect a DLLNode to the next available port */
    public int connect(DLLNode node) {
        int port = ports.size() + 1;
        ports.put(port, node);
        System.out.printf("  [SWITCH %s] Port %-2d ← connected to %s%n", name, port, node);
        return port;
    }

    /** Connect another Switch (for inter-switch links) */
    public int connectSwitch(DLLNode pseudoPort) {
        return connect(pseudoPort);
    }

    /** Print the current MAC Address Table */
    public void printMACTable() {
        System.out.printf("%n  ╔══════════════════════════════════╗%n");
        System.out.printf("  ║   Switch %s — MAC Address Table  ║%n", name);
        System.out.printf("  ╠══════════════╦═══════════════════╣%n");
        System.out.printf("  ║    MAC       ║  Port             ║%n");
        System.out.printf("  ╠══════════════╬═══════════════════╣%n");
        if (macTable.isEmpty()) {
            System.out.printf("  ║    (empty)                       ║%n");
        } else {
            macTable.forEach((mac, port) -> {
                DLLNode node = ports.get(port);
                String nodeName = (node != null) ? node.name : "?";
                System.out.printf("  ║  %-12s║  Port %-2d (%s)%n", mac, port, nodeName);
            });
        }
        System.out.printf("  ╚══════════════╩═══════════════════╝%n");
    }

    /** Print domain summary */
    public void printDomainSummary() {
        int numPorts = ports.size();
        System.out.printf("%n  ╔══════════════════════════════════╗%n");
        System.out.printf("  ║  Switch %s — Domain Summary      ║%n", name);
        System.out.printf("  ╠══════════════════════════════════╣%n");
        System.out.printf("  ║  Broadcast Domains  : %-3d         ║%n", 1);
        System.out.printf("  ║  Collision Domains  : %-3d         ║%n", numPorts);
        System.out.printf("  ║  (1 per port — full-duplex)      ║%n");
        System.out.printf("  ╚══════════════════════════════════╝%n");
    }

    /**
     * Core switch logic: receive a frame on a port, learn, then forward.
     * @param incomingFrame  the frame received
     * @param senderNode     the node that sent it (to identify the incoming port)
     */
    public void processFrame(Frame incomingFrame, DLLNode senderNode) {
        int inPort = getPortOf(senderNode);

        System.out.printf("%n  [SWITCH %s] 📥 Frame received on Port %d: %s%n",
                name, inPort, incomingFrame);

        // ADDRESS LEARNING
        if (!macTable.containsKey(incomingFrame.srcMAC)) {
            macTable.put(incomingFrame.srcMAC, inPort);
            System.out.printf("  [SWITCH %s] 📚 Learned: MAC %s → Port %d%n",
                    name, incomingFrame.srcMAC, inPort);
        }

        // BROADCAST
        if (incomingFrame.destMAC.equals("FF:FF:FF:FF:FF:FF")) {
            System.out.printf("  [SWITCH %s] 📢 Broadcast frame — flooding all ports except Port %d%n",
                    name, inPort);
            totalBroadcasts++;
            for (Map.Entry<Integer, DLLNode> e : ports.entrySet()) {
                if (e.getKey() != inPort) {
                    e.getValue().receive(incomingFrame);
                    System.out.printf("  [SWITCH %s] ➡️  Forwarded to Port %d (%s)%n",
                            name, e.getKey(), e.getValue().name);
                }
            }
            return;
        }

        // UNICAST: look up destination
        Integer destPort = macTable.get(incomingFrame.destMAC);

        if (destPort == null) {
            // Unknown MAC → flood
            System.out.printf("  [SWITCH %s] ❓ Unknown dest MAC %s → flooding%n",
                    name, incomingFrame.destMAC);
            for (Map.Entry<Integer, DLLNode> e : ports.entrySet()) {
                if (e.getKey() != inPort) {
                    e.getValue().receive(incomingFrame);
                    System.out.printf("  [SWITCH %s] ➡️  Forwarded (flood) to Port %d (%s)%n",
                            name, e.getKey(), e.getValue().name);
                }
            }
        } else if (destPort == inPort) {
            // Same port — discard (filtering)
            System.out.printf("  [SWITCH %s] 🚫 Source and dest on same port %d — frame filtered%n",
                    name, inPort);
        } else {
            // Forward to known port only
            DLLNode destNode = ports.get(destPort);
            destNode.receive(incomingFrame);
            System.out.printf("  [SWITCH %s] ✅ Forwarded to Port %d (%s)%n",
                    name, destPort, destNode.name);
            totalFramesForwarded++;
        }
    }

    private int getPortOf(DLLNode node) {
        for (Map.Entry<Integer, DLLNode> e : ports.entrySet()) {
            if (e.getValue() == node) return e.getKey();
        }
        return -1;
    }

    public Map<Integer, DLLNode> getPorts() { return ports; }
    public Map<String, Integer>  getMacTable() { return macTable; }
    public int getPortCount() { return ports.size(); }
}
