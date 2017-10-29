package com.github.jmeta.tools.benchmark.api.services;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.tools.benchmark.api.services.AbstractTimeProvider;
import com.github.jmeta.tools.benchmark.api.services.MeasurementSession;
import com.github.jmeta.tools.benchmark.api.services.SystemMillisTimeProvider;
import com.github.jmeta.tools.benchmark.api.services.SystemNanosTimeProvider;
import com.github.jmeta.tools.benchmark.api.types.MeasuredCommand;
import com.github.jmeta.tools.benchmark.api.types.MeasuredCommandExecution;
import com.github.jmeta.tools.benchmark.api.types.MeasurementResult;
import com.github.jmeta.tools.benchmark.api.types.WaitCommand;

/**
 * {@link MeasurementSessionTest} tests the {@link MeasurementSession} class.
 */
public class MeasurementSessionTest {

   /**
    * Tests {@link MeasurementSession#runMeasurement(MeasuredCommand[], int)}.
    */
   @Test
   public void test_runMeasurement() {
      long[] waitTimes = new long[] { 100, 101, 200, 103, };

      MeasuredCommand[] waitCommands = new MeasuredCommand[waitTimes.length];

      long waitSum = 0;

      for (int i = 0; i < waitTimes.length; i++) {
         waitCommands[i] = new WaitCommand(waitTimes[i]);
         waitSum += waitTimes[i];
      }

      for (int i = 0; i < TIME_PROVIDERS.size(); ++i) {
         AbstractTimeProvider provider = TIME_PROVIDERS.get(i);

         MeasurementSession session = new MeasurementSession(provider, "Hallo");

         Assert.assertEquals(MeasurementSession.NO_MEASUREMENT, session.getStartTime());

         long startTime = provider.getUnit().convert(System.nanoTime(), TimeUnit.NANOSECONDS);

         final int repeats = 3;

         MeasurementResult result = session.runMeasurement(waitCommands, repeats);

         Assert.assertNotNull(result);
         Assert.assertEquals(MeasurementSession.NO_MEASUREMENT, session.getStartTime());

         Assert.assertEquals(session.getSessionId(), result.getMeasurementSessionId());
         Assert.assertTrue(startTime <= result.getMeasurementStartTime());
         Assert.assertTrue(result.getMeasurementStopTime() > result.getMeasurementStartTime());
         Assert.assertTrue(result.getMeasurementStopTime() - result.getMeasurementStartTime() >= waitSum);

         List<MeasuredCommandExecution> execs = result.getCommandExecutions();

         Assert.assertEquals(waitCommands.length, result.getCommandCount());
         Assert.assertEquals(repeats, result.getRepeatCount());
         Assert.assertEquals(repeats * waitCommands.length, execs.size());

         // Check individual commands
         for (int k = 0; k < waitCommands.length; k++)
            for (int j = 0; j < repeats; j++) {
               MeasuredCommandExecution exec = execs.get(k * repeats + j);

               Assert.assertNotNull(exec);
               Assert.assertFalse(exec.getStartTime() == -1);
               Assert.assertFalse(exec.getStopTime() == -1);
               Assert.assertTrue(exec.getStopTime() > exec.getStartTime());
//               Assert.assertTrue("Actual waited time: " + Long.toString(exec.getStopTime() - exec.getStartTime()) + ", wait time was: " + waitTimes[k], exec.getStopTime() - exec.getStartTime() >= waitTimes[k]);
               Assert.assertEquals(waitCommands[k], exec.getExecutedCommand());
               Assert.assertNull(exec.getThrowed());
            }
      }
   }

   /**
    * Tests the methods {@link MeasurementSession#startMeasurement()} and {@link MeasurementSession#stopMeasurement()}.
    */
   @Test
   public void testStartStopMeasurement() {
      for (int i = 0; i < TIME_PROVIDERS.size(); ++i) {
         AbstractTimeProvider provider = TIME_PROVIDERS.get(i);

         MeasurementSession session = new MeasurementSession(provider, "Hallo");

         Assert.assertEquals(MeasurementSession.NO_MEASUREMENT, session.getStartTime());

         long startTime = session.startMeasurement();
         Assert.assertFalse(startTime <= 0);
         Assert.assertFalse(session.getStartTime() == MeasurementSession.NO_MEASUREMENT);
         Assert.assertEquals(session.getStartTime(), startTime);

         try {
            final int waitTime = 500;
            Thread.sleep(waitTime);

            MeasurementResult result = session.stopMeasurement();

            Assert.assertNotNull(result);
            Assert.assertEquals(MeasurementSession.NO_MEASUREMENT, session.getStartTime());

            Assert.assertEquals(0, result.getCommandCount());
            Assert.assertEquals(0, result.getRepeatCount());
            Assert.assertTrue(result.getCommandExecutions().isEmpty());
            Assert.assertEquals(session.getSessionId(), result.getMeasurementSessionId());
            Assert.assertEquals(startTime, result.getMeasurementStartTime());
            Assert.assertTrue(result.getMeasurementStopTime() > result.getMeasurementStartTime());
            Assert.assertTrue(result.getMeasurementStopTime() - result.getMeasurementStartTime() >= waitTime);
         } catch (InterruptedException e) {
            fail("Unexpected exception: " + e);
         }
      }
   }

   /**
    * Tests the methods {@link MeasurementSession#getMachineInfo()}, {@link MeasurementSession#getSessionId()} and
    * {@link MeasurementSession#getTimeProvider()}.
    */
   @Test
   public void testGetters() {
      for (int i = 0; i < TIME_PROVIDERS.size(); ++i) {
         AbstractTimeProvider provider = TIME_PROVIDERS.get(i);

         MeasurementSession session = new MeasurementSession(provider, "Hallo");

         Assert.assertEquals("Hallo", session.getMachineInfo());
         Assert.assertFalse(session.getSessionId() == 0);
         Assert.assertEquals(provider, session.getTimeProvider());
      }
   }

   private final static List<AbstractTimeProvider> TIME_PROVIDERS = new ArrayList<>();

   static {
      TIME_PROVIDERS.add(new SystemMillisTimeProvider());
      TIME_PROVIDERS.add(new SystemNanosTimeProvider());
   }
}
