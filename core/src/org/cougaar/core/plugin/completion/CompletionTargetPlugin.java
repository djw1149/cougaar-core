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

package org.cougaar.core.plugin.completion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.cougaar.core.blackboard.IncrementalSubscription;
import org.cougaar.core.blackboard.ChangeReport;
import org.cougaar.core.plugin.ComponentPlugin;
import org.cougaar.core.persist.PersistenceNotEnabledException;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Verb;
import org.cougaar.util.Collectors;
import org.cougaar.util.EmptyIterator;
import org.cougaar.util.Thunk;
import org.cougaar.util.UnaryPredicate;

/**
 * This plugin gathers and integrates completion information from one
 * agent to determine the "completion" of the current tasks. It
 * gathers the information and forwards the completion status of the
 * agent to another agent. This is typically the NodeAgent of the node
 * on which the agent is running.
 **/

public class CompletionTargetPlugin extends CompletionPlugin {
  private static final long NORMAL_SLEEP_INTERVAL = 5000L;
  private static final long NORMAL_ACTIVITY_DELAY = 600000;
  private static final String SLEEP_INTERVAL_KEY = "SLEEP_INTERVAL=";
  private static final String ACTIVITY_DELAY_KEY = "ACTIVITY_DELAY=";
  private long SLEEP_INTERVAL = NORMAL_SLEEP_INTERVAL;
  private long ACTIVITY_DELAY = NORMAL_ACTIVITY_DELAY;
  private ChangeReport myChangeReport = new ChangeReport() {};
  private Set myChangeReports = Collections.singleton(myChangeReport);
  private Set changedRelays = new HashSet();
  private static final Class[] requiredServices = {};
  protected Set ignoredVerbs = new HashSet();
  private IncrementalSubscription relaySubscription;
  private IncrementalSubscription activitySubscription;
  protected long now;           // Time of current execute()
  protected long scenarioNow;   // Scenario time of current execute()
  private long lastActivity;    // Time of last activity
  private double cpuConsumption = 0.0;
  private double taskCompletion = 0.0;
  private boolean updateTaskCompletionPending = true;
  private boolean debug = false;
  private Map filters = new WeakHashMap();
  protected UnaryPredicate createTasksPredicate() {
    return new UnaryPredicate() {
        public boolean execute(Object o) {
          if (o instanceof Task) {
            Task task = (Task) o;
            if (ignoredVerbs.contains(task.getVerb())) return false;
            return true;
          }
          return false;
        }
      };
  }

  private UnaryPredicate tasksPredicate;;

  private static UnaryPredicate activityPredicate =
    new UnaryPredicate() {
      public boolean execute(Object o) {
        if (o instanceof Task) return true;
        if (o instanceof PlanElement) return true;
        return false;
      }
    };

  private static class CompletionCounter implements Thunk {
    private double totalConfidence;
    private int peCount;
//     private Map verbCounts = new HashMap();
    private double completionThreshold = 0.99;
    void init() {
      totalConfidence = 0.0;
      peCount = 0;
//       verbCounts.clear();
    }
    public void apply(Object o) {
      Task task = (Task) o;
      PlanElement pe = task.getPlanElement();
      if (pe != null) {
        AllocationResult ar = pe.getEstimatedResult();
        if (ar != null) {
          double conf = ar.getConfidenceRating();
          conf = Math.min(conf/0.89999999, 1.0);
          totalConfidence += conf;
          peCount++;
          if (conf > completionThreshold) return;
        }
      }
//       Verb verb = task.getVerb();
//       Count count = (Count) verbCounts.get(verb);
//       if (count == null) {
//         count = new Count();
//         verbCounts.put(verb, count);
//       }
//       count.count++;
    }
  }
  private CompletionCounter completionCounter = new CompletionCounter();

  public CompletionTargetPlugin() {
    super(requiredServices);
  }

  protected void addIgnoredVerb(Verb verb) {
    ignoredVerbs.add(verb);
  }

  public void setupSubscriptions() {
    Collection params = getParameters();
    for (Iterator i = params.iterator(); i.hasNext(); ) {
      String param = (String) i.next();
      if (param.startsWith(SLEEP_INTERVAL_KEY)) {
        SLEEP_INTERVAL = Long.parseLong(param.substring(SLEEP_INTERVAL_KEY.length()));
        if (logger.isInfoEnabled()) logger.info("Set "
                                                + SLEEP_INTERVAL_KEY
                                                + SLEEP_INTERVAL);
        continue;
      }
      if (param.startsWith(ACTIVITY_DELAY_KEY)) {
        ACTIVITY_DELAY = Long.parseLong(param.substring(ACTIVITY_DELAY_KEY.length()));
        if (logger.isInfoEnabled()) logger.info("Set "
                                                + ACTIVITY_DELAY_KEY
                                                + ACTIVITY_DELAY);
        continue;
      }
    }
    debug = true;//getClusterIdentifier().toString().equals("47-FSB");
    relaySubscription = (IncrementalSubscription)
      blackboard.subscribe(targetRelayPredicate);
    activitySubscription = (IncrementalSubscription)
      blackboard.subscribe(activityPredicate, new AmnesiaCollection(), true);
    lastActivity = System.currentTimeMillis();
    startTimer(SLEEP_INTERVAL);
  }

