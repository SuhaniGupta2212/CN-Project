import datalink.*;
import datalink.error.*;
import datalink.flowcontrol.*;
import datalink.frame.*;
import datalink.mac.*;
import encoders.Manchester;
import encoders.NRZI;
import encoders.NRZL;
import java.util.*;
import main.*;
import physical.*;
import topology.*;

public class DLLMain {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        printBanner();

        while (true) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║   INTERACTIVE SIMULATOR      ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║ 1. Build Topology            ║");
            System.out.println("║ 2. Exit                      ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Choice: ");

            int ch = readInt();

            if (ch == 1) {
                buildTopology();
            } else {
                break;
            }
        }
    }

    // =========================================================
    // BUILDER
    // =========================================================
    static void buildTopology() {

    System.out.println("\nChoose device:");
    System.out.println("1. Switch");
    System.out.println("2. Bridge");
    System.out.println("3. Hub (Physical Layer)");

    System.out.print("Choice: ");

    int choice = readInt();

    if (choice == 1) buildSwitch();
    else if (choice == 2) buildBridge();
    else if (choice == 3) runPhysicalLayer();   // 🔥 NEW
}

static void runPhysicalLayer() {

    Scanner sc = new Scanner(System.in);

    // =========================
    // CREATE DEVICES
    // =========================
    System.out.print("Enter number of stations: ");
    int n = readInt();

    EndStation[] stations = new EndStation[n];

    for(int i = 0; i < n; i++){
        System.out.print("Enter name of station " + (i+1) + ": ");
        String name = sc.nextLine().trim();
        if(name.isEmpty()) name = "D" + (i+1);
        stations[i] = new EndStation(name);
    }

    // =========================
    // TOPOLOGY
    // =========================
    System.out.println("\nChoose Topology:");
    System.out.println("1. Direct Connection");
    System.out.println("2. Star (Hub)");

    int topoChoice = readInt();

    // =========================
    // SENDER / RECEIVER
    // =========================
    System.out.print("\nEnter sender station: ");
    String senderName = sc.nextLine();

    System.out.print("Enter destination station: ");
    String destName = sc.nextLine();

    // =========================
    // DATA INPUT
    // =========================
    System.out.print("\nEnter data: ");
    String data = sc.nextLine();

    System.out.println("\nOriginal Data: " + data);
    System.out.println("Bit Representation: " + Utils.stringToBits(data));

    // =========================
    // ENCODING
    // =========================
    System.out.println("\nSelect Encoding:");
    System.out.println("1. NRZ-L");
    System.out.println("2. NRZ-I");
    System.out.println("3. Manchester");

    int encChoice = readInt();

    String encoded = "";

    switch (encChoice) {
        case 1 -> encoded = new NRZL().encode(data);
        case 2 -> encoded = new NRZI().encode(data);
        case 3 -> encoded = new Manchester().encode(data);
    }

    System.out.println("\nEncoded Signal:");
    System.out.println(encoded);

    WaveformDisplay.showWaveform(encoded);
// =========================
// CREATE FRAME
// =========================
Data d = new Data(senderName, destName, encoded);

// =========================
// FIND SENDER
// =========================
EndStation sender = null;

for(EndStation s : stations){
    if(s.stationName.equals(senderName)){
        sender = s;
        break;
    }
}

if(sender == null){
    System.out.println("Sender not found!");
    return;
}

// =========================
// TRANSMISSION
// =========================
System.out.println("\n--- DATA FLOW ---");
System.out.println("Data → Bits → Encoded Signal → Transmission");

System.out.println("\n--- Transmission Start ---");

// 🔥 ALWAYS create hub (needed by your constructors)
Hub hub = new Hub("HUB1");

if(topoChoice == 1){

    // ✅ DIRECT TOPOLOGY (FIXED)
    DirectTopology direct = new DirectTopology(hub);
    direct.connectDevices(stations);
    direct.displayTopology(stations);

    // simulate direct send
    System.out.println("\n" + senderName + " sends data directly to " + destName);

} else {

    // ✅ STAR TOPOLOGY (FIXED)
    StarTopology st = new StarTopology(hub);
    st.connectDevices(stations);
    st.displayTopology(stations);

    // simulate hub behavior
    System.out.println("\n" + senderName + " sends data to HUB");
    System.out.println("HUB broadcasts to all devices:");

    for (EndStation s : stations) {
        if (!s.stationName.equals(senderName)) {
            System.out.println("→ " + s.stationName + " receives data");
        }
    }
}

System.out.println("--- Transmission End ---");}

    // =========================================================
    // SWITCH BUILDER (WITH HUB + TOPOLOGY)
    // =========================================================
    static void buildSwitch() {

        System.out.print("\nEnter Switch Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) name = "SW1";

        Switch sw = new Switch(name);

        Map<DLLNode, List<DLLNode>> hubMap = new HashMap<>();
        List<DLLNode> directDevices = new ArrayList<>();

        // ================================
        // HUBS
        // ================================
        System.out.print("\nHow many hubs? ");
        int hubCount = readInt();

        for (int h = 0; h < hubCount; h++) {

            System.out.print("\nEnter hub name: ");
            String hubName = sc.nextLine().trim();
            if (hubName.isEmpty()) hubName = "HUB" + (h + 1);

            Hub hub = new Hub(hubName);

            // 🔥 TOPOLOGY CHOICE
            System.out.println("Choose topology:");
            System.out.println("1. Star");
            System.out.println("2. Direct");
            int topo = readInt();

            System.out.print("How many devices in this hub? ");
            int n = readInt();

            EndStation[] stations = new EndStation[n];
            List<DLLNode> devices = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                System.out.print("Device name: ");
                String dname = sc.nextLine().trim();
                if (dname.isEmpty()) dname = hubName + "_D" + (i + 1);

                stations[i] = new EndStation(dname);

                DLLNode node = new DLLNode(dname, generateMAC(h * 10 + i + 1));
                devices.add(node);
            }

            // 🔥 APPLY TOPOLOGY
            if (topo == 1) {
                StarTopology star = new StarTopology(hub);
                star.connectDevices(stations);
                star.displayTopology(stations);
            } else {
                DirectTopology direct = new DirectTopology(hub);
                direct.connectDevices(stations);
                direct.displayTopology(stations);
            }

            DLLNode hubPort = new DLLNode(hubName + "-PORT", generateMAC(100 + h));
            sw.connect(hubPort);

            hubMap.put(hubPort, devices);

            System.out.println("✅ " + hubName + " connected to switch.");
        }

        // ================================
        // DIRECT DEVICES
        // ================================
        System.out.print("\nDirect devices to switch? (yes/no): ");
        if (sc.nextLine().equalsIgnoreCase("yes")) {

            System.out.print("How many? ");
            int n = readInt();

            for (int i = 0; i < n; i++) {
                System.out.print("Device name: ");
                String dname = sc.nextLine().trim();
                if (dname.isEmpty()) dname = "D" + (i + 1);

                DLLNode node = new DLLNode(dname, generateMAC(200 + i));
                sw.connect(node);
                directDevices.add(node);
            }
        }

        System.out.println("\n✅ Network created successfully.");

        runNetwork(sw, hubMap, directDevices, hubCount);
    }

    // =========================================================
    // BRIDGE BUILDER (SAME STYLE MENU)
    // =========================================================
