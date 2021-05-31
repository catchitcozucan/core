package com.github.catchitcozucan.core.demo.shoe;

import com.github.catchitcozucan.core.demo.shoe.internal.OrderRepository;
import com.github.catchitcozucan.core.impl.JobBase;
import com.github.catchitcozucan.core.interfaces.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShippingShoesJob extends JobBase<Order> implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShippingShoesJob.class);
	public static final String SHIPPING_ORDERED_SHOES = "Shipping ordered shoes";

	public ShippingShoesJob() {
		super(OrderRepository.getInstance(), ShipAShoeProcess.CRITERIA_STATES);
	}

	@Override
	public String name() {
		return SHIPPING_ORDERED_SHOES;
	}

	@Override
	public void doJob() {
		fetchSubjectsInCriteriaState().forEachOrdered(o -> exec(new ShipAShoeProcess(o, OrderRepository.getInstance())));
		LOGGER.info(getTotalExectime());
	}
}
