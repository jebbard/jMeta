package com.github.jmeta.tools.benchmark.api.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MeasurementDuration} represents a duration of a measurement in a
 * specific {@link TimeUnit}. It contains methods to get the total time in
 * various forms and formats.
 *
 * It is sometimes not feasible to work with nanosecond or millisecond
 * durations. Instead, a more readable representation of such durations is
 * needed. This class helps achieving this especially by its
 * {@link #format(String)} method.
 *
 * An important concept used is the {@link TimeUnit} which allows to distinguish
 * the basic time units starting from nanoseconds (smallest) to days (biggest).
 * We don't support weeks or years, because weeks tend to be not very widely
 * used and human beings can cope with days, years have a varying length which
 * disqualifies them as really useful in exact terms.
 *
 * This class basically distinguishes between whole {@link TimeUnit}s of a given
 * type that are contained in the duration time, and remaining
 * {@link TimeUnit}s. E.g. if this {@link MeasurementDuration} stores 1100
 * milliseconds, these correspond to one whole seconds, so
 * {@link #getWholeUnits(TimeUnit)} called with {@link TimeUnit#SECONDS} would
 * return 1. The remainder is 100, so 100 milliseconds remain, these will be
 * returned by a call to {@link #getRemainingUnits(TimeUnit)} with
 * {@link TimeUnit#MILLISECONDS}.
 *
 * The class additionally provides convenience methods for the time units from
 * seconds to days.
 */
public class MeasurementDuration {

	private static class FormatValue {

		public static Pattern FORMAT_VALUE_PATTERN = Pattern.compile("\\$\\{(.*?)\\}(\\{(.*?)\\})?(\\{(.*?)\\})?");

		private static char ZERO_HANDLING_NONE = ' ';

		private static char ZERO_HANDLING_OMIT = '-';

		private static char ZERO_HANDLING_OMIT_IF = '?';

		private static Map<Character, TimeUnit> SUPPORTED_IDENTIFIERS = new HashMap<>();

		static {
			FormatValue.SUPPORTED_IDENTIFIERS.put('d', TimeUnit.DAYS);
			FormatValue.SUPPORTED_IDENTIFIERS.put('h', TimeUnit.HOURS);
			FormatValue.SUPPORTED_IDENTIFIERS.put('M', TimeUnit.MINUTES);
			FormatValue.SUPPORTED_IDENTIFIERS.put('s', TimeUnit.SECONDS);
			FormatValue.SUPPORTED_IDENTIFIERS.put('m', TimeUnit.MILLISECONDS);
			FormatValue.SUPPORTED_IDENTIFIERS.put('\u03BC', TimeUnit.MICROSECONDS);
			FormatValue.SUPPORTED_IDENTIFIERS.put('n', TimeUnit.NANOSECONDS);
		}

		private static Map<Character, Integer> LEADING_ZERO_IDENTIFIERS = new HashMap<>();

		static {
			FormatValue.LEADING_ZERO_IDENTIFIERS.put('h', 1);
			FormatValue.LEADING_ZERO_IDENTIFIERS.put('M', 1);
			FormatValue.LEADING_ZERO_IDENTIFIERS.put('s', 1);
			FormatValue.LEADING_ZERO_IDENTIFIERS.put('m', 2);
			FormatValue.LEADING_ZERO_IDENTIFIERS.put('\u03BC', 2);
			FormatValue.LEADING_ZERO_IDENTIFIERS.put('n', 2);
		}

		private char identifier;

		private String prefix = "";

		private String suffix = "";
		private String delimiter = "";
		private long unitValue;
		private char zeroHandling = FormatValue.ZERO_HANDLING_NONE;

		private boolean includeLeadingZeros;

		private String leadingZeros = "";

		private boolean followingUnitsAreOmitted = false;

		public String getFormatString() {
			if (isOmitted()) {
				return "";
			}

			return getRawFormatString();
		}

		public String getRawFormatString() {
			final String returnValue = prefix + leadingZeros + unitValue + suffix;

			if (followingUnitsAreOmitted) {
				return returnValue;
			}

			return returnValue + delimiter;
		}

		public TimeUnit getUnit() {
			return FormatValue.SUPPORTED_IDENTIFIERS.get(identifier);
		}

		public boolean isOmitted() {
			return (unitValue == 0) && ((zeroHandling == FormatValue.ZERO_HANDLING_OMIT)
				|| ((zeroHandling == FormatValue.ZERO_HANDLING_OMIT_IF) && followingUnitsAreOmitted));
		}

		public void parseDelimiter(String placeHolder) {
			int startIndex = 1;

			if (zeroHandling != FormatValue.ZERO_HANDLING_NONE) {
				startIndex++;
			}

			if (includeLeadingZeros) {
				startIndex++;
			}

			delimiter = placeHolder.substring(startIndex);
		}

		public void parseLeadingZeros(String placeHolder) {
			final char firstChar = placeHolder.charAt(0);

			includeLeadingZeros = firstChar == '0';

			// Skip first character if leading zeroes is true, check consistency
			if (includeLeadingZeros) {
				if (placeHolder.length() < 2) {
					throw new RuntimeException("Invalid identifier: " + placeHolder);
				}

				final char secondChar = placeHolder.charAt(1);

				if (!FormatValue.LEADING_ZERO_IDENTIFIERS.containsKey(secondChar)) {
					throw new RuntimeException("Identifier " + secondChar + " does not support filling with zeroes");
				}
			}
		}

		public void parsePrefixSuffix(String prefixSuffixGroup) {
			if (prefixSuffixGroup != null) {
				// We have a prefix
				if (prefixSuffixGroup.startsWith("p:")) {
					if (prefix.isEmpty()) {
						prefix = prefixSuffixGroup.substring(2);
					} else {
						throw new RuntimeException("Encountered prefix twice for identifier " + identifier);
					}
				}

				// We have a prefix
				else if (prefixSuffixGroup.startsWith("s:")) {
					if (suffix.isEmpty()) {
						suffix = prefixSuffixGroup.substring(2);
					} else {
						throw new RuntimeException("Encountered suffix twice for identifier " + identifier);
					}
				} else {
					throw new RuntimeException(
						"Prefix p: or Suffix s: is missing in the expression: " + prefixSuffixGroup);
				}
			}
		}

		public void parseValue(String placeHolder) {
			char identifierChar = ' ';

			if (includeLeadingZeros) {
				identifierChar = placeHolder.charAt(1);
			} else {
				identifierChar = placeHolder.charAt(0);
			}

			// Get identifier
			if (!FormatValue.SUPPORTED_IDENTIFIERS.containsKey(identifierChar)) {
				throw new RuntimeException("Unknown identifier: " + identifier + "; supported placeholders are: "
					+ FormatValue.SUPPORTED_IDENTIFIERS.keySet());
			}

			identifier = identifierChar;
		}

		public void parseZeroHandling(String placeHolder) {
			if (!includeLeadingZeros && (placeHolder.length() > 1)) {
				char zeroHandlingChar = placeHolder.charAt(1);

				if ((zeroHandlingChar == FormatValue.ZERO_HANDLING_OMIT)
					|| (zeroHandlingChar == FormatValue.ZERO_HANDLING_OMIT_IF)) {
					zeroHandling = zeroHandlingChar;
				}
			}

			else if (includeLeadingZeros && (placeHolder.length() > 2)) {
				char zeroHandlingChar = placeHolder.charAt(2);

				if ((zeroHandlingChar == FormatValue.ZERO_HANDLING_OMIT)
					|| (zeroHandlingChar == FormatValue.ZERO_HANDLING_OMIT_IF)) {
					zeroHandling = zeroHandlingChar;
				}
			}
		}

		public void setFollowingUnitsAreOmitted(boolean followingUnitsAreZero) {
			followingUnitsAreOmitted = followingUnitsAreZero;
		}

		public void setValue(MeasurementDuration duration) {
			// Get value
			unitValue = duration.getRemainingUnits(FormatValue.SUPPORTED_IDENTIFIERS.get(identifier));

			// Generate leading zeroes
			if (includeLeadingZeros) {
				int maxZeroes = FormatValue.LEADING_ZERO_IDENTIFIERS.get(identifier);
				int potency = 10;

				for (int i = 0; i < maxZeroes; i++) {
					if (unitValue < potency) {
						leadingZeros += "0";
					}

					potency *= 10;
				}
			}
		}
	}

	/**
	 * A long pattern containing all duration information up to days. Units are
	 * skipped if being zero. Duration information with this pattern is returned by
	 * the {@link #longFormat()} method.
	 */
	public static String LONG_PATTERN_EN = "${d-, }{s: days}${h-, }{s: hours}${M-, }{s: minutes}${s-, }{s: seconds}${m-, }{s: milliseconds}${\u03BC-, }{s: microseconds}${n-, }{s: nanoseconds}";
	/**
	 * A short pattern containing all duration information up to days. Units are
	 * skipped if being zero. Duration information with this pattern is returned by
	 * the {@link #shortFormat()} method.
	 */
	public static String SHORT_PATTERN = "${d-, }{s: d}${h-, }{s: h}${M-, }{s: min}${s-, }{s: s}${m-, }{s: ms}${\u03BC-, }{s: \u03BCs}${n-, }{s: ns}";

	/**
	 * A tiny pattern containing all duration information up to days. Days are
	 * skipped if being zero, microseconds and milliseconds are skipped if being
	 * zero and all smaller units are zero. Hours, minutes and seconds are always
	 * given, even if zero, in the format hh:mm:ss. Duration information with this
	 * pattern is returned by the {@link #tinyFormat()} method.
	 */
	public static String TINY_PATTERN = "${d-, }{s:d}${0h:}${0M:}${0s}${0m?}{p:.}${0\u03BC?}{p:.}${0n?}{p:.}";

	private static Map<TimeUnit, TimeUnit> UPPER_UNITS = new HashMap<>();

	static {
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS);
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS);
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.SECONDS, TimeUnit.MINUTES);
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.MINUTES, TimeUnit.HOURS);
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.HOURS, TimeUnit.DAYS);
		MeasurementDuration.UPPER_UNITS.put(TimeUnit.DAYS, null);
	}

	private final TimeUnit measurementUnit;

	private final long durationTime;

	/**
	 * Creates a new {@link MeasurementDuration}.
	 * 
	 * @param durationTime    The duration time in the given {@link TimeUnit}.
	 * @param measurementUnit The {@link TimeUnit} the duration time is given in.
	 */
	public MeasurementDuration(long durationTime, TimeUnit measurementUnit) {
		Reject.ifNull(measurementUnit, "measurementUnit");

		this.durationTime = durationTime;
		this.measurementUnit = measurementUnit;
	}

	/**
	 * Format this {@link MeasurementDuration} according to the given pattern. The
	 * place holder basic format for time units is
	 * ${[0]ph[-|+][sep]}{p:prefix}{s:suffix}, where "prefix" is an arbitrary prefix
	 * text, "suffix" is an arbitrary suffix text that must not contain "}" or "{",
	 * and "ph" stands for one of the following identifiers:
	 * <ul>
	 * <li>d - Whole days in this duration</li>
	 * <li>h - Remaining hours in this duration</li>
	 * <li>M - Remaining minutes in this duration</li>
	 * <li>s - Remaining seconds in this duration</li>
	 * <li>m - Remaining milliseconds in this duration</li>
	 * <li>� - Remaining microseconds in this duration</li>
	 * <li>n - Remaining nanoseconds in this duration</li>
	 * </ul>
	 * 
	 * Optionally prepend "0" to these identifiers to specify that the identifier is
	 * to be filled up with left leading zeros up to its maximum digit count. This
	 * applies to all identifiers except d (days). For d, prepending "0" is not
	 * allowed.
	 * 
	 * Optionally append "-" to any of these identifiers to specify that the whole
	 * value including prefix and suffix shall be omitted in case of a zero value in
	 * the {@link MeasurementDuration}.
	 * 
	 * Instead of "-", optionally append "?" to any of these identifiers to specify
	 * that the whole value including prefix and suffix shall be omitted, if and
	 * only if its value AND the values of all of the next smaller units are 0 and
	 * specified to be omitted in the {@link MeasurementDuration}. If every place
	 * holder in the string contains the "-" or "+" character, and every value to
	 * insert turns out to be zero, the zero value of the {@link TimeUnit} returned
	 * by {@link #getMeasurementUnit()} is inserted into the string. This avoids
	 * empty strings in such situations.
	 * 
	 * Optionally append an arbitrary separator string "pre" behind the identifier,
	 * which is only used if end of string is not reached.
	 * 
	 * The prefix and suffix brackets are optional and may be omitted.
	 * 
	 * The pattern may contain arbitrary other text, and it may contain each place
	 * holder multiple times in different variations.
	 * 
	 * @param pattern The pattern as described above.
	 * @return A formatted String filled with the value of this
	 *         {@link MeasurementDuration} according to the specified pattern.
	 */
	public String format(String pattern) {
		Reject.ifNull(pattern, "pattern");

		Matcher matcher = FormatValue.FORMAT_VALUE_PATTERN.matcher(pattern);

		List<FormatValue> formatValues = new ArrayList<>();

		// first run: find all formats
		while (matcher.find()) {
			String placeHolder = matcher.group(1);

			if (placeHolder.isEmpty()) {
				throw new RuntimeException("Empty placeholder found which is invalid");
			}

			FormatValue nextValue = new FormatValue();

			nextValue.parseLeadingZeros(placeHolder);
			nextValue.parseValue(placeHolder);
			nextValue.parseZeroHandling(placeHolder);
			nextValue.parseDelimiter(placeHolder);
			nextValue.parsePrefixSuffix(matcher.group(3));
			nextValue.parsePrefixSuffix(matcher.group(5));
			nextValue.setValue(this);

			formatValues.add(nextValue);
		}

		// Determine if some values must be omitted
		int omittedCount = 0;

		for (int i = formatValues.size() - 1; i >= 0; --i) {
			FormatValue value = formatValues.get(i);

			value.setFollowingUnitsAreOmitted(omittedCount == (formatValues.size() - 1 - i));

			if (value.isOmitted()) {
				omittedCount++;
			}
		}

		StringBuffer result = new StringBuffer();

		matcher.reset();

		int index = 0;

		// second run: build output
		while (matcher.find()) {
			FormatValue nextValue = formatValues.get(index++);

			// Ensure that at least one is non-zero
			if ((nextValue.getUnit() == measurementUnit) && (omittedCount == formatValues.size())) {
				matcher.appendReplacement(result, nextValue.getRawFormatString());
			} else {
				matcher.appendReplacement(result, nextValue.getFormatString());
			}
		}

		matcher.appendTail(result);

		return result.toString();
	}

	/**
	 * Returns the duration time in the {@link TimeUnit} returned by
	 * {@link #getMeasurementUnit()}.
	 * 
	 * @return the duration time in the {@link TimeUnit} returned by
	 *         {@link #getMeasurementUnit()}.
	 */
	public long getDurationTime() {
		return durationTime;
	}

	/**
	 * Returns the {@link TimeUnit} of the duration time returned by
	 * {@link #getDurationTime()}.
	 * 
	 * @return the {@link TimeUnit} of the duration time returned by
	 *         {@link #getDurationTime()}.
	 */
	public TimeUnit getMeasurementUnit() {
		return measurementUnit;
	}

	/**
	 * Returns the remaining hours contained in this {@link MeasurementDuration}.
	 * See {@link #getRemainingUnits(TimeUnit)} for details.
	 * 
	 * @return the remaining hours contained in this {@link MeasurementDuration}
	 */
	public long getRemainingHours() {
		return getRemainingUnits(TimeUnit.HOURS);
	}

	/**
	 * Returns the remaining minutes contained in this {@link MeasurementDuration}.
	 * See {@link #getRemainingUnits(TimeUnit)} for details.
	 * 
	 * @return the remaining minutes contained in this {@link MeasurementDuration}
	 */
	public long getRemainingMinutes() {
		return getRemainingUnits(TimeUnit.MINUTES);
	}

	/**
	 * Returns the remaining seconds contained in this {@link MeasurementDuration}.
	 * See {@link #getRemainingUnits(TimeUnit)} for details.
	 * 
	 * @return the remaining seconds contained in this {@link MeasurementDuration}
	 */
	public long getRemainingSeconds() {
		return getRemainingUnits(TimeUnit.SECONDS);
	}

	/**
	 * A generic method returning the number of remaining {@link TimeUnit}s in the
	 * stored duration time of this {@link MeasurementDuration}. E.g. if this
	 * {@link MeasurementDuration} stores 1100 milliseconds, these correspond to one
	 * whole second and 100 milliseconds, so {@link #getRemainingUnits(TimeUnit)}
	 * called with {@link TimeUnit#MILLISECONDS} would return 100.
	 * 
	 * @param unit The {@link TimeUnit} for which to get remaining units contained.
	 * @return The number of remaining units of the given {@link TimeUnit} contained
	 *         in this {@link MeasurementDuration}.
	 */
	public long getRemainingUnits(TimeUnit unit) {
		TimeUnit upperUnit = MeasurementDuration.UPPER_UNITS.get(unit);

		if (upperUnit == null) {
			return getWholeUnits(unit);
		}

		long wholeUpperUnits = getWholeUnits(upperUnit);

		long wholeUnits = measurementUnit.convert(wholeUpperUnits, upperUnit);

		if (wholeUnits == 0) {
			return getWholeUnits(unit);
		}

		return unit.convert(durationTime % wholeUnits, measurementUnit);
	}

	/**
	 * Returns the whole days contained in this {@link MeasurementDuration}. See
	 * {@link #getWholeUnits(TimeUnit)} for details. There is no convenience method
	 * for remaining days, because days is the biggest time unit used, and therefore
	 * whole days equal remaining days.
	 * 
	 * @return the whole days contained in this {@link MeasurementDuration}
	 */
	public long getWholeDays() {
		return getWholeUnits(TimeUnit.DAYS);
	}

	/**
	 * Returns the whole hours contained in this {@link MeasurementDuration}. See
	 * {@link #getWholeUnits(TimeUnit)} for details.
	 * 
	 * @return the whole hours contained in this {@link MeasurementDuration}
	 */
	public long getWholeHours() {
		return getWholeUnits(TimeUnit.HOURS);
	}

	/**
	 * Returns the whole minutes contained in this {@link MeasurementDuration}. See
	 * {@link #getWholeUnits(TimeUnit)} for details.
	 * 
	 * @return the whole minutes contained in this {@link MeasurementDuration}
	 */
	public long getWholeMinutes() {
		return getWholeUnits(TimeUnit.MINUTES);
	}

	/**
	 * Returns the whole seconds contained in this {@link MeasurementDuration}. See
	 * {@link #getWholeUnits(TimeUnit)} for details.
	 * 
	 * @return the whole seconds contained in this {@link MeasurementDuration}
	 */
	public long getWholeSeconds() {
		return getWholeUnits(TimeUnit.SECONDS);
	}

	/**
	 * A generic method returning the number of whole {@link TimeUnit}s in the
	 * stored duration time of this {@link MeasurementDuration}. E.g. if this
	 * {@link MeasurementDuration} stores 1100 milliseconds, these correspond to one
	 * whole seconds, so {@link #getWholeUnits(TimeUnit)} called with
	 * {@link TimeUnit#SECONDS} would return 1.
	 * 
	 * @param unit The {@link TimeUnit} for which to get whole contained units.
	 * @return The number of whole units of the given {@link TimeUnit} contained in
	 *         this {@link MeasurementDuration}.
	 */
	public long getWholeUnits(TimeUnit unit) {
		return unit.convert(durationTime, measurementUnit);
	}

	/**
	 * Convenience method that returns the duration time as a string formatted
	 * according to the {@link #LONG_PATTERN_EN} pattern. See
	 * {@link #format(String)} for details.
	 * 
	 * @return formatted string of the duration time according to
	 *         {@link #LONG_PATTERN_EN}.
	 */
	public String longFormat() {
		return format(MeasurementDuration.LONG_PATTERN_EN);
	}

	/**
	 * Convenience method that returns the duration time as a string formatted
	 * according to the {@link #SHORT_PATTERN} pattern. See {@link #format(String)}
	 * for details.
	 * 
	 * @return formatted string of the duration time according to
	 *         {@link #SHORT_PATTERN}.
	 */
	public String shortFormat() {
		return format(MeasurementDuration.SHORT_PATTERN);
	}

	/**
	 * Convenience method that returns the duration time as a string formatted
	 * according to the {@link #TINY_PATTERN} pattern. See {@link #format(String)}
	 * for details.
	 * 
	 * @return formatted string of the duration time according to
	 *         {@link #TINY_PATTERN}.
	 */
	public String tinyFormat() {
		return format(MeasurementDuration.TINY_PATTERN);
	}
}