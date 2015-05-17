# uk.org.bobulous.java.collections
A package related to Java collections. Still in alpha testing and prone to fundamental change. Relies on the [uk.org.bobulous.java.intervals package](https://github.com/Bobulous/uk.org.bobulous.java.intervals).

Currently contains a `Combinatorics` class which provides methods for generating combinations of elements found in a given `Set<T>`, returning results in either an unordered `Set<Set<T>>` or as a `SortedSet<SortedSet<T>>` ordered by the `SortedSetComparator` which is also included in the package. `Combinatorics` also contains a method which generates all permutations of a given set, and methods to report the total number of combinations or permutations for given sizes.

For example, given a `Set<String>` called `tenStringSet` which contains ten strings, all 210 possible combinations of size six can be generated with the following code:

    Set<Set<String>> sixElementCombinations = Combinatorics.combinations(tenStringSet, 6);

To avoid the steep memory cost and processing delay of generating all combinations or permutations of a large source set, the `Combinatorics` class offers methods which return an iterator so that one combination or permutation can be requested at a time as needed.

For example, given a `Set<Integer>` called `tenIntegerSet` which contains ten integers, an iterator can be produced to act on one permutation at a time with the following code:

    Iterator<List<Integer>> permIterator = Combinatorics.iteratorOfPermutations(tenIntegerSet);
    while (permutationIterator.hasNext()) {
        List<Integer> currentPermutation = permutationIterator.next();
        // Do something useful with the currentPermutation.
    }

Be aware that a with a source set of ten elements, there are 10! = 3,628,800 permutations.

See the Javadoc for each class and method for full details.
