/**
 * Original work by Ola Aronsson 2020
 * Courtesy of nollettnoll AB &copy; 2012 - 2020
 * <p>
 * Licensed under the Creative Commons Attribution 4.0 International (the "License")
 * you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * <p>
 * https://creativecommons.org/licenses/by/4.0/
 * <p>
 * The software is provided “as is”, without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other liability,
 * whether in an action of contract, tort or otherwise, arising from, out of or
 * in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.catchitcozucan.core.ErrorCodeCarrier;
import com.github.catchitcozucan.core.histogram.HistogramProvider;
import com.github.catchitcozucan.core.histogram.HistogramStatus;
import com.github.catchitcozucan.core.histogram.LifeCycleHistogramCollector;
import com.github.catchitcozucan.core.histogram.LifeCycleProvider;
import com.github.catchitcozucan.core.impl.source.processor.Nameable;
import com.github.catchitcozucan.core.interfaces.IsolationLevel;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.internal.util.SizeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class JobBase<T extends ProcessSubjectBase> implements Job, HistogramProvider {

    private static final String ZERO_MILLIS = "0 millis";
    private static final String S_EXITING_JOB_S_AS_FINISHED_B_AFTER_S_EXIT_STATE_IS_S = "%s exiting job '%s' as finished : %b after %s. Exit state is : [%s]";
    private static final String S_ENTERING_JOB_S_PRE_PROCESS_STATE_S_S = "%s entering job '%s'. Pre-process state : %s [%s]";
    private static final String S_ON_ITEM_S_FOR_SUBJECT_S = "'%s' on item %s for subject %s";
    private static final String ITEM_S_FOR_SUBJECT_S = "Item %s for subject %s";
    private static final String YOU_ARE_REQUIRED_TO_PASS_IN_SOME_DATA_YOU_SENT_NULL_OR_AN_EMPTY_ARRAY = "You are required to pass in some data - you sent null or an empty array";
    private LifeCycleHistogramCollector cycleHistogramCollector;
    private long processInstanceExectime;
    private long totalExectime;
    private int numberOfProcesses;
    private static final String TIME_REPORTING = "JOB EXEC TIME : %s PROCESS AVARAGE EXEC TIME : %s";
    private boolean amIWorking;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobBase.class);
    protected final PersistenceService<T> persistenceService;
    private final Enum<?>[] criteriaStates;
    private Nameable[] nameables;
    private boolean acceptEmptyHistogram;

    protected JobBase(PersistenceService<T> persistenceService, Enum<?>[] criteriaStates) {
        this.persistenceService = persistenceService;
        this.criteriaStates = criteriaStates;
        setupNameAbles();
    }

    protected JobBase(PersistenceService<T> persistenceService, Enum<?>[] criteriaStates, boolean acceptEmptyHistogram) {
        this.persistenceService = persistenceService;
        this.criteriaStates = criteriaStates;
        setupNameAbles();
        this.acceptEmptyHistogram = acceptEmptyHistogram;
    }

    @Override
    public boolean isExecuting() {
        return amIWorking;
    }

    @Override
    public HistogramStatus getHistogram() {
        if (collectorIsAvailable()) {
            return new HistogramStatus(name(), persistenceService.provideSubjectStream().collect(cycleHistogramCollector), null);
        } else if (!acceptEmptyHistogram) {
            return null;
        } else {
            Map<String, Integer> statesEmpty = new LinkedHashMap<>();
            Arrays.stream(nameables).sequential().forEach(n -> statesEmpty.put(n.name(), 0));
            return new HistogramStatus(name(), statesEmpty);
        }
    }

    @Override
    public HistogramStatus makeSampleHistogram(Integer[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException(YOU_ARE_REQUIRED_TO_PASS_IN_SOME_DATA_YOU_SENT_NULL_OR_AN_EMPTY_ARRAY);
        }
        return new HistogramStatus(name(), makeUpData(data), null);
    }

    @Override
    public IsolationLevel.Level provideIsolationLevel() {
        return IsolationLevel.Level.KIND_EXCLUSIVE;
    }

    @Override
    public RejectionAction provideRejectionAction() {
        return RejectionAction.PUT_ON_WAITING_LIST;
    }

    @Override
    public final Type provideType() {
        return Type.JOB;
    }

    @Override
    public boolean rejectedFromTheOutSideWorld() {
        return false;
    }

    @Override
    public void interruptExecution() {} // it is not necessary, though healthy, to implement this

    protected Stream<T> fetchSubjectsInCriteriaState() {
        return persistenceService.provideStateFilteredSubjectStream(); //NOSONAR
    }

    protected void exec(Process proc) {
        if (processInstanceExectime == 0) {
            processInstanceExectime = System.currentTimeMillis();
        }
        String prefix = String.format(ITEM_S_FOR_SUBJECT_S, ((ProcessBase) proc).getSubject().id(), ((ProcessBase) proc).getSubject().subjectIdentifier());
        try {
            LOGGER.info(String.format(S_ENTERING_JOB_S_PRE_PROCESS_STATE_S_S, prefix, name(), ((ProcessBase) proc).getCurrentStatus(), ((ProcessBase) proc).getStatusDescription()));//NOSONAR
            amIWorking = true;
            proc.process();
        } catch (Exception e) {
            ProcessBase<T> processBase = ((ProcessBase) proc);
            String messageSuffix = String.format(S_ON_ITEM_S_FOR_SUBJECT_S, proc.name(), ((ProcessBase) proc).getSubject().id(), ((ProcessBase) proc).getSubject().subjectIdentifier());
            Enum<?> currentStatusUponFailure = processBase.getCurrentStatusUponFailure();
            if (currentStatusUponFailure == null) {
                currentStatusUponFailure = processBase.getCurrentStatus();
            }
            if (e instanceof ErrorCodeCarrier) { // NOSONAR BULL.
                processBase.bail(messageSuffix, new InternalJobNonRuntimeException(messageSuffix, e), currentStatusUponFailure, processBase.evalutateForErrorCode((ErrorCodeCarrier) e));
            } else {
                processBase.bail(messageSuffix, new InternalJobNonRuntimeException(messageSuffix, e), currentStatusUponFailure, null);
            }
        } finally {
            amIWorking = false;
            if (processInstanceExectime != 0) {
                totalExectime += System.currentTimeMillis() - processInstanceExectime;
                processInstanceExectime = 0;
                numberOfProcesses++;
            }
            LOGGER.info(String.format(S_EXITING_JOB_S_AS_FINISHED_B_AFTER_S_EXIT_STATE_IS_S, prefix, name(), ((ProcessBase) proc).finished(), totalExectime, ((ProcessBase) proc).getStatusDescription()));//NOSONAR
        }
    }

    protected String getTotalExectime() {
        String execTimeFriendly = SizeUtils.getFormattedMillisPrintoutFriendly(totalExectime);
        String perProcessMillis;
        if (numberOfProcesses > 0) {
            perProcessMillis = SizeUtils.getFormattedMillisPrintoutFriendly((totalExectime / numberOfProcesses));
        } else {
            perProcessMillis = ZERO_MILLIS;
        }
        totalExectime = 0;
        return String.format(TIME_REPORTING, execTimeFriendly, perProcessMillis);
    }

    private Map<String, Integer> makeUpData(Integer[] data) {
        List<String> labels = new ArrayList<>();
        Arrays.stream(nameables).forEachOrdered(s -> labels.add(s.name()));
        String[] labelz = labels.toArray(new String[labels.size()]);
        Map<String, Integer> datan = new LinkedHashMap<>();
        for (int i = 0; i < labelz.length; i++) {
            Integer value = i < data.length ? data[i] : 0;
            datan.put(labelz[i], value);
        }
        return datan;
    }

    private boolean collectorIsAvailable() {
        if (cycleHistogramCollector == null) {
            Optional<T> processSubjectOptional = persistenceService.provideSubjectStream().filter(Objects::nonNull).findFirst();
            if (processSubjectOptional.isPresent()) {
                ProcessSubject<T> subject = processSubjectOptional.get();
                LifeCycleProvider lifeCycleProvider = new LifeCycleProvider() {
                    @Override
                    public Enum[] getCycle() {
                        return null; // will not be utilized here... //NOSONAR
                    }

                    @Override
                    public Nameable[] getCycleAsNameables() {
                        return nameables;
                    }

                    @Override
                    public Enum getCurrentStatus() {
                        return subject.getCurrentStatus();
                    }

                    @Override
                    public String getCurrentProcess() {
                        return subject.getCurrentProcess();
                    }
                };
                cycleHistogramCollector = new LifeCycleHistogramCollector(lifeCycleProvider);
            }
        }
        return cycleHistogramCollector != null;
    }

    private void setupNameAbles() {
        List<Nameable> enumValues = Arrays.stream(criteriaStates[0].getClass().getEnumConstants()).filter(Objects::nonNull).map(Object::toString).map(s -> (Nameable) () -> s).collect(Collectors.toList());
        nameables = enumValues.toArray(new Nameable[enumValues.size()]);
    }
}