static void buildBridge() {

    Bridge br = new Bridge("BR1");

    Map<DLLNode, List<DLLNode>> hubMap = new HashMap<>();
    List<DLLNode> directDevices = new ArrayList<>();

    // =========================
    // SIDE A
    // =========================
    System.out.println("\n--- CONFIGURE SIDE A ---");
    setupBridgeSide(br, hubMap, directDevices, true);

    // =========================
    // SIDE B
    // =========================
    System.out.println("\n--- CONFIGURE SIDE B ---");
    setupBridgeSide(br, hubMap, directDevices, false);

    // =========================
    // COLLECT ALL DEVICES
    // =========================
    List<DLLNode> all = new ArrayList<>();
    hubMap.values().forEach(all::addAll);
    all.addAll(directDevices);

    DLLNode[] nodes = all.toArray(new DLLNode[0]);

    // =========================
    // MENU (UNCHANGED)
    // =========================
    boolean run = true;

    while (run) {
        System.out.println("\n--- BRIDGE MENU ---");
        System.out.println("1. Send Unicast");
        System.out.println("2. Broadcast");
        System.out.println("3. CSMA/CD Demo");
        System.out.println("4. Go-Back-N");
        System.out.println("5. Show MAC Table");
        System.out.println("6. Domain Summary");
        System.out.println("7. Exit");

        int ch = readInt();

        switch (ch) {
            case 1 -> {
                DLLNode src = pickNode(nodes, "source");
                DLLNode dst = pickNode(nodes, "destination");

                Frame f = new Frame(dst.mac, src.mac, 0, "DATA");
                ChecksumControl.attachChecksum(f);
                br.processFrame(f, src);
            }
            case 2 -> {
                DLLNode src = pickNode(nodes, "source");

                Frame f = new Frame("FF:FF:FF:FF:FF:FF", src.mac, 0, "BROADCAST");
                ChecksumControl.attachChecksum(f);
                br.processFrame(f, src);
            }
            case 3 -> csmacdDemo(nodes);
            case 4 -> goBackNDemo(nodes);
            case 5 -> br.printMACTable();
            case 6 -> br.printDomainSummary();
            case 7 -> run = false;
        }
    }
}    // NETWORK RUN
  

