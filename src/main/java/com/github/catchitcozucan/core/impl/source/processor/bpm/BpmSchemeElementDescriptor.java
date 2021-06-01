/**
 *    Original work by Ola Aronsson 2020
 *    Courtesy of nollettnoll AB &copy; 2012 - 2020
 *
 *    Licensed under the Creative Commons Attribution 4.0 International (the "License")
 *    you may not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *                https://creativecommons.org/licenses/by/4.0/
 *
 *    The software is provided “as is”, without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other liability,
 *    whether in an action of contract, tort or otherwise, arising from, out of or
 *    in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl.source.processor.bpm;

import static com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants.UNDERSCORE;
import static com.github.catchitcozucan.core.util.MavenWriter.MESSAGE_SEPARATOR;

import com.github.catchitcozucan.core.impl.source.processor.EnumContainer;
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

    private static final String MY_STATE_NAME_PROPERTY = "myStateName";
    private static final String INDEX_PROPERTY = "index";
    private static final String EXPECTEDTYPEBEFORE_PROPERTY = "expectedTypeBefore";
    private static final String EXPECTEDTYPEAFTER_PROPERTY = "expectedTypeAfter";
    private static final String TASKNAME_PROPERTY = "taskName";
    private static final String STATUSUPONFAILURE_PROPERTY = "statusUponFailure";
    private static final String STATUSUPONSUCCESS_PROPERTY = "statusUponSuccess";
    private static final String PROCESS_NAME_PROPERTY = "ProcessName";
    private static final String STEP_METHOD_NAME_PROPERTY = "stepMethodName";
    private static final String ACTIVITY_PROPERTY = "Activity";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_SUCCESS_STATE = "In your status enum, state at index [UNKNOWN]  the statusUponFailure is missing in your generated step - this is the link to the connected success state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_S_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_SUCCESS_STATE = "In your status enum, state %s the statusUponFailure is missing in your generated step - this is the link to the connected success state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_SUCCESS_STATE = "In your status enum, state at index %d the statusUponFailure is missing in your generated step - this is the link to the connected success state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_FAILURE_STATE = "In your status enum, state  at index [UNKNOWN] the statusUponFailure is missing in your generated step - this is the link to the connected failure state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_S_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_FAILURE_STATE = "In your status enum, state %s the statusUponFailure is missing in your generated step - this is the link to the connected failure state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_FAILURE_STATE = "In your status enum, state at index %d the statusUponFailure is missing in your generated step - this is the link to the connected failure state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_THE_DESCRIPTION_IS_MISSING_IN_YOUR_GENERATED_STEP_WE_CONSIDER_THIS_YOUR_TASK_NAM = "In your status enum, state at index [UNKNOWN] the description is missing in your generated step - we consider this your task nam";
    private static final String IN_YOUR_STATUS_ENUM_STATE_S_THE_DESCRIPTION_IS_MISSING_IN_YOUR_GENERATED_STEP_WE_CONSIDER_THIS_YOUR_TASK_NAME = "In your status enum, state %s the description is missing in your generated step - we consider this your task name";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_THE_DESCRIPTION_IS_MISSING_IN_YOUR_GENERATED_STEP_WE_CONSIDER_THIS_YOUR_TASK_NAME = "In your status enum, state at index %d the description is missing in your generated step - we consider this your task name";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_SHOULD_BE_FOLLOWED_BY_ANOTHER_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_YOUR_FINISH_STATE = "In your status enum, state at index [UNKNOWN] should be followed by another state OR if it's the first state, we should understand that it's your finish state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_S_SHOULD_BE_FOLLOWED_BY_ANOTHER_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_YOUR_FINISH_STATE = "In your status enum, state %s should be followed by another state OR if it's the first state, we should understand that it's your finish state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_SHOULD_BE_FOLLOWED_BY_ANOTHER_STATE_OR_IF_IT_S_THE_LAST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_YOUR_FINISH_STATE = "In your status enum, state at index %d should be followed by another state OR if it's the last state, we should understand that it's your finish state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_SHOULD_HAVE_A_PREVIOUS_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_THE_PRE_PROCESS_STATE = "In your status enum, state at index [UNKNOWN] should have a previous state OR if it's the first state, we should understand that it's the pre-process state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_S_SHOULD_HAVE_A_PREVIOUS_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_THE_PRE_PROCESS_STATE = "In your status enum, state %s should have a previous state OR if it's the first state, we should understand that it's the pre-process state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_SHOULD_HAVE_A_PREVIOUS_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_THE_PRE_PROCESS_STATE = "In your status enum, state at index %d should have a previous state OR if it's the first state, we should understand that it's the pre-process state";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_LACKS_A_NAME = "In your status enum, state at index %d lacks a name";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_LACKS_A_PROCESS_NAME = "In your status enum, state at index %d lacks a processName";
    private static final String IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_YOUR_POSITIVE_OP_FAILURE = "In your status enum, state at index %d your positive state does NOT have an even index. This means that your statues are NOT lined up in the suppported order [ INITIAL_STATE, FAILURE_FST_OP, FST_OP_SUCCESSFULL, SND_OP_FAILURE.. ]";

    public enum Type {
        START_EVENT, ACTIVITY, FINISH_STATE
    }

    enum TypeInBetween {
        FLOW, GATEWAY, DEFINITIONS, PROCESS
    }

    private final String id = new StringBuilder(ACTIVITY_PROPERTY).append(getBpmStyleId()).toString();
    private final String myStateName;
    private final Integer index;
    private final Type expectedTypeBefore;
    private final Type expectedTypeAfter;
    private final String taskName;
    private final String statusUponFailure;
    private final String statusUponSuccess;
    private final String processName;
    private final EnumContainer enumContainer;
    private final String stepMethodName;
    private final Boolean acceptEnumFailures;

    public static String generateIdForTypeInBetween(TypeInBetween typeInBetweens) {
        return new StringBuilder(typeInBetweens.name()).append(getBpmStyleId()).toString();
    }

    public static String generateIdForType(Type typeInBetweens) {
        return new StringBuilder(typeInBetweens.name()).append(getBpmStyleId()).toString();
    }

    private static final String getBpmStyleId() {
        return new StringBuilder(UNDERSCORE).append(IdGenerator.getInstance().getIdMoreRandom(7, 0).toLowerCase()).toString();
    }

    // @formatter:off
	@Override
	public String doToString() {
		return new ToStringBuilder(PROCESS_NAME_PROPERTY, processName)
				.append(MY_STATE_NAME_PROPERTY, myStateName)
                .append(INDEX_PROPERTY, index)
				.append(EXPECTEDTYPEBEFORE_PROPERTY, expectedTypeBefore)
				.append(EXPECTEDTYPEAFTER_PROPERTY, expectedTypeAfter)
				.append(TASKNAME_PROPERTY, taskName)
                .append(STEP_METHOD_NAME_PROPERTY, stepMethodName)
				.append(STATUSUPONFAILURE_PROPERTY, statusUponFailure)
				.append(STATUSUPONSUCCESS_PROPERTY, statusUponSuccess)
				.toString();
	}
	// @formatter:on

    public String validateForErrorOutput() { // NOSONAR
        StringBuilder formattingErrors = new StringBuilder();
        boolean indexIsUnknown = false;
        boolean enumNameIsUnknown = false;
        if (index == null) {
            indexIsUnknown = true;
        } else {
            if (index % 2 != 0) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_YOUR_POSITIVE_OP_FAILURE, index)).append(MESSAGE_SEPARATOR);
            }
        }

        if (StringUtils.isBlank(processName)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_LACKS_A_PROCESS_NAME, index)).append(MESSAGE_SEPARATOR);
            } else {
                enumNameIsUnknown = true;
            }
        }
        if (StringUtils.isBlank(myStateName)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_LACKS_A_NAME, index)).append(MESSAGE_SEPARATOR); // yes, HIGHLY unlikely in an enum :)
            } else {
                enumNameIsUnknown = true;
            }
        }
        if (expectedTypeBefore == null) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_SHOULD_HAVE_A_PREVIOUS_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_THE_PRE_PROCESS_STATE, index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_S_SHOULD_HAVE_A_PREVIOUS_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_THE_PRE_PROCESS_STATE, enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_SHOULD_HAVE_A_PREVIOUS_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_THE_PRE_PROCESS_STATE).append(MESSAGE_SEPARATOR);
            }

        }
        if (expectedTypeAfter == null) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_SHOULD_BE_FOLLOWED_BY_ANOTHER_STATE_OR_IF_IT_S_THE_LAST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_YOUR_FINISH_STATE, index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_S_SHOULD_BE_FOLLOWED_BY_ANOTHER_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_YOUR_FINISH_STATE, enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_SHOULD_BE_FOLLOWED_BY_ANOTHER_STATE_OR_IF_IT_S_THE_FIRST_STATE_WE_SHOULD_UNDERSTAND_THAT_IT_S_YOUR_FINISH_STATE).append(MESSAGE_SEPARATOR);
            }
        }
        if (StringUtils.isBlank(taskName)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_THE_DESCRIPTION_IS_MISSING_IN_YOUR_GENERATED_STEP_WE_CONSIDER_THIS_YOUR_TASK_NAME, index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_S_THE_DESCRIPTION_IS_MISSING_IN_YOUR_GENERATED_STEP_WE_CONSIDER_THIS_YOUR_TASK_NAME, enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_THE_DESCRIPTION_IS_MISSING_IN_YOUR_GENERATED_STEP_WE_CONSIDER_THIS_YOUR_TASK_NAM).append(MESSAGE_SEPARATOR);
            }
        }
        if (StringUtils.isBlank(statusUponFailure)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_FAILURE_STATE, index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_S_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_FAILURE_STATE, enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_FAILURE_STATE).append(MESSAGE_SEPARATOR);
            }
        }
        if (StringUtils.isBlank(statusUponSuccess)) {
            if (!indexIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_N_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_SUCCESS_STATE, index)).append(MESSAGE_SEPARATOR);
            } else if (!enumNameIsUnknown) {
                formattingErrors.append(String.format(IN_YOUR_STATUS_ENUM_STATE_S_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_SUCCESS_STATE, enumNameIsUnknown)).append(MESSAGE_SEPARATOR);
            } else {
                formattingErrors.append(IN_YOUR_STATUS_ENUM_STATE_AT_INDEX_UNKNOWN_THE_STATUS_UPON_FAILURE_IS_MISSING_IN_YOUR_GENERATED_STEP_THIS_IS_THE_LINK_TO_THE_CONNECTED_SUCCESS_STATE).append(MESSAGE_SEPARATOR);
            }
        }
        return formattingErrors.toString();
    }

    @Override
    public int compareTo(BpmSchemeElementDescriptor bpmSchemeElementDescriptor) { // NOSONAR - _I AM_ having a specilized equals
        return index.compareTo(bpmSchemeElementDescriptor.index);
    }

}
