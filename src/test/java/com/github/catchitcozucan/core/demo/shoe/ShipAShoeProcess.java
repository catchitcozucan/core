package com.github.catchitcozucan.core.demo.shoe;

import com.github.catchitcozucan.core.ProcessBpmSchemeRepo;
import com.github.catchitcozucan.core.ProcessStep;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.ProcessBase;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.demo.shoe.internal.LaceProvider;
import com.github.catchitcozucan.core.demo.shoe.internal.ShoeProvider;
import com.github.catchitcozucan.core.demo.test.support.io.IO;

@ProcessBpmSchemeRepo(relativePath = "../../../../../../resources/bpmSchemes", activitiesPerColumn = "3")
public class ShipAShoeProcess extends ProcessBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShipAShoeProcess.class);
	public static final OrderStatus.Status[] CRITERIA_STATES = { OrderStatus.Status.NEW_ORDER, OrderStatus.Status.SHOE_NOT_YET_AVAILABLE, OrderStatus.Status.LACES_NOT_IN_PLACE, OrderStatus.Status.PACKAGING_FAILED, OrderStatus.Status.SHIPPING_FAILED };

	public ShipAShoeProcess(ProcessSubject processSubject, PersistenceService persistenceService) {
		super(processSubject, persistenceService);
	}

	// typically a switch for processSubject.status
	// calling different private methods depending on particular state
	@Override
	public void process() {
		OrderStatus.Status status = (OrderStatus.Status) getSubject().getCurrentStatus();
		switch (status) {
			case NEW_ORDER:
			case SHOE_NOT_YET_AVAILABLE:
				executeStep(doGetShoeStep);
				break;
			case LACES_NOT_IN_PLACE:
			case SHOE_FETCHED_FROM_WAREHOUSE:
				executeStep(doGetLacesStep);
				break;
			case PACKAGING_FAILED:
			case LACES_IN_PLACE:
				executeStep(doGetPackagingStep);
				break;
			case SHIPPING_FAILED:
			case PACKED:
				executeStep(doSendPackageStep);
				break;
			default:
				Throwable t = new ProcessRuntimeException(String.format("Got bad input : order %s which is in state %s [%s]", getSubject().id(), getSubject().getCurrentStatus().name(), currentStatusDescription()));
				LOGGER.error("err", t);
				throw (RuntimeException) t;
		}
	}

	// returning an array of states to dig up for processeing from the database
	@Override
	public Enum<?>[] criteriaStates() {
		return CRITERIA_STATES;
	}

	// declaring the state upon which we consider the processing to be finished
	@Override
	public Enum<?> finishedState() {
		return OrderStatus.Status.SHIPPED;
	}

	@Override
	public String name() {
		return "fetch ordered shoe and ship it";
	}

	@MakeStep(statusUponFailure = "Status.SHOE_NOT_YET_AVAILABLE", statusUponSuccess = "Status.SHOE_FETCHED_FROM_WAREHOUSE", description = "getShoe", enumStateProvider = com.github.catchitcozucan.core.demo.shoe.OrderStatus.class, sourceEncoding = IO.DEF_ENCODING)
	private void doGetShoe() {
		Order order = (Order) getSubject();
		order.setShoe(ShoeProvider.getInstance().getShoe(order.getRequestedColor(), order.getRequestedSize()));
	}

	@MakeStep(statusUponFailure = "Status.LACES_NOT_IN_PLACE", statusUponSuccess = "Status.LACES_IN_PLACE", description = "fetchLaces", enumStateProvider = com.github.catchitcozucan.core.demo.shoe.OrderStatus.class, sourceEncoding = IO.DEF_ENCODING)
	private void doGetLaces() {
		Order order = (Order) getSubject();
		order.getShoe().setLaces(LaceProvider.getInstance().getFreshLaces());
	}

	@MakeStep(statusUponFailure = "Status.PACKAGING_FAILED", statusUponSuccess = "Status.PACKED", description = "packaging", enumStateProvider = com.github.catchitcozucan.core.demo.shoe.OrderStatus.class, sourceEncoding = IO.DEF_ENCODING)
	private void doGetPackaging() {
		Order order = (Order) getSubject();
		order.packageOrder();
	}

	@MakeStep(statusUponFailure = "Status.SHIPPING_FAILED", statusUponSuccess = "Status.SHIPPED", description = "shipping", enumStateProvider = com.github.catchitcozucan.core.demo.shoe.OrderStatus.class, sourceEncoding = IO.DEF_ENCODING)
	private void doSendPackage() {
		Order order = (Order) getSubject();
		order.send();
	}

    ///////CHKSUM: 38347AD51A5105CC3136880C85908DE6XXXXXXXX/////////////////////
    //
    // The following code is generated by the DaProcessStepProcessor 
    // written by Ola Aronsson in 2020, courtesy of nollettnoll AB
    //
    // DO NOT edit this section. Modify @MakeStep or CHKSUM (then keep length)  to re-generate.
    //

    private final ProcessStep doGetShoeStep = new ProcessStep(){ 

        @Override
        public void execute() {
            doGetShoe();
        }

        @Override
        public String processName() {
            return "SHIPASHOEPROCESS";
        }

        @Override
        public String description() {
            return "getShoe";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.SHOE_FETCHED_FROM_WAREHOUSE;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.SHOE_NOT_YET_AVAILABLE;
        }

    };

    private final ProcessStep doSendPackageStep = new ProcessStep(){ 

        @Override
        public void execute() {
            doSendPackage();
        }

        @Override
        public String processName() {
            return "SHIPASHOEPROCESS";
        }

        @Override
        public String description() {
            return "shipping";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.SHIPPED;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.SHIPPING_FAILED;
        }

    };

    private final ProcessStep doGetLacesStep = new ProcessStep(){ 

        @Override
        public void execute() {
            doGetLaces();
        }

        @Override
        public String processName() {
            return "SHIPASHOEPROCESS";
        }

        @Override
        public String description() {
            return "fetchLaces";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.LACES_IN_PLACE;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.LACES_NOT_IN_PLACE;
        }

    };

    private final ProcessStep doGetPackagingStep = new ProcessStep(){ 

        @Override
        public void execute() {
            doGetPackaging();
        }

        @Override
        public String processName() {
            return "SHIPASHOEPROCESS";
        }

        @Override
        public String description() {
            return "packaging";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.PACKED;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.shoe.OrderStatus.Status.PACKAGING_FAILED;
        }

    };

    ///////////////////////////////////////////////////////////////////////////////
    //
    // End DaProcessStepProcessor generation
    //

}