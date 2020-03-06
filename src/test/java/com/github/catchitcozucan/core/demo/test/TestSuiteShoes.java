package com.github.catchitcozucan.core.demo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.catchitcozucan.core.demo.shoe.OrderStatus;
import com.github.catchitcozucan.core.demo.shoe.ShippingShoesJob;
import com.github.catchitcozucan.core.demo.shoe.internal.OrderRepository;
import com.github.catchitcozucan.core.demo.trip.TripStatus;
import com.github.catchitcozucan.core.histogram.HistogramStatus;
import com.github.catchitcozucan.core.impl.JobAsync;
import com.github.catchitcozucan.core.impl.JobBase;
import com.github.catchitcozucan.core.impl.JobThreadSafe;
import com.github.catchitcozucan.core.impl.ProcessingFlags;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;
import com.github.catchitcozucan.core.demo.test.support.io.IO;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSuiteShoes {

    private ShippingShoesJob job = new ShippingShoesJob();

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("-Running : " + description.getMethodName());
        }
    };

    @BeforeClass
    public static void setupLoggingEnv() {
        System.setProperty(ProcessingFlags.NEN_PROCESSING_LOG_DIR, System.getProperty("user.home") + "/.processing");
        System.setProperty(ProcessingFlags.NEN_PROCESSING_LOGGING_APP, "coolApp");
        System.setProperty(ProcessingFlags.NEN_PROCESSING_LOGGING_SEPARATE_FILE, "true");
    }

    @Before
    public void setup() {
        reInitRepo();
    }

    @After
    public void clearTmp() {
        OrderRepository.getInstance().physicallyWipe();
    }

    private void reInitRepo() {
        OrderRepository.getInstance().reInitFlush();
    }

    @Test
    public void a_whenNoJobHaRunYouStillGetStats() {
        HistogramStatus status = job.getHistogram(OrderRepository.getInstance().load().stream());
        assertEquals(status.getActuallyFinishedPercent(), Integer.valueOf(0));
        assertTrue(status.getActualProgressPercent() > 10);
    }

    @Test
    public void b_testJobShipsAll() {
        // at first none is shipped!
        assertFalse(OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).findFirst().isPresent());
        job.doJob();
        //..then ALL are shipped
        assertEquals(100l, OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).count());
    }

    @Test
    public void c_testStatistics() {
        // so json should look like this
        HistogramStatus status = job.getHistogram(OrderRepository.getInstance().load().stream());
        assertNotEquals(status.toString(), "{ \"entityNames\": \"Process-Histogram\", \"bucketNames\": [\"NEW_ORDER\", \"SHOE_NOT_YET_AVAILABLE\", \"SHOE_FETCHED_FROM_WAREHOUSE\", \"LACES_NOT_IN_PLACE\", \"LACES_IN_PLACE\", \"PACKAGING_FAILED\", \"PACKED\", \"SHIPPING_FAILED\", \"SHIPPED\"], \"histogramz\": [{\"nameOfHistogram\": \"Shipping ordered shoes\", \"sum\": 100, \"actuallyFinished\": " +
                "100, " + "\"actualStepProgress\": 100, \"data\": [0, 0, 0, 0, 0, 0, 0, 0, 100]}]}");

        job.doJob();

        // failures flipped and javascript-wrapped for page insertion..
        status = job.getHistogram(OrderRepository.getInstance().load().stream());
        assertEquals(status.toJson(true, false, true), "'{ \"entityNames\": \"Process-Histogram\", \"bucketNames\": [\"NEW_ORDER\", \"SHOE_NOT_YET_AVAILABLE\", \"SHOE_FETCHED_FROM_WAREHOUSE\", \"LACES_NOT_IN_PLACE\", \"LACES_IN_PLACE\", \"PACKAGING_FAILED\", \"PACKED\", \"SHIPPING_FAILED\", \"SHIPPED\"], \"histogramz\": [{\"nameOfHistogram\": \"Shipping ordered shoes\", \"sum\": 100, " +
                "\"actuallyFinished\": 100, " + "\"actualStepProgress\": 100," + " \"data\": [0, 0, 0, 0, 0, 0, 0, 0, 100]}]}'");


        // just.. to be clear, you can of course FAKE results easily too
        HistogramStatus statusHittePå = new HistogramStatus(job.name(), makeUpData(new Integer[] { 4, 7, 20, 1, 5, 2, 23, 0, 28 }), null);
        System.out.println(statusHittePå.toJson(true, false, true));

        // just.. to be clear, you can of course FAKE results easily too and only see negatives
        HistogramStatus statusHittePå2 = new HistogramStatus(job.name(), makeUpData(new Integer[] { 4, 7, 20, 1, 5, 2, 23, 0, 28 }), null);
        assertEquals("{ \"entityNames\": \"Process-Histogram\", \"bucketNames\": [\"SHOE_NOT_YET_AVAILABLE\", \"LACES_NOT_IN_PLACE\", \"PACKAGING_FAILED\", \"SHIPPING_FAILED\"], \"histogramz\": [{\"nameOfHistogram\": \"Shipping ordered shoes\", \"sum\": 90, \"actuallyFinished\": 31, \"actualStepProgress\": 61, \"data\": [-7, -1, -2, 0]}]}", statusHittePå2.toJson(true, true, false));


        // just.. to be clear, you can of course FAKE results easily too and only see negatives
        HistogramStatus statusHittePå3 = new HistogramStatus(job.name(), makeUpData(new Integer[] { 4, 7, 20, 1, 5, 2, 23, 0, 28 }), ".*NOT.*$");
        assertEquals("{ \"entityNames\": \"Process-Histogram\", \"bucketNames\": [\"SHOE_NOT_YET_AVAILABLE\", \"LACES_NOT_IN_PLACE\"], \"histogramz\": [{\"nameOfHistogram\": \"Shipping ordered shoes\", \"sum\": 90, \"actuallyFinished\": 31, \"actualStepProgress\": 61, \"data\": [-7, -1]}]}", statusHittePå3.toJson(true, true, false));
    }

    @Test
    public void d_testExceptionsInJob() {
        OrderRepository.getInstance().reInitFlushInvokeErrors();
        job.doJob();
        HistogramStatus status = job.getHistogram(OrderRepository.getInstance().load().stream());
        assertTrue(status.getActuallyFinishedPercent() < 100);
        assertTrue(status.getRawData().get(OrderStatus.Status.SHOE_NOT_YET_AVAILABLE.name()) > 1);
        assertTrue(status.getRawData().get(OrderStatus.Status.PACKAGING_FAILED.name()) > 1);
    }

    @Test
    public void e_testAsync() {
        //NONE are shipped
        assertFalse(OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).findFirst().isPresent());
        AtomicInteger callbackCounter = new AtomicInteger();
        callbackCounter.getAndSet(0);
        assertFalse(JobAsync.getInstance().isExecuting());
        JobAsync.getInstance().addJobListener(job -> callbackCounter.getAndIncrement());
        JobAsync.getInstance().addJob(job);
        IO.sleep(50);
        assertTrue(JobAsync.getInstance().isExecuting());
        IO.sleep(3000);
        //..then ALL are shipped
        assertEquals(100, OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).count());
        assertEquals(1, callbackCounter.get());
        assertFalse(JobAsync.getInstance().isExecuting());
    }

    @Test
    public void f_testMoreAsync() {
        Job newJob = new BasicJob() {
            @Override
            public String name() {
                return "doStuff";
            }

            @Override
            public void doJob() {
                IO.sleep(3600000);
            }

            @Override
            public ProcessSubject provideSubjectSample() {
                return new ProcessSubject() {

                    @Override
                    public Integer id() {
                        return 666;
                    }

                    @Override
                    public String subjectIdentifier() {
                        return "apa-bepa";
                    }

                    @Override
                    public int getErrorCode() {
                        return 666;
                    }

                    @Override
                    public Enum[] getCycle() {
                        return OrderStatus.Status.values();
                    }

                    @Override
                    public Enum getCurrentStatus() {
                        return OrderStatus.Status.PACKAGING_FAILED;
                    }

                    @Override
                    public String getCurrentProcess() {
                        return "ShoeProcess";
                    }
                };
            }
        };

        JobAsync.getInstance().addJobWithTimeout(newJob, 1, TimeUnit.MILLISECONDS);
        IO.sleep(300);
        JobAsync.getInstance().killAwaitTerminationNonBlocking(2, TimeUnit.SECONDS);
        System.out.println("non-blocking IS beautiful!!");
        IO.sleep(2500);
    }

    @Test
    public void g_testThreadSfeSynchcronusJob() {
        // at first none is shipped!
        assertFalse(OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).findFirst().isPresent());
        JobThreadSafe.init(job); // initialize (just for the next test to make sense
        assertFalse(JobThreadSafe.getInstance(ShippingShoesJob.class).isExecuting());
        Thread t = new Thread() {
            @Override
            public void run() {
                if (!JobThreadSafe.getInstance(ShippingShoesJob.class).isExecuting()) {
                    System.out.println("Waiting for job to start");
                }
                System.out.println("Yup... das was das..");
            }
        };
        t.setName("JobStatusTester");
        t.start();
        JobThreadSafe.getInstance(ShippingShoesJob.class).doJob();
        assertFalse(JobThreadSafe.getInstance(ShippingShoesJob.class).isExecuting());
        if (t != null) {
            assertTrue(!t.isAlive());
            try {
                t.interrupt();
            } catch (Exception ignore) {}
        }
        //..then ALL are shipped
        assertEquals(100l, OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).count());
    }

    @Test
    public void h_testProcessAsync() {
        Process p = new Process() {

            @Override
            public String name() {
                return "testing stuff";
            }

            @Override
            public void process() {
                IO.sleep(2000);
            }

            @Override
            public Enum<?>[] criteriaStates() {
                return TripStatus.Status.values();
            }

            @Override
            public Enum<?> finishedState() {
                return TripStatus.Status.values()[3];
            }
        };
        assertFalse(JobAsync.getInstance().isExecuting());
        JobAsync.getInstance().addProcess(p);
        IO.sleep(50);
        assertTrue(JobAsync.getInstance().isExecuting());
        IO.sleep(2500);
        assertFalse(JobAsync.getInstance().isExecuting());
    }

    private static Map<String, Integer> makeUpData(Integer[] data) {
        List<String> labels = new ArrayList<>();
        Arrays.stream(OrderStatus.Status.values()).forEachOrdered(s -> labels.add(s.name()));
        String[] labelz = labels.toArray(new String[labels.size()]);
        Map<String, Integer> datan = new LinkedHashMap<>();
        for (int i = 0; i < labelz.length; i++) {
            Integer value = i < data.length ? data[i] : 0;
            datan.put(labelz[i], value);
        }
        return datan;
    }

    public static abstract class BasicJob extends JobBase {}
}
