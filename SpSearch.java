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

 //tramite messaggi cerca un superpeer. ha un TTL dopo tot hops smette di propagarsi, ha una lista che gli impedisce di rivisitare i nodi visitati

public class SpSearch extends SingleValueHolder implements CDProtocol, EDProtocol {


//Members
int hops;

/** config parameter name for the cache size */
public static final String PAR_CACHE = "cache";
public String n;

//Stato della ricerca 
boolean searching = false;
boolean trovato = false;


//Costruttore

public SpSearch(String n){
super(n);
this.n = n;

value = 0;
	//Numero di hops massimi preso dal config
	hops = Configuration.getInt(n+"."+PAR_CACHE);
	
}




//=============================== METODI =============================================================

//Mostra il valore degli Hops
public double showvalue()
{
	return value;
}

//Resetta il valore degli Hops
public void resetvalue()
{
	value=0;
}


//Controlla se sta cercando
public boolean isSearching()
{
	return searching;
}

//Controlla se il superpeer é stato trovato
public boolean found()
{
	return trovato;
}


//Trasforma un linkable in un Vector

public void linkToVector(Linkable a, Vector v) {

for (int i =0; i<a.degree();i++)
	{
	v.add(a.getNeighbor(i));
	}

}



//Trasforma un vector in un Linkable

public void vectorToLink(Linkable a, Vector v){

for (int i =0; i<v.size();i++)
	{
	Node b = a.getNeighbor(i);
	b =(Node)v.get(i);
	}

}



//Trova la massima utilità in un vettore di nodi

public Node maxNodeVector (Vector spnew){

int max = 0;
	for (int i= 0; i< spnew.size(); i++)
	{
		Node x = (Node)spnew.get(i);
		Node maxnode = (Node)spnew.get(max);
		DinElec mx = (DinElec)maxnode.getProtocol(2);
		DinElec bx = (DinElec)x.getProtocol(2);
		if (mx.showutility() < bx.showutility()) //Protocol 2 = DinElec
		{
			max = i;
		}
	
	}
return (Node)spnew.get(max);
}



//Trova la minima utilità in un vettore di nodi

public Node minNodeVector (Vector spnew){

int max = 0;
	for (int i= 0; i< spnew.size(); i++)
	{
		Node x = (Node)spnew.get(i);
		Node maxnode = (Node)spnew.get(max);
		DinElec mx = (DinElec)maxnode.getProtocol(2);
		DinElec bx = (DinElec)x.getProtocol(2);
		
		if (mx.showutility() > bx.showutility()) //Protocol 2 = DinElec
		{
			max = i;
		}
	
	}
return (Node)spnew.get(max);
}



//======================   PROCESS   EVENT  =======================================================================================


/* OVERWIEV:
-Se il messaggio dice che il superpeer é stato trovato, Value prende il valore degli Hops utilizzati per trovarli e si segnala che il risultato é stato trovato
-Se il superpeer non é stato ancora trovato si controlla se il nodo attuale lo è
	Se non lo é e ci sono hops rimanenti si prosegue la riserca.
	Se non ci sono hops rimanenti si manda un messaggio al mittente con hops =-1


*/

public void processEvent(Node node, int pid, Object event ) {  

SearchMessage mex = (SearchMessage) event;

//Se il superpeer é stato trovato
if (mex.found ==true) 
{
	value = hops-mex.hopsleft;
	searching = false;
	if (hops>=0)
	{
	trovato = true;
	}	
	
	return;
}

//Se il superpeer non é stato troato
if (mex.found ==false)
{
	Transport t = (Transport)node.getProtocol(FastConfig.getTransport(pid));
	DinElec x =(DinElec)node.getProtocol(2); 

	//controlla se il nodo corrente é un superpeer
	if (x.IsSuperPeer())
	{
		t.send(node,mex.mitt,new SearchMessage(true,--mex.hopsleft,node,mex.visite),pid);
		
	}
	else 
	{	//Se non lo é e ci sono hops rimasti si prosegue la ricerca
		if (mex.hopsleft >=0) 
		{
			Linkable linkablesp = (Linkable) node.getProtocol(0); // 0 é sp l'insieme dei vicini Sp  

			
			if (linkablesp.degree()>0)
			{	
			//Sceglie il nodo con l'utility maggiore in sp che non é già stato visitato
			Vector vsp = new Vector();
			linkToVector(linkablesp,vsp);
			
			//controlla se é già stato visitato
			Node dest = maxNodeVector(vsp);
			int i = 1;
			while (mex.visite.contains(dest) && i<vsp.size())
			{
				vsp.remove(dest);
				dest = maxNodeVector(vsp);
				i++;
			}

			if(!dest.isUp()) return;
			mex.visite.add(node);
			t.send(node,dest,new SearchMessage(false,--mex.hopsleft,mex.mitt,mex.visite),pid);
			value = 0;
			}		
					
		}
		else 
		{
			//Non ci sono hops rimanenti, il superpeer non é stato trovato
			t.send(node,mex.mitt,new SearchMessage(true,-1,mex.mitt,mex.visite),pid);
		}

	}


}


}



//============================================NEXT CYCLE =============================================================


public void nextCycle(Node node,int pid)    //Invia un messaggio ad un vicino casuale in sp con la richiesta di farsi inviare Sp e Rp
{     
	
	Linkable linkablesp = (Linkable) node.getProtocol(0); // 0 é sp l'insieme dei vicini Sp  
	
	
	if (linkablesp.degree()>0  && searching ==false)
	{	
		//Sceglie il nodo con l'utility maggiore in sp
		Vector vsp = new Vector();
		linkToVector(linkablesp,vsp);		
		Node dest = maxNodeVector(vsp);

		if(!dest.isUp()) return;
		Transport t = (Transport)node.getProtocol(FastConfig.getTransport(pid));
		//Vettore Nodi Visitati?		
		Vector visite = new Vector();
		
		t.send(node,dest,new SearchMessage(false,hops,node,visite),pid);
		searching  = true; //Sta Cercando
		trovato = false;
		resetvalue(); //Resetta il valore degli hops a 0 
		
	}		

	

	

}






// CLONE
public SpSearch clone(){ 
String s = n;
SpSearch nw = new SpSearch(s);
nw.value = 0;
nw.hops = hops;


return nw;

}


}
