/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */

package org.cougaar.core.qos.metrics;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;


import javax.servlet.*;
import javax.servlet.http.*;

import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.TopologyReaderService;


public class AgentLoadServlet 
    extends MetricsServlet
    implements Constants
{
    public AgentLoadServlet(ServiceBroker sb) {
	super(sb);
    }
    protected String   myPath() {
	return "/metrics/agent/load";
    }

    protected String myTitle () {
	return "Agent Load for Node " + nodeID;
    }

    protected void outputPage(PrintWriter out) {
	// Get list of All Agents On this Node
	Set matches = null;
	try {
	    matches = topologyService.getAllEntries(null,  // Agent
						    nodeID,// only this node
						    null, // Host
						    null, // Site
						    null); // Enclave
	} catch (Exception ex) {
	    // Node hasn't finished initializing yet
	    return;
	}
	if (matches == null) return;

	//Header Row
	out.print("<table border=1>\n");
	out.print("<tr><b>");
	out.print("<td><b>AGENT</b></td>");
	out.print("<td><b>CPUload</b></td>");
	out.print("<td><b>Cred</b></td>");
	out.print("<td><b>MsgOut</b></td>");
	out.print("<td><b>MsgIn</b></td>");
	out.print("</b></tr>");

	//Rows
	Iterator itr = matches.iterator();
	while (itr.hasNext()) {
	    // Get Agent
	    TopologyEntry entry = (TopologyEntry) itr.next();
	    if ((entry.getType() & TopologyReaderService.AGENT) == 0) continue;

	    String name = entry.getAgent();
	    String agentPath = "Agent(" +name+ ")"+PATH_SEPR;
	    // Get Metrics
	    Metric cpuLoad = metricsService.getValue(agentPath
						     +ONE_SEC_LOAD_AVG);
	    Metric msgIn = new MetricImpl(new Double(0.00), 0,"units","test");
	    Metric msgOut = new MetricImpl(new Double(0.00), 0,"units","test");

	    //output Row
	    out.print("<tr><td><b>");
	    out.print(name);
	    out.print(" </b></td>");
	    out.print(Color.valueTable(cpuLoad, 0.0, 1.0,true, f4_2));
	    out.print(Color.credTable(cpuLoad));
	    out.print(Color.valueTable(msgIn, 0.0, 1.0, true, f4_2));
	    out.print(Color.valueTable(msgOut, 0.0, 1.0, true, f4_2));
	    out.print("</tr>\n");

	}
	out.print("</table>");
    }
}