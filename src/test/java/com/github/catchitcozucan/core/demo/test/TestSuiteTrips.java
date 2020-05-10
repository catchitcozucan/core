package com.github.catchitcozucan.core.demo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import com.github.catchitcozucan.core.demo.shoe.internal.OrderRepository;
import com.github.catchitcozucan.core.demo.test.support.io.IO;
import com.github.catchitcozucan.core.demo.trip.TripStatus;
import com.github.catchitcozucan.core.histogram.HistogramStatus;
import com.github.catchitcozucan.core.impl.ProcessingFlags;
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
import com.github.catchitcozucan.core.demo.trip.TripsJob;
import com.github.catchitcozucan.core.demo.trip.internal.TripOrderRepository;
import com.github.catchitcozucan.core.demo.test.support.io.service.SerializationService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSuiteTrips {

    private TripsJob job = new TripsJob();

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
        OrderRepository.getInstance().physicallyWipe();
        SerializationService.getInstance().setSilent(true);
    }

    @After
    public void clearTmp() {
        OrderRepository.getInstance().physicallyWipe();
    }

    private void reInitRepo() {

    }

    @Test
    public void a_trips_go_from_none_done_to_all_done() {

        HistogramStatus status = job.getHistogram(TripOrderRepository.getInstance().load().stream());
        assertEquals(status.getActuallyFinishedPercent(), Integer.valueOf(0));
        assertTrue(status.getActualProgressPercent() > 10);

        assertFalse(TripOrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(TripStatus.Status.CAR_CONFIRMED)).findFirst().isPresent());
        job.doJob();
        assertEquals(100l, TripOrderRepository.getInstance().load().stream().filter(o -> o.getCurrentStatus().equals(TripStatus.Status.CAR_CONFIRMED)).count());
    }

    @Test
    public void z_cleanUp() {
        File logPath1 = new File(new StringBuilder(System.getProperty("user.home")).append(File.separator).append(".processing").toString());
        File logPath2 = new File(new StringBuilder(System.getProperty("user.home")).append(File.separator).append("strutz").toString());
        if(logPath1.exists()){
            IO.deleteDirRecursively(logPath1);
        }
        if(logPath2.exists()){
            IO.deleteDirRecursively(logPath2);
        }
        assertFalse(logPath1.exists());
        assertFalse(logPath2.exists());
    }
}
