package com.github.catchitcozucan.core.demo.trip;

import com.github.catchitcozucan.core.CompileOptions;
import com.github.catchitcozucan.core.MakeStep;
import com.github.catchitcozucan.core.demo.trip.internal.BookingCentral;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.ProcessBase;
import com.github.catchitcozucan.core.interfaces.PersistenceService;

@CompileOptions(relativeBpmDirectoryPath = "../../../../../../../resources/bpmSchemes", bpmActivitiesPerColumn = "3")
public class TripProcess extends ProcessBase<TripSubject> {

    protected TripProcess(TripSubject processSubject, PersistenceService persistenceService) {
        super(processSubject, persistenceService);
    }

    @Override
    public String name() {
        return PROCESS_NAME_SHORT;
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
        TripStatus.Status currentStatus = (TripStatus.Status) getSubject().getCurrentStatus();
        processInternal(currentStatus);
    }

    @MakeStep(statusUponSuccess = "Status.FLIGHT_CONFIRMED", statusUponFailure = "Status.FLIGHT_NOT_CONFIRMED", enumStateProvider = com.github.catchitcozucan.core.demo.trip.TripStatus.class)
    private void bookFlight() {
        getSubject().setFlightConfirmation(BookingCentral.getFlightConfirmation());
    }

    @MakeStep(statusUponSuccess = "Status.HOTEL_CONFIRMED", statusUponFailure = "Status.HOTEL_NOT_CONFIRMED", enumStateProvider = com.github.catchitcozucan.core.demo.trip.TripStatus.class)
    private void bookHotel() {
        getSubject().setHotelConfirmation(BookingCentral.getHotelConfirmation());
    }

    @MakeStep(statusUponSuccess = "Status.CAR_CONFIRMED", statusUponFailure = "Status.CAR_NOT_CONFIRMED", enumStateProvider = com.github.catchitcozucan.core.demo.trip.TripStatus.class)
    private void bookCar() {
        getSubject().setCarConfirmation(BookingCentral.getCarConfirmation());
    }

    ///////CHKSUM: 8F9E8FBEC174C8235DE8B7A92BD44CF2XXXXXXXX/////////////////////
    //
    // The following code is generated by the DaProcessStepProcessor
    // written by Ola Aronsson in 2020, courtesy of nollettnoll AB
    //
    // DO NOT edit this section. Modify @MakeStep or CHKSUM (then keep length)  to re-generate.
    //

    //@formatter:off DO_NOT_FORMAT

    private final com.github.catchitcozucan.core.ProcessStep bookHotelStep = new com.github.catchitcozucan.core.ProcessStep(){

        @Override
        public void execute() {
            bookHotel();
        }

        @Override
        public String processName() {
            return "TRIPPROCESS";
        }

        @Override
        public String description() {
            return "bookHotel";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.trip.TripStatus.Status.HOTEL_CONFIRMED;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.trip.TripStatus.Status.HOTEL_NOT_CONFIRMED;
        }

    };

    private final com.github.catchitcozucan.core.ProcessStep bookCarStep = new com.github.catchitcozucan.core.ProcessStep(){

        @Override
        public void execute() {
            bookCar();
        }

        @Override
        public String processName() {
            return "TRIPPROCESS";
        }

        @Override
        public String description() {
            return "bookCar";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.trip.TripStatus.Status.CAR_CONFIRMED;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.trip.TripStatus.Status.CAR_NOT_CONFIRMED;
        }

    };

    private final com.github.catchitcozucan.core.ProcessStep bookFlightStep = new com.github.catchitcozucan.core.ProcessStep(){

        @Override
        public void execute() {
            bookFlight();
        }

        @Override
        public String processName() {
            return "TRIPPROCESS";
        }

        @Override
        public String description() {
            return "bookFlight";
        }

        @Override
        public Enum<?> statusUponSuccess() {
            return com.github.catchitcozucan.core.demo.trip.TripStatus.Status.FLIGHT_CONFIRMED;
        }

        @Override
        public Enum<?> statusUponFailure() {
            return com.github.catchitcozucan.core.demo.trip.TripStatus.Status.FLIGHT_NOT_CONFIRMED;
        }

    };

    public static final String PROCESS_NAME = com.github.catchitcozucan.core.demo.trip.TripProcess.class.getName().toUpperCase();
    public static final String PROCESS_NAME_SHORT = com.github.catchitcozucan.core.demo.trip.TripProcess.class.getSimpleName().toUpperCase();

    public static final Enum<?> FINISH_STATE = com.github.catchitcozucan.core.demo.trip.TripStatus.Status.values()[com.github.catchitcozucan.core.demo.trip.TripStatus.Status.values().length - 1];

    public static final com.github.catchitcozucan.core.demo.trip.TripStatus.Status[] CRITERIA_STATES = {
            com.github.catchitcozucan.core.demo.trip.TripStatus.Status.NEW_ORDER,
            com.github.catchitcozucan.core.demo.trip.TripStatus.Status.FLIGHT_NOT_CONFIRMED,
            com.github.catchitcozucan.core.demo.trip.TripStatus.Status.HOTEL_NOT_CONFIRMED,
            com.github.catchitcozucan.core.demo.trip.TripStatus.Status.CAR_NOT_CONFIRMED
    };

    public static java.util.List<String> criteriaProcessesStatusesAsStrings() {
        return java.util.Arrays.asList(CRITERIA_STATES).stream().map(Enum::name).collect(java.util.stream.Collectors.toList());
    }

    public static java.util.List<String> allProcessesStatusesAsStrings() {
        return java.util.Arrays.asList(com.github.catchitcozucan.core.demo.trip.TripStatus.Status.values()).stream().map(Enum::name).collect(java.util.stream.Collectors.toList());
    }

    public void processInternal(com.github.catchitcozucan.core.demo.trip.TripStatus.Status currentStatus) {
        switch (currentStatus) {
            case NEW_ORDER:
            case FLIGHT_NOT_CONFIRMED:
                executeStep(bookFlightStep);
                break;
            case FLIGHT_CONFIRMED:
            case HOTEL_NOT_CONFIRMED:
                executeStep(bookHotelStep);
                break;
            case HOTEL_CONFIRMED:
            case CAR_NOT_CONFIRMED:
                executeStep(bookCarStep);
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