package org.cougaar.core.society;

public interface ReceiveQueue 
{
    public void deliverMessage(Message message);
    public boolean matches(String name);

}
