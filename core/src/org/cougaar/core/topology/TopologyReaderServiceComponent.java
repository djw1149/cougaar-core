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

package org.cougaar.core.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.cougaar.core.component.BindingSite;
import org.cougaar.core.component.Component;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.node.NodeControlService;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.service.NamingService;
import org.cougaar.core.service.TopologyEntry;
import org.cougaar.core.service.TopologyReaderService;

import org.cougaar.util.GenericStateModelAdapter;

/**
 * This component creates and maintains the node-level
 * TopologyReaderService.
 *
 * @see TopologyReaderService for use by all components
 */
public final class TopologyReaderServiceComponent
extends GenericStateModelAdapter
implements Component 
{

  private ServiceBroker sb;

  private NamingService namingService;
  private LoggingService log;

  private TopologyReaderServiceProviderImpl topologyRSP;

  public void setBindingSite(BindingSite bs) {
    // only care about the service broker
    //this.sb = bs.getServiceBroker();
  }

  public void setNodeControlService(NodeControlService ncs) {
    this.sb = ncs.getRootServiceBroker();
  }

  public void load() {
    super.load();

    this.log = (LoggingService)
      sb.getService(this, LoggingService.class, null);
    if (log == null) {
      log = LoggingService.NULL;
    }

    this.namingService = (NamingService)
      sb.getService(this, NamingService.class, null);
    if (namingService == null) {
      throw new RuntimeException(
          "Unable to obtain naming service");
    }

    // create and advertise our services
    this.topologyRSP = new TopologyReaderServiceProviderImpl();
    sb.addService(TopologyReaderService.class, topologyRSP);
  }

  public void unload() {
    // clean up ns?
    // revoke our services
    if (topologyRSP != null) {
      sb.revokeService(TopologyReaderService.class, topologyRSP);
      topologyRSP = null;
    }
    // release all services
    if (namingService != null) {
      sb.releaseService(this, NamingService.class, namingService);
      namingService = null;
    }
    if ((log != null) && (log != LoggingService.NULL)) {
      sb.releaseService(this, LoggingService.class, log);
      log = null;
    }
    super.unload();
  }

  private class TopologyReaderServiceProviderImpl
    implements ServiceProvider {

      private final TopologyReaderServiceImpl topologyRS;

      public TopologyReaderServiceProviderImpl() {
        // keep only one instance
        topologyRS = new TopologyReaderServiceImpl();
      }

      public Object getService(
          ServiceBroker sb,
          Object requestor,
          Class serviceClass) {
        if (serviceClass == TopologyReaderService.class) {
          return topologyRS;
        } else {
          return null;
        }
      }

      public void releaseService(
          ServiceBroker sb,
          Object requestor,
          Class serviceClass,
          Object service) {
      }
    }

  private class TopologyReaderServiceImpl
    implements TopologyReaderService {

      public TopologyReaderServiceImpl() {
      }

      //
      // currently no caching!
      //
      // May want to tie into MTS metrics, to avoid lookup 
      // if recent inter-agent communication was observed.
      //

      public String getParentForChild(
          int parentType,
          int childType,
          String childName) {
        return lookupParentForChild(parentType, childType, childName);
      }

      public Set getChildrenOnParent(
          int childType,
          int parentType,
          String parentName) {
        return lookupChildrenOnParent(childType, parentType, parentName);
      }

      public Set getAll(int type) {
        return lookupAll(type);
      }

      public TopologyEntry getEntryForAgent(String agent) {
        return lookupEntryForAgent(agent);
      }

      public Set getAllEntries(
          String agent,
          String node,
          String host,
          String site,
          String enclave) {
        return lookupAllEntries(
            agent, node, host, site, enclave);
      }

      public long getIncarnationForAgent(String agent) {
        return lookupIncarnationForAgent(agent);
      }

      //
      // "lookup*" variants:
      //

      public String lookupParentForChild(
          int parentType,
          int childType,
          String childName) {
        validate(childType, parentType);
        validateName(childType, childName);
        String parentAttr = 
          TopologyNamingConstants.TYPE_TO_ATTRIBUTE_NAME[
            parentType];
        if (childType == AGENT) {
          return (String) searchAgent(childName, parentAttr);
        }
        String childAttr = 
          TopologyNamingConstants.TYPE_TO_ATTRIBUTE_NAME[
            childType];
        return (String)
          searchFirstValue(
              childAttr,
              childName,
              parentAttr);
      }

      public Set lookupChildrenOnParent(
          int childType,
          int parentType,
          String parentName) {
        validate(childType, parentType);
        validateName(parentType, parentName);
        String childAttr =
          TopologyNamingConstants.TYPE_TO_ATTRIBUTE_NAME[
            childType];
        String parentAttr =
          TopologyNamingConstants.TYPE_TO_ATTRIBUTE_NAME[
            parentType];
        Attributes match = new BasicAttributes();
        match.put(parentAttr, parentName);
        return searchAllValues(match, childAttr);
      }

      public Set lookupAll(int type) {
        validateRange(type);
        if (type == AGENT) {
          return searchAllAgents();
        }
        String attr = 
          TopologyNamingConstants.TYPE_TO_ATTRIBUTE_NAME[
            type];
        return searchAllValues(attr);
      }

      public long lookupIncarnationForAgent(String agent) {
        Long l = (Long)
          searchAgent(
              agent,
              TopologyNamingConstants.INCARNATION_ATTR);
        return ((l != null) ? l.longValue() : -1L);
      }

      public TopologyEntry lookupEntryForAgent(String agent) {
        Attributes ats = searchAgent(agent);
        TopologyEntry te = 
          ((ats != null) ? 
           createTopologyEntry(ats) :
           null);
        return te;
      }

      public Set lookupAllEntries(
          String agent,
          String node,
          String host,
          String site,
          String enclave) {
        if (agent != null) {
          TopologyEntry te = lookupEntryForAgent(agent);
          if ((te == null) ||
              ((node != null) && 
               (!(node.equals(te.getNode())))) ||
              ((host != null) && 
               (!(host.equals(te.getHost())))) ||
              ((site != null) && 
               (!(site.equals(te.getSite())))) ||
              ((enclave != null) && 
               (!(enclave.equals(te.getEnclave()))))) {
            return Collections.EMPTY_SET;
          }
          return Collections.singleton(te);
        }

        Attributes match = new BasicAttributes();
        if (node != null) {
          match.put(
              TopologyNamingConstants.NODE_ATTR,
              node);
        }
        if (host != null) {
          match.put(
              TopologyNamingConstants.HOST_ATTR,
              host);
        }
        if (site != null) {
          match.put(
              TopologyNamingConstants.SITE_ATTR,
              site);
        }
        if (enclave != null) {
          match.put(
              TopologyNamingConstants.ENCLAVE_ATTR,
              enclave);
        }

        List allAts = searchAllAttributes(match);
        Set tes;
        if (allAts == null) {
          tes = null;
        } else if (allAts.isEmpty()) {
          tes = Collections.EMPTY_SET;
        } else {
          int n = allAts.size();
          tes = new HashSet(n);
          for (int i = 0; i < n; i++) {
            Attributes ats = (Attributes) allAts.get(i);
            if (ats != null) {
              TopologyEntry te = createTopologyEntry(ats);
              if (te != null) {
                tes.add(te);
              }
            }
          }
        }

        return tes;
      }

      private TopologyEntry createTopologyEntry(Attributes ats) {

        String agent   = (String) getAttribute(ats,
            TopologyNamingConstants.AGENT_ATTR);
        String node    = (String) getAttribute(ats,
            TopologyNamingConstants.NODE_ATTR);
        String host    = (String) getAttribute(ats,
            TopologyNamingConstants.HOST_ATTR);
        String site    = (String) getAttribute(ats,
            TopologyNamingConstants.SITE_ATTR);
        String enclave = (String) getAttribute(ats,
            TopologyNamingConstants.ENCLAVE_ATTR);
        Long linc    = (Long) getAttribute(ats,
            TopologyNamingConstants.INCARNATION_ATTR);
        Long lmoveId = (Long) getAttribute(ats,
            TopologyNamingConstants.MOVE_ID_ATTR);
        Boolean bisNode = (Boolean) getAttribute(ats,
            TopologyNamingConstants.IS_NODE_ATTR);
        Integer istatus  = (Integer) getAttribute(ats,
            TopologyNamingConstants.STATUS_ATTR);

        return new TopologyEntry(
            agent, 
            node, 
            host, 
            site,
            enclave,
            linc.longValue(), 
            lmoveId.longValue(), 
            bisNode.booleanValue(), 
            istatus.intValue());
      }

      /** get named attribute, throw exception if not present */
      private Object getAttribute(Attributes ats, String id) {
        Attribute at = ats.get(id);
        if (at == null) {
          throw new RuntimeException(
              "Unknown attribute \""+id+"\"");
        }
        Object val;
        try {
          val = at.get();
        } catch (NamingException ne) {
          throw new RuntimeException(
              "Unable to get value for attribute \""+id+"\"");
        }
        if (val == null) {
          throw new RuntimeException(
              "Null value for attribute \""+id+"\"");
        }
        return val;
      }

      // validate utilities:

      private void validate(
          int childType, int parentType) {
        validateRange(childType);
        validateRange(parentType);
        validateRelation(childType, parentType);
      }

      private void validateName(int type, String name) {
        if (name == null) {
          throw new IllegalArgumentException(
              "Invalid name \""+
              getTypeAsString(type)+
              "\"");
        }
      }

      private void validateRange(int type) {
        if ((type < AGENT) ||
            (type > ENCLAVE)) {
          throw new IllegalArgumentException(
              "Invalid type \""+
              getTypeAsString(type)+
              "\"");
        }
      }

      private void validateRelation(
          int childType, int parentType) {
        if (childType >= parentType) {
          throw new IllegalArgumentException(
              "Child type \""+
              getTypeAsString(childType)+
              "\" must be "+
              "less than the parent type \""+
              getTypeAsString(parentType)+
              "\"");
        }
      }

      private String getTypeAsString(int i) {
        switch (i) {
          case AGENT: return "AGENT";
          case NODE: return "NODE";
          case HOST: return "HOST";
          case SITE: return "SITE";
          case ENCLAVE: return "ENCLAVE";
          default: return "UNKNOWN ("+i+")";
        }
      }

      // search utils:

      private Attributes searchAgent(String agent) {
        try {
          DirContext ctx = getTopologyContext();
          return ctx.getAttributes(agent);
        } catch (NamingException ne) {
          if (ne instanceof NameNotFoundException) {
            return null;
          }
          throw new RuntimeException(
              "Unable to access name server", ne);
        }
      }

      private Object searchAgent(
          String agent, String single_attr) {
        try {
          DirContext ctx = getTopologyContext();
          String[] ats_filter = { single_attr };
          Attributes ats = ctx.getAttributes(agent);
          if (ats != null) {
            Attribute at = ats.get(single_attr);
            if (at != null) {
              Object val = at.get();
              if (val != null) {
                return val;
              }
            }
          }
          return null;
        } catch (NamingException ne) {
          if (ne instanceof javax.naming.NameNotFoundException) {
            return null;
          }
          throw new RuntimeException(
              "Unable to access name server", ne);
        }
      }

      private Set searchAllAgents() {
        try {
          DirContext ctx = getTopologyContext();
          NamingEnumeration e = ctx.list("");
          Set ret;
          if (!(e.hasMore())) {
            ret = Collections.EMPTY_SET;
          } else {
            ret = new HashSet(13);
            do {
              NameClassPair ncp = (NameClassPair) e.next();
              String agent = ncp.getName();
              if (agent != null) {
                ret.add(agent);
              }
            } while (e.hasMore());
          }
          return ret;
        } catch (NamingException ne) {
          throw new RuntimeException(
              "Unable to access name server", ne);
        }
      }

      private Attributes searchFirstAttributes(Attributes match) {
        // could optimize:
        List ats = searchAllAttributes(match);
        if ((ats == null) || 
            (ats.size() < 1)) {
          return null;
        }
        return (Attributes) ats.get(0);
      }

      private Object searchFirstValue(
          String filter_name, String filter_value, String single_attr) {
        Attributes match = new BasicAttributes();
        match.put(filter_name, filter_value);
        Attributes ats = searchFirstAttributes(match);
        if (ats != null) {
          Attribute at = ats.get(single_attr);
          if (at != null) {
            Object val;
            try {
              val = at.get();
            } catch (NamingException ne) {
              throw new RuntimeException(
                  "Unable to get value for attribute \""+
                  single_attr+"\"");
            }
            if (val != null) {
              return val;
            }
          }
        }
        return null;
      }

      private List searchAllAttributes(Attributes match) {
        try {
          DirContext ctx = namingService.getRootContext();
          NamingEnumeration e =
            ctx.search(
                TopologyNamingConstants.TOPOLOGY_DIR,
                match,
                null);
          List ret;
          if (!(e.hasMore())) {
            ret = Collections.EMPTY_LIST;
          } else {
            ret = new ArrayList(13);
            do {
              SearchResult result = (SearchResult) e.next();
              if (result != null) {
                Attributes ats = result.getAttributes();
                if (ats != null) {
                  ret.add(ats);
                }
              }
            } while (e.hasMore());
          }
          return ret;
        } catch (NamingException e) {
          throw new RuntimeException(
              "Unable to access name server", e);
        }
      }

      private Set searchAllValues(String single_attr) {
        Attributes match = new BasicAttributes();
        return searchAllValues(match, single_attr);
      }

      private Set searchAllValues(Attributes match, String single_attr) {
        try {
          String[] ats_filter = { single_attr };
          DirContext ctx = namingService.getRootContext();
          NamingEnumeration e =
            ctx.search(
                TopologyNamingConstants.TOPOLOGY_DIR,
                match,
                ats_filter);
          Set ret;
          if (!(e.hasMore())) {
            ret = Collections.EMPTY_SET;
          } else {
            ret = new HashSet(13);
            do {
              SearchResult result = (SearchResult) e.next();
              if (result != null) {
                Attributes ats = result.getAttributes();
                if (ats != null) {
                  Attribute at = ats.get(single_attr);
                  if (at != null) {
                    Object val = at.get();
                    if (val != null) {
                      ret.add(val);
                    }
                  }
                }
              }
            } while (e.hasMore()); 
          }
          return ret;
        } catch (NamingException e) {
          throw new RuntimeException(
              "Unable to access name server", e);
        }

      }

      private DirContext getTopologyContext() throws NamingException {
        DirContext ctx = namingService.getRootContext();
        try {
          ctx = (DirContext) 
            ctx.lookup(
                TopologyNamingConstants.TOPOLOGY_DIR);
        } catch (NamingException ne) {
          throw ne;
        } catch (Exception e) {
          NamingException x = 
            new NamingException(
                "Unable to access name-server");
          x.setRootCause(e);
          throw x;
        }
        return ctx;
      }
    }

}