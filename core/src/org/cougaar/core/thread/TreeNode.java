/*
 * <copyright>
 *  Copyright 1997-2003 BBNT Solutions, LLC
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

package org.cougaar.core.thread;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class TreeNode
{
    private TreeNode parent;
    private ArrayList children;
    private Scheduler scheduler;
    private ThreadGroup group;
    private ThreadPool pool;

    TreeNode(Scheduler scheduler, ThreadServiceProxy parentService) 
    {
	children = new ArrayList();
	this.scheduler = scheduler;
	scheduler.setTreeNode(this);
	TreeNode parent = 
	    parentService == null ? null : parentService.getTreeNode();
	setParent(parent);
	
	if (parent == null) {
	    group = new ThreadGroup(getName());
	} else {
	    group = new ThreadGroup(parent.group, getName());
	}
	
	pool = ThreadPool.getPool(group);
    }


    void setParent(TreeNode parent) {
	this.parent = parent;
	if (parent != null) parent.addChild(this);
    }

    TreeNode getParent() {
	return parent;
    }


    void addChild(TreeNode child) {
	synchronized (children) {
	    children.add(child);
	}
    }

    ArrayList getChildren() {
	return children;
    }

    void listRunningThreads(List records) {
	pool.listRunningThreads(records);
    }

    void listQueuedThreads(List records) {
	scheduler.listQueuedThreads(records);
	if (children != null) {
	    synchronized (children) {
		Iterator itr = children.iterator();
		TreeNode child = null;
		while (itr.hasNext()) {
		    child = (TreeNode) itr.next();
		    child.listQueuedThreads(records);
		}
	    }
	}
    }


    Scheduler getScheduler() {
	return scheduler;
    }

    String getName() {
	return scheduler.getName();
    }

    ThreadPool getPool() {
	return pool;
    }

    ThreadGroup getGroup() {
	return group;
    }
}
