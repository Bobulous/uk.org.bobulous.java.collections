# uk.org.bobulous.java.collections
A package related to Java collections. Still in alpha testing and prone to fundamental change.

Currently contains a Combinatorics class which allows all possible combinations to be generated from a provided Set, either as an unordered Set or as a SortedSet which is ordered based on the SortedSetComparator which is also included in the package. Also contains a method which generates all permutations of a given Set. Currently these methods complete within a reasonable time for sets which contain ten or fewer elements, but completion time becomes diabolically lengthy for sets of greater sizes. Attempting to generate permutations with a set of size eleven takes over a minute and requires 8GiB of heap space. Attempting to generate permutations with a set of size twelve leads to heap space exhaustion after several minutes.

Relies on the uk.org.bobulous.java.intervals package (also available on my GitHub account).

See the Javadoc for each class and method for full details.
