/*
 * <copyright>
 * Copyright 2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.core.agent;

import java.io.InputStream;
import java.util.*;
import org.cougaar.util.*;
import org.cougaar.core.component.*;
import org.cougaar.core.cluster.*;
import org.cougaar.core.society.*;
import org.cougaar.core.mts.MessageTransportService;

import java.beans.*;
import java.lang.reflect.*;


/** A container for Agent Components.
 **/
public class AgentManager 
  extends ContainerSupport
  implements ContainerAPI
{
    public AgentManager() {
    if (!attachBinderFactory(new AgentBinderFactory())) {
      throw new RuntimeException("Failed to load the AgentBinderFactory");
    }
  }

  /** this constructor used for backwards compatability mode.  Goes away
   * when we are a contained by a Node component
   **/
  public AgentManager(ComponentDescription comdesc) {
    if (!attachBinderFactory(new AgentBinderFactory())) {
      throw new RuntimeException("Failed to load the AgentBinderFactory");
    }
  }

 private AgentManagerBindingSite bindingSite = null;

  public final void setBindingSite(BindingSite bs) {
    super.setBindingSite(bs);
    if (bs instanceof AgentManagerBindingSite) {
      bindingSite = (AgentManagerBindingSite) bs;
      setChildServiceBroker(new AgentManagerServiceBroker(bindingSite));
    } else {
      throw new RuntimeException("Tried to laod "+this+"into "+bs);
    }

    // We cannot start adding services until after the serviceBroker has been created.
    // add some services for the agents (clusters).
    // maybe this can be hooked in from Node soon.
    //childContext.addService(MetricsService.class, new MetricsServiceProvider(agent));
    //childContext.addService(MessageTransportService.class, new MessageTransportServiceProvider(agent));

  }

  protected final AgentManagerBindingSite getBindingSite() {
    return bindingSite;
  }


  protected ComponentFactory specifyComponentFactory() {
    return super.specifyComponentFactory();
  }
  protected String specifyContainmentPoint() {
    return "Node.AgentManager";
  }

  protected ContainerAPI getContainerProxy() {
    return new AgentManagerProxy();
  }
  
  public void requestStop() { }

 /**
  * Add a cluster
  */
  public boolean add(Object obj) {
    //System.err.print("AgentManager adding Cluster");

    if (!(super.add(obj))) {
      // unable to add
      return false;
    }

    // send the Agent an "initialized" message
    //
    // maybe we can replace this with a more direct API?

    Agent agent;
    if ((obj instanceof ComponentDescription) ||
        (obj instanceof StateTuple)) {
      // get the description
      ComponentDescription desc;
      if (obj instanceof ComponentDescription) {
        desc = (ComponentDescription)obj;
      } else {
        desc = ((StateTuple)obj).getComponentDescription();
      }
      // use the description to find the AgentBinder that we just 
      //   added -- is there a better way to do this?
      AgentBinder agentBinder = null;
      for (Iterator iter = super.boundComponents.iterator(); ;) {
        if (!(iter.hasNext())) {
          // unable to find our own child?
          return false;
        }
        Object oi = iter.next();
        if (!(oi instanceof BoundComponent)) {
          continue;
        }
        BoundComponent bci = (BoundComponent)oi;
        Object cmpi = bci.getComponent();
        if (!(desc.equals(cmpi))) {
          continue;
        }
        Binder bi = bci.getBinder();
        if (bi instanceof AgentBinder) {
          agentBinder = (AgentBinder)bi;
          break;
        }
      }

      // get the Cluster itself -- this is a hack!
      agent = agentBinder.getAgent();
    } else if (obj instanceof Agent) {
      agent = (Agent)obj;
    } else {
      // unable to hookup?
      return false;
    }

    // get the Cluster itself -- this is a hack!
    if (!(agent instanceof ClusterServesClusterManagement)) {
      return false;
    }
    ClusterServesClusterManagement cluster = 
      (ClusterServesClusterManagement)agent;

    //System.out.println("Cluster: "+cluster);

    // hookup the Cluster
    return hookupCluster(cluster);
  }

  private boolean hookupCluster(ClusterServesClusterManagement cluster) {
     ClusterIdentifier cid = cluster.getClusterIdentifier();
     String cname = cid.toString();
     // tell the cluster to proceed.
     try {
       ClusterInitializedMessage m = new ClusterInitializedMessage();
       m.setOriginator(cid);
       m.setTarget(cid);
       cluster.receiveMessage(m);

       // register cluster with Node's ExternalNodeActionListener
       getBindingSite().registerCluster(cluster);

     } catch (Exception e) {
       System.err.println("\nUnable to initialize and register cluster["+cluster+"]  "+e);
       e.printStackTrace();
     }
     
     // if we are all the way to this point return true
     return true;
  }

  private static void debugState(Object state, String path) {
    if (state instanceof StateTuple[]) {
      StateTuple[] tuples = (StateTuple[])state;
      for (int i = 0; i < tuples.length; i++) {
        String prefix = path+"["+i+" / "+tuples.length+"]";
        StateTuple sti = tuples[i];
        if (sti == null) {
          System.out.println(
              prefix+": null");
          continue;
        }
        ComponentDescription cdi = sti.getComponentDescription();
        if (cdi == null) {
          System.out.println(
            prefix+": {null, ..}");
          continue;
        }
        System.out.println(
            prefix+": "+
            cdi.getInsertionPoint()+" = "+
            cdi.getClassname()+" "+
            cdi.getParameter());
        Object si = sti.getState();
        if (si != null) {
          debugState(si, prefix);
        }
      }
    } else {
      System.out.println(path+" non-StateTuple[] "+state);
    }
  }

  /**
   * Support Node-issued agent mobility requests.
   * <p>
   * @param agentID agent to move
   * @param nodeID destination node address
   */
  public void moveAgent(
      ClusterIdentifier agentID,
      NodeIdentifier nodeID) {

    // check parameters, security, etc
    if ((agentID == null) ||
        (nodeID == null)) {
      // error
      return;
    }

    // lookup the agent on this node
    ComponentDescription origDesc = null;
    Agent agent = null;
    for (Iterator iter = super.boundComponents.iterator(); ;) {
      if (!(iter.hasNext())) {
        // no such agent?
        return;
      }
      Object oi = iter.next();
      if (!(oi instanceof BoundComponent)) {
        continue;
      }
      BoundComponent bc = (BoundComponent)oi;
      Binder b = bc.getBinder();
      if (!(b instanceof AgentBinder)) {
        continue;
      }
      Agent a = ((AgentBinder)b).getAgent();
      if ((a != null) &&
          (agentID.equals(a.getAgentIdentifier()))) {
        // found our agent
        agent = a;
        Object cmp = bc.getComponent();
        if (cmp instanceof ComponentDescription) {
          origDesc = (ComponentDescription)cmp;
        }
        break;
      }
    }

    // suspend the agent's activity, prepare for state capture
    agent.suspend();

    // recursively gather the agent state
    Object state = 
      ((agent instanceof StateObject) ?
       ((StateObject)agent).getState() :
       null);

    System.out.println("state is: "+state);
    debugState(state, "");

    // create a ComponentDescription for the agent
    ComponentDescription cd;
    if (origDesc != null) {      
      Vector param = new Vector(1);
      param.add("copied"+agentID.toString());
      cd = new ComponentDescription(
          origDesc.getName(),
          origDesc.getInsertionPoint(),
          origDesc.getClassname(),
          origDesc.getCodebase(),
          param, //origDesc.getParameter(),
          origDesc.getCertificate(),
          origDesc.getLeaseRequested(),
          origDesc.getPolicy());
    } else {
      // lost the description?
      Vector param = new Vector(1);
      param.add("copied"+agentID.toString());
      cd = new ComponentDescription(
          "org.cougaar.core.cluster.ClusterImpl",
          "Node.AgentManager.Agent",
          "org.cougaar.core.cluster.ClusterImpl",
          null,  // codebase
          param,
          null,  // certificate
          null,  // lease
          null); // policy
    }

    // create a StateTuple
    StateTuple st = new StateTuple(cd, state);

    System.out.println("add("+st+")");
    add(st);
    if (true) {

      // stop and unload the original agent
      agent.stop();
      agent.unload();

      // cancel all services requested by the agent

      // unhand the original agent, let GC reclaim it
      //
      // ContainerSupport should be modified to clean this up...
      for (Iterator iter = super.boundComponents.iterator();
           iter.hasNext();
          ) {
        Object oi = iter.next();
        if (!(oi instanceof BoundComponent)) {
          continue;
        }
        BoundComponent bc = (BoundComponent)oi;
        Binder b = bc.getBinder();
        if (!(b instanceof AgentBinder)) {
          continue;
        }
        Agent a = ((AgentBinder)b).getAgent();
        if ((a != null) &&
            (agentID.equals(a.getAgentIdentifier()))) {
          // remove our agent
          iter.remove();
          break;
        }
      }
      return;
    }

    // create an ADD ComponentMessage with the ComponentDescription
    ComponentMessage addMsg =
      new ComponentMessage(
          new NodeIdentifier(bindingSite.getIdentifier()),
          nodeID,
          ComponentMessage.ADD,
          cd);

    // get the message transport
    MessageTransportService mts = (MessageTransportService)
      getServiceBroker().getService(
          this,
          MessageTransportService.class,
          null);
    if (mts == null) {
      // error!  we should have requested this earlier...
      System.err.println("Unable to get MessageTransport for mobility message");
      return;
    }

    // send message to destination node
    mts.sendMessage(addMsg);

    // wait for add acknowledgement -- postponed to 8.6+

    // destroy the original agent on this node

    System.out.println(
        "Move "+agentID+" to "+nodeID);
  }

  public String getName() {
    return getBindingSite().getName();
  }


  //
  // support classes
  //

  private static class AgentManagerServiceBroker 
    extends PropagatingServiceBroker
  {
    public AgentManagerServiceBroker(BindingSite bs) {
      super(bs);
    }
  }
 

  private class AgentManagerProxy implements AgentManagerForBinder, 
                                             ClusterManagementServesCluster, 
                                             BindingSite {

    public String getName() {return AgentManager.this.getName(); }
    
    // BindingSite
    public ServiceBroker getServiceBroker() {
      return AgentManager.this.getServiceBroker();
    }
    public void requestStop() {}
    public boolean remove(Object o) {return true; }
  }

}

