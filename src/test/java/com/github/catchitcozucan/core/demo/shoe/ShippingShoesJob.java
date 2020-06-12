package com.github.catchitcozucan.core.demo.shoe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.catchitcozucan.core.impl.JobBase;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.catchitcozucan.core.demo.shoe.internal.OrderRepository;

public class ShippingShoesJob extends JobBase implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShippingShoesJob.class);

	public ShippingShoesJob() {
		super(OrderRepository.getInstance(), ShipAShoeProcess.CRITERIA_STATES);
	}

	@Override
	public String name() {
		return "Shipping ordered shoes";
	}

	@Override
	public void doJob() {
		fetchSubjectsInCriteriaState().forEachOrdered(o -> exec(new ShipAShoeProcess(o, OrderRepository.getInstance())));
		LOGGER.info(getTotalExectime());
	}

	@Override
	public ProcessSubject provideSubjectSample() {
		return new Order();
	}

}
