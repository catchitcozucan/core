package com.github.catchitcozucan.core.impl.startup;

import java.util.concurrent.TimeUnit;

public class NumberOfTimeUnits {
    final long number;
    final TimeUnit unit;

    public NumberOfTimeUnits(long number, TimeUnit unit) {
        this.number = number;
        this.unit = unit;
    }

    public long getNumber() {
        return number;
    }

    public TimeUnit getUnit() {
        return unit;
    }

}
