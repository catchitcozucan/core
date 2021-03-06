/*
 *    Copyright [2020] [Ola Aronsson, courtesy of nollettnoll AB]
 *
 *    Licensed under the Creative Commons Attribution 4.0 International (the "License")
 *    you may not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *                https://creativecommons.org/licenses/by/4.0/
 *
 *    The software is provided “as is”, without warranty of any kind, express or
 *    implied, including but not limited to the warranties of merchantability,
 *    fitness for a particular purpose and noninfringement. In no event shall the
 *    authors or copyright holders be liable for any claim, damages or other liability,
 *    whether in an action of contract, tort or otherwise, arising from, out of or
 *    in connection with the software or the use or other dealings in the software.
 */
package com.github.catchitcozucan.core.impl;

import com.github.catchitcozucan.core.internal.util.domain.BaseDomainObject;

public class RunState extends BaseDomainObject {

	public enum State {
		InQueue, InProcess, InWait; //NOSONAR
	}

	private final State state;
	private final String id;

	public RunState(State state, String id) {
		this.state = state;
		this.id = id;
	}

	public State getState() {
		return state;
	}

	public String getId() {
		return id;
	}

	@Override
	public String doToString() {
		return id;
	}
}
