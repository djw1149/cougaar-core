// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package org.cougaar.core.society.rmi;

public final class MTImpl_Stub
    extends java.rmi.server.RemoteStub
    implements org.cougaar.core.society.rmi.MT, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_getMessageAddress_0;
    private static java.lang.reflect.Method $method_rerouteMessage_1;
    
    static {
	try {
	    $method_getMessageAddress_0 = org.cougaar.core.society.rmi.MT.class.getMethod("getMessageAddress", new java.lang.Class[] {});
	    $method_rerouteMessage_1 = org.cougaar.core.society.rmi.MT.class.getMethod("rerouteMessage", new java.lang.Class[] {org.cougaar.core.society.Message.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public MTImpl_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of getMessageAddress()
    public org.cougaar.core.society.MessageAddress getMessageAddress()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_getMessageAddress_0, null, -3927034548767378042L);
	    return ((org.cougaar.core.society.MessageAddress) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of rerouteMessage(Message)
    public void rerouteMessage(org.cougaar.core.society.Message $param_Message_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_rerouteMessage_1, new java.lang.Object[] {$param_Message_1}, -4741386825613829822L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
