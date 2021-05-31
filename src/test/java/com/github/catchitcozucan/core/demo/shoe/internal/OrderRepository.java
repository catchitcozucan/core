package com.github.catchitcozucan.core.demo.shoe.internal;

import static com.github.catchitcozucan.core.demo.trip.internal.TripOrderRepository.ERROR_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.catchitcozucan.core.demo.shoe.Order;
import com.github.catchitcozucan.core.demo.shoe.OrderStatus;
import com.github.catchitcozucan.core.demo.shoe.ShipAShoeProcess;
import com.github.catchitcozucan.core.demo.test.support.ArrayRotator;
import com.github.catchitcozucan.core.demo.test.support.io.IO;
import com.github.catchitcozucan.core.demo.test.support.io.service.SerializationService;
import com.github.catchitcozucan.core.interfaces.PersistenceService;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;


public class OrderRepository implements PersistenceService<Order> {

    private static final String CATCHITCOZUCAN_CORE = "catchitcozucan.core";
    private static final String ORDER_ID = "orderId";
    private static final String ERR = "ERR_";
    private static final int ID_LEN = 9;
    private static OrderRepository INSTANCE;
    private ArrayRotator<OrderStatus.Status> STATUSES = new ArrayRotator<>(ShipAShoeProcess.CRITERIA_STATES);
    private AtomicInteger id = new AtomicInteger(1);
    private List<Order> orders;
    private List<Enum> criteriaList = Arrays.stream(ShipAShoeProcess.CRITERIA_STATES).collect(Collectors.toList());

    private OrderRepository() {
        physicallyWipe();
        reInitFlush();
    }

    public void reInit() {
        physicallyWipe();
        makeOrders(false, false);
    }

    public void reInitFlush() {
        physicallyWipe();
        makeOrders(true, false);
    }

    public void reInitFlushInvokeErrors() {
        physicallyWipe();
        makeOrders(true, true);
    }

    public void physicallyWipe() {
        SerializationService.getInstance().setSilent(true); // will create store path if it's not there..
        Arrays.stream(IO.locateFilesRecursively(SerializationService.STOREPATH, CATCHITCOZUCAN_CORE, false)).forEach(f -> f.delete());
    }

    private void makeOrders(boolean force, boolean invokeErrorSubjects) {
        if (force || orders == null) {
            orders = new ArrayList<>();
        }
        if (orders.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                Order o = new Order();
                o.setId(id.getAndIncrement());
                o.setStatus(STATUSES.getRandom());
                o = backTrackOrder(o);
                if (invokeErrorSubjects) {
                    if (i % 7 == 0) {
                        IO.setFieldValue(o, ORDER_ID, ERROR_ID);
                    }
                }
                orders.add(o);
            }
        }
        orders.stream().forEach(o -> save(o));
    }

    // this is for simulating, then, what actually HAS
    // happened already provided certin states..
    private Order backTrackOrder(Order order) {
        OrderStatus.Status status = (OrderStatus.Status) order.getCurrentStatus();
        switch (status) {
            case LACES_NOT_IN_PLACE:
            case SHOE_FETCHED_FROM_WAREHOUSE:
                if (order.getShoe() == null) {
                    order.setShoe(ShoeProvider.getInstance().getShoe(order.getRequestedColor(), order.getRequestedSize()));
                }
                break;
            case PACKAGING_FAILED:
            case LACES_IN_PLACE:
                if (order.getShoe() == null) {
                    order.setShoe(ShoeProvider.getInstance().getShoe(order.getRequestedColor(), order.getRequestedSize()));
                }
                order.getShoe().setLaces(LaceProvider.getInstance().getFreshLaces());
                break;
            case SHIPPING_FAILED:
            case PACKED:
                if (order.getShoe() == null) {
                    order.setShoe(ShoeProvider.getInstance().getShoe(order.getRequestedColor(), order.getRequestedSize()));
                }
                order.getShoe().setLaces(LaceProvider.getInstance().getFreshLaces());
                order.packageOrder();
                break;
        }
        return order;
    }

    public static synchronized OrderRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OrderRepository();
        }
        return INSTANCE;
    }

    public List<Order> load() {
        List<Order> ordersToLoad = new ArrayList<>();
        orders.stream().forEach(o -> ordersToLoad.add((Order) SerializationService.getInstance().perform(SerializationService.OP.LOAD, o)));
        return ordersToLoad;
    }

    @Override
    public void save(Order order) {
        SerializationService.getInstance().perform(SerializationService.OP.SAVE, order);
    }

    @Override
    public Stream provideSubjectStream() {
        return load().stream();
    }

    @Override
    public Stream provideStateFilteredSubjectStream() {
        return load().stream().filter(s -> criteriaList.contains(s.getCurrentStatus())).collect(Collectors.toList()).stream();
    }
}
