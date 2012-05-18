/* 
 * <copyright>
 *  
 *  Copyright 2002-2004 BBNT Solutions, LLC
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

package org.cougaar.core.adaptivity;


/** 
 * A phrase used to express a boolean comparison between a string
 * standing in for condition data or an operating mode and a Object
 * holding the value and a set of valid values
 */
public class ConstraintPhrase extends ConstraintOpValue {
  String proxyName;
  
  /**
   * @param name of the input source, e.g., condition name
   * @param op ConstraintOperator
   * @param av array of OMCRange descriptions list allowed ranges.
   */
  public ConstraintPhrase(String name, ConstraintOperator op, OMCRangeList av) {
    this(name);
    setOperator(op);
    setAllowedValues(av);
  }

  public ConstraintPhrase(String name) {
    super();
    proxyName = name;
  }
  
  /** 
   * @return The name of the condition or operating mode 
   */
  public String getProxyName() {
    return proxyName;
  }

  @Override
public String toString() {
    return proxyName + " " + super.toString();
  }
}
