/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.core.society.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;
  
import java.util.*;
import org.cougaar.core.util.*;
import org.cougaar.util.*;

import org.cougaar.core.society.Message;
import org.cougaar.core.society.MessageAddress;
import org.cougaar.core.society.MessageTransport;
import org.cougaar.core.society.ReceiveQueue;

/** actual RMI remote object providing the implementation of MessageTransport client
 **/

public class MTImpl extends UnicastRemoteObject implements MT 
{
    private MessageTransport transport;
    private MessageAddress address;
    private ReceiveQueue recvQ;

    //public MTImpl() throws RemoteException {} // not used

    public MTImpl(MessageTransport mt, MessageAddress addr) 
	throws RemoteException 
    {
	this(mt, addr, null);
    }

    public MTImpl(MessageTransport mt, MessageAddress addr, ReceiveQueue recvQ) 
	throws RemoteException 
    {
	super();
	transport = mt;
	address = addr;
	this.recvQ = recvQ;
    }

    public void rerouteMessage(Message m) {
	try {
	    if (recvQ != null) {
		// System.err.println("Something efficient happened today ");
		recvQ.deliverMessage(m);
	    } else {
		// System.err.println("Unwinding will happen today ");
		transport.rerouteMessage(m);
	    }
	} catch (Exception e) {
	    System.err.println("\n\nCaught exception in shim: "+e);
	    e.printStackTrace();
	}
    }

    public MessageAddress getMessageAddress() {
	return address;
    }
}
