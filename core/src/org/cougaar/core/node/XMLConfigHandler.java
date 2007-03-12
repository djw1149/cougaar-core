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

package org.cougaar.core.node;

import java.io.CharArrayWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cougaar.core.component.ComponentDescription;
import org.cougaar.util.Strings;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX {@link org.xml.sax.ContentHandler} that handles the society
 * config parsing.
 */
public class XMLConfigHandler extends DefaultHandler {

  private static final int STATE_INITIAL = 0;
  private static final int STATE_PARSING = 1;
  private static final int STATE_PARSED = 2;

  private final Map agents;
  private final String nodename;
  private final String agentname;
  private final Logger logger;

  private final Map currentComponent = new HashMap(11);
  private final CharArrayWriter argValueBuffer = new CharArrayWriter();

  private boolean thisNode;
  private boolean thisAgent;
  private List currentList;
  private String currentAgent;
  private boolean inArgument;

  private int state = STATE_INITIAL;

  public XMLConfigHandler(
      String nodename,
      String agentname) {
    this.nodename = nodename;
    this.agentname = agentname;
    this.logger = Logging.getLogger(getClass());
    agents = new HashMap(agentname == null ? 11 : 1);
  }
  
  public Map getAgents() {
    return agents;
  }

  public void startDocument() {
    // not thread-safe, but close enough to warn developers
    if (state != STATE_INITIAL) {
      throw new RuntimeException(
          "ContentHandler "+
          (state == STATE_PARSING ?
           "already parsing" :
           "completed parsing, please create a new instance"));
    }
    state = STATE_PARSING;
  }

  public void endDocument() {
    if (state != STATE_PARSING) {
      throw new RuntimeException(
          "ContentHandler "+
          (state == STATE_INITIAL ?
           "never started" :
           "already closed")+
          " document?");
    }
    state = STATE_PARSED;
  }

  // begin element
  public void startElement(
      String namespaceURI,
      String localName,
      String qName,
      Attributes atts)
    throws SAXException {

    if (logger.isDetailEnabled()) {
      StringBuffer buf = new StringBuffer();
      buf.append("startElement(");
      buf.append("namswpaceURI=").append(namespaceURI);
      buf.append(", localName=").append(localName);
      buf.append(", qName=").append(qName);
      buf.append(", atts[").append(atts.getLength());
      buf.append("]{");
      for (int i = 0, n = atts.getLength(); i < n; i++) {
        buf.append("\n(uri=").append(atts.getURI(i));
        buf.append(" localName=").append(atts.getLocalName(i));
        buf.append(" qName=").append(atts.getQName(i));
        buf.append(" type=").append(atts.getType(i));
        buf.append(" value=").append(atts.getValue(i));
        buf.append("), ");
      }
      buf.append("\n}");
      logger.detail(buf.toString());
    }

    if (localName.equals("node")) {
      startNode(atts);
    } else if (localName.equals("agent")) {
      startAgent(atts);
    }
      
    if (!thisNode && !thisAgent) {
      return;
    }

    if (localName.equals("component")) {
      startComponent(atts);
    } else if (localName.equals("argument")) {
      startArgument(atts);
    } else {
      // ignore
    }
  }

  // misc characters within an element, e.g. argument data
  public void characters(char[] ch, int start, int length)
    throws SAXException {

    if (logger.isDetailEnabled()) {
      StringBuffer buf = new StringBuffer();
      buf.append("characters(ch[").append(ch.length).append("]{");
      buf.append(ch, start, length);
      buf.append("}, ").append(start).append(", ");
      buf.append(length).append(")");
      logger.detail(buf.toString());
    }

    if (inArgument) {
      // inside component argument, so save characters
      argValueBuffer.write(ch, start, length);
    }
  }

  // end element
  public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException {

    if (logger.isDetailEnabled()) {
      logger.detail(
          "endElement("+
          "namswpaceURI="+namespaceURI+
          ", localName="+localName+
          ", qName="+qName+")");
    }

    if (!thisNode && !thisAgent) {
      return;
    }

    if (localName.equals("argument")) {
      endArgument();
    } else if (localName.equals("component")) {
      endComponent();
    } else if (localName.equals("agent")) {
      endAgent();
    } else if (localName.equals("node")) {
      endNode();
    } else {
      // ignore
    }
  }

  // xml parser error
  public void error(SAXParseException exception) throws SAXException {
    logger.error("Error parsing the file", exception);
    super.error(exception);
  }

  // xml parser warning
  public void warning(SAXParseException exception) throws SAXException {
    logger.warn("Warning parsing the file", exception);
    super.warning(exception);
  }

  // our element handlers:

