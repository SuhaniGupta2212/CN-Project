package datalink;

import datalink.error.ChecksumControl;
import datalink.frame.Frame;
import java.util.*;

/**
 * Data-Link-Layer aware end station.
 * Each node has a MAC address and an inbox of frames.
 */
public class DLLNode {
    public final String name;
    public final String mac;
    // inbox: frames delivered to this node (simulates NIC buffer)
    private final List<Frame> inbox = new ArrayList<>();

    public DLLNode(String name, String mac) {
        this.name = name;
        this.mac  = mac;
    }

    /** Called by Switch/Bridge when forwarding a frame to this node */
 public void receive(Frame f) {

    // Accept only if:
    // 1. It is meant for this node
    // 2. OR it is broadcast
    if (!this.mac.equals(f.destMAC) &&
        !f.destMAC.equals("FF:FF:FF:FF:FF:FF")) {
        return; // ignore silently
    }

    System.out.println("\n[RECEIVER " + name + "] Frame arrived");

    // ✔ Checksum verification at RECEIVER (correct place)
    boolean ok = ChecksumControl.verify(f);

    if (ok) {
    System.out.println("[RECEIVER " + name + "] ✔ Checksum OK");

    inbox.add(f);   // ✅ ADD THIS LINE

    System.out.println(name + " received: " + f.data);
}
    else {
        System.out.println("[RECEIVER " + name + "] ❌ Corrupted → Dropped");
    }
}
    /** Pull all received frames (clears the buffer) */
    public List<Frame> drainInbox() {
        List<Frame> copy = new ArrayList<>(inbox);
        inbox.clear();
        return copy;
    }

    public boolean hasFrames() { return !inbox.isEmpty(); }

    @Override
    public String toString() { return name + "(" + mac + ")"; }
}
