/*
 * Copyright © 2015 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package uk.org.bobulous.java.collections;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import uk.org.bobulous.java.intervals.GenericInterval;
import uk.org.bobulous.java.intervals.Interval;

/**
 * A utility class which provides static methods related to enumerative
 * combinatorics.
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 */
public final class Combinatorics {

	/*
	 Private constructor because this class is never intended to be instantiated.
	 */
	private Combinatorics() {
	}

	/**
	 * Finds every combination which can be produced using the elements from the
	 * provided source set.
	 * <p>
	 * The returned <code>Set</code> will contain an empty set (which represents
	 * the combination which uses none of the elements from the source set) and
	 * sets of every size from one element up to and including the size of the
	 * number of elements contained in the source set. Each element in the
	 * source set can only be used once per combination. The total number of
	 * combinations (and thus the total size of the returned <code>Set</code>)
	 * will be two raised to the power of the size of the source set. (Be aware
	 * that for a source set which contains twenty elements there are more than
	 * one million combinations.)</p>
	 *
	 * @param <T> the type of the elements contained by the provided set.
	 * @param sourceElements a <code>Set</code> which represents the source set
	 * of elements to be used in producing combinations. Cannot be
	 * <code>null</code> and cannot contain more than thirty elements.
	 * @return a <code>Set</code> which contains one or more sets, one for each
	 * possible combination of the elements found in
	 * <code>sourceElements</code>.
	 */
	public static final <T> Set<Set<T>> combinations(Set<T> sourceElements) {
		Objects.requireNonNull(sourceElements);
		if (sourceElements.size() > 30) {
			throw new IllegalArgumentException(
					"Size of sourceElements cannot be greater than thirty elements.");
		}
		return combinations(sourceElements, true, null);
	}

	/**
	 * Finds every combination of the specified size using the elements from the
	 * provided source set.
	 * <p>
	 * The returned <code>Set</code> will contain every combination of the
	 * specified size which can be produced using the elements contained in the
	 * source set, where each element in the source set can only be used once
	 * per combination.</p>
	 *
	 * @param <T> the type of the elements contained in the provided set.
	 * @param sourceElements a <code>Set</code> which represents the source set
	 * of elements to be used in producing combinations. Cannot be
	 * <code>null</code> and cannot contain more than thirty elements.
	 * @param choose the number of source elements to be included in each
	 * combination returned by this method. The number must be at least zero and
	 * must not be greater than the size of <code>sourceElements</code>.
	 * @return a <code>Set</code> which contains one or more sets, one for each
	 * combination of the given size produced using the elements found in
	 * <code>sourceElements</code>.
	 */
	public static final <T> Set<Set<T>> combinations(Set<T> sourceElements,
			int choose) {
		Objects.requireNonNull(sourceElements);
		if (sourceElements.size() > 30) {
			throw new IllegalArgumentException(
					"Size of sourceElements cannot be greater than thirty elements.");
		}
		if (choose < 0) {
			throw new IllegalArgumentException(
					"Parameter choose must be non-negative.");
		}
		if (choose > sourceElements.size()) {
			throw new IllegalArgumentException(
					"Parameter choose cannot be greater than the size of sourceElements.");
		}
		return combinations(sourceElements, false, new GenericInterval<>(choose,
				choose));
	}