  public void execute() {
    processSubscriptions();
    if (relaySubscription.hasChanged()) {
      changedRelays.clear();
      changedRelays.addAll(relaySubscription.getAddedCollection());
      Collection changes = relaySubscription.getChangedCollection();
      if (changes.size() > 0) {
        for (Iterator i = changes.iterator(); i.hasNext(); ) {
          CompletionRelay relay = (CompletionRelay) i.next();
          Set changeReports = relaySubscription.getChangeReports(relay);
          if (changeReports == null || !changeReports.equals(myChangeReports)) {
            changedRelays.add(relay);
          }
        }
      }
      if (changedRelays.size() > 0) {
        checkPersistenceNeeded(changedRelays);
      }
    }
  }

  private void processSubscriptions() {
    boolean timerExpired = timerExpired();
    now = System.currentTimeMillis();
    scenarioNow = getAlarmService().currentTimeMillis();
    if (activitySubscription.hasChanged()) {
      lastActivity = now;
      updateTaskCompletionPending = true; // Activity has changed task completion
    }
    updateCPUConsumption(now);
    if (timerExpired) {
      if (updateTaskCompletionPending) {
        updateTaskCompletion();
      }
      cancelTimer();
      startTimer(SLEEP_INTERVAL);
    }
    maybeRespondToRelays();
  }

  protected void setPersistenceNeeded() {
    try {
      blackboard.persistNow();
      if (logger.isInfoEnabled()) {
        logger.info("doPersistence()");
      }
    } catch (PersistenceNotEnabledException pnee) {
      logger.error(pnee.getMessage(), pnee);
    }
  }

  private void updateCPUConsumption(long now) {
    cpuConsumption =
      Math.max(0.0, 1.0 - (((double) (now - lastActivity)) / ACTIVITY_DELAY));
  }

  private void updateTaskCompletion() {
    if (tasksPredicate == null) tasksPredicate = createTasksPredicate();
    Collection tasks = blackboard.query(tasksPredicate);
    completionCounter.init();
    if (tasks.size() > 0) {
      Collectors.apply(completionCounter, tasks);
      taskCompletion = completionCounter.totalConfidence / tasks.size();
    } else {
      taskCompletion = 1.0;
    }
    updateTaskCompletionPending = false;
  }

  /**
   * Create a new Laggard if the conditions warrant. The conditions
   * warranting a new laggard are embodied in the LaggardFilter, but
   * we want to defer recomputing task completion as long as possible
   * because it is moderately expensive. So, if the filter suppresses
   * transmission for either value of task completion, then task
   * completion is not updated transmission is suppressed. Otherwise,
   * task completion is updated and a new laggard created.
   **/
  private Laggard createLaggard(CompletionRelay relay) {
    boolean cpuConsumed = cpuConsumption > relay.getCPUThreshold();
    LaggardFilter filter = (LaggardFilter) filters.get(relay);
    if (filter == null) {
      filter = new LaggardFilter();
      filters.put(relay, filter);
    }
    if (updateTaskCompletionPending) {
      if (filter.filter(true, now) || !cpuConsumed && filter.filter(false, now)) {
        updateTaskCompletion();
      }
    }
    boolean tasksIncomplete = taskCompletion < relay.getCompletionThreshold();
    boolean isLaggard = cpuConsumed || tasksIncomplete;
    if (filter.filter(isLaggard, now)) {
      Laggard newLaggard =
        new Laggard(getAgentIdentifier(), taskCompletion, cpuConsumption, isLaggard);
      filter.setOldLaggard(newLaggard);
      return newLaggard;
    }
    return null;
  }

  private void maybeRespondToRelays() {
    if (debug && logger.isDebugEnabled() && relaySubscription.size() == 0) {
      return;
    }
    for (Iterator relays = relaySubscription.iterator(); relays.hasNext(); ) {
      CompletionRelay relay = (CompletionRelay) relays.next();
      Laggard newLaggard = createLaggard(relay);
      if (newLaggard != null) {
        if (logger.isDebugEnabled())
          logger.debug("Send response to "
                       + relay.getSource() +
                       ": "
                       + newLaggard);
        relay.setResponseLaggard(newLaggard);
        blackboard.publishChange(relay, myChangeReports);
      } else {
//         if (logger.isDebugEnabled()) logger.debug("No new response to " + relay.getSource());
      }
    }
  }
}    
