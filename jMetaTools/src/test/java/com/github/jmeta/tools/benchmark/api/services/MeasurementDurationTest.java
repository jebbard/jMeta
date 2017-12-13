package com.github.jmeta.tools.benchmark.api.services;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.tools.benchmark.api.types.MeasurementDuration;

/**
 * {@link MeasurementDurationTest} tests the {@link MeasurementDuration} class.
 */
public class MeasurementDurationTest {

   private static final String COMPLETE_PATTERN = "${d}_${h}_${M}_${s}_${m}_${\u03BC}_${n}";

   /**
    * Tests handling of seconds.
    */
   @Test
   public void testSeconds() {
      MeasurementDuration md1 = createMeasurementDurationWithDefaultChecks(1400, TimeUnit.MILLISECONDS);
      checkDefaultDurations(md1, 0, 0, 0, 1, 0, 0, 1);

      MeasurementDuration md2 = createMeasurementDurationWithDefaultChecks(1400, TimeUnit.SECONDS);
      checkDefaultDurations(md2, 0, 0, 23, 1400, 0, 23, 20);

      MeasurementDuration md3 = createMeasurementDurationWithDefaultChecks(60000, TimeUnit.MILLISECONDS);
      checkDefaultDurations(md3, 0, 0, 1, 60, 0, 1, 0);

      MeasurementDuration md4 = createMeasurementDurationWithDefaultChecks(1400, TimeUnit.MICROSECONDS);
      checkDefaultDurations(md4, 0, 0, 0, 0, 0, 0, 0);
   }

   /**
    * Tests handling of second fractions (milliseconds and so on).
    */
   @Test
   public void testSecondFractions() {
      MeasurementDuration md1 = createMeasurementDurationWithDefaultChecks(1400, TimeUnit.MILLISECONDS);
      assertEquals(1400, md1.getWholeUnits(TimeUnit.MILLISECONDS));
      assertEquals(0, md1.getRemainingUnits(TimeUnit.MICROSECONDS));
      assertEquals(1, md1.getWholeUnits(TimeUnit.SECONDS));

      MeasurementDuration md2 = createMeasurementDurationWithDefaultChecks(1400, TimeUnit.SECONDS);
      assertEquals(1400000, md2.getWholeUnits(TimeUnit.MILLISECONDS));
      assertEquals(1400000000, md2.getWholeUnits(TimeUnit.MICROSECONDS));
      assertEquals(1400, md2.getWholeUnits(TimeUnit.SECONDS));
      assertEquals(0, md2.getRemainingUnits(TimeUnit.MILLISECONDS));
      assertEquals(0, md2.getRemainingUnits(TimeUnit.MICROSECONDS));
      assertEquals(20, md2.getRemainingUnits(TimeUnit.SECONDS));

      MeasurementDuration md3 = createMeasurementDurationWithDefaultChecks(60000, TimeUnit.MILLISECONDS);
      assertEquals(60000, md3.getWholeUnits(TimeUnit.MILLISECONDS));
      assertEquals(0, md3.getRemainingUnits(TimeUnit.MILLISECONDS));
      assertEquals(60, md3.getWholeUnits(TimeUnit.SECONDS));
      assertEquals(0, md3.getRemainingUnits(TimeUnit.SECONDS));

      MeasurementDuration md4 = createMeasurementDurationWithDefaultChecks(1400, TimeUnit.MICROSECONDS);
      assertEquals(1, md4.getWholeUnits(TimeUnit.MILLISECONDS));
      assertEquals(1400, md4.getWholeUnits(TimeUnit.MICROSECONDS));
      assertEquals(1400000, md4.getWholeUnits(TimeUnit.NANOSECONDS));
      assertEquals(1, md4.getRemainingUnits(TimeUnit.MILLISECONDS));
      assertEquals(400, md4.getRemainingUnits(TimeUnit.MICROSECONDS));
      assertEquals(0, md4.getRemainingUnits(TimeUnit.NANOSECONDS));
   }