	/**
	 * Finds every combination of the specified sizes using the elements from
	 * the provided source set.
	 * <p>
	 * The returned <code>Set</code> will contain every combination of the sizes
	 * permitted by the specified interval, each combination produced using the
	 * elements contained in the source set, where each element in the source
	 * set can only be used once per combination.</p>
	 *
	 * @param <T> the type of the elements contained in the provided set.
	 * @param sourceElements a <code>Set</code> which represents the source set
	 * of elements to be used to produce combinations. Cannot be
	 * <code>null</code> and cannot contain more than thirty elements.
	 * @param chooseInterval an <code>Interval&lt;Integer&gt;</code> which
	 * specifies the interval of combination sizes to be included in the set of
	 * combinations returned by this method. The lower endpoint value must be at
	 * least zero, and the upper endpoint value must not be greater than the
	 * size of the <code>sourceElements</code> set, and the interval must not be
	 * an empty set (which excludes all values).
	 * @return a <code>Set</code> which contains one or more sets, one for each
	 * combination of the permitted sizes, produced using the elements found in
	 * <code>sourceElements</code>.
	 */
	public static final <T> Set<Set<T>> combinations(Set<T> sourceElements,
			Interval<Integer> chooseInterval) {
		Objects.requireNonNull(sourceElements);
		Objects.requireNonNull(chooseInterval);
		if (sourceElements.size() > 30) {
			throw new IllegalArgumentException(
					"Size of sourceElements cannot be greater than thirty elements.");
		}
		validateInterval(chooseInterval, sourceElements.size());
		return combinations(sourceElements, false, chooseInterval);
	}

	/*
	 Private method to contain the code used by the public overloaded methods.
	 */
	private static <T> Set<Set<T>> combinations(Set<T> sourceElements,
			boolean chooseAll, Interval<Integer> chooseInterval) {
		List<T> sourceList = new ArrayList<>(sourceElements);
		int elementCount = sourceList.size();
		final int combinationCount = (int) Math.
				pow(2.0, (double) elementCount);
		// The final set will always contain combinationCount elements, so it
		// makes sense to set the capacity of the HashSet so that
		// combinationCount is less than 75% of the capacity. This should avoid
		// the need for the HashSet to resize itself at any point.
		int initialSetCapacity = 1 + combinationCount * 4 / 3;
		Set<Set<T>> allCombinations = new HashSet<>(initialSetCapacity);
		for (int combination = 0; combination < combinationCount; ++combination) {
			Set<T> currentCombination;
			currentCombination = new HashSet<>();
			BitSet comboMask = BitSet.valueOf(new long[]{combination});
			if (!chooseAll && !chooseInterval.includes(comboMask.cardinality())) {
				// We are only interested in combinations which contain a number
				// of elements permitted by the chooseInterval. If
				// this comboMask would result in number of elements which falls
				// outside of this interval then skip to the next comboMask.
				continue;
			}
			for (int elementIndex = 0; elementIndex < elementCount;
					++elementIndex) {
				if (comboMask.get(elementIndex)) {
					currentCombination.add(sourceList.get(elementIndex));
				}
			}
			allCombinations.add(currentCombination);
		}
		return allCombinations;
	}

	/**
	 * Finds every combination which can be produced using the elements from the
	 * provided source set, and returns the results in a <code>SortedSet</code>.
	 * <p>
	 * The returned <code>SortedSet</code> will contain an empty set (which
	 * represents the combination which uses none of the elements from the
	 * source set) and sets of every size from one element up to and including
	 * the size of the number of elements contained in the source set. Each
	 * element in the source set can only be used once per combination. The
	 * total number of combinations (and thus the total size of the returned
	 * <code>SortedSet</code>) will be two raised to the power of the size of
	 * the source set.</p>
	 * <p>
	 * The returned <code>SortedSet</code> will be ordered firstly by the size
	 * of each combination, so that the empty set appears first, then the sets
	 * holding combinations of size one, then the sets holding combinations of
	 * size two, and so on until the set whose size is that of the source set.
	 * Where combinations have the same size, the second level of ordering takes
	 * into consideration the element values within each combination set, so
	 * that the natural ordering of the element type is used to determine which
	 * combination comes first in the order. See {@link SortedSetComparator} for
	 * the full details of the <code>Comparator</code> used in this ordering.
	 * </p>
	 *
	 * @param <T> the type of the elements contained by the provided set. Must
	 * be a type which has a natural order.
	 * @param sourceElements a <code>Set</code> which represents the source set
	 * of elements to be combined. Cannot be <code>null</code>, cannot contain a
	 * <code>null</code> element, and cannot contain more than thirty elements.
	 * @return a <code>SortedSet</code> which contains one or more sets, one for
	 * each possible combination of the elements found in
	 * <code>sourceElements</code>, sorted by combination sizes and by the
	 * natural order of the elements within each combination.
	 */
	public static final <T extends Comparable<T>> SortedSet<SortedSet<T>> combinationsSorted(
			Set<T> sourceElements) {
		Objects.requireNonNull(sourceElements);
		if (sourceElements.size() > 30) {
			throw new IllegalArgumentException(
					"Size of sourceElements cannot be greater than thirty elements.");
		}
		if (sourceElements.contains(null)) {
			throw new NullPointerException(
					"sourceElements must not contain a null element.");
		}
		return combinationsSorted(sourceElements, true, null);
	}

