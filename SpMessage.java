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


public class SpMessage {

public int type;
public Node linkSp;
public Node linkRp;
public Node mitt;

public SpMessage(Node sp, Node rp, int type, Node node){
this.type=type;
if (type == 1)
{
this.linkSp = sp; 
this.linkRp = rp;
}
this.mitt = node;
}


}
