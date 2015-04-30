/*
 * Copyright © 2015 Bobulous <http://www.bobulous.org.uk/>.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/.
 */
package uk.org.bobulous.java.collections;

import java.util.Objects;
import java.util.Set;

/**
 * A utility class to provide static methods for <code>Set</code> objects.
 *
 * @author Bobulous <http://www.bobulous.org.uk/>
 */
public final class SetUtilities {

	/*
	 * Private constructor because this class is never intended to be
	 * instantiated.
	 */
	private SetUtilities() {
	}

	/**
	 * Reports on whether the specified <code>Set</code> contains
	 * <code>null</code>.
	 * <p>
	 * This method is necessary because calling <code>Set.contains(null)</code>
	 * will throw a <code>NullPointerException</code> if the underlying
	 * <code>Set</code> instance is a type which does not permit
	 * <code>null</code> as an element.</p>
	 * TODO: Double-check that the JDK libraries do not provide a safe way of doing this already.
	 *
	 * @param set a <code>Set</code> to check for a <code>null</code> element.
	 * @return <code>true</code> if the specified <code>Set</code> contains
	 * <code>null</code>; <code>false</code> otherwise.
	 */
	public static final boolean containsNull(Set<?> set) {
		Objects.requireNonNull(set);
		try {
			if (set.contains(null)) {
				return true;
			}
			return false;
		} catch (NullPointerException npe) {
			// If set.contains(null) causes a NullPointerException to be thrown
			// then it means that the underlying Set instance is of a type which
			// is not permitted to contain null. So it cannot contain null.
			return false;
		}
	}
}
