/*
 * <copyright>
 *  Copyright 2001 BBNT Solutions, LLC
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
package org.cougaar.planning.servlet.data.hierarchy;

import org.cougaar.planning.servlet.data.xml.*;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import org.xml.sax.Attributes;

/**
 * Represents the organization data within the hierarchy PSP
 * @author Benjamin Lubin; last modified by: $Author: gvidaver $
 *
 * @since 1/24/01
 **/
public class Organization
  implements XMLable, DeXMLable, Serializable{
  
  //Variables:
  ////////////
  
  public final static String NAME_TAG = "Org";
  protected final static String UID_TAG = "OrgID";
  protected final static String PRETTY_NAME_TAG = "Name";
  protected final static String RELATION_TAG = "Rel";
  
  public static final int ADMIN_SUBORDINATE = 0;
  public static final int SUBORDINATE = 1;
  
  protected String UID;
  protected String prettyName;
  protected List relations;
  
  //Constructors:
  ///////////////
  
  public Organization(){
    relations = new ArrayList();
  }
  
  //Members:
  //////////
  
  public void setUID(String UID){this.UID=UID;}
  public void setPrettyName(String name){prettyName=name;}
  public void addRelation(String UID, int relation){
    relations.add(new OrgRelation(UID, relation));
  }
  public void addRelation(String UID, String relation){
    relations.add(new OrgRelation(UID, relation));
  }
  
  public String getUID(){return UID;}
  public String getPrettyName(){return prettyName;}
  public int getNumRelations(){return relations.size();}
  public String getRelationUIDAt(int i){
    OrgRelation or = (OrgRelation)relations.get(i);
    return or.org;
  }
  public int getRelationAt(int i){
    OrgRelation or = (OrgRelation)relations.get(i);
    return or.relation;
  }
  public OrgRelation getOrgRelationAt (int i) {
    return (OrgRelation)relations.get(i);
  }
  public String getRelatedOrgAt (int i) {
    return ((OrgRelation)relations.get(i)).getRelatedOrg();
  }
  public String getNamedRelationAt (int i) {
    return ((OrgRelation)relations.get(i)).getName();
  }
  public List getRelations () {
    return relations;
  }
  //XMLable members:
  //----------------
  
  /**
   * Write this class out to the Writer in XML format
   * @param w output Writer
   **/
  public void toXML(XMLWriter w) throws IOException{
    w.optagln(NAME_TAG);
    
    w.tagln(UID_TAG, getUID());
    w.tagln(PRETTY_NAME_TAG, getPrettyName());
    
    for(int i=0;i<getNumRelations();i++){
      OrgRelation relation = getOrgRelationAt (i);
      w.sitagln(RELATION_TAG,
		UID_TAG, getRelationUIDAt(i),
		RELATION_TAG, 
		(relation.hasName ()? relation.getName() :
		 Integer.toString(getRelationAt(i))));
    }
    
    w.cltagln(NAME_TAG);
  }
  
  //DeXMLable members:
  //------------------
  
  /**
   * Report a startElement that pertains to THIS object, not any
   * sub objects.  Call also provides the elements Attributes and data.  
   * Note, that  unlike in a SAX parser, data is guaranteed to contain 
   * ALL of this tag's data, not just a 'chunk' of it.
   * @param name startElement tag
   * @param attr attributes for this tag
   * @param data data for this tag
   **/
  public void openTag(String name, Attributes attr, String data)
    throws UnexpectedXMLException{
    if(name.equals(NAME_TAG)){
    }else if(name.equals(PRETTY_NAME_TAG)){
      setPrettyName(data);
    }else if(name.equals(UID_TAG)){
      setUID(data);
    }else if (name.equals(RELATION_TAG)){
      try{
	addRelation(attr.getValue(UID_TAG),
		    Integer.parseInt(attr.getValue(RELATION_TAG)));
      }catch(NumberFormatException e){
	addRelation(attr.getValue(UID_TAG),
		    attr.getValue(RELATION_TAG));
      }
    }else{
      throw new UnexpectedXMLException("Unexpected tag: "+name);
    }
  }
  
  /**
   * Report an endElement.
   * @param name endElement tag
   * @return true iff the object is DONE being DeXMLized
   **/
  public boolean closeTag(String name)
    throws UnexpectedXMLException{
    return name.equals(NAME_TAG);
  }
  
  /**
   * This function will be called whenever a subobject has
   * completed de-XMLizing and needs to be encorporated into
   * this object.
   * @param name the startElement tag that caused this subobject
   * to be created
   * @param obj the object itself
   **/
  public void completeSubObject(String name, DeXMLable obj)
    throws UnexpectedXMLException{
  }

  public static class OrgRelation implements Serializable, Comparable{
    public String org;
    public int relation;
    public String relationName;
    public boolean hasName = false;
    public OrgRelation(String o, int r){
      org=o;
      relation=r;
    }
    public OrgRelation(String org, String relation) {
      this.org=org;
      relationName=relation;
      hasName = true;
    }
    public int compareTo (Object other) {
      if (!(other instanceof OrgRelation))
	return 0;
      return org.compareTo(((OrgRelation) other).org);
    }
    public boolean hasName () { return hasName; }
    public String  getName () { return relationName; }
    public String  getRelatedOrg () { return org; }
    /** 
     * Set the serialVersionUID to keep the object serializer from seeing
     * xerces (org.xml.sax.Attributes).
     */
    private static final long serialVersionUID = 109382094832059403L;
  }

  /** 
   * Set the serialVersionUID to keep the object serializer from seeing
   * xerces (org.xml.sax.Attributes).
   */
  private static final long serialVersionUID = 893487223987438473L;
}
