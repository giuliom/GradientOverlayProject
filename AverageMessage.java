package example.progetto;


import peersim.util.*;
import peersim.core.*;
import peersim.config.*;
import peersim.cdsim.*;
import java.util.Random;
import peersim.transport.Transport;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;


public class AverageMessage {

public double value;
public Node sender;

public AverageMessage(double value, Node sender){
	this.value = value;
	this.sender = sender;
}

}