  private void startNode(Attributes atts)
    throws SAXException {

    String name = Strings.intern(atts.getValue("name"));

    thisNode = 
      (nodename == null ||
       nodename.equals(name));

    thisAgent =
      (agentname == null ||
       agentname.equals(name));

    if (!thisNode || !thisAgent) {
      if (logger.isDebugEnabled()) {
        logger.debug("skipping node "+name);
      }
      currentAgent = null;
      return;
    }

    if (logger.isInfoEnabled()) {
      logger.info("starting node "+name);
    }

    // make a new place for the node's components
    currentAgent = name;
    currentList = (List) agents.get(name);
    if (currentList == null) {
      currentList = new ArrayList(1);
      agents.put(name, currentList);
    }
  }

  private void endNode()
    throws SAXException {
    if (thisNode) {
      thisNode = false;
      currentAgent = null;
      currentList = null;
      if (logger.isInfoEnabled()) {
        logger.info("finished node "+nodename);
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("skipped node");
      }
    }
  }

  private void startAgent(Attributes atts)
    throws SAXException {

    if (!thisNode && nodename != null) {
      return;
    }

    String name = Strings.intern(atts.getValue("name"));

    thisAgent = 
      (agentname == null ||
       agentname.equals(name));

    if (!thisAgent) {
      if (logger.isDebugEnabled()) {
        logger.debug("skipping agent "+name);
      }
      // keep node's currentAgent and currentList
      return;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("starting agent "+name);
    }

    // make a new place for the agent's components
    currentAgent = name;
    currentList = (List) agents.get(name);
    if (currentList == null) {
      currentList = new ArrayList(1);
      agents.put(name, currentList);
    }
  }

  private void endAgent()
    throws SAXException {
    if (thisAgent) {
      if (logger.isDebugEnabled()) {
        logger.debug("finished agent "+currentAgent);
      }
      thisAgent = false;
      // restore name to node's name
      currentAgent = nodename;
      currentList = (List) agents.get(currentAgent);
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("skipped agent");
      }
    }
  }

  private void startComponent(Attributes atts)
    throws SAXException {

    if (currentList == null) {
      throw new RuntimeException(
          "component not within node or agent?");
    }
    if (!currentComponent.isEmpty()) {
      throw new RuntimeException(
          "startComponent already within component? "+
          currentComponent);
    }

    currentComponent.put("name", Strings.intern(atts.getValue("name")));
    currentComponent.put("class", Strings.intern(atts.getValue("class")));
    currentComponent.put("priority", Strings.intern(atts.getValue("priority")));
    currentComponent.put("insertionpoint", Strings.intern(atts.getValue("insertionpoint")));
  }

  private void endComponent()
    throws SAXException {

    if (currentComponent.isEmpty()) {
      throw new RuntimeException(
          "endComponent not within component?");
    }
    ComponentDescription desc = 
      makeComponentDesc(currentComponent);
    currentComponent.clear();

    currentList.add(desc);
  }

  private void startArgument(Attributes atts)
    throws SAXException {
    if (currentComponent.isEmpty()) {
      throw new RuntimeException(
          "Argument ("+atts+") not in component!");
    }
    if (inArgument) {
      throw new RuntimeException(
          "Already have an argument value buffer? "+argValueBuffer);
    }
    inArgument = true;

    String name = atts.getValue("name");
    if (name != null) {
      if (name.indexOf('=') >= 0) {
        throw new RuntimeException("Invalid '=' in attribute name: "+name);
      }
      argValueBuffer.append(name.trim());
      argValueBuffer.append("=");
    }
    String value = atts.getValue("value");
    if (value != null) {
      argValueBuffer.append(value.trim());
    }
  }

  private void endArgument()
    throws SAXException {
    if (!inArgument) {
      throw new RuntimeException("Not in argument?");
    }
    inArgument = false;

    String argument = argValueBuffer.toString().trim();
    argValueBuffer.reset();

    ArrayList argumentList = (ArrayList)
      currentComponent.get("arguments");
    if (argumentList == null) {
      argumentList = new ArrayList(1);
      currentComponent.put("arguments", argumentList);
    }
    argumentList.add(argument);
  }

  // utility methods:

  private static ComponentDescription makeComponentDesc(
      Map componentProps) {
    String name = (String) componentProps.get("name");
    String classname = (String) componentProps.get("class");
    String priority = (String) componentProps.get("priority");
    String insertionPoint = (String) componentProps.get("insertionpoint");
    //      String order = (String) componentProps.get("order");
    ArrayList args = (ArrayList) componentProps.get("arguments");
    List vParams = null;
    if ((args != null) && (args.size() > 0)) {
      vParams = Collections.unmodifiableList(args);
    }
    return
      makeComponentDesc(
          name, vParams, classname, priority, insertionPoint);
  }

  private static ComponentDescription makeComponentDesc(
      String name,
      List vParams,
      String classname,
      String priority,
      String insertionPoint) {
    return new ComponentDescription(
        name,
        insertionPoint,
        classname,
        null, //codebase
        vParams, //params
        null, //certificate
        null, //lease
        null, //policy
        ComponentDescription.parsePriority(priority));
      }
}
