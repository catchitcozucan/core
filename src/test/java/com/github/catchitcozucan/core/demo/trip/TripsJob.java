package com.github.catchitcozucan.core.demo.trip;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.catchitcozucan.core.impl.JobBase;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.catchitcozucan.core.demo.trip.internal.TripOrderRepository;

public class TripsJob extends JobBase implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobBase.class);

	@Override
	public String name() {
		return "Trip job";
	}

	@Override
	public ProcessSubject provideSubjectSample() {
		return new TripSubject("r2332f23f32");
	}

	@Override
	public void doJob() {
		fetchSubjects().stream().forEachOrdered(o -> exec(new TripProcess(o, TripOrderRepository.getInstance())));
		LOGGER.info(getTotalExectime());
	}


	public List<ProcessSubject> fetchSubjects() {
		return TripOrderRepository.getInstance().load().stream()
				.filter(subject -> isOrderInPickupState(subject))
				.collect(Collectors.toList());
	}

	private boolean isOrderInPickupState(ProcessSubject p){
		return Arrays.stream(TripProcess.CRITERIA_STATES).filter(state -> state.equals(p.getCurrentStatus())).findFirst().isPresent();
	}

}
