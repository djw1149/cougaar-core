/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */

package org.cougaar.core.society;

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Constructor;

/**
 * The root class of all aspect-ready factories.  Such factories have
 * the ability to attach a cascading series of aspect delegates to the
 * instances the factory is making.  The aspects themselves are
 * instantiated once, via reflection, and passed in to the constructor
 * of the factory.  The aspect delegates are made on the fly by each
 * aspect, if it wishes to attach one for a given factory
 * interface. */
abstract public class AspectFactory
{
    private static final boolean debug =
	Boolean.getBoolean("org.cougaar.core.society.transport.DebugTransport");
    private ArrayList aspects;

    protected AspectFactory(ArrayList aspects) {
	this.aspects = aspects;
    }

    /**
     * Loops through the aspects, allowing each one to attach an
     * aspect delegat in a cascaded series.  If any spects attach a
     * delegate, the final aspect delegate is returned.  If no aspects
     * attach a delegate, the original object, as created by the
     * factory, is returned.  The 'iface' argument describes the
     * abstract type of the objects which the factory creates.  */
    public Object attachAspects(Object delegate, Class iface) {
	if (aspects != null) {
	    Iterator itr = aspects.iterator();
	    while (itr.hasNext()) {
		MessageTransportAspect aspect = 
		    (MessageTransportAspect) itr.next();
		Object candidate = aspect.getDelegate(delegate, iface);
		if (candidate != null) delegate = candidate;
		if (debug) System.out.println("======> " + delegate);
	    }
	}
	return delegate;
    }

}
