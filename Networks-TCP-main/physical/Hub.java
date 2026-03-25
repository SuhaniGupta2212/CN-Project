package physical;

import java.util.*;
import main.*;

public class Hub {

    public String name;
    List<EndStation> devices = new ArrayList<>();

    // ✅ ADD THIS CONSTRUCTOR
    public Hub(String name) {
        this.name = name;
    }

    public void connect(EndStation s) {
        devices.add(s);
    }

    public void receiveAndTransmit(Data d, EndStation sender) {

        System.out.println("\n[HUB " + name + "] Broadcasting...");

        for (EndStation s : devices) {
            if (s != sender) {
                s.check(d);
            }
        }
    }
}