   /**
    * Tests handling of minutes.
    */
   @Test
   public void testMinutes() {
      MeasurementDuration md1 = createMeasurementDurationWithDefaultChecks(71000, TimeUnit.MILLISECONDS);
      checkDefaultDurations(md1, 0, 0, 1, 71, 0, 1, 11);

      MeasurementDuration md3 = createMeasurementDurationWithDefaultChecks(6666, TimeUnit.SECONDS);
      checkDefaultDurations(md3, 0, 1, 111, 6666, 1, 51, 6);
   }

   /**
    * Tests handling of hours.
    */
   @Test
   public void testHours() {
      MeasurementDuration md1 = createMeasurementDurationWithDefaultChecks(66666000, TimeUnit.MILLISECONDS);
      checkDefaultDurations(md1, 0, 18, 1111, 66666, 18, 31, 6);

      MeasurementDuration md3 = createMeasurementDurationWithDefaultChecks(7200, TimeUnit.SECONDS);
      checkDefaultDurations(md3, 0, 2, 120, 7200, 2, 0, 0);
   }

   /**
    * Tests handling of days.
    */
   @Test
   public void testDays() {
      MeasurementDuration md1 = createMeasurementDurationWithDefaultChecks(66666001000L, TimeUnit.MILLISECONDS);
      checkDefaultDurations(md1, 771, 18518, 1111100, 66666001, 14, 20, 1);
   }

   /**
    * Tests {@link MeasurementDuration#format(String)}.
    */
   @Test
   public void testFormat() {
      checkFormat(TimeUnit.MILLISECONDS, 22, 5, 6, 31, 919, 0, 0);
      checkFormat(TimeUnit.NANOSECONDS, 22, 5, 6, 31, 919, 111, 111);
      checkFormat(TimeUnit.NANOSECONDS, 22, 0, 0, 0, 0, 0, 0);
      checkFormat(TimeUnit.MINUTES, 0, 11, 4, 0, 0, 0, 0);
      checkFormat(TimeUnit.SECONDS, 0, 0, 11, 4, 0, 0, 0);
      checkFormat(TimeUnit.MILLISECONDS, 0, 0, 0, 11, 4, 0, 0);
      checkFormat(TimeUnit.MICROSECONDS, 0, 0, 0, 0, 11, 4, 0);
      checkFormat(TimeUnit.NANOSECONDS, 0, 0, 0, 0, 0, 11, 4);
      checkFormat(TimeUnit.DAYS, 22, 0, 0, 0, 0, 0, 0);
      checkFormat(TimeUnit.HOURS, 22, 5, 0, 0, 0, 0, 0);
      checkFormat(TimeUnit.MINUTES, 22, 0, 20, 0, 0, 0, 0);
      checkFormat(TimeUnit.SECONDS, 0, 11, 4, 11, 0, 0, 0);
      checkFormat(TimeUnit.MILLISECONDS, 0, 0, 11, 4, 5, 0, 0);
      checkFormat(TimeUnit.MICROSECONDS, 0, 0, 0, 11, 4, 1, 0);
      checkFormat(TimeUnit.NANOSECONDS, 0, 0, 0, 0, 11, 4, 1);

      // Special case all zeros
      MeasurementDuration zeroDur1 = createMeasurementDuration(TimeUnit.MICROSECONDS, 0, 0, 0, 0, 0, 0, 0);

      Assert.assertEquals("0 microseconds", zeroDur1.format(MeasurementDuration.LONG_PATTERN_EN));
      Assert.assertEquals("0 \u03BCs", zeroDur1.format(MeasurementDuration.SHORT_PATTERN));
      Assert.assertEquals("00:00:00", zeroDur1.format(MeasurementDuration.TINY_PATTERN));
      Assert.assertEquals("0_0_0_0_0_0_0", zeroDur1.format(COMPLETE_PATTERN));

   }

