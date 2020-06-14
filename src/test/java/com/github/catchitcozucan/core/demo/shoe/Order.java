package com.github.catchitcozucan.core.demo.shoe;

import com.github.catchitcozucan.core.demo.shoe.internal.Shoe;
import com.github.catchitcozucan.core.demo.trip.internal.TripOrderRepository;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.impl.ProcessSubjectBase;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;

public class Order extends ProcessSubjectBase {

	static final long serialVersionUID = 14662657334L;

	private Integer id;
	private Integer orderId = IdGenerator.getInstance().getNextId();
	private String adressId = IdGenerator.getInstance().getId(16);
	private Shoe shoe;
	private boolean isPacked;
	private String trackinId;

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public Integer id() {
		return orderId;
	}

	@Override
	public String subjectIdentifier() {
		return adressId;
	}

	public void packageOrder() {
		isPacked = true;
		if (orderId.intValue() == TripOrderRepository.ERROR_ID) {
			throw new ProcessRuntimeException("WTF - simulating 'planned' went wrong");
		}
	}

	public String getRequestedColor() {
		return "grue";
	}

	public long getRequestedSize() {
		return 42l;
	}

	public void setShoe(Shoe shoe) {
		this.shoe = shoe;
		if (orderId.intValue() == TripOrderRepository.ERROR_ID) {
			throw new RuntimeException("WTF - simulating something went to shitz in runtime!");
		}
	}

	public void send() {
		trackinId = IdGenerator.getInstance().getId(12);
	}

	public Shoe getShoe() {
		return shoe;
	}

	@Override
	public Enum[] getCycle() {
		return OrderStatus.Status.values();
	}

	@Override
	public Enum getCurrentStatus() {
		return status;
	}

	@Override
	public String doToString() {
		return Integer.toString(id);
	}
}
