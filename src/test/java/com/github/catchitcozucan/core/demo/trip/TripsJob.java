package com.github.catchitcozucan.core.demo.trip;

import com.github.catchitcozucan.core.demo.trip.internal.TripOrderRepository;
import com.github.catchitcozucan.core.impl.JobBase;
import com.github.catchitcozucan.core.interfaces.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TripsJob extends JobBase<TripSubject> implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobBase.class);
	public static final String TRIP_JOB = "Trip job";

	public TripsJob() {
		super(TripOrderRepository.getInstance(), TripProcess.CRITERIA_STATES);
	}

	@Override
	public String name() {
		return TRIP_JOB;
	}

	@Override
	public void doJob() {
		fetchSubjectsInCriteriaState().forEachOrdered(o -> exec(new TripProcess(o, TripOrderRepository.getInstance())));
		LOGGER.info(getTotalExectime());
	}
}
