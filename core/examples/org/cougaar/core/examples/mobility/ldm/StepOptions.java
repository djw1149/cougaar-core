/* 
 * <copyright>
 * Copyright 2002 BBNT Solutions, LLC
 * under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Cougaar Open Source License as published by
 * DARPA on the Cougaar Open Source Website (www.cougaar.org).
 *
 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
package org.cougaar.core.examples.mobility.ldm;

import java.io.Serializable;
import org.cougaar.core.mobility.Ticket;
import org.cougaar.core.mts.MessageAddress;

/**
 * Immutable step configuration options.
 */
public final class StepOptions implements Serializable {

  private final Object ownerId;
  private final MessageAddress source;
  private final MessageAddress target;
  private final Ticket ticket;
  private final long pauseTime;
  private final long timeoutTime;

  public StepOptions(
      Object ownerId,
      MessageAddress source,
      MessageAddress target,
      Ticket ticket,
      long pauseTime,
      long timeoutTime) {
    this.ownerId = ownerId;
    this.source = source;
    this.target = target;
    this.ticket = ticket;
    this.pauseTime = pauseTime;
    this.timeoutTime = timeoutTime;
    if (ticket == null) {
      throw new IllegalArgumentException(
          "null ticket");
    }
    if ((pauseTime > 0) &&
        (timeoutTime > 0) &&
        (pauseTime > timeoutTime)) {
      throw new IllegalArgumentException(
          "pause time ("+pauseTime+") must be <="+
          " to the timeout time ("+timeoutTime+")");
    }
  }

  public Object getOwnerId() {
    return ownerId;
  }
  public MessageAddress getSource() {
    return source;
  }
  public MessageAddress getTarget() {
    return target;
  }
  public Ticket getTicket() {
    return ticket;
  }
  public long getPauseTime() {
    return pauseTime;
  }
  public long getTimeoutTime() {
    return timeoutTime;
  }
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof StepOptions)) {
      return false;
    } else {
      StepOptions so = (StepOptions) o;
      return 
        ((ownerId != null) ?
         (ownerId.equals(so.ownerId)) :
         (so.ownerId == null)) &&
        ((source != null) ?
         (source.equals(so.source)) :
         (so.source == null)) &&
        ((target != null) ? 
         (target.equals(so.target)) :
         (so.target == null)) &&
        ticket.equals(so.ticket) &&
        (pauseTime == so.pauseTime) &&
        (timeoutTime == so.timeoutTime);
    }
  }
  public int hashCode() {
    return 
      ((ownerId != null) ? ownerId.hashCode() : 5) ^
      ticket.hashCode() ^
      ((source != null) ? source.hashCode() : 7);
  }
  
  public String toString() {
    return 
      "step {"+
      "\n  ownerId: "+ownerId+
      "\n  source:  "+source+
      "\n  target:  "+target+
      "\n  ticket:  "+ticket+
      "\n  pause:   "+pauseTime+
      "\n  timeout: "+timeoutTime+
      "\n}";
  }
}
