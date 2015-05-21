/*
 * Copyright © 2015 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package uk.org.bobulous.java.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;

/**
 * A <code>Comparator</code> which compares two <code>SortedSet</code> instances
 * based on the number of elements in each set, and then on the natural ordering
 * of the elements in each set.
 * <p>
 * If set alpha contains fewer elements than set beta then
 * <code>compare(alpha, beta)</code> will return a negative integer; if alpha
 * contains a greater number of elements than beta then a positive integer will
 * be returned.</p>
 * <p>
 * If both sets alpha and beta have the same number of elements then the
 * elements of the two sets will be compared. If their first elements (based on
 * the ordering of the <code>SortedSet</code>) are different then a negative
 * integer will be returned if alpha's first element is considered to come
 * before beta's first element (based on the natural ordering of the element
 * type) and a positive integer will be returned if alpha's first element is
 * considered to come after beta's. If the first elements are both identical
 * then the second elements of the two sets are compared in the same way, and so
 * on. Only if both sets have an identical size and identical elements in the
 * same order will <code>compare(alpha, beta)</code> return zero.</p>
 * <p>
 * For example, when the two sets are of type SortedSet&lt;Character&gt; with
 * values:
 * <ul>
 * <li>alpha: {'a', 'b', 'c', 'd'}</li>
 * <li>beta: {'a', 'b', 'c'}
 * </ul>
 * then <code>compare(alpha, beta)</code> will return a positive integer because
 * alpha has a greater number of elements than beta. So the
 * <code>SortedSet</code> alpha is considered to come after the
 * <code>SortedSet</code> beta under the ordering defined by this
 * <code>Comparator</code>.
 * </p>
 * <p>
 * As another example, when the two sets are of type SortedSet&lt;Integer&gt;
 * with values:
 * <ul>
 * <li>alpha: {5, 6, 7, 8}</li>
 * <li>beta: {5, 6, 7, 9}
 * </ul>
 * then <code>compare(alpha, beta)</code> will return a negative integer because
 * the two sets have an identical number of elements, and the first three
 * elements of the two sets have an identical value, but the value of the fourth
 * element of alpha is 8 which comes before the value 9 of beta's fourth
 * element. So the <code>SortedSet</code> alpha is considered to come before the
 * <code>SortedSet</code> beta under the ordering defined by this
 * <code>Comparator</code>.
 * </p>
 * <p>
 * <code>SortedSetComparator</code> is <em>consistent with equals</em> so long
 * as the <code>compareTo</code> method of the element type <code>T</code> is
 * consistent with equals <strong>and</strong> both sets are ordered the same
 * way. If the two <code>SortedSet</code> instances were constructed with
 * different <code>Comparator</code> types which do not guarantee to return the
 * same result under the same conditions, then <code>SortedSetComparator</code>
 * will not be consistent with equals, because the two sets might contain
 * exactly the same elements but in a different order.</p>
 * <p>
 * If either of the <code>SortedSet</code> instances passed to the
 * <code>compare</code> method contains a <code>null</code> element then a
 * <code>NullPointerException</code> will be thrown. This is because
 * <code>null</code> is not an instance of any class and so cannot have a place
 * within the natural order of any type, and it is the natural order which is
 * used by <code>compare</code> to compare the two sets.</p>
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 * @param <T> the type of the elements contained in the <code>SortedSet</code>.
 * Must be a type which implements <code>Comparable&lt;T&gt;</code>, in other
 * words the type must have a natural order.
 */