   /**
    * Helper method checking {@link MeasurementDuration#format(String)}. For better usability, it takes the number of
    * days, hours, minutes and so on to create a duration time. Then it creates a corresponding
    * {@link MeasurementDuration} with a specified {@link TimeUnit}. And finally it formats the
    * {@link MeasurementDuration} using {@link MeasurementDuration#LONG_PATTERN_EN},
    * {@link MeasurementDuration#SHORT_PATTERN} and {@link MeasurementDuration#TINY_PATTERN} as well as a custom
    * pattern.
    * 
    * The method highly depends upon the structure of each pattern. Thus, if one of the mentioned patterns changes this
    * method is highly likely to require changes, too.
    * 
    * @param targetUnit
    *           The measurement {@link TimeUnit} of the {@link MeasurementDuration} to create.
    * @param days
    *           The number of days
    * @param hours
    *           The number of hours
    * @param minutes
    *           The number of minutes
    * @param seconds
    *           The number of seconds
    * @param milliseconds
    *           The number of milliseconds
    * @param microseconds
    *           The number of microseconds
    * @param nanoseconds
    *           The number of nanoseconds
    */
   private void checkFormat(TimeUnit targetUnit, int days, int hours, int minutes, int seconds, int milliseconds,
      int microseconds, int nanoseconds) {
      MeasurementDuration dur = createMeasurementDuration(targetUnit, days, hours, minutes, seconds, milliseconds,
         microseconds, nanoseconds);

      String hourFormat = "%1$02d";
      String minuteFormat = "%2$02d";
      String secondFormat = "%3$02d";
      String millisecondFormat = "%4$03d";
      String microsecondFormat = "%5$03d";
      String nanosecondFormat = "%6$03d";

      final String expectedLongFormat = (days != 0 ? days + " days" : "")
         + (days != 0 && (hours != 0
            || minutes != 0 || seconds != 0 || milliseconds != 0 || microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (hours != 0 ? hours + " hours" : "")
         + (hours != 0 && (minutes != 0 || seconds != 0 || milliseconds != 0 || microseconds != 0 || nanoseconds != 0)
            ? ", "
            : "")
         + (minutes != 0 ? minutes + " minutes" : "")
         + (minutes != 0 && (seconds != 0 || milliseconds != 0 || microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (seconds != 0 ? seconds + " seconds" : "")
         + (seconds != 0 && (milliseconds != 0 || microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (milliseconds != 0 ? milliseconds + " milliseconds" : "")
         + (milliseconds != 0 && (microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (microseconds != 0 ? microseconds + " microseconds" : "")
         + (microseconds != 0 && nanoseconds != 0 ? ", " : "") + (nanoseconds != 0 ? nanoseconds + " nanoseconds" : "");
      final String expectedShortFormat = (days != 0 ? days + " d" : "")
         + (days != 0 && (hours != 0
            || minutes != 0 || seconds != 0 || milliseconds != 0 || microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (hours != 0 ? hours + " h" : "")
         + (hours != 0 && (minutes != 0 || seconds != 0 || milliseconds != 0 || microseconds != 0 || nanoseconds != 0)
            ? ", "
            : "")
         + (minutes != 0 ? minutes + " min" : "")
         + (minutes != 0 && (seconds != 0 || milliseconds != 0 || microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (seconds != 0 ? seconds + " s" : "")
         + (seconds != 0 && (milliseconds != 0 || microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (milliseconds != 0 ? milliseconds + " ms" : "")
         + (milliseconds != 0 && (microseconds != 0 || nanoseconds != 0) ? ", " : "")
         + (microseconds != 0 ? microseconds + " \u03BCs" : "") + (microseconds != 0 && nanoseconds != 0 ? ", " : "")
         + (nanoseconds != 0 ? nanoseconds + " ns" : "");
      final String expectedTinyFormat = (days != 0 ? days + "d, " : "") + hourFormat + ":" + minuteFormat + ":"
         + secondFormat
         + (((microseconds != 0 && nanoseconds != 0) || milliseconds != 0) ? "." + millisecondFormat : "")
         + ((nanoseconds != 0 || microseconds != 0) ? "." + microsecondFormat : "")
         + (nanoseconds != 0 ? "." + nanosecondFormat : "");
      final String expectedCompleteFormat = days + "_" + hours + "_" + minutes + "_" + seconds + "_" + milliseconds
         + "_" + microseconds + "_" + nanoseconds;

      Assert.assertEquals(expectedLongFormat, dur.format(MeasurementDuration.LONG_PATTERN_EN));
      Assert.assertEquals(expectedShortFormat, dur.format(MeasurementDuration.SHORT_PATTERN));
      Assert.assertEquals(
         String.format(expectedTinyFormat, hours, minutes, seconds, milliseconds, microseconds, nanoseconds),
         dur.format(MeasurementDuration.TINY_PATTERN));
      Assert.assertEquals(expectedCompleteFormat, dur.format(COMPLETE_PATTERN));
   }

   /**
    * For better usability, this method creates a {@link MeasurementDuration} corresponding to the number of days,
    * hours, minutes and so on to create a duration time.
    * 
    * @param targetUnit
    *           The measurement {@link TimeUnit} of the {@link MeasurementDuration} to create.
    * @param days
    *           The number of days
    * @param hours
    *           The number of hours
    * @param minutes
    *           The number of minutes
    * @param seconds
    *           The number of seconds
    * @param milliseconds
    *           The number of milliseconds
    * @param microseconds
    *           The number of microseconds
    * @param nanoseconds
    *           The number of nanoseconds
    * @return The {@link MeasurementDuration} corresponding to the targetUnit and the given specific durations.
    */
   private static MeasurementDuration createMeasurementDuration(TimeUnit targetUnit, int days, int hours, int minutes,
      int seconds, int milliseconds, int microseconds, int nanoseconds) {
      return new MeasurementDuration(targetUnit.convert(days, TimeUnit.DAYS) + targetUnit.convert(hours, TimeUnit.HOURS)
         + targetUnit.convert(minutes, TimeUnit.MINUTES) + targetUnit.convert(seconds, TimeUnit.SECONDS)
         + targetUnit.convert(milliseconds, TimeUnit.MILLISECONDS)
         + targetUnit.convert(microseconds, TimeUnit.MICROSECONDS)
         + targetUnit.convert(nanoseconds, TimeUnit.NANOSECONDS), targetUnit);
   }

   /**
    * Checks the {@link MeasurementDuration} for having the expected whole and remaining units.
    * 
    * @param measurementDur
    *           The {@link MeasurementDuration} to check.
    * @param expectedWholeDays
    *           Expected whole days
    * @param expectedWholeHours
    *           Expected whole hours
    * @param expectedWholeMinutes
    *           Expected whole minutes
    * @param expectedWholeSeconds
    *           Expected whole seconds
    * @param expectedRemainingHours
    *           Expected remaining hours
    * @param expectedRemainingMinutes
    *           Expected remaining minutes
    * @param expectedRemainingSeconds
    *           Expected remaining seconds
    */
   private void checkDefaultDurations(MeasurementDuration measurementDur, long expectedWholeDays,
      long expectedWholeHours, long expectedWholeMinutes, long expectedWholeSeconds, long expectedRemainingHours,
      long expectedRemainingMinutes, long expectedRemainingSeconds) {
      long remainingSeconds = measurementDur.getRemainingSeconds();
      long totalSeconds = measurementDur.getWholeSeconds();
      long remainingMinutes = measurementDur.getRemainingMinutes();
      long totalMinutes = measurementDur.getWholeMinutes();
      long remainingHours = measurementDur.getRemainingHours();
      long totalHours = measurementDur.getWholeHours();
      long totalDays = measurementDur.getWholeDays();

      assertEquals(expectedRemainingSeconds, remainingSeconds);
      assertEquals(expectedWholeSeconds, totalSeconds);
      assertEquals(expectedRemainingMinutes, remainingMinutes);
      assertEquals(expectedWholeMinutes, totalMinutes);
      assertEquals(expectedRemainingHours, remainingHours);
      assertEquals(expectedWholeHours, totalHours);
      assertEquals(expectedWholeDays, totalDays);
   }

   /**
    * Creates a new {@link MeasurementDuration} and performs some default checks with it.
    * 
    * @param durationTime
    *           The duration time.
    * @param unit
    *           The {@link TimeUnit}
    * @return The created {@link MeasurementDuration}.
    */
   private MeasurementDuration createMeasurementDurationWithDefaultChecks(final long durationTime,
      final TimeUnit unit) {
      MeasurementDuration measurementDur = new MeasurementDuration(durationTime, unit);
      assertEquals(measurementDur.getDurationTime(), durationTime);
      assertEquals(measurementDur.getMeasurementUnit(), unit);

      return measurementDur;
   }

}
