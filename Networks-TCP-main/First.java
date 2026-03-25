import encoders.*;
import java.util.*;
import main.*;
import physical.*;
import topology.*;
public class First {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // =========================
        // CREATE DEVICES
        // =========================
        System.out.print("Enter number of stations: ");
        int n = sc.nextInt();
        sc.nextLine();

        EndStation[] stations = new EndStation[n];

        for(int i = 0; i < n; i++){
            System.out.print("Enter name of station " + (i+1) + ": ");
            String name = sc.nextLine();
            stations[i] = new EndStation(name);
        }

        // =========================
        // TOPOLOGY
        // =========================
        System.out.println("\nChoose Topology:");
        System.out.println("1. Direct Connection");
        System.out.println("2. Star Topology");

        int topoChoice = sc.nextInt();
        sc.nextLine();

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

        int encChoice = sc.nextInt();
        sc.nextLine();

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

        if(topoChoice == 1){
            DirectTopology dt = new DirectTopology();
            dt.displayTopology(stations);
            dt.transmit(d, stations);

        } else if(topoChoice == 2){
            Hub hub = new Hub();
            StarTopology st = new StarTopology(hub);

            st.connectDevices(stations);
            st.displayTopology(stations);
            st.transmit(d, sender);
        }

        System.out.println("--- Transmission End ---");

        sc.close();
    }
}