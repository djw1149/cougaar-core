/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.core.service;

import org.cougaar.core.component.Service;
import org.cougaar.core.thread.Schedulable;

/**
 * The ThreadService provides Schedulables for use by a COUGAAR
 * objects (known as the 'consumer') The body of code to be run is
 * passed in as a Runnable.  An optional name can be passed in as
 * well, and should be passed in if at all possible.
 */
public interface ThreadService extends Service
{
    public static final int BEST_EFFORT_LANE  = 0;
    public static final int WILL_BLOCK_LANE   = 1;
    public static final int CPU_INTENSE_LANE  = 2;
    public static final int WELL_BEHAVED_LANE = 3;

    public static final int LANE_COUNT = 4;

    Schedulable getThread(Object consumer, Runnable runnable);
    Schedulable getThread(Object consumer, Runnable runnable, String name);

    Schedulable getThread(Object consumer, Runnable runnable, String name,
			  int lane);

    /**
     * @deprecated Use the schedule methods on Schedulable.
     */
    void schedule(java.util.TimerTask task, long delay);

    /**
     * @deprecated Use the schedule methods on Schedulable.
     */
    void schedule(java.util.TimerTask task, long delay, long interval);

    /**
     * @deprecated Use the schedule methods on Schedulable.
     */
    void scheduleAtFixedRate(java.util.TimerTask task, long delay, long interval);

}