public final class SortedSetComparator<T extends Comparable<T>> implements
		Comparator<SortedSet<T>> {

	/*
	 Private constructor, to force the use of the getInstance method.
	 */
	private SortedSetComparator() {
	}

	/**
	 * Singleton instance of <code>SortedSetComparator</code> which is used in
	 * all cases.
	 * <p>
	 * The <code>compare</code> method behaves identically regardless of the
	 * type of the element type, and this class is immutable, so we only need
	 * one instance which will be used in all cases.</p>
	 */
	private static final SortedSetComparator singleton
			= new SortedSetComparator();

	/**
	 * Returns an instance of <code>SortedSetComparator</code> which compares
	 * sorted sets of the specified element type.
	 * <p>
	 * To get an <code>SortedSetComparator</code> which compares sets of element
	 * type <code>K</code> you need to call this method like so:</p>
	 * <p>
	 * <code>SortedSetComparator&lt;K&gt; comparator =
	 * SortedSetComparator.&lt;K&gt;getInstance();</code></p>
	 *
	 * @param <K> the element type of <code>SortedSet</code> objects which will
	 * be compared by this <code>Comparator</code>.
	 * @return a <code>Comparator</code> which compares two
	 * <code>SortedSet</code> objects having the specified element type.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Comparable<K>> SortedSetComparator<K> getInstance() {
		return (SortedSetComparator<K>) singleton;
	}

	/**
	 * Compares <code>SortedSet</code> objects based on their size and on the
	 * elements they contain.
	 * <p>
	 * If set alpha contains fewer elements than set beta then
	 * <code>compare(alpha, beta)</code> will return a negative integer; if
	 * alpha contains a greater number of elements than beta then a positive
	 * integer will be returned.</p>
	 * <p>
	 * If both sets alpha and beta have the same number of elements then the
	 * elements of the two sets will be compared. If their first elements (based
	 * on the ordering of the <code>SortedSet</code>) are different then a
	 * negative integer will be returned if alpha's first element is considered
	 * to come before beta's first element (based on the natural ordering of the
	 * element type) and a positive integer will be returned if alpha's first
	 * element is considered to come after beta's first element. If the first
	 * elements are both identical then the second elements of the two sets are
	 * compared in the same way, and so on. Only if both sets have an identical
	 * size and identical elements in the same order will
	 * <code>compare(alpha, beta)</code> return zero.</p>
	 *
	 * @param alpha a <code>SortedSet&lt;T&gt;</code>. Must not be
	 * <code>null</code>.
	 * @param beta the basis <code>SortedSet&lt;T&gt;</code> against which the
	 * first will be compared. Must not be <code>null</code>.
	 * @return an <code>int</code> which will be negative if <code>alpha</code>
	 * is judged to come before <code>beta</code> based on the ordering
	 * described; positive if <code>alpha</code> comes after <code>beta</code>;
	 * and zero if both sets have identical size, identical elements and
	 * identical order.
	 */
	@Override
	public int compare(SortedSet<T> alpha, SortedSet<T> beta) {
		Objects.requireNonNull(alpha);
		Objects.requireNonNull(beta);
		if (SetUtilities.containsNull(alpha) || SetUtilities.containsNull(beta)) {
			throw new NullPointerException(
					"SortedSetComparator cannot compare any Set which contains "
					+ "a null element.");
		}
		int sizeComparison = Integer.compare(alpha.size(), beta.size());
		if (sizeComparison != 0) {
			// Sizes of the two sets differ, so the one with fewest elements is
			// considered to come before the one with a higher number of
			// elements.
			return sizeComparison;
		}
		// Sizes are the same, so we must compare the elements within the two
		// sets based on their natural ordering.
		Iterator<T> setOneIterator = alpha.iterator();
		Iterator<T> setTwoIterator = beta.iterator();
		while (setOneIterator.hasNext()) {
			// Compare the values of the elements at the same "position" within
			// each Set. (These sets are both of type SortedSet so they have a
			// natural ordering to their elements.)
			T setOneElement = setOneIterator.next();
			T setTwoElement = setTwoIterator.next();
			int elementComparison = setOneElement.compareTo(setTwoElement);
			if (elementComparison != 0) {
				// The two elements do not have the same value, so whichever Set
				// has the element which comes first (in terms of natural
				// ordering) is considered to come before the other Set.
				return elementComparison;
			}
		}
		// The size of both sets is identical, and all of the elements are
		// identical, so we consider these sets to be identical in terms of this
		// Comparator.
		return 0;
	}
}
