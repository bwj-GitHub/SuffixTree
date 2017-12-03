package tests;

import java.util.ArrayList;

import main.Alphabet;
import main.SuffixTree;

/**
 * Methods for building large test sets for pattern matching.
 * @author brandon
 *
 */
public class TestSuffixTree {

	public static String getRepeatedString(String subString, int repeats) {
		String s = "";
		for (int i = 0; i < repeats; i++) {
			s = String.join("", s, subString);
		}
		return s;
	}
	
	/**
	 * Search for pattern in string in O(|string|*|pattern|)
	 * @param string
	 * @param pattern
	 */
	public static void naiveSearch(String string, String pattern) {
		char[] s = string.toCharArray();
		char[] p = pattern.toCharArray();
		int n = s.length;
		int m = p.length;
		ArrayList<Integer> matches = new ArrayList<Integer>();

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < n; i++) {
			if (i + m > n) {
				break;  // done searching
			}
			boolean isMatch = true;
			for (int j = 0; j < m; j++) {
				if (s[i + j] != p[j]) {
					isMatch = false;
				}
			}
			if (isMatch) {
				matches.add(i);
			}
		}
		long end0 = System.currentTimeMillis();

		System.out.println(matches);
		double searchTime = (double) (end0 - startTime) / 1000.0;  // in seconds
		System.out.println(String.format("SearchTime = %f", searchTime));
	}

	public static void testOnString(String string, String pattern, Alphabet alphabet) {
		// Build SuffixTree:
		long startTime = System.currentTimeMillis();
		SuffixTree ST = new SuffixTree(string.toCharArray(), alphabet, false);
		long end0 = System.currentTimeMillis();
		ArrayList<Integer> results = ST.search(pattern.toCharArray(), alphabet);
		long finishedSearch = System.currentTimeMillis();
		System.out.println(results);
		
		double treeBuildTime = (double) (end0 - startTime) / 1000.0;  // in seconds
		double searchTime = (double) (finishedSearch - end0) / 1000.0;
		System.out.println(String.format("BuildTime = %f; searchTime = %f",
				treeBuildTime, searchTime));
	}

	public static void main(String[] args) {
		// Very long tests:
		Alphabet alphabet = new Alphabet("acgt$".toCharArray());
		String repeated = "attctgctagctgccatgga";  // 20 chars
		String searchSequence = "taggaattcttatagcacgg";  // 20chars, 'tagg' is unique

		String longString = getRepeatedString(repeated, 1000);  // 20k chars
		String superLongString = getRepeatedString(longString, 4);  // 80k chars

		// Short Test, 60 chars, 2 matches: 0, 40
		String test0 = searchSequence + repeated + searchSequence + "$";
		testOnString(test0, searchSequence, alphabet);
		naiveSearch(test0, searchSequence);
		System.out.println("--");

		// Test 20020 chars, 1 match: 20000
		String test1 = longString + searchSequence + "$";
		testOnString(test1, searchSequence, alphabet);
		naiveSearch(test1, searchSequence);
		System.out.println("--");

		// Test 40040 chars 2 matches: 0, 20020
		String test2 = searchSequence + longString + searchSequence + "$";
		testOnString(test2, searchSequence, alphabet);
		naiveSearch(test2, searchSequence);
		System.out.println("--");

		// 80k chars 1 match: 0:
		String test3 = searchSequence + superLongString + "$";
		testOnString(test3, searchSequence, alphabet);
		naiveSearch(test3, searchSequence);
		System.out.println("--");
	}

}
