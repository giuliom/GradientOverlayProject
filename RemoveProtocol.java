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


//Classe che estende IdleProtocol e Permette la rimozione di nodi dal protocollo
public class RemoveProtocol extends IdleProtocol {



public RemoveProtocol (String s){

super(s);

}


public boolean remNeighbor(Node n)
{
	boolean found = false;
	for (int i = 0; i < len; i++) 
	{
		if (neighbors[i] == n)
			{neighbors[i] =null;
			 found = true;}
		
		
	}

	
	if (found == true) 
	{
		len--;
		Node[] temp = new Node[len];
		int nw = 0;
		
		for (int i = 0; i<neighbors.length;i++)
		{
		if (neighbors[i] !=null)
			{
			temp[nw] = neighbors[i];				
			nw++;
			}
		}
				


		neighbors = temp;
	}
	
	return true;
}



}
