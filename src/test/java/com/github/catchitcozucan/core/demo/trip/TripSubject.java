package com.github.catchitcozucan.core.demo.trip;

import com.github.catchitcozucan.core.impl.ProcessSubjectBase;
import com.github.catchitcozucan.core.impl.source.processor.EnumContainer;
import com.github.catchitcozucan.core.impl.source.processor.Nameable;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TripSubject extends ProcessSubjectBase {

	static final long serialVersionUID = 14662657334L;

	private final String passPortNumber;
	private final Integer id;
	private String flightConfirmation;
	private String hotelConfirmation;
	private String carConfirmation;

	public TripSubject(String passportNumber) {
		this.passPortNumber = passportNumber;
		id = IdGenerator.getInstance().getNextId();
	}

	@Override
	public Integer id() {
		return id;
	}

	@Override
	public String subjectIdentifier() {
		return passPortNumber;
	}

	@Override
	public Nameable[] getCycle() {
		return EnumContainer.enumsToNameableArray(TripStatus.Status.values());
	}
}
