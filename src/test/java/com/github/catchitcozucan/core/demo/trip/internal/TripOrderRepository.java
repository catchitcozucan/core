package com.github.catchitcozucan.core.demo.trip.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.catchitcozucan.core.demo.shoe.ShipAShoeProcess;
import com.github.catchitcozucan.core.demo.test.support.ArrayRotator;
import com.github.catchitcozucan.core.demo.test.support.io.IO;
import com.github.catchitcozucan.core.demo.test.support.io.service.SerializationService;
import com.github.catchitcozucan.core.demo.trip.TripProcess;
import com.github.catchitcozucan.core.demo.trip.TripStatus;
import com.github.catchitcozucan.core.demo.trip.TripSubject;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;

public class TripOrderRepository implements PersistenceService {

	public static final int ERROR_ID = 6666;
	private static TripOrderRepository INSTANCE;
	private ArrayRotator<TripStatus.Status> STATUSES = new ArrayRotator<>(TripProcess.CRITERIA_STATES);
	private AtomicInteger id = new AtomicInteger(0);
	private List<ProcessSubject> orders;
	private List<Enum> criteriaList = Arrays.stream(TripProcess.CRITERIA_STATES).collect(Collectors.toList());

	private TripOrderRepository() {
		physicallyWipe();
		reInitFlush();
	}

	public void reInit() {
		physicallyWipe();
		makeOrders(false, false);
	}

	public void reInitFlush() {
		physicallyWipe();
		makeOrders(true, false);
	}

	public void reInitFlushInvokeErrors() {
		physicallyWipe();
		makeOrders(true, true);
	}

	public void physicallyWipe() {
		SerializationService.getInstance(); // will create store path if it's not there..
		Arrays.stream(IO.locateFilesRecursively(SerializationService.STOREPATH, "black.process", false)).forEach(f -> f.delete());
	}

	private void makeOrders(boolean force, boolean invokeErrorSubjects) {
		if (force || orders == null) {
			orders = new ArrayList<>();
		}
		if (orders.isEmpty()) {
			for (int i = 0; i < 100; i++) {
				TripSubject o = new TripSubject("19731111" + id.incrementAndGet());
				o.setStatus(STATUSES.getRandom());
				o = backTrackOrder(o);
				if (invokeErrorSubjects) {
					if (i % 7 == 0) {
						IO.setFieldValue(o, "orderId", ERROR_ID);
					}
				}
				orders.add(o);
			}
		}
		orders.stream().forEach(o -> save(o));
	}

	// this is for simulating, then, what actually HAS
	// happened already provided certin states..
	private TripSubject backTrackOrder(TripSubject order) {
		TripStatus.Status status = (TripStatus.Status) order.getCurrentStatus();
		switch (status) {
			case HOTEL_NOT_CONFIRMED:
			case FLIGHT_CONFIRMED:
				if (order.getFlightConfirmation() == null) {
					order.setFlightConfirmation(BookingCentral.getFlightConfirmation());
				}
				break;
			case CAR_NOT_CONFIRMED:
			case HOTEL_CONFIRMED:
				if (order.getFlightConfirmation() == null) {
					order.setFlightConfirmation(BookingCentral.getFlightConfirmation());
				}
				if (order.getHotelConfirmation() == null) {
					order.setHotelConfirmation(BookingCentral.getHotelConfirmation());
				}

				break;
		}
		return order;
	}

	public static synchronized TripOrderRepository getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TripOrderRepository();
		}
		return INSTANCE;
	}

	public List<ProcessSubject> load() {
		List<ProcessSubject> ordersToLoad = new ArrayList<>();
		orders.stream().forEach(o -> ordersToLoad.add((TripSubject) SerializationService.getInstance().perform(SerializationService.OP.LOAD, o)));
		return ordersToLoad;
	}

	@Override
	public void save(ProcessSubject processSubject) {
		SerializationService.getInstance().perform(SerializationService.OP.SAVE, processSubject);
	}

	@Override
	public Stream<ProcessSubject> provideSubjectStream() {
		return TripOrderRepository.getInstance().load().stream();
	}

	@Override
	public Stream<ProcessSubject> provideStateFilteredSubjectStream() {
		return load().stream().filter(s -> criteriaList.contains(s.getCurrentStatus())).collect(Collectors.toList()).stream();
	}
}
