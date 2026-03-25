package topology;

import main.*;
import physical.*;

public class DirectTopology {

    Hub hub;

    public DirectTopology(Hub hub) {
        this.hub = hub;
    }

    public void connectDevices(EndStation[] stations) {
        for (EndStation s : stations) {
            hub.connect(s);
        }
    }

    public void displayTopology(EndStation[] stations) {
        System.out.println("\n--- DIRECT TOPOLOGY ---");

        if (stations.length >= 2) {
            System.out.println(stations[0].stationName + " -------- " + stations[1].stationName);
        } else {
            System.out.println("Not enough devices for direct connection");
        }
    }

    public void transmit(Data d, EndStation[] stations) {

        for (EndStation s : stations) {
            if (s.stationName.equals(d.dest)) {
                s.check(d);
                return;
            }
        }

        System.out.println("Destination not found!");
    }
}