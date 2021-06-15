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

import java.io.Serializable;
import java.util.Arrays;

import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.source.processor.EnumContainer;
import com.github.catchitcozucan.core.impl.source.processor.Nameable;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.domain.ToStringBuilder;


public abstract class ProcessSubjectBase<T> extends BaseDomainObject implements ProcessSubject<T>, Serializable {

    public static final String COMMA_SPACE = ", ";
    public static final String LEFT_BRACKET = "[";
    public static final String RIGHT_BRACKET = "]";
    public static final String SET_STATUS_FROM_STRING_FAILED_WE_DO_NOT_KNOW_OF_ANY_STATUS_S_KNOWN_STATES_ARE_S = "setStatusFromString() failed - we do not know of any status : %s. Known states are %s";
    protected Enum<?> status;
    private Integer errorCode;
    private String processName;
    private static final String IDENTFIER = "id";
    private static final String SUBJECT_IDENTIFIER = "subjectIdentifier";

    public enum Status {
        PRE_PROCESS('£'), PROCESS_FINISHED('¤');
        private final Character sts;

        Status(Character sts) {
            this.sts = sts;
        }

        public Character getSts() {
            return sts;
        }
    }

    public void setStatus(Enum<?> status) {
        this.status = status;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode == null ? 0 : errorCode;
    }

    boolean hasError() {
        return errorCode != null;
    }

    void clearError() {
        this.errorCode = null;
    }

    @Override
    public Nameable[] getCycleAsNameables() {
        return EnumContainer.enumsToNameableArray(getCycle());
    }

    @Override
    public String doToString() {
        return new ToStringBuilder(IDENTFIER, id()).append(SUBJECT_IDENTIFIER, subjectIdentifier()).toString();
    }

    Enum<?> getStatus() { // NOSONAR
        return status;
    }

    @Override
    public Enum getCurrentStatus() {
        return status;
    }

    @Override
    public String getCurrentProcess() {
        return processName;
    }

    @Override
    public boolean isFinished() {
        Enum[] cycle = getCycle();
        return cycle[cycle.length - 1].equals(status);
    }

    public Enum getStatusFromString(String statusString) {
        return getStatusFromStringInner(statusString);
    }

    public void setStatusFromString(String statusString) {
        setStatus(getStatusFromStringInner(statusString));
    }

    private Enum getStatusFromStringInner(String statusString) {
        Enum status = EnumContainer.ofString(getCycle(), statusString);
        if (status != null) {
            return status;
        } else {
            StringBuilder statusBuilder = new StringBuilder(LEFT_BRACKET);
            Arrays.stream(EnumContainer.enumsToNameableArray(getCycle())).sequential().forEach(s -> statusBuilder.append(s.name()).append(COMMA_SPACE));
            if (statusBuilder.length() > 2) {
                statusBuilder.delete(statusBuilder.length() - 2, statusBuilder.length());
            }
            statusBuilder.append(RIGHT_BRACKET);
            throw new ProcessRuntimeException(String.format(SET_STATUS_FROM_STRING_FAILED_WE_DO_NOT_KNOW_OF_ANY_STATUS_S_KNOWN_STATES_ARE_S, statusString, statusBuilder.toString())); // NOSONAR
        }
    }
}