static void setupBridgeSide(Bridge br,
                           Map<DLLNode, List<DLLNode>> hubMap,
                           List<DLLNode> directDevices,
                           boolean isSideA) {

    System.out.println("1. Connect Hub");
    System.out.println("2. Direct Devices");
    int choice = readInt();

    if (choice == 1) {

        // =========================
        // HUB
        // =========================
        System.out.print("Enter hub name: ");
        String hubName = sc.nextLine().trim();
        if (hubName.isEmpty()) hubName = "HUB";

        Hub hub = new Hub(hubName);

        System.out.println("Choose topology:");
        System.out.println("1. Star");
        System.out.println("2. Direct");
        int topo = readInt();

        System.out.print("How many devices in hub? ");
        int n = readInt();

        EndStation[] stations = new EndStation[n];
        List<DLLNode> devices = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.print("Device name: ");
            String dname = sc.nextLine().trim();
            if (dname.isEmpty()) dname = hubName + "_D" + (i + 1);

            stations[i] = new EndStation(dname);

            DLLNode node = new DLLNode(dname, generateMAC(i + 1));
            devices.add(node);
        }

        // TOPOLOGY DISPLAY
        if (topo == 1) {
            StarTopology star = new StarTopology(hub);
            star.connectDevices(stations);
            star.displayTopology(stations);
        } else {
            DirectTopology direct = new DirectTopology(hub);
            direct.connectDevices(stations);
            direct.displayTopology(stations);
        }

        DLLNode port = new DLLNode(hubName + "-PORT", generateMAC(100));

        if (isSideA) br.connectToSideA(port);
        else br.connectToSideB(port);

        hubMap.put(port, devices);

    } else {

        // =========================
        // DIRECT DEVICES
        // =========================
        System.out.print("How many devices? ");
        int n = readInt();

        for (int i = 0; i < n; i++) {
            System.out.print("Device name: ");
            String dname = sc.nextLine().trim();
            if (dname.isEmpty()) dname = "D" + (i + 1);

            DLLNode node = new DLLNode(dname, generateMAC(200 + i));

            if (isSideA) br.connectToSideA(node);
            else br.connectToSideB(node);

            directDevices.add(node);
        }
    }
}// =========================================================
    static void runNetwork(Switch sw,
                           Map<DLLNode, List<DLLNode>> hubMap,
                           List<DLLNode> directDevices,
                           int hubCount) {

        List<DLLNode> all = new ArrayList<>();
        hubMap.values().forEach(all::addAll);
        all.addAll(directDevices);

        DLLNode[] nodes = all.toArray(new DLLNode[0]);

        switchMenu(sw, nodes, hubCount);
    }

    // =========================================================
    // SWITCH MENU (UNCHANGED)
    // =========================================================
    static void switchMenu(Switch sw, DLLNode[] nodes, int hubCount) {

        boolean run = true;

        while (run) {
            System.out.println("\n--- SWITCH MENU ---");
            System.out.println("1. Send Unicast");
            System.out.println("2. Broadcast");
            System.out.println("3. CSMA/CD Demo");
            System.out.println("4. Go-Back-N");
            System.out.println("5. Show MAC Table");
            System.out.println("6. Domain Summary");
            System.out.println("7. Exit");

            int ch = readInt();

            switch (ch) {
                case 1 -> sendUnicast(nodes, sw);
                case 2 -> sendBroadcast(nodes, sw);
                case 3 -> csmacdDemo(nodes);
                case 4 -> goBackNDemo(nodes);
                case 5 -> sw.printMACTable();
                case 6 -> printDomains(nodes, hubCount);
                case 7 -> run = false;
            }
        }
    }

    // =========================================================
    // DOMAIN LOGIC
    // =========================================================
    static void printDomains(DLLNode[] nodes, int hubCount) {

        int collisionDomains = (hubCount == 0) ? nodes.length : hubCount;

        System.out.println("\n📊 DOMAIN SUMMARY");
        System.out.println("Broadcast Domains: 1");
        System.out.println("Collision Domains: " + collisionDomains);
    }

    // =========================================================
    // EXISTING FUNCTIONS
    // =========================================================

    static void sendUnicast(DLLNode[] nodes, Switch sw) {
        DLLNode src = pickNode(nodes, "source");
        DLLNode dst = pickNode(nodes, "destination");

        Frame f = new Frame(dst.mac, src.mac, 0, "DATA");
        ChecksumControl.attachChecksum(f);
        sw.processFrame(f, src);
    }

    static void sendBroadcast(DLLNode[] nodes, Switch sw) {
        DLLNode src = pickNode(nodes, "source");

        Frame f = new Frame("FF:FF:FF:FF:FF:FF", src.mac, 0, "BROADCAST");
        ChecksumControl.attachChecksum(f);
        sw.processFrame(f, src);
    }

    static void csmacdDemo(DLLNode[] nodes) {
        DLLNode a = pickNode(nodes, "node A");
        DLLNode b = pickNode(nodes, "node B");

        CSMACD.reset();
        CSMACD.simulateConcurrentTransmitter(b.mac);
        CSMACD.transmit(a.name, a.mac);
        CSMACD.releaseConcurrentTransmitter(b.mac);
    }

    static void goBackNDemo(DLLNode[] nodes) {
        DLLNode src = pickNode(nodes, "sender");
        DLLNode dst = pickNode(nodes, "receiver");

        GoBackN gbn = new GoBackN(3, 0.2);
        gbn.send(List.of("HELLO"), src.name, src.mac, dst.name, dst.mac);
    }

    // =========================================================
    // UTIL
    // =========================================================
    static DLLNode[] createNodes(int n) {
        DLLNode[] nodes = new DLLNode[n];
        for (int i = 0; i < n; i++)
            nodes[i] = new DLLNode("D" + (i + 1), generateMAC(i + 1));
        return nodes;
    }

    static DLLNode pickNode(DLLNode[] nodes, String role) {
        System.out.println("\nSelect " + role + ":");
        for (int i = 0; i < nodes.length; i++)
            System.out.println((i + 1) + ". " + nodes[i].name);
        return nodes[readInt() - 1];
    }

    static String generateMAC(int i) {
        return String.format("00:0A:00:00:00:%02X", i);
    }

    static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.print("Enter number: ");
            }
        }
    }

    static void printBanner() {
        System.out.println("\n===== DATA LINK LAYER SIMULATOR =====");
    }
}