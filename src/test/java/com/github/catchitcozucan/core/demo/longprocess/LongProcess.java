package com.github.catchitcozucan.core.demo.longprocess;

import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.ProcessBpmSchemeRepo;
import com.github.catchitcozucan.core.ProcessStep;
import com.github.catchitcozucan.core.impl.ProcessBase;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ProcessBpmSchemeRepo(relativePath = "../../../../../../resources/bpmSchemes", activitiesPerColumn = "3")
public class LongProcess extends ProcessBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(LongProcess.class);

	public static final LongProcessStatus.Status[] CRITERIA_STATES = { LongProcessStatus.Status.ENTRY_STATE,
            LongProcessStatus.Status.FAILED_STATE1,
            LongProcessStatus.Status.FAILED_STATE2,
            LongProcessStatus.Status.FAILED_STATE3,
            LongProcessStatus.Status.FAILED_STATE4,
            LongProcessStatus.Status.FAILED_STATE5,
            LongProcessStatus.Status.FAILED_STATE6,
            LongProcessStatus.Status.FAILED_STATE7,
            LongProcessStatus.Status.FAILED_STATE8,
            LongProcessStatus.Status.FAILED_STATE9,
            LongProcessStatus.Status.FAILED_STATE10,
            LongProcessStatus.Status.FAILED_STATE11,
            LongProcessStatus.Status.FAILED_STATE12,
            LongProcessStatus.Status.FAILED_STATE13,
            LongProcessStatus.Status.FAILED_STATE14,
            LongProcessStatus.Status.FAILED_STATE15,
            LongProcessStatus.Status.FAILED_STATE16,
            LongProcessStatus.Status.FAILED_STATE17,
            LongProcessStatus.Status.FAILED_STATE18,
            LongProcessStatus.Status.FAILED_STATE19,
            LongProcessStatus.Status.FAILED_STATE20,
            LongProcessStatus.Status.FAILED_STATE21
    };

	protected LongProcess(ProcessSubject processSubject, PersistenceService persistenceService) {
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
		return LongProcessStatus.Status.STATE21;
	}

	@Override
	public void process() {
	}

	@MakeStep(description = "step1", statusUponSuccess = "Status.STATE1", statusUponFailure = "Status.FAILED_STATE1", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
	private void step1() {}

    @MakeStep(description = "step2", statusUponSuccess = "Status.STATE2", statusUponFailure = "Status.FAILED_STATE2", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step2() {}

    @MakeStep(description = "step3", statusUponSuccess = "Status.STATE3", statusUponFailure = "Status.FAILED_STATE3", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step3() {}

    @MakeStep(description = "step4", statusUponSuccess = "Status.STATE4", statusUponFailure = "Status.FAILED_STATE5", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step4() {}

    @MakeStep(description = "step5", statusUponSuccess = "Status.STATE5", statusUponFailure = "Status.FAILED_STATE6", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step5() {}

    @MakeStep(description = "step6", statusUponSuccess = "Status.STATE6", statusUponFailure = "Status.FAILED_STATE6", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step6() {}

    @MakeStep(description = "step7", statusUponSuccess = "Status.STATE7", statusUponFailure = "Status.FAILED_STATE7", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step7() {}

    @MakeStep(description = "step8", statusUponSuccess = "Status.STATE8", statusUponFailure = "Status.FAILED_STATE8", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step8() {}

    @MakeStep(description = "step9", statusUponSuccess = "Status.STATE9", statusUponFailure = "Status.FAILED_STATE9", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step9() {}

    @MakeStep(description = "step10", statusUponSuccess = "Status.STATE10", statusUponFailure = "Status.FAILED_STATE10", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step10() {}

    @MakeStep(description = "step11", statusUponSuccess = "Status.STATE11", statusUponFailure = "Status.FAILED_STATE11", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step11() {}

    @MakeStep(description = "step12", statusUponSuccess = "Status.STATE12", statusUponFailure = "Status.FAILED_STATE12", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step12() {}

    @MakeStep(description = "step13", statusUponSuccess = "Status.STATE13", statusUponFailure = "Status.FAILED_STATE13", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step13() {}

    @MakeStep(description = "step14", statusUponSuccess = "Status.STATE14", statusUponFailure = "Status.FAILED_STATE14", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step14() {}

    @MakeStep(description = "step15", statusUponSuccess = "Status.STATE15", statusUponFailure = "Status.FAILED_STATE15", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step15() {}

    @MakeStep(description = "step16", statusUponSuccess = "Status.STATE16", statusUponFailure = "Status.FAILED_STATE16", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step16() {}

    @MakeStep(description = "step17", statusUponSuccess = "Status.STATE17", statusUponFailure = "Status.FAILED_STATE17", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step17() {}

    @MakeStep(description = "step18", statusUponSuccess = "Status.STATE18", statusUponFailure = "Status.FAILED_STATE18", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step18() {}

    @MakeStep(description = "step19", statusUponSuccess = "Status.STATE19", statusUponFailure = "Status.FAILED_STATE19", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step19() {}

    @MakeStep(description = "step20", statusUponSuccess = "Status.STATE20", statusUponFailure = "Status.FAILED_STATE20", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step20() {}

    @MakeStep(description = "step21", statusUponSuccess = "Status.STATE21", statusUponFailure = "Status.FAILED_STATE21", enumStateProvider = com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.class)
    private void step21() {}

    ///////CHKSUM: 1534E83EBA40B1AC4593C4CC99566972XXXXXXXX/////////////////////
    //
    // The following code is generated by the DaProcessStepProcessor 
    // written by Ola Aronsson in 2020, courtesy of nollettnoll AB
    //
    // DO NOT edit this section. Modify @MakeStep or CHKSUM (then keep length)  to re-generate.
    //

    private final ProcessStep step2Step = new ProcessStep(){

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

    private final ProcessStep step19Step = new ProcessStep(){ 

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

    private final ProcessStep step1Step = new ProcessStep(){ 

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

    private final ProcessStep step18Step = new ProcessStep(){ 

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

    private final ProcessStep step17Step = new ProcessStep(){ 

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

    private final ProcessStep step16Step = new ProcessStep(){ 

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

    private final ProcessStep step15Step = new ProcessStep(){ 

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

    private final ProcessStep step14Step = new ProcessStep(){ 

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

    private final ProcessStep step9Step = new ProcessStep(){ 

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

    private final ProcessStep step7Step = new ProcessStep(){ 

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

    private final ProcessStep step8Step = new ProcessStep(){ 

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

    private final ProcessStep step4Step = new ProcessStep(){ 

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
            return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE5;
        }

    };

    private final ProcessStep step3Step = new ProcessStep(){ 

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

    private final ProcessStep step6Step = new ProcessStep(){ 

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

    private final ProcessStep step5Step = new ProcessStep(){ 

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
            return com.github.catchitcozucan.core.demo.longprocess.LongProcessStatus.Status.FAILED_STATE6;
        }

    };

    private final ProcessStep step13Step = new ProcessStep(){ 

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

    private final ProcessStep step12Step = new ProcessStep(){ 

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

    private final ProcessStep step11Step = new ProcessStep(){ 

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

    private final ProcessStep step10Step = new ProcessStep(){ 

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

    private final ProcessStep step21Step = new ProcessStep(){ 

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

    private final ProcessStep step20Step = new ProcessStep(){ 

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

    ///////////////////////////////////////////////////////////////////////////////
    //
    // End DaProcessStepProcessor generation
    //

}