package com.github.catchitcozucan.core.impl.source.processor;

import static com.github.catchitcozucan.core.util.MavenWriter.MESSAGE_SEPARATOR;

import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;
import com.github.catchitcozucan.core.internal.util.domain.StringUtils;
import com.github.catchitcozucan.core.internal.util.domain.ToStringBuilder;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class BpmSchemeElementDescriptor extends BaseDomainObject implements Comparable<BpmSchemeElementDescriptor> {

    private static final String MY_STATE_NAME = "myStateName";
    private static final String INDEX = "index";
    private static final String EXPECTEDTYPEBEFORE = "expectedTypeBefore";
    private static final String EXPECTEDTYPEAFTER = "expectedTypeAfter";
    private static final String TASKNAME = "taskName";
    private static final String STATUSUPONFAILURE = "statusUponFailure";
    private static final String STATUSUPONSUCCESS = "statusUponSuccess";
    public static final String UNDERSCORE = "_";
    public static final String ACTIVITY = "Activity";

    enum Type {
        START_ELEMENT, TASK_ELEMENT, FINISH_STATE;
    }

    enum TypeInBetween {
        Flow, Gateway, Definitions, Process;
    }

    private final String id = new StringBuilder(ACTIVITY).append(getBpmStyleId()).toString();
    private final String myStateName;
    private final Integer index;
    private final Type expectedTypeBefore;
    private final Type expectedTypeAfter;
    private final String taskName;
    private final String statusUponFailure;
    private final String statusUponSuccess;

    public static String generateIdForInBetween(TypeInBetween typeInBetweens) {
        return new StringBuilder(typeInBetweens.name()).append(getBpmStyleId()).toString();
    }

    private static final String getBpmStyleId() {
        return new StringBuilder(UNDERSCORE).append(IdGenerator.getInstance().getIdMoreRandom(7, 0).toLowerCase()).toString();
    }

    // @formatter:off
	@Override
	public String doToString() {
		return new ToStringBuilder(MY_STATE_NAME, myStateName)
				.append(INDEX, index)
				.append(EXPECTEDTYPEBEFORE, expectedTypeBefore)
				.append(EXPECTEDTYPEAFTER, expectedTypeAfter)
				.append(TASKNAME, taskName)
				.append(STATUSUPONFAILURE, statusUponFailure)
				.append(STATUSUPONSUCCESS, statusUponSuccess)
				.toString();
	}
	// @formatter:on

    public String validateForErrorOutput() {
        StringBuilder formattingErrors = new StringBuilder();
        boolean indexIsUnknown = false;
        boolean enumNameIsUnknown = false;
        if (index == null) {
            indexIsUnknown = true;
        } else {
            if (index % 2 != 0) {
                formattingErrors.append(String.format("In your status enum, state at index %n your positive state does NOT have an even index. This means that your statues are NOT lined up in the suppported order [ INITIAL_STATE, FAILURE_FST_OP, FST_OP_SUCCESSFULL, SND_OP_FAILURE.. ]", index)).append(MESSAGE_SEPARATOR);
            }
        }

        if (StringUtils.isBlank(myStateName)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state at index %n lacks a name", index)).append(MESSAGE_SEPARATOR); // yes, HIGHLY unlikely in an enum :)
            } else {
                enumNameIsUnknown = true;
            }
        }
        if (expectedTypeBefore == null) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state at index %n should have a previous state OR if it's the first state, we should understand that it's the pre-process state", index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state %s should have a previous state OR if it's the first state, we should understand that it's the pre-process state", enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append("In your status enum, state at index [UNKNOWN] should have a previous state OR if it's the first state, we should understand that it's the pre-process state").append(MESSAGE_SEPARATOR);
            }

        }
        if (expectedTypeAfter == null) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state at index %n should be followed by another state OR if it's the last state, we should understand that it's your finish state", index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state %s should be followed by another state OR if it's the first state, we should understand that it's your finish state", enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append("In your status enum, state at index [UNKNOWN] should be followed by another state OR if it's the first state, we should understand that it's your finish state").append(MESSAGE_SEPARATOR);
            }
        }
        if (StringUtils.isBlank(taskName)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state at index %n the description is missing in your generated step - we consider this your task name", index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state %s the description is missing in your generated step - we consider this your task name", enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append("In your status enum, state at index [UNKNOWN] the description is missing in your generated step - we consider this your task nam").append(MESSAGE_SEPARATOR);
            }
        }
        if (StringUtils.isBlank(statusUponFailure)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state at index %n the statusUponFailure is missing in your generated step - this is the link to the connected failure state", index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state %s the statusUponFailure is missing in your generated step - this is the link to the connected failure state", enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append("In your status enum, state  at index [UNKNOWN] the statusUponFailure is missing in your generated step - this is the link to the connected failure state").append(MESSAGE_SEPARATOR);
            }
        }
        if (StringUtils.isBlank(statusUponSuccess)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state at index %n the statusUponFailure is missing in your generated step - this is the link to the connected success state", index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format("In your status enum, state %s the statusUponFailure is missing in your generated step - this is the link to the connected success state", enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append("In your status enum, state at index [UNKNOWN]  the statusUponFailure is missing in your generated step - this is the link to the connected success state").append(MESSAGE_SEPARATOR);
            }
        }
        return formattingErrors.toString();
    }

    @Override
    public int compareTo(BpmSchemeElementDescriptor bpmSchemeElementDescriptor) {
        return index.compareTo(bpmSchemeElementDescriptor.index);
    }
}
