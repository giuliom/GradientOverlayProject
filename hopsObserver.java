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


public class hopsObserver implements Control {

String name;

private static final String PAR_PROT = "protocol";
private static final String PAR_ACCURACY = "accuracy";

/** Protocol identifier */
private final int pid;
/** Accuracy for standard deviation used to stop the simulation */
private final double accuracy;


//Costruttore
public hopsObserver(String s){

this.name = s;
accuracy = Configuration.getDouble(name + "." + PAR_ACCURACY, -1);
pid = Configuration.getPid(name + "." + PAR_PROT);
}



public boolean execute() {
        long time = peersim.core.CommonState.getTime();
	IncrementalStats stats = new IncrementalStats();

	//Numero Searchers	
	int number =0; 
	
	double avstep =0;			       

//COME FUNZIONA: per ogni nodo calcola il valore di SpSearch: Se é >0 significa che sta cercando e lo aggiunge alle statistiche	
        for (int i = 0; i < Network.size(); i++) 
	{

            SpSearch protocol = (SpSearch) Network.get(i).getProtocol(pid); 
            if (protocol.found()) {
		number++;
		stats.add(protocol.showvalue());
		
		}
		
        }

	//Numero di Hops Per cercatore in media
	double statfinal = stats.getSum()/number;
	
        /* Printing statistics */
        System.out.println(name + ": "  + " Searchers " + number +"  Avg Hops "+ statfinal);
	
	stats.reset();

        /* Terminate if accuracy target is reached */
        return (stats.getStD()<=accuracy && CommonState.getTime()>0);
    }

}
