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


public class SearchMessage {

boolean found;
int hopsleft;
Node mitt;
Vector visite;

public SearchMessage (boolean found, int hopsleft,Node mitt,Vector visite){

this.found = found;
this.hopsleft = hopsleft;
this.mitt = mitt;
this.visite = visite;
}

}