	/**
	 * Finds every combination of the specified size which can be produced using
	 * the elements from the provided source set, and returns the results in a
	 * <code>SortedSet</code>.
	 * <p>
	 * The returned <code>SortedSet</code> will contain every combination of the
	 * specified size which can be produced using the elements contained in the
	 * source set, where each element in the source set can only be used once
	 * per combination.</p>
	 * <p>
	 * The returned <code>SortedSet</code> will be ordered by comparing the
	 * element values within each combination set, so that the natural ordering
	 * of the element type is used to determine which combination comes first in
	 * the order. See {@link SortedSetComparator} for the full details of the
	 * <code>Comparator</code> used in this ordering.
	 * </p>
	 *
	 * @param <T> the type of the elements contained by the provided set. Must
	 * be a type which has a natural order.
	 * @param sourceElements a <code>Set</code> which represents the source set
	 * of elements to be combined. Cannot be <code>null</code>, cannot contain a
	 * <code>null</code> element, and cannot contain more than thirty elements.
	 * @param choose the number of source elements to be included in the
	 * combinations returned by this method.
	 * @return a <code>SortedSet</code> which contains one or more sets, one for
	 * each combination of the given size produced using the elements found in
	 * <code>sourceElements</code>, sorted by the natural order of the elements
	 * within each combination.
	 */
	public static final <T extends Comparable<T>> SortedSet<SortedSet<T>> combinationsSorted(
			Set<T> sourceElements, int choose) {
		Objects.requireNonNull(sourceElements);
		if (sourceElements.size() > 30) {
			throw new IllegalArgumentException(
					"Size of sourceElements cannot be greater than thirty elements.");
		}
		if (sourceElements.contains(null)) {
			throw new NullPointerException(
					"sourceElements must not contain a null element.");
		}
		if (choose < 0) {
			throw new IllegalArgumentException(
					"Parameter choose must be non-negative.");
		}
		if (choose > sourceElements.size()) {
			throw new IllegalArgumentException(
					"Parameter choose cannot be greater than the size of sourceElements.");
		}
		return combinationsSorted(sourceElements, false, new GenericInterval<>(
				choose, choose));
	}

