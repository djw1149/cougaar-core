/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.core.plugin;

import org.cougaar.core.cluster.Subscriber;
import org.cougaar.core.cluster.PersistenceSubscriber;
import org.cougaar.core.cluster.Distributor;
import org.cougaar.core.cluster.ClusterContext;

public abstract class PersistencePlugIn extends PlugInAdapter
{
  protected Subscriber constructSubscriber(Distributor distributor) {
    return new PersistenceSubscriber(this, distributor);
  }

}
