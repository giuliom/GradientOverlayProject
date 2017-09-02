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
import java.lang.Math;



public class SpGrad  implements CDProtocol, EDProtocol {


//Members
String n;

/** config parameter name for the cache size */
public static final String PAR_CACHE = "cache";
private int maxdegree = 20;


//Costruttore

public SpGrad(String n){

	maxdegree = Configuration.getInt(n+"."+PAR_CACHE);
	
}




//=============================== METODI =============================================================



//Trasforma un linkable in un Vector

public void linkToVector(RemoveProtocol a, Vector v) {

for (int i =0; i<a.degree();i++)
	{
	v.add(a.getNeighbor(i));
	}

}



//Trasforma un vector in un Linkable

public void vectorToLink(RemoveProtocol a, Vector v){

for (int i =0; i<v.size();i++)
	{
	Node b = a.getNeighbor(i);
	b =(Node)v.get(i);
	}

}



//Trova la massima utilità in un vettore di nodi

public Node maxNodeVector (RemoveProtocol spnew){

int max = 0;
	for (int i= 0; i< spnew.degree(); i++)
	{
		Node x = spnew.getNeighbor(i);
		Node maxnode = spnew.getNeighbor(max);
		DinElec mx = (DinElec)maxnode.getProtocol(2);
		DinElec bx = (DinElec)x.getProtocol(2);
		if (mx.showutility() < bx.showutility()) //Protocol 2 = DinElec
		{
			max = i;
		}
	
	}
return spnew.getNeighbor(max);
}



//Trova la minima utilità in un vettore di nodi

public Node minNodeVector (RemoveProtocol spnew){

int max = 0;
	for (int i= 0; i< spnew.degree(); i++)
	{
		Node x = spnew.getNeighbor(i);
		Node maxnode = (Node)spnew.getNeighbor(max);
		DinElec mx = (DinElec)maxnode.getProtocol(2);
		DinElec bx = (DinElec)x.getProtocol(2);
		
		if (mx.showutility() > bx.showutility()) //Protocol 2 = DinElec
		{
			max = i;
		}
	
	}
return spnew.getNeighbor(max);
}


//Calcola la similitudine tra il peer e un peer di sp

public Node simil(RemoveProtocol spnew, Node p){

//DinElec del nodo P
DinElec px = (DinElec)p.getProtocol(2);

int max = 0;
	for (int i= 0; i< spnew.degree(); i++)
	{
		Node x = spnew.getNeighbor(i);
		Node maxnode = (Node)spnew.getNeighbor(max);
		DinElec mx = (DinElec)maxnode.getProtocol(2);
		DinElec bx = (DinElec)x.getProtocol(2);
		
		if (bx.showutility() > px.showutility() && mx.showutility()<px.showutility() ||
			Math.abs(bx.showutility()-px.showutility()) < Math.abs(mx.showutility()-px.showutility()) && bx.showutility() > px.showutility()  && mx.showutility() > px.showutility()   ||    bx.showutility() < px.showutility() && mx.showutility() < px.showutility() )
		{
			max = i;
		}
	}
return spnew.getNeighbor(max);
}









//======================   PROCESS   EVENT  =======================================================================================


/* OVERWIEV:
Processa i messaggi di tipo SpMessage.
*Se il messaggio è di tipo 0: 
	-ceglie due nodi nei protocolli Sp (0) e Rp (1) secondo politiche stabilite
	-risponde al mittente con un messaggio di tipo=1 inviando i due nodi joinsp e joinrp

*Se il messaggio é di tipo 1:
	-Se il protocollo Sp del nodo non ha raggiunto il limite, aggiunge il nodo joinsp
	-Se ha raggiunto il limite sceglie il nodo di sp con l'utilità minore e lo sostituisce solo se
	 la sua utilità é minore del nodo del vicino
	
	-Sceglie un nodo casuale nel vettore rp del messaggio
	-Se il protocollo Rp del nodo non ha raggiunto il limite, lo aggiunge
	-Se ha raggiunto il limite sceglie un nodo casuale di rp e lo sostituisce con il nodo del vicino

*/

public void processEvent(Node node, int pid, Object event ) {  

SpMessage mex = (SpMessage) event;


//Messaggio di tipo 0
if (mex.type == 0)
{
	Transport t = (Transport)node.getProtocol(FastConfig.getTransport(pid));
	
	RemoveProtocol sp = (RemoveProtocol) node.getProtocol(0); //Sp del nodo attuale
	RemoveProtocol rp = (RemoveProtocol) node.getProtocol(1); //Rp del nodo attuale
		
	//Il nodo con max Similitudine dal protocollo Sp del vicino da aggiungere	
	Node joinsp = simil(sp,mex.mitt);
	
	//il nodo casuale da sostituire in Rp
	Node joinrp;
	Random generator = new Random();
	int a = rp.degree();
	int r = generator.nextInt(a);
	
	if (rp.degree() ==0) joinrp=null;
	else{	joinrp = rp.getNeighbor(r);}

	
	t.send(node,mex.mitt,new SpMessage(joinsp,joinrp,1,node),pid);
	return;
}
else	
{
	

	RemoveProtocol spdest = (RemoveProtocol)node.getProtocol(0); //Sp del nodo attuale
	RemoveProtocol rpdest = (RemoveProtocol)node.getProtocol(1); //Rp del nodo attuale

	//I Nodi contenuti nel messaggio	
	Node join = mex.linkSp;
	Node joinrp = mex.linkRp;


	//---MODIFICA DI Sp---

	

	if (spdest.degree() < maxdegree)
	{
		spdest.addNeighbor(join);
	}
	else
	{
		

		//si rimpiazza il nodo con min Utility di Spdest solo se la sua utilità é minore del nuovo arrivato
		Node replace = minNodeVector(spdest);
		DinElec mx = (DinElec)replace.getProtocol(2);
		DinElec bx = (DinElec)join.getProtocol(2);


		if (mx.showutility() < bx.showutility()) //protocol 2 = DinElec
		{
			spdest.remNeighbor(replace);
			
			//Sostituisce spdest con la nuova versione 
			spdest.addNeighbor(join);
			
			
		}
	}



	//---MODIFICA DI Rp---	

	//il nodo che verrà aggiunto scelto a caso tra i nodi in Rp del vicino
	

	//Se Rp non ha ancora raggiunto il limite lo aggiunge	
	if (rpdest.degree() < maxdegree)
	{
		rpdest.addNeighbor(joinrp);
	}
	else
	{
		//Rimpiazza un nodo in Rp scelto in modo casuale
		
		Random generator = new Random();		
		int r = generator.nextInt(rpdest.degree());

		Node replace2 = rpdest.getNeighbor(r); 
		
		rpdest.remNeighbor(replace2);
		rpdest.addNeighbor(joinrp);
		
		
	
		
	}
}

}





//============================================NEXT CYCLE =============================================================


public void nextCycle(Node node,int pid)    //Invia un messaggio ad un vicino casuale in sp con la richiesta di farsi inviare Sp e Rp
{     
	
	Linkable linkablesp = (Linkable) node.getProtocol(0); // 0 é sp l'insieme dei vicini Sp  

	//sceglie un vicino casuale in sp
	Random generator = new Random();
	int a = linkablesp.degree();
	int r = generator.nextInt(a);
	
	if (linkablesp.degree()>0)
	{	
		
		Node dest = linkablesp.getNeighbor(r); 
		if(!dest.isUp()) return;
		Transport t = (Transport)node.getProtocol(FastConfig.getTransport(pid));
		t.send(node,dest,new SpMessage(null,null,0,node),pid);
		
	}		

	

	

}










public SpGrad clone(){
return this;
}


}