	/**
	 * Finds every combination of the specified sizes using elements from the
	 * provided source set, and returns the results in a <code>SortedSet</code>.
	 * <p>
	 * The returned <code>SortedSet</code> will contain every combination of the
	 * sizes permitted by the specified interval, each combination produced
	 * using the elements contained in the source set, where each element in the
	 * source set can only be used once per combination.</p>
	 * <p>
	 * The returned <code>SortedSet</code> will be ordered firstly by the size
	 * of each combination, so that the smallest permitted combination size
	 * appears first, and so on until the largest permitted combination size.
	 * Where combinations have the same size, the second level of ordering takes
	 * into consideration the element values within each combination set, so
	 * that the natural ordering of the element type is used to determine which
	 * combination comes first in the order. See {@link SortedSetComparator} for
	 * the full details of the <code>Comparator</code> used in this ordering.
	 * </p>
	 *
	 * @param <T> the type of the elements contained by the provided set. Must
	 * be a type which has a natural order.
	 * @param sourceElements a <code>Set</code> which represents the source set
	 * of elements to be combined. Cannot be <code>null</code>, cannot contain a
	 * <code>null</code> element, and cannot contain more than thirty elements.
	 * @param chooseInterval an <code>Interval&lt;Integer&gt;</code> which
	 * specifies the interval of combination sizes to be included in the set of
	 * combinations returned by this method. The lower endpoint value must be at
	 * least zero, and the upper endpoint value must not be greater than the
	 * size of the <code>sourceElements</code> set, and the interval must not be
	 * an empty set (which excludes all values).
	 * @return a <code>SortedSet</code> which contains one or more sets, one for
	 * each combination of the permitted sizes, produced using the elements
	 * found in <code>sourceElements</code>, sorted by combination sizes and by
	 * the natural order of the elements within each combination.
	 */
	public static final <T extends Comparable<T>> SortedSet<SortedSet<T>> combinationsSorted(
			Set<T> sourceElements, Interval<Integer> chooseInterval) {
		Objects.requireNonNull(sourceElements);
		if (sourceElements.size() > 30) {
			throw new IllegalArgumentException(
					"Size of sourceElements cannot be greater than thirty elements.");
		}
		if (SetUtilities.containsNull(sourceElements)) {
			throw new NullPointerException(
					"sourceElements must not contain a null element.");
		}
		Objects.requireNonNull(chooseInterval);
		validateInterval(chooseInterval, sourceElements.size());
		return combinationsSorted(sourceElements, false, chooseInterval);
	}

	/*
	 Check that the interval permits at least one integer between zero and the
	 specified maximum combination size, and no integer less than zero or greater
	 than the maximum combination size.
	 */
	private static void validateInterval(Interval<Integer> interval,
			int maxCombinationSize) {
		Integer lower = interval.getLowerEndpoint();
		if (lower == null || lower < 0) {
			throw new IllegalArgumentException(
					"The lower endpoint of chooseInterval must be non-negative.");
		}
		Integer upper = interval.getUpperEndpoint();
		if (upper == null || upper > maxCombinationSize) {
			throw new IllegalArgumentException(
					"The upper endpoint of chooseInterval cannot be greater than the size of sourceElements.");
		}
		// Check that the interval is not an empty set (which does not permit
		// any integers at all).
		if (upper < lower || upper - lower < 2) {
			if (!interval.includes(lower) && !interval.includes(upper)) {
				throw new IllegalArgumentException(
						"chooseInterval cannot be an empty set.");
			}
		}
	}

	/*
	 Private method to contain the code used by the public overloaded methods.
	 */
	private static <T extends Comparable<T>> SortedSet<SortedSet<T>> combinationsSorted(
			Set<T> sourceElements, boolean chooseAll,
			Interval<Integer> chooseInterval) {
		List<T> sourceList = new ArrayList<>(sourceElements);
		int elementCount = sourceList.size();
		final int combinationCount = (int) Math.
				pow(2.0, (double) elementCount);
		SortedSet<SortedSet<T>> allCombinations = new TreeSet<>(
				SortedSetComparator.<T>getInstance());
		for (int combination = 0; combination < combinationCount; ++combination) {
			SortedSet<T> currentCombination;
			currentCombination = new TreeSet<>();
			BitSet comboMask = BitSet.valueOf(new long[]{combination});
			if (!chooseAll && !chooseInterval.includes(comboMask.cardinality())) {
				// We are only interested in combinations which contain a number
				// of elements permitted by the chooseInterval. If
				// this comboMask would result in number of elements which falls
				// outside of this interval then skip to the next comboMask.
				continue;
			}
			for (int elementIndex = 0; elementIndex < elementCount;
					++elementIndex) {
				if (comboMask.get(elementIndex)) {
					currentCombination.add(sourceList.get(elementIndex));
				}
			}
			allCombinations.add(currentCombination);
		}
		return allCombinations;
	}
}
