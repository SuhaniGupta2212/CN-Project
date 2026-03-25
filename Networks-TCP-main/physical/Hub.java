package physical;

import java.util.*;
import main.Data;
import main.EndStation;

public class Hub{
    List<EndStation> ll = new ArrayList<>();

    public void connect(EndStation s){
        ll.add(s);
    }

    public void receiveAndTransmit(Data d , EndStation s){ // it will get data d , which it is sent by station s
        System.out.println("Data recieved");

        for(EndStation i : ll){
            if(i!=s){
                i.check(d);
            }
        }
    }
}
