package com.github.catchitcozucan.core.histogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.catchitcozucan.core.impl.source.processor.DaProcessStepConstants;
import com.github.catchitcozucan.core.internal.util.io.IO;

public class HistogramStatus implements Comparable<HistogramStatus> {
	private static final String DEFAULT_LABEL = "Unaned Processing";
	private static final String NOT_ = "_NOT_"; // NOSONAR BULL.
	private static final String FAIL = "FAIL";
	private static final String ERROR = "ERROR";
	private static final String ENTITY_NAMES = "\"entityNames\"";
	private static final String BUCKET_NAMES = "\"bucketNames\"";
	private static final String CURLY_SPACE = "{ ";
	private static final String COLON_BRACKET = ": [";
	private static final String ENTITY_NAMES_PROCESS_HISTOGRAM_BUCKET_NAMES = new StringBuilder(CURLY_SPACE).append(ENTITY_NAMES).append(": \"Process-Histogram\", ").append(BUCKET_NAMES).append(COLON_BRACKET).toString();
	private static final String JSON_QOUTES = "\"";
	private static final String LEFT_BRACKET = "[";
	private static final String COMMA_WITH_SPACE = ", ";
	private static final String HISTOGRAMZ1 = "\"histogramz\"";
	private static final String HISTOGRAMZ = "], " + HISTOGRAMZ1 + COLON_BRACKET;
	private static final String LABEL = "{\"nameOfHistogram\": \"";
	private static final String SUM = "\", \"sum\": ";
	private static final String ACTUALLY_FINISHED = ", \"actuallyFinished\": ";
	private static final String ACTUAL_STEP_PROGRESS = ", \"actualStepProgress\": ";
	private static final String DATA = ", \"data\": [";
	private static final String JSON_BODY_CLOSURE = "]}]}";
	private static final String SINGLE_QOUTE = "'";
	private static final String SONGLE_QUOTE = SINGLE_QOUTE;
	private static final String COLON_QOUTION = ": \"";
	private static final String QOUTE_COMMA = "\", ";
	private static final String COLON_SPACE = ": ";
	private static final String COMMA = ",";
	private static final String BRACKET_CURLY = "]}";
	private static final String SINGLE_QOUTE_PLUS = "'+";
	private static final String COMMA_SINGLE_QOUTE_PLUS = ",'+";

	private static final double DOUBLE_100 = 100d;
	private static final int INT_ZERO = 0;

	private final String nameOfHistogram;
	private final Integer[] data; // NOSONAR BULL
	private final String[] dataLabels;
	private final long sum; //NOSONAR BULL.
	private final Integer actuallyFinishedPercent;
	private final Integer actualProgressPercent;
	private final Map<String, Integer> rawData;
	private String failureStatusRegExp;

	public HistogramStatus(String nameOfHistogram, Map<String, Integer> sortedInEnum, String failureStatusRegExp) {
		this(nameOfHistogram, sortedInEnum);
		this.failureStatusRegExp = failureStatusRegExp;
	}

	public HistogramStatus(String nameOfHistogram, Map<String, Integer> sortedInEnum) {
		if (!IO.hasContents(nameOfHistogram)) {
			this.nameOfHistogram = DEFAULT_LABEL;
		} else {
			this.nameOfHistogram = nameOfHistogram;
		}
		if (sortedInEnum == null || sortedInEnum.isEmpty()) {
			actuallyFinishedPercent = INT_ZERO;
			actualProgressPercent = INT_ZERO;
			sum = 0l;
			data = new Integer[INT_ZERO];
			dataLabels = new String[INT_ZERO];
		} else {
			int[] progress = getProgress(sortedInEnum);
			actuallyFinishedPercent = progress[INT_ZERO];
			actualProgressPercent = progress[1];
			sum = sortedInEnum.values().stream().mapToInt(x -> x).sum();
			data = sortedInEnum.values().toArray(new Integer[sortedInEnum.size()]);
			dataLabels = sortedInEnum.keySet().toArray(new String[sortedInEnum.size()]);
		}
		rawData = sortedInEnum;
		failureStatusRegExp = null;
	}

	public Map<String, Integer> getRawData() {
		return rawData;
	}

	public String getLabel() {
		return nameOfHistogram;
	}

	public Integer getActuallyFinishedPercent() {
		return actuallyFinishedPercent;
	}

