package com.github.catchitcozucan.core.impl;

import com.github.catchitcozucan.core.interfaces.IsolationLevel;
import com.github.catchitcozucan.core.interfaces.Task;

public abstract class TaskBase implements Task {

    @Override
    public IsolationLevel.Level provideIsolationLevel() {
        return IsolationLevel.Level.INCLUSIVE;
    }

    @Override
    public RejectionAction provideRejectionAction() {
        return RejectionAction.PUT_ON_WAITING_LIST;
    }

    @Override
    public void interruptExecution(){} // it is not necessary, though healthy, to implement this

    @Override
    public final Type provideType() {
        return Type.TASK;
    }
}
