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

package org.cougaar.domain.planning.ldm.plan;

/* Constant names for Auxiliary Query types which are used to return
 * extra information within an AllocationResult that are not necessarily
 * related to a preference
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: AuxiliaryQueryType.java,v 1.4 2001-08-22 20:14:10 mthome Exp $
 */
public interface AuxiliaryQueryType {
  
  static final int PORT_NAME = 0;
    
  static final int FAILURE_REASON = 1;
  
  static final int UNIT_SOURCED = 2;
  
  static final int POE_DATE = 3;
  
  static final int POD_DATE = 4;
  
  static final int READINESS = 5;
  
  static final int OVERTIME = 6;
  
  static final int PLANES_AVAILABLE = 7;
  

  static final int LAST_AQTYPE = 7;
  static final int AQTYPE_COUNT = LAST_AQTYPE+1;
  
  static final int UNDEFINED = -1;
}

