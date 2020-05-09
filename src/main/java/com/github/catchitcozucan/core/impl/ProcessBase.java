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

import java.lang.reflect.Method;

import com.github.catchitcozucan.core.ErrorCodeCarrier;
import com.github.catchitcozucan.core.ProcessStep;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.interfaces.IsolationLevel;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.util.ThrowableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ProcessBase implements Process {

    private boolean hasBailed; // NOSONAR - THIS CODE IS JUST A SKETCH SO FAR
    private final ProcessSubject processSubject; // NOSONAR - THIS CODE IS JUST A SKETCH SO FAR
    private final Method stsMethod;
    private final Class statusClass;
    private static final String STS_NO_ARG_METHOD_SIGNATURE = "getSts";
    private PersistenceService persistenceService;
    private Enum<?> statusUponFailure = null;

    private static Logger LOGGER = null; // NOSONAR

    static {
        ProcessLogging.initLogging();
        LOGGER = LoggerFactory.getLogger(ProcessBase.class);
    }

    protected ProcessBase(ProcessSubject processSubject, PersistenceService persistenceService) {
        ((ProcessSubjectBase) processSubject).clearError();
        statusUponFailure = null;
        this.persistenceService = persistenceService;
        this.processSubject = processSubject;
        Method tmpMethod = null;
        Class tmpClass = null;
        try {
            if (processSubject.getCurrentStatus() == null) {
                throw new ProcessRuntimeException(String.format("Your status CAN NEVER BE NULL! Your ProcessSubject-impl %s is wrong, perhaps you have just extended the base class but did override the getStatus()-method!", processSubject.getClass())); // NOSONAR
            }
            tmpClass = processSubject.getCurrentStatus().getClass();
            tmpMethod = tmpClass.getDeclaredMethod(STS_NO_ARG_METHOD_SIGNATURE, new Class[] {}); // NOSONAR
        } catch (NoSuchMethodException ignore) {

            if (!(processSubject.getCurrentStatus() instanceof Enum)) {
                throw new ProcessRuntimeException(String.format("Your status class %s HAS NOT implemented the simple getSts()-no-arg-method nor does it seem to implement name() (is it an enum!!!)!", processSubject.getCurrentStatus().getClass().getName()));
            }
        }
        stsMethod = tmpMethod;
        statusClass = tmpClass;
    }

    @Override
    public IsolationLevel.Level provideIsolationLevel() {
        return IsolationLevel.Level.INCLUSIVE;
    }

    @Override
    public RejectionAction provideRejectionAction() {
        return RejectionAction.PUT_ON_WAITING_LIST;
    }

    @Override
    public final Type provideType() {
        return Type.PROCESS;
    }

    //
    // Crucial information on the Error handling!
    //
    // 1. Upon exception we will ALWAYS bail (catch, log, set status and error code to your subject).
    // 2. ErrorCodeCarriers error codes will ALWAYS be logged AND set to the subject (though
    //    of course it is up to the PersistenceService implementation to actually save them in
    //    your db).
    // 2. plain ProcessRuntimeExceptions are considred "handled", will be logged as WARN, no stack.
    // 3. any other RuntimeException (here converted to InternalProcessOtherRuntimeException) will log stack but as WARN.
    // 4. all NON-RunException (here converted to InternalProcessNonRuntimeException) will log stack as ERROR.
    // 5. ProcessBase shall NEVER leak any exceptions. If it does, it is a serious
    //    internal code malfunction that may stop job execution!
    // 6. There is ONE exception : if your process impl does not extend ProcessBase,
    //    the job will never run, we will through on IllegalArgumentException and die.
    //
    protected boolean executeStep(ProcessStep toExecute) { // NOSONAR complex stuff this..
        if (!hasBailed()) {
            ((ProcessSubjectBase) getSubject()).clearError();
            setProcessName(toExecute.processName());
            currentStatusUponFailure(toExecute.statusUponFailure());
            String messageSuffix = String.format("process %s executing step '%s' on item %s for subject %s", toExecute.processName(), toExecute.description(), id(), subjectIdentifier());
            try {
                toExecute.execute();
            } catch (ProcessRuntimeException e) {
                bail(messageSuffix, e, toExecute.statusUponFailure(), evalutateForErrorCode((ErrorCodeCarrier) e)); // Exception, typiskt får vi in en ErrorCode - beror på kasss indata
            } catch (RuntimeException e) { // NOSONAR - it is LOGGED in the bail()-method. it should NOT be thrown
                if (e instanceof ErrorCodeCarrier) { // NOSONAR
                    bail(messageSuffix, new InternalProcessOtherRuntimeException(messageSuffix, e), toExecute.statusUponFailure(), evalutateForErrorCode((ErrorCodeCarrier) e));
                } else {
                    bail(messageSuffix, new InternalProcessOtherRuntimeException(messageSuffix, e), toExecute.statusUponFailure(), null);
                }
            } catch (Exception e) {
                if (e instanceof ErrorCodeCarrier) { // NOSONAR
                    bail(messageSuffix, new InternalProcessNonRuntimeException(messageSuffix, e), toExecute.statusUponFailure(), evalutateForErrorCode((ErrorCodeCarrier) e));
                } else {
                    bail(messageSuffix, new InternalProcessNonRuntimeException(messageSuffix, e), toExecute.statusUponFailure(), null);
                }
            } finally {
                currentStatusUponFailure(null);
                if (!hasBailed()) {
                    saveInStatus(toExecute.statusUponSuccess()); // Yihoo. det gick bra
                    LOGGER.info(String.format("Succeded %s. item is now in state %s [%s]", messageSuffix, currentStatusName(), currentStatusDescription())); // NOSONAR
                    if (finished()) {
                        logFinished();
                    } else {
                        process();
                    }
                }
            }
        }
        return !hasBailed();
    }

    private void setProcessName(String processName) {
        ((ProcessSubjectBase) processSubject).setProcessName(processName);
    }

    protected ProcessSubject getSubject() {
        return processSubject;
    }

    protected String currentStatusDescription() {
        return getStatusDescription();
    }

    boolean finished() {
        return processSubject.getCurrentStatus().equals(finishedState());
    }

    Enum<?> getCurrentStatus() { // NOSONAR we WORK ENUMS!
        return processSubject.getCurrentStatus();
    }

    String getStatusDescription() {
        try {
            String raw = null;
            if (stsMethod == null) {
                return processSubject.getCurrentStatus().name();
            } else {
                raw = stsMethod.invoke(processSubject.getCurrentStatus(), new Class[] {}).toString(); // NOSONAR
                if (raw.length() != 1) {
                    throw new ProcessRuntimeException(String.format("Status-description-enun is implemented wrongly - it should be a char of length 1, I got %s", raw));
                }
                return Character.toString(raw.charAt(0));
            }
        } catch (ClassCastException ce) {
            throw new ProcessRuntimeException(String.format("Sent in class %s does not match expected invoker-held-status-class %s", processSubject.getCurrentStatus().getClass().getName(), statusClass.getName()));
        } catch (Exception e) {
            throw new ProcessRuntimeException("Could not getStatusDescription", e);
        }
    }

    String getStatusName() {
        return processSubject.getCurrentStatus().name();
    }

    boolean hasBailed() {
        return hasBailed;
    }

    void bail() {
        hasBailed = true;
    }

    String currentStatusName() {
        return processSubject.getCurrentStatus().name();
    }

    String subjectIdentifier() {
        return processSubject.subjectIdentifier();
    }

    Integer id() {
        return processSubject.id();
    }

    private void currentStatusUponFailure(Enum<?> statusUponFailure) {
        this.statusUponFailure = statusUponFailure;
    }

    Enum<?> getCurrentStatusUponFailure() { // NOSONAR
        return statusUponFailure;
    }

    Integer evalutateForErrorCode(ErrorCodeCarrier e) {
        if (e.getErrorCode() == 0) {
            return null;
        } else {
            return Integer.valueOf(e.getErrorCode());
        }
    }

    void bail(String messageSuffix, Throwable t, Enum<?> statusUponFailure, Integer errorCode) {
        String errorCodeStr = null;
        hasBailed = true;
        if (errorCode != null) {
            ((ProcessSubjectBase) processSubject).setErrorCode(errorCode);
            errorCodeStr = Integer.toString(errorCode);
        }
        ((ProcessSubjectBase) processSubject).setStatus(statusUponFailure);
        persistenceService.save(processSubject);
        String message = null;
        if (errorCodeStr == null) {
            message = String.format("Processstep %s failed [%s]. Item %s is now in state %s [%s]", messageSuffix, ThrowableUtils.getTopStackInfo(t), id(), currentStatusName(), currentStatusDescription());
        } else {
            message = String.format("Processstep %s failed Errorcode %s [%s]. Item %s is now in state %s [%s]", messageSuffix, errorCodeStr, ThrowableUtils.getTopStackInfo(t), id(), currentStatusName(), currentStatusDescription());
        }
        if (t instanceof InternalJobNonRuntimeException) {
            message = message.replace("Processstep", "CRITICAL : processStep LEAKED to JOB during");
        }
        if (t instanceof ProcessRuntimeException) {
            if (t.getCause() != null) {
                message = new StringBuilder(message).append(" cause : ").append(ThrowableUtils.getTopStackInfo(t.getCause())).toString();
            }
            LOGGER.warn(message);
        } else if (t instanceof InternalJobNonRuntimeException || t instanceof InternalProcessNonRuntimeException) {
            LOGGER.error(message, t);
        } else {
            LOGGER.warn(message, t);
        }
    }

    private void logFinished() {
        LOGGER.info(String.format("Process instance of '%s' has successfully completed. Item %s has reached final state %s [%s]", name(), id(), currentStatusName(), currentStatusDescription())); //NOSONAR
    }

    private void saveInStatus(Enum<?> statusUponSuccess) {
        ((ProcessSubjectBase) processSubject).setStatus(statusUponSuccess);
        persistenceService.save(processSubject);
    }
}
