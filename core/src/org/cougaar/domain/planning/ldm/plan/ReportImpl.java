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

import java.io.Serializable;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.cougaar.domain.planning.ldm.plan.Report;
import org.cougaar.domain.planning.ldm.plan.NewReport;

import org.cougaar.core.society.UID;

import org.cougaar.core.util.XMLizable;
import org.cougaar.core.util.XMLize;


/** Report Implementation
 * @author  ALPINE <alpine-software@bbn.com>
 * @version $Id: ReportImpl.java,v 1.4 2001-08-22 20:14:15 mthome Exp $
 *
 * Informational report contains a text and an associated date. 
 **/

public class ReportImpl 
  implements Report, NewReport, XMLizable,  Serializable {

  protected String myText; // answers the question "Right, what's all this, then?"
  protected Date myDate; // Date associated with message. (When created?)
  private UID myUID;

  /**
   * Constructor - takes no args
   */
  public ReportImpl() {
    myText = null;
    myDate = null;
    myUID = null;
  }

  /**
   * Constructor - takes text, date, and UID args
   *
   * @param text String with text of report
   * @param date Date associated with report (probably creation date)
   * @param uid  UID for report
   */
  public ReportImpl(String text,
                         Date date,
                         UID uid) {
    myText = text;
    myDate = date;
    myUID = uid;
  }
  
  /**
   * setText - set text for message
   * 
   * @param infoText String with new text
   */
  public void setText(String reportText) {
    myText = reportText;
  }

  /**
   * getText - return text of message
   * 
   * @return String with text of the report
   */
  public String getText() {
    return myText;
  }

  /**
   * setDate - set date associated with the report
   *
   * @param date Date to be associated with the report
   */
  public void setDate(Date date) {
    myDate = date;
  }

  /**
   * getDate - return date associated with the report
   * 
   * @return Date associated with the report
   */
  public Date getDate() {
    return myDate;
  }

  /**
   * setUID - set uid for the object
   * Why is this public?  Does it make sense to allow random changes to 
   * UID?
   *
   * @param uid UID assigned to object
   */
  public void setUID(UID uid) {
    myUID = uid;
  }
  
  /**
   * getUID - get uid for the object
   *
   * @return UID assigned to object
   */
  public UID getUID() { 
    return myUID;
  }

  /** getXML - add the Report to the document as an XML Element and return the 
   * 
   * BOZO - not currently handling XML
   *
   * @param doc Document to which XML Element will be added
   * @return Element 
   **/
  public Element getXML(Document doc) {
    return XMLize.getPlanObjectXML(this, doc);
  }

}
 
