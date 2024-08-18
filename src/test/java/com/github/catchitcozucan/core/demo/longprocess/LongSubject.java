package com.github.catchitcozucan.core.demo.longprocess;

import com.github.catchitcozucan.core.demo.trip.TripStatus;
import com.github.catchitcozucan.core.impl.ProcessSubjectBase;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LongSubject extends ProcessSubjectBase {

	static final long serialVersionUID = 13661657334L;

	private final String name;
	private final Integer id;
	private String flightConfirmation;
	private String hotelConfirmation;
	private String carConfirmation;

	public LongSubject(String name) {
		this.name = name;
		id = IdGenerator.getInstance().getNextId();
	}

	@Override
	public Integer id() {
		return id;
	}

	@Override
	public String subjectIdentifier() {
		return name;
	}

	@Override
	public boolean isInFailState() {
		return LongProcess.FAIL_STATES.stream().anyMatch(f -> f.equals(getCurrentStatus().name()));
	}

	@Override
	public Enum[] getCycle() {
		return TripStatus.Status.values();
	}
}
