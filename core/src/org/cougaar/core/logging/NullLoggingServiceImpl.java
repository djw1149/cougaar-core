/*
 * <copyright>
 *  
 *  Copyright 2001-2004 BBNT Solutions, LLC
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

package org.cougaar.core.logging;

import org.cougaar.core.service.LoggingService;
import org.cougaar.util.log.NullLogger;

/**
 * LoggingService where all "is*()" methods return
 * false, and all "log()" methods are ignored.
 * <p>
 * This is handle if<pre> 
 *   serviceBroker.getService(.., LoggingService.class, ..);
 * </pre>
 * returns null.
 */
public final class NullLoggingServiceImpl 
  extends NullLogger
  implements LoggingService 
{
  // singleton:
  private static final NullLoggingServiceImpl SINGLETON = new NullLoggingServiceImpl();

  /** @deprecated old version of getLoggingService() **/
  public static NullLoggingServiceImpl getNullLoggingServiceImpl() {
    return SINGLETON;
  }

  /** @return a singleton instance of the NullLoggingService **/
  public static LoggingService getLoggingService() { return SINGLETON; }
}