	public Integer getActualProgressPercent() {
		return actualProgressPercent;
	}

	@Override
	public String toString() {
		return toJson(false, false, false);
	}

	public String toJson(boolean flipFailures, boolean returnOnlyFailures, boolean javascriptWrapped) { // NOSONAR this is complex stuff
		StringBuilder json = new StringBuilder();
		String[] dataLabelsReduced = null;
		if (javascriptWrapped) {
			json.append(SINGLE_QOUTE);
		}
		json.append(ENTITY_NAMES_PROCESS_HISTOGRAM_BUCKET_NAMES);

		if (!returnOnlyFailures) {
			Arrays.stream(dataLabels).forEach(d -> json.append(JSON_QOUTES).append(d).append(JSON_QOUTES).append(COMMA_WITH_SPACE));
		} else {
			List<String> actualLabelsToBePresented = new ArrayList<>();
			Arrays.stream(dataLabels).filter(this::labelSignifiesAFailure).forEachOrdered(d -> appendActual(json, actualLabelsToBePresented, d));
			dataLabelsReduced = actualLabelsToBePresented.stream().toArray(String[]::new);
		}
		if (!json.toString().endsWith(LEFT_BRACKET)) {
			json.delete(json.length() - 2, json.length());
		}
		json.append(HISTOGRAMZ);
		json.append(LABEL);
		json.append(nameOfHistogram);
		json.append(SUM);
		json.append(sum);
		json.append(ACTUALLY_FINISHED);
		json.append(actuallyFinishedPercent);
		json.append(ACTUAL_STEP_PROGRESS);
		json.append(actualProgressPercent);
		json.append(DATA);

		if (!flipFailures && !returnOnlyFailures) {
			Arrays.stream(data).forEach(d -> json.append(d).append(COMMA_WITH_SPACE));
		} else {

			if (flipFailures && !returnOnlyFailures) {
				// "flip" values for failures and errors
				Integer[] flipped = new Integer[data.length];
				for (int i = INT_ZERO; i < dataLabels.length; i++) {
					if (labelSignifiesAFailure(dataLabels[i])) {
						flipped[i] = data[i] - data[i] - data[i]; // NOSONAR BULL.
					} else {
						flipped[i] = data[i];
					}
				}
				Arrays.stream(flipped).forEach(d -> json.append(d).append(COMMA_WITH_SPACE));
			} else if (returnOnlyFailures) {

				if (dataLabelsReduced == null || dataLabelsReduced.length == INT_ZERO) { // NOSONAR you are just.. wrong.
					json.append("[]");
				} else if (dataLabelsReduced.length > INT_ZERO) {
					Integer[] flipped = new Integer[dataLabelsReduced.length];
					Integer[] indeces = new Integer[dataLabelsReduced.length];
					int i = INT_ZERO;
					int j = -1;
					for (String label : dataLabels) {
						j++;
						for (String r : dataLabelsReduced) {
							if (r.equals(label)) {
								indeces[i] = j;
								i++;
								break;
							}
						}
					}
					int h = INT_ZERO;
					for (Integer k : indeces) {
						if (!flipFailures) {
							flipped[h] = data[k];
						} else {
							flipped[h] = data[k] - data[k] - data[k];  // NOSONAR you are just.. wrong.
						}
						h++;
					}
					Arrays.stream(flipped).forEachOrdered(d -> json.append(d).append(COMMA_WITH_SPACE));
				} else {
					json.append(LEFT_BRACKET);
				}
			}
		}
		if (!json.toString().endsWith(LEFT_BRACKET)) {
			json.delete(json.length() - 2, json.length());
		}
		json.append(JSON_BODY_CLOSURE);
		if (javascriptWrapped) {
			json.append(SONGLE_QUOTE);
		}
		return json.toString();
	}

