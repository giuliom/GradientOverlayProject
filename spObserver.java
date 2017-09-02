package example.progetto;

import peersim.util.*;
import peersim.core.*;
import peersim.config.*;
import peersim.cdsim.*;
import java.util.Random;
import peersim.transport.Transport;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.vector.SingleValueHolder;
import java.util.*;


public class spObserver implements Control {

String name;

private static final String PAR_PROT = "protocol";
private static final String PAR_ACCURACY = "accuracy";

/** Protocol identifier */
private final int pid;
/** Accuracy for standard deviation used to stop the simulation */
private final double accuracy;


//Costruttore
public spObserver(String s){

this.name = s;
accuracy = Configuration.getDouble(name + "." + PAR_ACCURACY, -1);
pid = Configuration.getPid(name + "." + PAR_PROT);
}


//Scorre tutti i Protocolli DinElec dei nodi e conta i superpeers
 public boolean execute() {
        long time = peersim.core.CommonState.getTime();
	IncrementalStats stats = new IncrementalStats();
	int number =0; 
		       

        for (int i = 0; i < Network.size(); i++) {

            DinElec protocol = (DinElec) Network.get(i)
                    .getProtocol(pid); //2 = DinElec
            if (protocol.IsSuperPeer()) {
		number++;
		}
		
        }
	stats.add(number);
        /* Printing statistics */
        System.out.println(name + ": " + "  SuperPeers: " + number +"  Network size: "+ Network.size());

        /* Terminate if accuracy target is reached */
        return (stats.getStD()<=accuracy && CommonState.getTime()>0);
    }

}

 
