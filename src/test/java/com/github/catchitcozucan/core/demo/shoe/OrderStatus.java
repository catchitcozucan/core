package com.github.catchitcozucan.core.demo.shoe;

import com.github.catchitcozucan.core.ProcessStatus;

@ProcessStatus
public class OrderStatus {

    public enum Status {
        NEW_ORDER('A'),
        SHOE_NOT_YET_AVAILABLE('b'),
        SHOE_FETCHED_FROM_WAREHOUSE('B'),
        LACES_NOT_IN_PLACE('c'),
        LACES_IN_PLACE('C'),
        PACKAGING_FAILED('d'),
        PACKED('D'),
        SHIPPING_FAILED('e'),
        SHIPPED('E');

        private final Character sts;

        Status(Character sts) {
            this.sts = sts;
        }

        public Character getSts() {
            return sts;
        }
    }

    public static Status bySts(Character status) {
        for (Status s : Status.values()) {
            if (s.getSts().equals(status)) {
                return s;
            }
        }
        return null;
    }
}
