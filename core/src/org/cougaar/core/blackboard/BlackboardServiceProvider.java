/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.core.blackboard;

import org.cougaar.core.service.*;

import org.cougaar.core.agent.*;

import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.blackboard.Subscriber;
import org.cougaar.core.blackboard.Distributor;

import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.blackboard.QuerySubscription;

import java.util.*;

/** A BlackboardServiceProvider is a provider class that PluginManager calls
 * when a client requests a BlackboardService.
 **/
public class BlackboardServiceProvider implements ServiceProvider {
  
  private Distributor distributor;
  
  public BlackboardServiceProvider(Distributor distributor) {
    super();
    this.distributor = distributor;
  }
  
  public Object getService(BlackboardClient bbclient) {
    return new Subscriber(bbclient, distributor);
  }
  
  public Object getService(ServiceBroker sb, Object requestor, Class serviceClass) {
    if (serviceClass == BlackboardService.class) {
      return new Subscriber( (BlackboardClient)requestor, distributor );
    } else if (serviceClass == BlackboardMetricsService.class) {
      return getBlackboardMetricsService();
    } else if (serviceClass == BlackboardQueryService.class) {
      return new BlackboardQueryServiceImpl(requestor);
    } else {
      throw new IllegalArgumentException("BlackboardServiceProvider does not provide a service for: "+
                                         serviceClass);
    }
  }

  public void releaseService(ServiceBroker sb, Object requestor, Class serviceClass, Object service)  {
    // ?? each client will get its own subscriber - how can we clean them up??
  }

  // blackboard metrics
  private BlackboardMetricsService bbmetrics = null;

  // only need one instance of this service.
  private BlackboardMetricsService getBlackboardMetricsService() {
    if (bbmetrics != null) {
      return bbmetrics;
    } else {
      //create one
      bbmetrics = new BlackboardMetricsServiceImpl(distributor);
      return bbmetrics;
    }
  }
  
  /** The implementation of BlackboardQueryService
   **/
  private final class BlackboardQueryServiceImpl 
    implements BlackboardQueryService 
  {
    // keep the requestor around just in case...
    private Object requestor;  
    private BlackboardQueryServiceImpl(Object requestor) {
      this.requestor = requestor;
    }

    public final Collection query(UnaryPredicate isMember) {
      QuerySubscription qs = new QuerySubscription(isMember);
      //qs.setSubscriber(null);  // ignored
      distributor.fillQuery(qs);
      return qs.getCollection();
    }
  }
}


  
  
