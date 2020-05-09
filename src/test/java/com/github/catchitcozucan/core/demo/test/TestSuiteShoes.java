/*
 *    Original work by Ola Aronsson 2020
 *    Courtesy of nollettnoll AB &copy; 2012 - 2020
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
package com.github.catchitcozucan.core.demo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
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
import com.github.catchitcozucan.core.demo.test.support.io.IO;
import com.github.catchitcozucan.core.demo.trip.TripStatus;
import com.github.catchitcozucan.core.exception.ProcessRuntimeException;
import com.github.catchitcozucan.core.histogram.HistogramStatus;
import com.github.catchitcozucan.core.impl.CatchIt;
import com.github.catchitcozucan.core.impl.JobBase;
import com.github.catchitcozucan.core.impl.ProcessingFlags;
import com.github.catchitcozucan.core.impl.startup.NumberOfTimeUnits;
import com.github.catchitcozucan.core.interfaces.AsyncJobListener;
import com.github.catchitcozucan.core.interfaces.CatchItConfig;
import com.github.catchitcozucan.core.interfaces.IsolationLevel;
import com.github.catchitcozucan.core.interfaces.Job;
import com.github.catchitcozucan.core.interfaces.LogConfig;
import com.github.catchitcozucan.core.interfaces.PoolConfig;
import com.github.catchitcozucan.core.interfaces.Process;
import com.github.catchitcozucan.core.interfaces.ProcessSubject;
import com.github.catchitcozucan.core.interfaces.Task;
import com.github.catchitcozucan.core.interfaces.TypedRelativeWithName;
import com.github.catchitcozucan.core.internal.util.id.IdGenerator;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSuiteShoes {

    public static final String MYTASK = "MYTASK";
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
        CatchIt.init();
    }

    @Before
    public void setup() {
        reInitRepo();
    }

    @After
    public void clearTmp() {
        OrderRepository.getInstance().physicallyWipe();
        CatchIt.killExecutions(false);
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
        assertNotEquals(status.toString(),
                "{ \"entityNames\": \"Process-Histogram\", \"bucketNames\": [\"NEW_ORDER\", \"SHOE_NOT_YET_AVAILABLE\", \"SHOE_FETCHED_FROM_WAREHOUSE\", \"LACES_NOT_IN_PLACE\", \"LACES_IN_PLACE\", \"PACKAGING_FAILED\", \"PACKED\", \"SHIPPING_FAILED\", \"SHIPPED\"], \"histogramz\": [{\"nameOfHistogram\": \"Shipping ordered shoes\", \"sum\": 100, \"actuallyFinished\": " + "100, " +
                        "\"actualStepProgress\": 100, \"data\": [0, 0, 0, 0, 0, 0, 0, 0, 100]}]}");

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
        assertFalse(CatchIt.getInstance().isExecuting());
        AsyncJobListener listener = new AsyncJobListener() {

            @Override
            public void jobExiting(Job job) {
                callbackCounter.getAndIncrement();
            }
        };
        CatchIt.getInstance().addJobListener(listener);
        CatchIt.getInstance().submitJob(job);
        IO.sleep(50);
        assertTrue(CatchIt.getInstance().isExecuting());
        IO.sleep(3000);
        //..then ALL are shipped
        assertEquals(100, OrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(OrderStatus.Status.SHIPPED)).count());
        assertEquals(1, callbackCounter.get());
        CatchIt.getInstance().getCurrentState().stream().forEach(r -> System.out.println(new StringBuilder("E{").append("state :").append(r.getState().name()).append(", ").append(r.getId()).append("}").toString()));
        assertFalse(CatchIt.getInstance().isExecuting());
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

        CatchIt.getInstance().submitJobWithTimeout(newJob, new NumberOfTimeUnits(1, TimeUnit.MILLISECONDS));
        IO.sleep(300);
        CatchIt.getInstance().killAwaitTerminationNonBlocking(new NumberOfTimeUnits(2, TimeUnit.SECONDS));
        IO.sleep(2500);
        CatchIt.getInstance().getCurrentState().stream().forEach(r -> System.out.println(new StringBuilder("F{").append("state :").append(r.getState().name()).append(", ").append(r.getId()).append("}").toString()));
        assertFalse(CatchIt.getInstance().isExecuting());
    }

    @Test
    public void g_testProcessAsync() {
        Process p = new Process() {

            @Override
            public void interruptExecution() {

            }

            @Override
            public IsolationLevel.Level provideIsolationLevel() {
                return IsolationLevel.Level.INCLUSIVE;
            }

            @Override
            public String name() {
                return "testing stuff";
            }

            @Override
            public Type provideType() {
                return Type.PROCESS;
            }

            @Override
            public RejectionAction provideRejectionAction() {
                return RejectionAction.IGNORE;
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
        CatchIt.getInstance().getCurrentState().stream().forEach(r -> System.out.println(new StringBuilder("H{").append("state :").append(r.getState().name()).append(", ").append(r.getId()).append("}").toString()));
        assertFalse(CatchIt.getInstance().isExecuting());
        CatchIt.getInstance().submitProcess(p);
        IO.sleep(50);
        assertTrue(CatchIt.getInstance().isExecuting());
        IO.sleep(2500);
        assertFalse(CatchIt.getInstance().isExecuting());
    }

    @Test(expected = ProcessRuntimeException.class)
    public void h_testRejectionAsyncRejectedByAnotherThingRunning() {
        Task t1 = makeTask(IsolationLevel.Level.INCLUSIVE, TypedRelativeWithName.RejectionAction.IGNORE, false, true);
        CatchIt.getInstance().submitTask(t1);
        Task t2 = makeTask(IsolationLevel.Level.EXCLUSIVE, TypedRelativeWithName.RejectionAction.REJECT, false, false);
        CatchIt.getInstance().submitTask(t2);
    }

    @Test(expected = ProcessRuntimeException.class)
    public void i_testRejectionAsyncRejectedByType() {
        Task t1 = makeTask(IsolationLevel.Level.INCLUSIVE, TypedRelativeWithName.RejectionAction.IGNORE, false, true);
        CatchIt.getInstance().submitTask(t1);
        Task t2 = makeTask(IsolationLevel.Level.TYPE_EXCLUSIVE, TypedRelativeWithName.RejectionAction.REJECT, false, false);
        CatchIt.getInstance().submitTask(t2);
    }

    @Test
    public void j_testRejectionAsyncRejectedByKindButKindDiffers() {
        Task t1 = makeTask(IsolationLevel.Level.INCLUSIVE, TypedRelativeWithName.RejectionAction.IGNORE, true, true);
        CatchIt.getInstance().submitTask(t1);
        Task t2 = makeTask(IsolationLevel.Level.KIND_EXCLUSIVE, TypedRelativeWithName.RejectionAction.REJECT, false, false);
        CatchIt.getInstance().submitTask(t2);
    }

    @Test(expected = ProcessRuntimeException.class)
    public void k_testRejectionAsyncRejectedByKind() {
        Task t1 = makeTask(IsolationLevel.Level.INCLUSIVE, TypedRelativeWithName.RejectionAction.IGNORE, false, true);
        CatchIt.getInstance().submitTask(t1);
        Task t2 = makeTask(IsolationLevel.Level.KIND_EXCLUSIVE, TypedRelativeWithName.RejectionAction.REJECT, false, false);
        CatchIt.getInstance().submitTask(t2);
    }

    @Test
    public void l_testRejectionAsyncAlreadyInQueDoNotCareForNewComers() {
        Task t1 = makeTask(IsolationLevel.Level.INCLUSIVE, TypedRelativeWithName.RejectionAction.REJECT, false, true);
        CatchIt.getInstance().submitTask(t1);
        Task t2 = makeTask(IsolationLevel.Level.KIND_EXCLUSIVE, TypedRelativeWithName.RejectionAction.IGNORE, false, false);
        CatchIt.getInstance().submitTask(t2);
    }

    @Test
    public void m_UtilizeBasicControl() {
        CatchItConfig config = new CatchItConfig() {
            @Override
            public PoolConfig getPoolConfig() {
                return new PoolConfig() {
                    @Override
                    public NumberOfTimeUnits maxExecTimePerRunnable() {
                        return new NumberOfTimeUnits(2, TimeUnit.SECONDS);
                    }

                    @Override
                    public int maxQueueSize() {
                        return 5;
                    }

                    @Override
                    public int maxNumberOfThreads() {
                        return 5;
                    }
                };
            }

            @Override
            public LogConfig getLogConfig() {
                return new LogConfig() {
                    @Override
                    public String getLoggingApp() {
                        return "strutz";
                    }

                    @Override
                    public String getSytemLogParentDir() {
                        return System.getProperty("user.home");
                    }

                    @Override
                    public boolean getLogSeparately() {
                        return true;
                    }
                };
            }
        };
        CatchIt.stop();
        CatchIt.init(config);
        Task t1 = makeTask(IsolationLevel.Level.INCLUSIVE, TypedRelativeWithName.RejectionAction.REJECT, false, true);
        CatchIt.getInstance().submitTask(t1);
        Task t2 = makeTask(IsolationLevel.Level.KIND_EXCLUSIVE, TypedRelativeWithName.RejectionAction.IGNORE, false, false);
        CatchIt.getInstance().submitTask(t2);
        assertNotNull(CatchIt.getInstance().getCurrentState());
        assertTrue(CatchIt.currentlyExecuting());
        CatchIt.stop();
        assertFalse(CatchIt.currentlyExecuting());
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

    private Task makeTask(IsolationLevel.Level isolationLevel, TypedRelativeWithName.RejectionAction rejectionAction, boolean uniqueName, boolean takesTime) {
        String name = MYTASK;
        if (uniqueName) {
            name = IdGenerator.getInstance().getIdMoreRandom(9, 2);
        }
        final String myName = name;
        return new Task() {
            @Override
            public void interruptExecution() {

            }

            @Override
            public void run() {
                if (takesTime) {
                    IO.sleep(600);
                }
            }

            @Override
            public IsolationLevel.Level provideIsolationLevel() {
                return isolationLevel;
            }

            @Override
            public String name() {
                return myName;
            }

            @Override
            public Type provideType() {
                return Type.TASK;
            }

            @Override
            public RejectionAction provideRejectionAction() {
                return rejectionAction;
            }
        };
    }

    public static abstract class BasicJob extends JobBase {
    }
}
