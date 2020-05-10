package com.github.catchitcozucan.core.interfaces;

public interface RejectableTypedRelativeWithName extends InterruptSignalable {
    enum Type {
        TASK, PROCESS, JOB
    }
    enum RejectionAction {
        REJECT, IGNORE, PUT_ON_WAITING_LIST
    }
    IsolationLevel.Level provideIsolationLevel();
    String name();
    Type provideType();
    RejectionAction provideRejectionAction();
    boolean rejectedFromTheOutSideWorld();
}