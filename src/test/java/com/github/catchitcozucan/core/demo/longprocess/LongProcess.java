package com.github.catchitcozucan.core.demo.longprocess;

import com.github.catchitcozucan.core.CompileOptions;
import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.ProcessBase;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CompileOptions(relativeBpmDirectoryPath = "../../../../../../../resources/bpmSchemes", bpmActivitiesPerColumn = "3")
public class LongProcess extends ProcessBase<LongSubject> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LongProcess.class);

	protected LongProcess(LongSubject processSubject, PersistenceService persistenceService) {
		super(processSubject, persistenceService);
	}

	@Override
	public String name() {
		return "The looooong process";
	}

	@Override
	public Enum<?>[] criteriaStates() {
		return CRITERIA_STATES;
	}

	@Override
	public Enum<?> finishedState() {
		return FINISH_STATE;
	}

	@Override
	public void process() {
	}

	@MakeStep(description = "step1", statusUponSuccess = "Status.STATE1", statusUponFailure = "Status.FAILED_STATE1", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step1() {
	}

	@MakeStep(description = "step2", statusUponSuccess = "Status.STATE2", statusUponFailure = "Status.FAILED_STATE2", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step2() {
	}

	@MakeStep(description = "step3", statusUponSuccess = "Status.STATE3", statusUponFailure = "Status.FAILED_STATE3", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step3() {
	}

	@MakeStep(description = "step4", statusUponSuccess = "Status.STATE4", statusUponFailure = "Status.FAILED_STATE4", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step4() {
	}

	@MakeStep(description = "step5", statusUponSuccess = "Status.STATE5", statusUponFailure = "Status.FAILED_STATE5", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step5() {
	}

	@MakeStep(description = "step6", statusUponSuccess = "Status.STATE6", statusUponFailure = "Status.FAILED_STATE6", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step6() {
	}

	@MakeStep(description = "step7", statusUponSuccess = "Status.STATE7", statusUponFailure = "Status.FAILED_STATE7", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step7() {
	}

	@MakeStep(description = "step8", statusUponSuccess = "Status.STATE8", statusUponFailure = "Status.FAILED_STATE8", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step8() {
	}

	@MakeStep(description = "step9", statusUponSuccess = "Status.STATE9", statusUponFailure = "Status.FAILED_STATE9", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step9() {
	}

	@MakeStep(description = "step10", statusUponSuccess = "Status.STATE10", statusUponFailure = "Status.FAILED_STATE10", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step10() {
	}

	@MakeStep(description = "step11", statusUponSuccess = "Status.STATE11", statusUponFailure = "Status.FAILED_STATE11", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step11() {
	}

	@MakeStep(description = "step12", statusUponSuccess = "Status.STATE12", statusUponFailure = "Status.FAILED_STATE12", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step12() {
	}

	@MakeStep(description = "step13", statusUponSuccess = "Status.STATE13", statusUponFailure = "Status.FAILED_STATE13", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step13() {
	}

	@MakeStep(description = "step14", statusUponSuccess = "Status.STATE14", statusUponFailure = "Status.FAILED_STATE14", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step14() {
	}

	@MakeStep(description = "step15", statusUponSuccess = "Status.STATE15", statusUponFailure = "Status.FAILED_STATE15", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step15() {
	}

	@MakeStep(description = "step16", statusUponSuccess = "Status.STATE16", statusUponFailure = "Status.FAILED_STATE16", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step16() {
	}

	@MakeStep(description = "step17", statusUponSuccess = "Status.STATE17", statusUponFailure = "Status.FAILED_STATE17", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step17() {
	}

	@MakeStep(description = "step18", statusUponSuccess = "Status.STATE18", statusUponFailure = "Status.FAILED_STATE18", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step18() {
	}

	@MakeStep(description = "step19", statusUponSuccess = "Status.STATE19", statusUponFailure = "Status.FAILED_STATE19", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step19() {
	}

	@MakeStep(description = "step20", statusUponSuccess = "Status.STATE20", statusUponFailure = "Status.FAILED_STATE20", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step20() {
	}

	@MakeStep(description = "step21", statusUponSuccess = "Status.STATE21", statusUponFailure = "Status.FAILED_STATE21", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step21() {
	}

	///////CHKSUM: E1F46BA2014D5912A0D5116DB000E593XXXXXXXX/////////////////////
	//
	// The following code is generated by the DaProcessStepProcessor 
	// written by Ola Aronsson in 2020, courtesy of nollettnoll AB
	//
	// DO NOT edit this section. Modify @MakeStep or CHKSUM (then keep length)  to re-generate.
	//

	//@formatter:off DO_NOT_FORMAT

	private final com.github.catchitcozucan.core.ProcessStep step2Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step2();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step2";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE2;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE2;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step19Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step19();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step19";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE19;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE19;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step1Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step1();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step1";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE1;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE1;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step18Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step18();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step18";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE18;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE18;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step17Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step17();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step17";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE17;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE17;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step16Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step16();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step16";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE16;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE16;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step15Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step15();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step15";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE15;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE15;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step14Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step14();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step14";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE14;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE14;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step9Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step9();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step9";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE9;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE9;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step7Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step7();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step7";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE7;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE7;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step8Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step8();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step8";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE8;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE8;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step4Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step4();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step4";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE4;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE4;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step3Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step3();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step3";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE3;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE3;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step6Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step6();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step6";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE6;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE6;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step5Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step5();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step5";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE5;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE5;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step13Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step13();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step13";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE13;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE13;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step12Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step12();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step12";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE12;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE12;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step11Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step11();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step11";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE11;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE11;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step10Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step10();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step10";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE10;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE10;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step21Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step21();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step21";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE21;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE21;
		}

	};

	private final com.github.catchitcozucan.core.ProcessStep step20Step = new com.github.catchitcozucan.core.ProcessStep(){ 

		@Override
		public void execute() {
			step20();
		}

		@Override
		public String processName() {
			return "LONGPROCESS";
		}

		@Override
		public String description() {
			return "step20";
		}

		@Override
		public Enum<?> statusUponSuccess() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.STATE20;
		}

		@Override
		public Enum<?> statusUponFailure() {
			return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE20;
		}

	};

	public static final String PROCESS_NAME = com.github.catchitcozucan.core.demo.longprocess.LongProcess.class.getName().toUpperCase();
	public static final String PROCESS_NAME_SHORT = com.github.catchitcozucan.core.demo.longprocess.LongProcess.class.getSimpleName().toUpperCase();

	public static final Enum<?> FINISH_STATE = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.values()[com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.values().length - 1];

	public static final com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status[] CRITERIA_STATES = {
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.ENTRY_STATE,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE1,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE2,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE3,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE4,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE5,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE6,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE7,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE8,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE9,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE10,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE11,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE12,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE13,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE14,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE15,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE16,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE17,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE18,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE19,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE20,
		com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE21
	};

	public static java.util.List<String> criteriaProcessesStatusesAsStrings() {
		return java.util.Arrays.asList(CRITERIA_STATES).stream().map(Enum::name).collect(java.util.stream.Collectors.toList());
	}

	public static java.util.List<String> allProcessesStatusesAsStrings() {
		return java.util.Arrays.asList(com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.values()).stream().map(Enum::name).collect(java.util.stream.Collectors.toList());
	}

	public void processInternal(com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status currentStatus) {
		switch (currentStatus) {
				case ENTRY_STATE:
				case FAILED_STATE1:
					executeStep(step1Step);
					break;
				case STATE1:
				case FAILED_STATE2:
					executeStep(step2Step);
					break;
				case STATE2:
				case FAILED_STATE3:
					executeStep(step3Step);
					break;
				case STATE3:
				case FAILED_STATE4:
					executeStep(step4Step);
					break;
				case STATE4:
				case FAILED_STATE5:
					executeStep(step5Step);
					break;
				case STATE5:
				case FAILED_STATE6:
					executeStep(step6Step);
					break;
				case STATE6:
				case FAILED_STATE7:
					executeStep(step7Step);
					break;
				case STATE7:
				case FAILED_STATE8:
					executeStep(step8Step);
					break;
				case STATE8:
				case FAILED_STATE9:
					executeStep(step9Step);
					break;
				case STATE9:
				case FAILED_STATE10:
					executeStep(step10Step);
					break;
				case STATE10:
				case FAILED_STATE11:
					executeStep(step11Step);
					break;
				case STATE11:
				case FAILED_STATE12:
					executeStep(step12Step);
					break;
				case STATE12:
				case FAILED_STATE13:
					executeStep(step13Step);
					break;
				case STATE13:
				case FAILED_STATE14:
					executeStep(step14Step);
					break;
				case STATE14:
				case FAILED_STATE15:
					executeStep(step15Step);
					break;
				case STATE15:
				case FAILED_STATE16:
					executeStep(step16Step);
					break;
				case STATE16:
				case FAILED_STATE17:
					executeStep(step17Step);
					break;
				case STATE17:
				case FAILED_STATE18:
					executeStep(step18Step);
					break;
				case STATE18:
				case FAILED_STATE19:
					executeStep(step19Step);
					break;
				case STATE19:
				case FAILED_STATE20:
					executeStep(step20Step);
					break;
				case STATE20:
				case FAILED_STATE21:
					executeStep(step21Step);
					break;
			default:
				throw new ProcessRuntimeException(String.format("Got bad input : %s %s which is in state %s [%s]",
					PROCESS_NAME,
					getSubject().id(),
					getSubject().getCurrentStatus().name(),
					currentStatusDescription()));
		 }
	}

	//@formatter:on END DO_NOT_FORMAT

	///////////////////////////////////////////////////////////////////////////////
	//
	// End DaProcessStepProcessor generation
	//

}