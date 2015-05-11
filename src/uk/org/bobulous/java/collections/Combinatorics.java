/*
 * Copyright © 2015 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package uk.org.bobulous.java.collections;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

	/**
	 * A map which holds every factorial which can be represented by a
	 * <code>long</code> primitive. The domain contains only the integers zero
	 * to twenty because the factorial function is not defined for negative
	 * numbers, and the factorial of twenty-one is a number too large to be
	 * represented by a long in Java.
	 */
	private static final Map<Byte, Long> MAP_FROM_N_TO_N_FACTORIAL;

	static {
		Map<Byte, Long> map = new HashMap<>(32);
		map.put((byte) 0, 1L);
		map.put((byte) 1, 1L);
		map.put((byte) 2, 2L);
		map.put((byte) 3, 6L);
		map.put((byte) 4, 24L);
		map.put((byte) 5, 120L);
		map.put((byte) 6, 720L);
		map.put((byte) 7, 5_040L);
		map.put((byte) 8, 40_320L);
		map.put((byte) 9, 362_880L);
		map.put((byte) 10, 3_628_800L);
		map.put((byte) 11, 39_916_800L);
		map.put((byte) 12, 479_001_600L);
		map.put((byte) 13, 6_227_020_800L);
		map.put((byte) 14, 87_178_291_200L);
		map.put((byte) 15, 1_307_674_368_000L);
		map.put((byte) 16, 20_922_789_888_000L);
		map.put((byte) 17, 355_687_428_096_000L);
		map.put((byte) 18, 6_402_373_705_728_000L);
		map.put((byte) 19, 121_645_100_408_832_000L);
		map.put((byte) 20, 2_432_902_008_176_640_000L);
		MAP_FROM_N_TO_N_FACTORIAL = Collections.unmodifiableMap(map);
	}

	/*
	 Private constructor because this class is never intended to be instantiated.
	 */
	private Combinatorics() {
	}

	/**
	 * Returns the factorial of the given number.
	 *
	 * @param n a number of at least zero and at most twenty.
	 * @return the factorial of the given number.
	 */
	private static long factorial(int n) {
		if (n < 0 || n > 20) {
			throw new IllegalArgumentException(
					"n must be at least zero and not greater than twenty.");
		}
		return MAP_FROM_N_TO_N_FACTORIAL.get((byte) n);
	}

	/*
	*** COMBINATION METHODS
	*/
	
	/**
	 * Calculates the total number of combinations of all sizes which could be
	 * generated from a set of the given size. The number returned will count
	 * the empty set in the total.
	 *
	 * @param setSize the number of elements in the source set. Cannot be less
	 * than zero and cannot be larger than 62.
	 * @return the number of combinations of all sizes which could be generated
	 * from a set which has <code>setSize</code> elements.
	 */
	public static final long numberOfCombinations(int setSize) {
		if (setSize < 0) {
			throw new IllegalArgumentException(
					"setSize cannot be less than zero.");
		}
		if (setSize > 62) {
			throw new IllegalArgumentException(
					"setSize cannot be greater than 62.");
		}
		return 1L << setSize;
	}

	/**
	 * Calculates the number of combinations of the specified size which could
	 * be generated from a source set of the specified size. The number returned
	 * will count the empty set in the total.
	 *
	 * @param setSize the number of elements in the source set. Cannot be less
	 * than zero and cannot be larger than 62.
	 * @param chooseSize the number of elements in each combination. Cannot be
	 * less than zero and cannot be larger than <code>setSize</code>.
	 * @return the number of combinations of size <code>chooseSize</code> which
	 * could be generated from a set which has <code>setSize</code> elements.
	 */
	public static final long numberOfCombinations(int setSize, int chooseSize) {
		if (setSize < 0) {
			throw new IllegalArgumentException(
					"setSize cannot be less than zero.");
		}
		if (setSize > 62) {
			throw new IllegalArgumentException(
					"setSize cannot be greater than 62.");
		}
		if (chooseSize < 0) {
			throw new IllegalArgumentException(
					"chooseSize cannot be less than zero.");
		}
		if (chooseSize > setSize) {
			throw new IllegalArgumentException(
					"chooseSize cannot be greater than setSize.");
		}
		return numberOfCombinationsWithoutValidation(setSize, chooseSize);
	}

	/*
	 Use a private method without parameter validation to handle the calculations
	 which will be called repeatedly when numerOfCombinations is called with an
	 interval of choose sizes.
	 */
	private static long numberOfCombinationsWithoutValidation(int setSize,
			int chooseSize) {
		if (chooseSize == 0 || chooseSize == setSize) {
			return 1L;
		}
		if (chooseSize == 1 || chooseSize == setSize - 1) {
			return setSize;
		}
		/*
		 To calculate n!/(k!(n-k)!) where n==setSize and k==chooseSize and where
		 n! is the factorial function applied to n, take a shortcut by factoring
		 out all common factors in the numerator and denominator.
		 If k is larger than (n-k) then factor out all values from k! from both
		 the numerator and the denominator.
		 If (n-k) is larger than k then factor out all values from (n-k)! from
		 both the numerator and the denominator.
		 */
		int sizeMinusChoose = setSize - chooseSize;
		int breakPoint = Math.max(chooseSize, sizeMinusChoose);
		int numeratorStartPoint = Math.min(chooseSize, sizeMinusChoose);
		// Start off by gathering all numbers which have not been factored out
		// of the numerator.
		List<Integer> numeratorFactors = new ArrayList<>(setSize - breakPoint);
		for (int x = setSize; x > breakPoint; --x) {
			numeratorFactors.add(x);
		}
		// Now gather all the numbers which have not been factored out of the
		// denominator.
		ArrayList<Integer> denominatorFactors
				= new ArrayList(numeratorStartPoint - 1);
		for (int x = numeratorStartPoint; x > 1; --x) {
			denominatorFactors.add(x);
		}

		// Divide the numerator by the denominator to get the final result.
		if (setSize > 28) {
			// The long primitive cannot handle a numerator which is larger than
			// 28! divided by 14! so if the size of the set is greater than 28
			// we must switch to using BigInteger for the calculations.
			return bigIntegerDivision(numeratorFactors, denominatorFactors);
		} else {
			return longDivision(numeratorFactors, denominatorFactors);
		}
	}

	/**
	 * Calculates the product of all specified numerator factors divided by the
	 * product of all the specified denominator factors. A <code>long</code>
	 * primitive is used for the calculations, so the product of the numerator
	 * factors cannot have a value which is greater than 28!/14! (twenty-eight
	 * factorial divided by fourteen factorial).
	 *
	 * @param numeratorFactors a <code>Collection&lt;Integer&gt;</code> which
	 * contains all of the factors which make up the numerator.
	 * @param denominatorFactors a <code>Collection&lt;Integer&gt;</code> which
	 * contains all of the factors which make up the denominator.
	 * @return the numerator product divided by the denominator product.
	 */
	private static long longDivision(Collection<Integer> numeratorFactors,
			Collection<Integer> denominatorFactors) {
		// Calculate the numerator and the denominator products.
		long numerator = 1L;
		for (int x : numeratorFactors) {
			numerator *= x;
		}
		long denominator = 1L;
		for (int x : denominatorFactors) {
			denominator *= x;
		}
		// Return the numerator divided by the denominator.
		return numerator / denominator;
	}

	/**
	 * Calculates the product of all specified numerator factors divided by the
	 * product of all the specified denominator factors. A
	 * <code>BigDecimal</code> primitive is used for the calculations, so the
	 * product of the numerator factors can be arbitrarily large.
	 *
	 * @param numeratorFactors a <code>Collection&lt;Integer&gt;</code> which
	 * contains all of the factors which make up the numerator.
	 * @param denominatorFactors a <code>Collection&lt;Integer&gt;</code> which
	 * contains all of the factors which make up the denominator.
	 * @return the numerator product divided by the denominator product.
	 */
	private static long bigIntegerDivision(Collection<Integer> numeratorFactors,
			Collection<Integer> denominatorFactors) {
		// Calculate the numerator and the denominator products.
		BigInteger numerator = BigInteger.ONE;
		for (int x : numeratorFactors) {
			numerator = numerator.multiply(BigInteger.valueOf(x));
		}
		BigInteger denominator = BigInteger.ONE;
		for (int x : denominatorFactors) {
			denominator = denominator.multiply(BigInteger.valueOf(x));
		}
		// Return the numerator divided by the denominator.
		return numerator.divide(denominator).longValue();
	}

	/**
	 * Calculates the total number of combinations of the sizes permitted by the
	 * supplied interval which could be generated from a source set of the
	 * specified size.
	 *
	 * @param setSize the number of elements in the source set. Cannot be less
	 * than zero and cannot be larger than 62.
	 * @param chooseInterval an <code>Interval&lt;Integer&gt;</code> which
	 * defines the interval of combinations sizes to be included in the returned
	 * count. The interval cannot be empty, and the lower endpoint cannot be
	 * less than zero, and the upper endpoint cannot be greater than
	 * <code>setSize</code>.
	 * @return the number of combinations of the permitted sizes which could be
	 * generated from a set which has <code>setSize</code> elements.
	 */
	public static final long numberOfCombinations(int setSize,
			Interval<Integer> chooseInterval) {
		if (setSize < 0) {
			throw new IllegalArgumentException(
					"setSize cannot be less than zero.");
		}
		if (setSize > 62) {
			throw new IllegalArgumentException(
					"setSize cannot be greater than 62.");
		}
		Objects.requireNonNull(chooseInterval);
		validateInterval(chooseInterval, setSize);
		// If the interval permits all combination sizes, or all but the zero
		// and/or full set sizes, then the calculation can be simplified.
		if (chooseInterval.includes(setSize)) {
			if (chooseInterval.includes(0)) {
				// The interval permits all possible combination sizes from zero
				// up to and including the full set size, so call the basic
				// version of this method.
				return numberOfCombinations(setSize);
			} else if (chooseInterval.includes(1)) {
				// The interval permits all possible combinations except the
				// empty set, so simply subtract one from the count of all
				// possible combinations.
				return numberOfCombinations(setSize) - 1;
			}
		} else if (chooseInterval.includes(0)) {
			if (chooseInterval.includes(setSize - 1)) {
				// The interval does not permit the full set size, but it does
				// permit every other size, so simply subtract one from the
				// count of all possible combinations (because only the
				// combination which holds the entire source set is being
				// excluded).
				return numberOfCombinations(setSize) - 1;
			}
		} else if (chooseInterval.includes(1) && chooseInterval.includes(setSize
				- 1)) {
			// The interval does not permit the combination of size zero (the
			// empty set) nor does it permit the combination which contains all
			// of the source elements, but it does permit every other size of
			// combination. So simply subtract two from the count of all
			// possible combinations.
			return numberOfCombinations(setSize) - 2;
		}
		int firstSize = chooseInterval.getLowerEndpoint();
		int lastSize = chooseInterval.getUpperEndpoint();
		long totalCombinationCount = 0;
		for (int comboSize = firstSize; comboSize <= lastSize; ++comboSize) {
			if (!chooseInterval.includes(comboSize)) {
				// This size must be excluded by an open endpoint in the
				// interval so skip to the next size.
				continue;
			}
			totalCombinationCount += numberOfCombinationsWithoutValidation(
					setSize, comboSize);
		}
		return totalCombinationCount;
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
		return generateCombinations(sourceElements, true, null);
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
		return generateCombinations(sourceElements, false,
				new GenericInterval<>(choose,
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
		return generateCombinations(sourceElements, false, chooseInterval);
	}

	/*
	 Private method to contain the code used by the public overloaded methods.
	 */
	private static <T> Set<Set<T>> generateCombinations(
			Set<T> sourceElements,
			boolean chooseAll, Interval<Integer> chooseInterval) {
		List<T> sourceList = new ArrayList<>(sourceElements);
		int elementCount = sourceList.size();
		final long combinationCount;
		if (chooseAll) {
			combinationCount = numberOfCombinations(elementCount);
		} else {
			combinationCount = numberOfCombinations(elementCount,
					chooseInterval);
		}
		// The final set will always contain combinationCount elements, so it
		// makes sense to set the capacity of the HashSet so that
		// combinationCount is less than 75% of the capacity. This should avoid
		// the need for the HashSet to resize itself at any point.
		long idealSetCapacity = 1L + combinationCount * 4L / 3L;
		Set<Set<T>> allCombinations = new HashSet<>(idealSetCapacity
				> Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) idealSetCapacity);

		for (int comboSize = 0; comboSize <= elementCount; ++comboSize) {
			if (!chooseAll && !chooseInterval.includes(comboSize)) {
				// We are only interested in combinations which contain a number
				// of elements permitted by the chooseInterval.
				continue;
			}
			if (comboSize == 0) {
				allCombinations.add(new HashSet<>(1));
				continue;
			}
			
			// Use an array of "pegs" which each point to an element index. Each
			// peg must point to a unique element index, and new combinations
			// will be found by shifting the pegs across the element indices
			// from left (element index zero) to right (element index equal to
			// comboSize - 1) until all possible combinations have been found.
			int[] pegElementIndices = new int[comboSize];
			// Start off with the pegs lined up tight against the left.
			for (int i = 0; i < comboSize; ++i) {
				pegElementIndices[i] = i;
			}
			do {
				Set<T> currentCombination = new HashSet<>(1 + comboSize * 4 / 3);
				for (int setIndex : pegElementIndices) {
					currentCombination.add(sourceList.get(setIndex));
				}
				allCombinations.add(currentCombination);

				// Check whether the left-most peg is as far to the right as it
				// can go. When this happens all of the pegs will be lined up
				// tightly against the right, and this means there is no more room
				// to shift the pegs.
				if (pegElementIndices[0] == elementCount - comboSize) {
					// We have reached the final combination of this size, so
					// exit the loop.
					break;
				}

				// Now shift the bits in the BitSet to the find the next combination.
				// Find the right-most (highest index) "peg" which can shift to
				// the right by one position to give us a new combination.
				for (int peg = comboSize - 1; peg >= 0; --peg) {
					if (pegElementIndices[peg] < elementCount - comboSize + peg) {
						// This index refers to a peg which has room to move one
						// to the right so increase its index value by one.
						++pegElementIndices[peg];
						// If there are any subsequent pegs (pegs with a higher
						// array index than the peg we just shifted) then reset
						// all of their indices so that they line up tight
						// against the peg we just shifted.
						for (int subPeg = peg + 1; subPeg < comboSize; ++subPeg) {
							pegElementIndices[subPeg] = pegElementIndices[peg] + subPeg - peg;
						}
						break;
					}
				}
			} while (true);
		}
		return allCombinations;
	}
	
	/**
	 * Returns an iterator which will iterate through every combination of the
	 * elements found in the supplied set. The empty set will be amongst the
	 * combinations returned by the iterator.
	 * <p>
	 * The provided set can be arbitrarily large because this method does not
	 * return all combinations in one go so the limitations of the
	 * {@link #combinations(java.util.Set)} family of methods do not apply.
	 * However, be aware that the total number of combinations of all sizes is
	 * equal to two raised to the power of the number of elements in the
	 * provided set. Iterating through all of the 4.2 billion combinations
	 * generated from a set of size 32 took twenty-four minutes on the
	 * development machine, and completion time is likely to be more than double
	 * for a source set with a size greater by one extra element.</p>
	 *
	 * @param <T> the type of the elements found in the supplied set.
	 * @param sourceElements a <code>Set</code> of elements to be used to create
	 * combinations. Must not be null.
	 * @return an <code>Iterator&lt;Set&lt;T&gt;&gt;</code> which will iterate
	 * through every possible combination of all sizes, including the empty set.
	 */
	public static final <T> Iterator<Set<T>> iteratorOfCombinations(
			Set<T> sourceElements) {
		Objects.requireNonNull(sourceElements);
		return new CombinationsIterator(sourceElements, null);
	}

	/**
	 * Returns an iterator which will iterate through all combinations of the
	 * specified size which can be produced from the elements of the supplied
	 * set.
	 *
	 * @param <T> the type of the elements found in the supplied set.
	 * @param sourceElements a <code>Set</code> of elements to be used to create
	 * combinations. Must not be null.
	 * @param chooseSize the number of elements to be included in each
	 * combination returned by this iterator. Must be at least zero and at must
	 * not be greater than the number of elements found in the supplied set.
	 * @return an <code>Iterator&lt;Set&lt;T&gt;&gt;</code> which will iterate
	 * through all combinations of the specified size.
	 */
	public static final <T> Iterator<Set<T>> iteratorOfCombinations(
			Set<T> sourceElements, int chooseSize) {
		Objects.requireNonNull(sourceElements);
		if (chooseSize < 0) {
			throw new IllegalArgumentException(
					"chooseSize cannot be less than zero.");
		}
		if (chooseSize > sourceElements.size()) {
			throw new IllegalArgumentException(
					"chooseSize cannot be greater than the size of sourceElements.");
		}
		return new CombinationsIterator(sourceElements, new GenericInterval<>(
				chooseSize, chooseSize));
	}

	/**
	 * Returns an iterator which will iterate through combinations which can be
	 * produced from elements of the supplied set, including all combinations
	 * with sizes permitted by the supplied interval.
	 *
	 * @param <T> the type of the elements found in the supplied set.
	 * @param sourceElements a <code>Set</code> of elements to be used to create
	 * combinations. Must not be null.
	 * @param chooseInterval an <code>Interval&lt;Integer&gt;</code> which
	 * specifies which combination sizes should be returned by this iterator.
	 * Must not be null, and cannot have a lower endpoint less than zero, nor an
	 * upper endpoint greater than the number of elements found in the supplied
	 * set.
	 * @return an <code>Iterator&lt;Set&lt;T&gt;&gt;</code> which will iterate
	 * through all combinations of the permitted sizes.
	 */
	public static final <T> Iterator<Set<T>> iteratorOfCombinations(
			Set<T> sourceElements, Interval<Integer> chooseInterval) {
		Objects.requireNonNull(sourceElements);
		Objects.requireNonNull(chooseInterval);
		int elementCount = sourceElements.size();
		validateInterval(chooseInterval, elementCount);
		return new CombinationsIterator(sourceElements, chooseInterval);
	}

	/**
	 * An iterator which returns a single combination at a time.
	 *
	 * @param <T> the type of the elements which will be found in the source set
	 * supplied to this iterator, and the type of the elements which will be
	 * found in the combination sets returned by this iterator.
	 */
	private static class CombinationsIterator<T> implements Iterator<Set<T>> {

		private final List<T> sourceElements;
		int elementCount;
		private final Interval<Integer> chooseInterval;
		private int currentComboSize;
		private int firstExcludedComboSize;
		private int[] pegElementIndices;

		/**
		 * Constructs a <code>CombinationsIterator&lt;T&gt;</code> which will
		 * return combinations using elements from the supplied set and having
		 * sizes permitted by the supplied interval.
		 *
		 * @param sourceElements a <code>Set</code> of elements to be used to
		 * create combinations. Must not be null.
		 * @param chooseInterval an <code>Interval&lt;Integer&gt;</code> which
		 * specifies which combination sizes should be returned by this
		 * iterator. If <code>null</code> is provided then all combination sizes
		 * from zero up to and including the number of elements in the source
		 * set will be included in the results returned by this iterator; if an
		 * actual interval is provided then it cannot have a lower endpoint less
		 * than zero, nor an upper endpoint greater than the number of elements
		 * found in the supplied set.
		 */
		CombinationsIterator(Set<T> sourceElements,
				Interval<Integer> chooseInterval) {
			Objects.requireNonNull(sourceElements);
			this.sourceElements = new ArrayList<>(sourceElements);
			elementCount = this.sourceElements.size();
			this.chooseInterval = chooseInterval;
			if (this.chooseInterval != null) {
				validateInterval(this.chooseInterval, sourceElements.size());
				currentComboSize = this.chooseInterval.getLowerEndpoint();
				if (this.chooseInterval.getLowerEndpointMode().equals(
						Interval.EndpointMode.OPEN)) {
					++currentComboSize;
				}
				firstExcludedComboSize = this.chooseInterval.
						getUpperEndpoint();
				if (this.chooseInterval.getUpperEndpointMode().equals(
						Interval.EndpointMode.CLOSED)) {
					++firstExcludedComboSize;
				}
			} else {
				currentComboSize = 0;
				firstExcludedComboSize = sourceElements.size() + 1;
			}
			if (currentComboSize > 0) {
				this.pegElementIndices = new int[currentComboSize];
				// Start off with the pegs lined up tight against the left.
				for (int i = 0; i < currentComboSize; ++i) {
					pegElementIndices[i] = i;
				}
			}
		}

		@Override
		public boolean hasNext() {
			if (currentComboSize < firstExcludedComboSize) {
				// We've not exceeded the final combination size that will be
				// generated by this iterator, so there must still be at least
				// one combination still to come.
				return true;
			}
			return false;
		}

		@Override
		public Set<T> next() {
			Set<T> currentCombination = new HashSet<>(1 + currentComboSize * 4
					/ 3);
			if (currentComboSize > 0) {
				for (int setIndex : pegElementIndices) {
					currentCombination.add(sourceElements.get(setIndex));
				}
			}

			if (currentComboSize == 0 || pegElementIndices[0] == elementCount
					- currentComboSize) {
				// We've found all of the combinations of the current size, so
				// move up to the next combination size.
				++currentComboSize;
				if (currentComboSize < firstExcludedComboSize) {
					pegElementIndices = new int[currentComboSize];
					// Start off with the pegs lined up tight against the left.
					for (int i = 0; i < currentComboSize; ++i) {
						pegElementIndices[i] = i;
					}
				}
			} else {
				// We still have more combinations available in the current size
				// so shift the pegs to reach the next combination.
				for (int peg = currentComboSize - 1; peg >= 0; --peg) {
					if (pegElementIndices[peg] < elementCount - currentComboSize
							+ peg) {
						// This index refers to a peg which has room to move one
						// to the right so increase its index value by one.
						++pegElementIndices[peg];
						// If there are any subsequent pegs (pegs with a higher
						// array index than the peg we just shifted) then reset
						// all of their indices so that they line up tight
						// against the peg we just shifted.
						for (int subPeg = peg + 1; subPeg < currentComboSize;
								++subPeg) {
							pegElementIndices[subPeg] = pegElementIndices[peg]
									+ subPeg - peg;
						}
						break;
					}
				}
			}
			return currentCombination;
		}
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
		if (SetUtilities.containsNull(sourceElements)) {
			throw new NullPointerException(
					"sourceElements must not contain a null element.");
		}
		return generateCombinationsSorted(sourceElements, true, null);
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
		if (SetUtilities.containsNull(sourceElements)) {
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
		return generateCombinationsSorted(sourceElements, false,
				new GenericInterval<>(choose, choose));
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
		return generateCombinationsSorted(sourceElements, false, chooseInterval);
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
	private static <T extends Comparable<T>> SortedSet<SortedSet<T>> generateCombinationsSorted(
			Set<T> sourceElements, boolean chooseAll,
			Interval<Integer> chooseInterval) {
		List<T> sourceList = new ArrayList<>(sourceElements);
		int elementCount = sourceList.size();
		SortedSet<SortedSet<T>> allCombinations = new TreeSet<>(
				SortedSetComparator.<T>getInstance());
		int maxBitMaskValue = 1 << elementCount;
		for (int combination = 0; combination < maxBitMaskValue; ++combination) {
			BitSet comboMask = BitSet.valueOf(new long[]{combination});
			if (!chooseAll && !chooseInterval.includes(comboMask.cardinality())) {
				// We are only interested in combinations which contain a number
				// of elements permitted by the chooseInterval. If
				// this comboMask would result in number of elements which falls
				// outside of this interval then skip to the next comboMask.
				continue;
			}
			SortedSet<T> currentCombination = new TreeSet<>();
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

	/*
	 *** PERMUTATION METHODS ***
	 */
	
	/**
	 * Returns the number of permutations which would exist for a set of the
	 * specified size.
	 *
	 * @param setSize the number of elements in a set. Cannot be less than one
	 * and cannot be greater than twenty.
	 * @return the number of permutations which could be generated from a set of
	 * the given size.
	 */
	public static final long numberOfPermutations(int setSize) {
		if (setSize < 1) {
			throw new IllegalArgumentException(
					"setSize must be greater than zero.");
		}
		if (setSize > 20) {
			throw new IllegalArgumentException(
					"setSize cannot be greater than twenty.");
		}
		return factorial(setSize);
	}

	/**
	 * Generates all permutations of the given <code>Set</code>.
	 * <p>
	 * Be warned that at a source set of size eleven will lead to heap memory
	 * error unless at least 8GiB are allocated to the application. And a heap
	 * memory error is almost certain with a source set of size twelve.</p>
	 *
	 * @param <T> the type of the elements contained in the supplied
	 * <code>Set</code>.
	 * @param sourceElements the source set which will be used to generate
	 * permutations. Cannot be <code>null</code>, cannot contain a
	 * <code>null</code> element and cannot contain more than twelve elements.
	 * @return a <code>Set&lt;List&lt;T&gt;&gt;</code> which contains every
	 * possible permutation of the source set.
	 */
	public static final <T> Set<List<T>> permutations(Set<T> sourceElements) {
		Objects.requireNonNull(sourceElements);
		int elementCount = sourceElements.size();
		if (elementCount < 1) {
			throw new IllegalArgumentException(
					"sourceElements must contain at least one element.");
		}
		if (elementCount > 12) {
			throw new IllegalArgumentException(
					"sourceElements cannot contain more than twelve elements.");
		}
		if (SetUtilities.containsNull(sourceElements)) {
			throw new NullPointerException(
					"sourceElements cannot contain a null element.");
		}
		List<T> elements = new ArrayList<>(sourceElements);

		int totalPermutationQuantity = (int) factorial(elementCount);
		long idealSetCapacity = 1L + (int) (totalPermutationQuantity * 4L / 3L);
		Set<List<T>> perms = new HashSet<>((int) idealSetCapacity);

		// Add the initial list as the first permutation.
		perms.add(new ArrayList<>(elements));

		// Use Fuchs' Counting QuickPerm Algorithm to iterate through all
		// remaining permutations. See http://quickperm.org/
		int[] swapCount = new int[elementCount];
		int elementIndex = 1;
		while (elementIndex < elementCount) {
			if (swapCount[elementIndex] < elementIndex) {
				int swapIndex
						= elementIndex % 2 == 0 ? 0 : swapCount[elementIndex];
				T currentElement = elements.get(elementIndex);
				T swapperElement = elements.get(swapIndex);
				elements.set(elementIndex, swapperElement);
				elements.set(swapIndex, currentElement);
				perms.add(new ArrayList<>(elements));
				++swapCount[elementIndex];
				elementIndex = 1;
			} else {
				swapCount[elementIndex] = 0;
				++elementIndex;
			}
		}

		// Return the generated Set of all permutations.
		return perms;
	}
}