	public void setFailureStatusRegExp(String failureStatusRegExp){
		this.failureStatusRegExp = failureStatusRegExp;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof HistogramStatus)) {
			return false;
		}
		return hashCode() == other.hashCode();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(HistogramStatus other) {
		return nameOfHistogram.compareTo(other.getLabel());
	}

	public static String makeHistogramJson(String entitiesGeneralName, List<HistogramStatus> histogramStatuses, boolean javascriptWrapped, Enum[] states) {
		List<String> enumStateNames = Arrays.stream(states).map(Enum::name).collect(Collectors.toList());
		String[] orderedStates = enumStateNames.toArray(new String[enumStateNames.size()]);
		return makeHistogramJson(entitiesGeneralName, histogramStatuses, javascriptWrapped, orderedStates);
	}

	public static String makeHistogramJson(String entitiesGeneralName, List<HistogramStatus> histogramStatuses, boolean javascriptWrapped, String[] states) {
		StringBuilder bucketNames = new StringBuilder("[");
		Arrays.stream(states).forEach(n -> bucketNames.append(JSON_QOUTES).append(n).append(JSON_QOUTES).append(COMMA_WITH_SPACE));
		bucketNames.delete(bucketNames.length() - 2, bucketNames.length());
		bucketNames.append("]");
		final StringBuilder json = new StringBuilder();
		if (javascriptWrapped) {
			json.append(SINGLE_QOUTE);
		}
		json.append(CURLY_SPACE).append(ENTITY_NAMES).append(COLON_QOUTION).append(entitiesGeneralName).append(QOUTE_COMMA).append(BUCKET_NAMES).append(COLON_SPACE).append(bucketNames.toString()).append(COMMA_WITH_SPACE).append(HISTOGRAMZ1).append(COLON_BRACKET);
		if (javascriptWrapped) {
			json.append(SINGLE_QOUTE_PLUS);
		}
		json.append(DaProcessStepConstants.NL);
		if (javascriptWrapped) {
			histogramStatuses.stream().forEach(h -> {
				if (javascriptWrapped) {
					json.append(SINGLE_QOUTE);
				}
				json.append(h.toString());
				if (javascriptWrapped) {
					json.append(COMMA_SINGLE_QOUTE_PLUS);
				}
				json.append(DaProcessStepConstants.NL).toString();
			});
		}
		json.delete(json.lastIndexOf(COMMA), json.length());
		json.append(BRACKET_CURLY);
		if (javascriptWrapped) {
			json.append(SINGLE_QOUTE);
		}
		return json.toString();
	}

	private boolean labelSignifiesAFailure(String label) {
		String upper = label.toUpperCase();
		if (failureStatusRegExp == null) {
			return upper.contains(NOT_) || upper.contains(FAIL) || upper.contains(ERROR);
		} else {
			return label.matches(failureStatusRegExp);
		}
	}

	private static int[] getProgress(Map<String, Integer> sortedInEnumBins) {
		int noOfElements = sortedInEnumBins.values().stream().mapToInt(x -> x).sum();
		Optional<Integer> optFinished = sortedInEnumBins.values().stream().skip(sortedInEnumBins.keySet().stream().count() - 1).findFirst();
		int actuallyFinished = 0;
		if (optFinished.isPresent()) {
			actuallyFinished = optFinished.get();
		}
		int finishedpercent = (actuallyFinished > INT_ZERO) ? (int) Math.round(((double) actuallyFinished / (double) noOfElements) * DOUBLE_100) : INT_ZERO;
		long maxSteps = (sortedInEnumBins.size() - 1) * (long) noOfElements;
		long stepsDone = getHistogramStepsDone(sortedInEnumBins);
		int actualProgress = (int) Math.round(((double) stepsDone / (double) maxSteps) * DOUBLE_100);
		return new int[] { finishedpercent, actualProgress };
	}

	private static long getHistogramStepsDone(Map<String, Integer> sortedInEnumBins) {
		Optional<Map.Entry<String, Integer>> entry;
		String[] orderedKeyz = sortedInEnumBins.keySet().toArray(new String[sortedInEnumBins.size()]);
		int binPos = 1;
		long stepsDone = INT_ZERO;
		while (binPos < sortedInEnumBins.size()) {
			final int binPos_ = sortedInEnumBins.size() - binPos;
			entry = sortedInEnumBins.entrySet().stream().filter(e -> e.getKey().equals(orderedKeyz[binPos_])).findFirst();
			if (entry.isPresent() && entry.get().getValue() > INT_ZERO) {
				stepsDone += entry.get().getValue() * binPos_;
			}
			binPos++;
		}
		return stepsDone;
	}

	private void appendActual(StringBuilder json, List<String> collectedLabels, String labelName) {
		json.append(JSON_QOUTES).append(labelName).append(JSON_QOUTES).append(COMMA_WITH_SPACE);
		collectedLabels.add(labelName);
	}
}
