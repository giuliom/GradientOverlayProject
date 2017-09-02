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


public class DinElec extends SingleValueHolder implements CDProtocol, EDProtocol {

//Members

//Bool indica se è un superpeer cioè se utility>value
private boolean superpeer= false;

private double utility = 0; //l'utilità del nodo calcolata da utility_func() in base all'uptime del singolo nodo
private static int period ;  //Intervallo di tempo per il calcolo della utilità 
private String prefix1;
public static final String PAR_PER = "period";
//super(value) é il tempo medio di uptime dell'overlay calcolato con il gossiping

//Constructor

public DinElec (String prefix){
super(prefix);
prefix1 = prefix;
period =  Configuration.getInt(prefix+"."+PAR_PER);

}


//Methods

public void utility_func()  //calcola l' utilità del peer (in base all' uptime)
{
	utility +=period;
	value +=period;
}

public double showutility()
{
	return utility;
}


public boolean IsSuperPeer()
{
	return superpeer;
}


public void average(double b) //calcola l'utilità media per il gossip based avg e determina se é un Superpeer
{
	
	value += b; //b é value=avg_utily
	value = value/2;
	if (utility> value)
	{
		superpeer=true;
	}	
	else
		superpeer= false;
}
	

public void processEvent(Node node, int pid, Object event ) {  //riceve il messaggio e calcola la media

	AverageMessage aem = (AverageMessage)event;
	if( aem.sender!=null )
		((Transport)node.getProtocol(FastConfig.getTransport(pid))).send(node,aem.sender,new AverageMessage(value,null),pid); 
	average(aem.value);
	
	
}




public void nextCycle(Node node,int pid)
{ 
	
	Linkable linkable = (Linkable) node.getProtocol(1); // 1 é Rp l'insieme dei vicini random  RICONTROLLARE!
	utility_func(); //Aggiorna utility e value
	Random generator = new Random();
	int a = linkable.degree();
	int r = generator.nextInt(a);

	if (linkable.degree()>0)
	{	
		
		Node dest = linkable.getNeighbor(r); //Nodo random con cui scambiarsi avg_utility
		if(!dest.isUp()) return;
		Transport t = (Transport)node.getProtocol(FastConfig.getTransport(pid));
		t.send(node,dest,new AverageMessage(value,node),pid);
		
		
	}		
	

}

public DinElec clone(){ //Clone da ridefinire per la classe

DinElec copy = new DinElec(prefix1);
copy.superpeer = superpeer;
copy.utility = utility;
copy.period = period;
copy.value = value;
return copy;
}

}
