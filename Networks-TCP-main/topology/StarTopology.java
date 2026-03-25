package topology;

import main.*;
import physical.*;
public class StarTopology {

    Hub hub;

    public StarTopology(Hub hub) {
        this.hub = hub;
    }

    public void connectDevices(EndStation[] stations) {
        for (EndStation s : stations) {
            hub.connect(s);
        }
    }

    public void displayTopology(EndStation[] stations) {
        System.out.println("\n--- STAR TOPOLOGY ---");
        System.out.println("          HUB");
        for (EndStation s : stations) {
            System.out.println("           |");
            System.out.println("         " + s.stationName);
        }
    }

    public void transmit(Data d, EndStation sender) {
        hub.receiveAndTransmit(d, sender);
    }
}