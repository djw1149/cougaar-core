/*
 * <copyright>
 *  Copyright 2001-2003 BBNT Solutions, LLC
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

package org.cougaar.core.domain;

import java.util.List;
import java.util.Set;
import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.blackboard.Blackboard;
import org.cougaar.core.blackboard.DirectiveMessage;
import org.cougaar.core.blackboard.EnvelopeTuple;
import org.cougaar.core.component.ServiceBroker;
import org.cougaar.core.component.ServiceProvider;
import org.cougaar.core.service.DomainForBlackboardService;

/** A DomainServiceProvider is a provider class for domain factory services. **/
public class DomainForBlackboardServiceProvider implements ServiceProvider {
  private DomainForBlackboardServiceImpl theService;

  public DomainForBlackboardServiceProvider(DomainForBlackboardServiceImpl theDomainService) {
    this.theService = theDomainService;
  }
  
  public Object getService(ServiceBroker sb, Object requestor, Class serviceClass) {
    if ((DomainForBlackboardService.class.isAssignableFrom(serviceClass)) &&
        (requestor instanceof Blackboard)) {
      return new DomainForBlackboardServiceProxy();
    } else {
      return null;
    }
  }

  public void releaseService(ServiceBroker sb, Object requestor, Class serviceClass, Object service)  {
  }

  private final class DomainForBlackboardServiceProxy 
    implements DomainForBlackboardService {
    public Factory getFactory(String domainName) {
      return theService.getFactory(domainName);
    }

    public Factory getFactory(Class domainClass) {
      return theService.getFactory(domainClass);
    }

    public List getFactories() {
      return theService.getFactories();
    }

    public void invokeDelayedLPActions() {
      theService.invokeDelayedLPActions();
    }

    public void invokeEnvelopeLogicProviders(EnvelopeTuple tuple, 
                                             boolean persistenceEnv) {
      theService.invokeEnvelopeLogicProviders(tuple, persistenceEnv);
    }

    public void invokeMessageLogicProviders(DirectiveMessage message) {
      theService.invokeMessageLogicProviders(message);
    }

    public void invokeRestartLogicProviders(MessageAddress cid) {
      theService.invokeRestartLogicProviders(cid);
    }

    public void invokeABAChangeLogicProviders(Set communities) {
      theService.invokeABAChangeLogicProviders(communities);
    }

    public void setBlackboard(Blackboard blackboard) {
      theService.setBlackboard(blackboard);
    }
  } // end of DomainForBlackboardServiceProxy

}


  
  
