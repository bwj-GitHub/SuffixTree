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
	public static ArrayList<Integer> naiveSearch(String string, String pattern) {
		char[] s = string.toCharArray();
		char[] p = pattern.toCharArray();
		int n = s.length;
		int m = p.length;
		ArrayList<Integer> matches = new ArrayList<Integer>();

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
		return matches;
	}

	public static double getStdev(double[] nums, double mean) {
		int n = nums.length;
		double squaredSum = 0.0;
		for (int i = 0; i < n; i++) {
			squaredSum += Math.pow((nums[i] - mean), 2);
		}
		return Math.sqrt(squaredSum / n);
	}

	public static void multiRunNaiveSearch(String string, String pattern, int nRuns) {
		double totalTime = 0;
		double[] runTimes = new double[nRuns];
		for (int i = 0; i < nRuns; i++) {
			long start = System.currentTimeMillis();
			ArrayList<Integer> results = naiveSearch(string, pattern);
			long end = System.currentTimeMillis();
			double searchTime = (double) (end - start) / 1000.0;
			runTimes[i] = searchTime;
			totalTime += searchTime;
			
//			if (i == 0) {
//				System.out.println(results);
//			}
		}
		
//		for (double runTime: runTimes) {
//			System.out.println("Run Time=" + runTime);
//		}
		totalTime /= nRuns;
		double stdev = getStdev(runTimes, totalTime);
		System.out.println("Naive Search Time=" + totalTime + "; std.=" + stdev);
	}

	public static ArrayList<Integer> testOnString(String string, String pattern,
			Alphabet alphabet) {
		// Build SuffixTree:
//		long startTime = System.currentTimeMillis();
		SuffixTree ST = new SuffixTree(string.toCharArray(), alphabet, false);
//		long end0 = System.currentTimeMillis();
		ArrayList<Integer> results = ST.search(pattern.toCharArray(), alphabet);
//		long finishedSearch = System.currentTimeMillis();
//		System.out.println(results);

//		System.out.println("Suffix Tree Search:");
//		double treeBuildTime = (double) (end0 - startTime) / 1000.0;  // in seconds
//		double searchTime = (double) (finishedSearch - end0) / 1000.0;
//		System.out.println(String.format("BuildTime = %f; searchTime = %f",
//				treeBuildTime, searchTime));
		return results;
	}

	public static void multiRunTest(String string, String pattern,
			Alphabet alphabet, int nRuns) {
		double totalTime = 0;
		double[] runTimes = new double[nRuns];
		for (int i = 0; i < nRuns; i++) {
			long start = System.currentTimeMillis();
			ArrayList<Integer> results = testOnString(string, pattern, alphabet);
			long end = System.currentTimeMillis();
			double searchTime = (double) (end - start) / 1000.0;
			totalTime += searchTime;
			runTimes[i] = searchTime;
			if (i == 0) {
				System.out.println(results.size());
			}
		}
		totalTime /= nRuns;
		double stdev = getStdev(runTimes, totalTime);
//		for (double runTime: runTimes) {
//			System.out.println("Run Time=" + runTime);
//		}
		
		System.out.println("Suffix Tree Search Time=" + totalTime + "; std.=" + stdev);
	}
	
	public static void doSmallPatternTests() {
		Alphabet alphabet = new Alphabet("acgt$".toCharArray());
		String repeated = "attctgctagctgccatgga";  // 20 chars
		String searchSequence = "taggaattcttatagcacgg";  // 20chars, 'tagg' is unique

		String longString = getRepeatedString(repeated, 1000);  // 20k chars
		String superLongString = getRepeatedString(longString, 4);  // 80k chars
		String longString320k = getRepeatedString(superLongString, 4);  // 160k
		
		// Throw away test runs, for JIT:
		// Test Pattern 2k chars:
		System.out.println("Preparing...");
		String test0 = longString320k + searchSequence + 
				longString320k + "$";
		multiRunTest(test0, searchSequence, alphabet, 5);
		multiRunNaiveSearch(test0, searchSequence, 5);
		System.out.println("-------");

		// Test 20020 chars, 1 match: 20000
		System.out.println("--20k--");
		String test1 = longString + searchSequence + "$";
		multiRunTest(test1, searchSequence, alphabet, 10);
		multiRunNaiveSearch(test1, searchSequence, 10);
		System.out.println("--");

		// Test 40020 chars 1 matches: 2000
		System.out.println("--40k--");
		String test2 = longString + searchSequence + longString + "$";
		multiRunTest(test2, searchSequence, alphabet, 10);
		multiRunNaiveSearch(test2, searchSequence, 10);
		System.out.println("--");

		// Test ~80020 chars 1 match: 0:
		System.out.println("--80k--");
		String test3 = searchSequence + superLongString + "$";
		multiRunTest(test3, searchSequence, alphabet, 10);
		multiRunNaiveSearch(test3, searchSequence, 10);
		System.out.println("--");

		// Test 160020 chars
		System.out.println("--160k--");
		String test4 = superLongString + searchSequence + superLongString + "$";
		multiRunTest(test4, searchSequence, alphabet, 10);
		multiRunNaiveSearch(test4, searchSequence, 10);
		System.out.println("--");

		// Test 320020 chars
		System.out.println("--320k--");
		String test5 = superLongString + superLongString + searchSequence + 
				superLongString + superLongString + "$";
		multiRunTest(test5, searchSequence, alphabet, 10);
		multiRunNaiveSearch(test5, searchSequence, 10);
		System.out.println("--");

		// Test 640020 chars
		System.out.println("--640k--");
		String test6 = longString320k + searchSequence + 
				longString320k + "$";
		multiRunTest(test6, searchSequence, alphabet, 10);
		multiRunNaiveSearch(test6, searchSequence, 10);
		System.out.println("--");
	}

	public static void doVaryingPatternSizeTests() {
		Alphabet alphabet = new Alphabet("acgt$".toCharArray());

		// Create strings:
		String repeated = "attctgctagctgccatgga";  // 20 chars
		String s20k = getRepeatedString(repeated, 1000);  // 20k chars
		String s40k = getRepeatedString(s20k, 2);
		String s80k = getRepeatedString(s40k, 2);
		String s160k = getRepeatedString(s80k, 2);
		String s320k = getRepeatedString(s160k, 2);

		// Create patterns:
		String p20 = "taggaattcttatagcacgg";  // 20chars, 'tagg' is unique
		String p2k = getRepeatedString(p20, 100);  // 2k chars
		String p4k = getRepeatedString(p2k, 2);  // 4k chars
		String p8k = getRepeatedString(p4k, 2);  // 8k chars
		String p16k = getRepeatedString(p8k, 2);  // 16k chars
		String p32k = getRepeatedString(p16k, 2);  // 32k chars
		String p64k = getRepeatedString(p32k, 2);  // 64k chars

		// Throw away test runs, for JIT:
		System.out.println("Preparing...");
		String test0 = s320k + p2k + s320k + "$";
		multiRunTest(test0, p2k, alphabet, 20);
		multiRunNaiveSearch(test0, p2k, 20);
		System.out.println("-------");

		// Test Pattern 2k chars:
		System.out.println("2k");
		String test1 = s320k + p2k + getRepeatedString(repeated, 15900) + "$";
//		String test1 = s320k + p2k + s320k + "$";
		multiRunTest(test1, p2k, alphabet, 10);
		multiRunNaiveSearch(test1, p2k, 10);
		System.out.println("--");

		// Test Pattern 4k chars:
		System.out.println("4k");
		String test2 = s320k + p4k + getRepeatedString(repeated, 15800) + "$";
//		String test2 = s320k + p4k + s320k + "$";
		multiRunTest(test2, p4k, alphabet, 10);
		multiRunNaiveSearch(test2, p4k, 10);
		System.out.println("--");

		// Test Pattern 8k chars:
		System.out.println("8k");
		String test3 = s320k + p8k + getRepeatedString(repeated, 15600) + "$";
//		String test3 = s320k + p8k + s320k + "$";
		multiRunTest(test3, p8k, alphabet, 10);
		multiRunNaiveSearch(test3, p8k, 10);
		System.out.println("--");

		// Test Pattern 16k chars:
		System.out.println("16k");
		String test4 = s320k + p16k + getRepeatedString(repeated, 15200) + "$";
//		String test4 = s320k + p16k + s320k + "$";
		multiRunTest(test4, p16k, alphabet, 10);
		multiRunNaiveSearch(test4, p16k, 5);
		System.out.println("--");

		// Test Pattern 32k chars:
		System.out.println("32k");
		String test5 = s320k + p32k + getRepeatedString(repeated, 14400) + "$";
//		String test5 = s320k + p32k + s320k + "$";
		multiRunTest(test5, p32k, alphabet, 10);
		multiRunNaiveSearch(test5, p32k, 5);
		System.out.println("--");

		// Test Pattern 64k chars:
		System.out.println("64k");
		String test6 = s320k + p64k + getRepeatedString(repeated, 12800) + "$";
//		String test6 = s320k + p64k + s320k + "$";
		multiRunTest(test6, p64k, alphabet, 10);
		multiRunNaiveSearch(test6, p64k, 5);
		System.out.println("--");


	}

	public static void doVaryingK() {
		Alphabet alphabet = new Alphabet("acgt$".toCharArray());

		// Create strings:
		String repeated = "attctgctagctgccatgga";  // 20 chars
		String s20k = getRepeatedString(repeated, 1000);  // 20k chars
		String s40k = getRepeatedString(s20k, 2);
		String s80k = getRepeatedString(s40k, 2);
		String s160k = getRepeatedString(s80k, 2);
		String s320k = getRepeatedString(s160k, 2);

		// Create patterns:
		String p20 = "taggaattcttatagcacgg";  // 20chars, 'tagg' is unique
		String p2k = getRepeatedString(p20, 100);  // 2k chars
		String p4k = getRepeatedString(p2k, 2);  // 4k chars
		String p8k = getRepeatedString(p4k, 2);  // 8k chars
		String p16k = getRepeatedString(p8k, 2);  // 16k chars
		String p32k = getRepeatedString(p16k, 2);  // 32k chars
		String p64k = getRepeatedString(p32k, 2);  // 64k chars
		String p128k = getRepeatedString(p64k, 2);  // 128k chars

		// Throw away test runs, for JIT:
		System.out.println("Preparing...");
		String test = s320k + p4k + s320k + "$";
		multiRunTest(test, p2k, alphabet, 20);
		multiRunNaiveSearch(test, p2k, 10);
		System.out.println("-------");

		// Test 100 matches:
		String test0 = s320k + p2k + getRepeatedString(repeated, 15900) + "$";
//		String test0 = s320k + p2k + s320k + "$";
		multiRunTest(test0, p20, alphabet, 10);
		multiRunNaiveSearch(test0, p20, 10);
		System.out.println("-------");

		// Test 200 matches:
		String test1 = s320k + p4k + getRepeatedString(repeated, 15800) + "$";
//		String test1 = s320k + p4k + s320k + "$";
		multiRunTest(test1, p20, alphabet, 10);
		multiRunNaiveSearch(test1, p20, 10);
		System.out.println("-------");

		// Test 400 matches:
		String test2 = s320k + p8k + getRepeatedString(repeated, 15600) + "$";
//		String test2 = s320k + p8k + s320k + "$";
		multiRunTest(test2, p20, alphabet, 10);
		multiRunNaiveSearch(test2, p20, 10);
		System.out.println("-------");

		// Test 800 matches:
		String test3 = s320k + p16k + getRepeatedString(repeated, 15200) + "$";
//		String test3 = s320k + p16k + s320k + "$";
		multiRunTest(test3, p20, alphabet, 10);
		multiRunNaiveSearch(test3, p20, 10);
		System.out.println("-------");

		// Test 1600 matches: -800
		String test4 = s320k + p32k + getRepeatedString(repeated, 14400) + "$";
//		String test4 = s320k + p32k + s320k + "$";
		multiRunTest(test4, p20, alphabet, 10);
		multiRunNaiveSearch(test4, p20, 10);
		System.out.println("-------");

		// Test 3200 matches:
		String test5 = s320k + p64k + getRepeatedString(repeated, 12800) + "$";
//		String test5 = s320k + p64k + s320k + "$";
		multiRunTest(test5, p20, alphabet, 10);
		multiRunNaiveSearch(test5, p20, 10);
		System.out.println("-------");

		// Test 4800 matches:
		
		String test5b = s320k + p64k + getRepeatedString(repeated, 11200) + p32k + "$";
//		String test5b = s320k + p64k + s320k + "$";
		multiRunTest(test5b, p20, alphabet, 10);
		multiRunNaiveSearch(test5b, p20, 10);
		System.out.println("-------");

		// Test 6400 matches:
		String test6 = s320k + p128k + getRepeatedString(repeated, 9600) + "$";
//		String test6 = s320k + p128k + s320k + "$";
		multiRunTest(test6, p20, alphabet, 10);
		multiRunNaiveSearch(test6, p20, 10);
		System.out.println("-------");

		// Test 9600 matches:
		String test6b = s320k + p128k + getRepeatedString(repeated, 6400) + p64k + "$";
//		String test6b = s320k + p128k + s320k + p64k + "$";
		multiRunTest(test6b, p20, alphabet, 10);
		multiRunNaiveSearch(test6b, p20, 10);
		System.out.println("-------");

		// Test 12800 matches:
		String test7 = s320k + p128k + getRepeatedString(repeated, 3200) + p128k + "$";
//		String test7 = s320k + p128k + s320k + p128k + "$";
		multiRunTest(test7, p20, alphabet, 10);
		multiRunNaiveSearch(test7, p20, 10);
		System.out.println("-------");

		// Test 16000 matches:
		// NOTE: Too many matches; StackOverFlowError
//		String test8 = p64k + s320k + p128k + s320k + p128k + "$";
//		multiRunTest(test8, p20, alphabet, 10);
//		multiRunNaiveSearch(test8, p20, 10);
//		System.out.println("-------");
	}

	public static void main(String[] args) {
//		doSmallPatternTests();
//		doVaryingPatternSizeTests();
//		doVaryingK();

		// Test very large string sizes:
		Alphabet alphabet = new Alphabet("acgt$".toCharArray());
		String p20 = "taggaattcttatagcacgg";  // 20chars, 'tagg' is unique
		String repeated = "attctgctagctgccatgga";  // 20 chars
		String p2k = getRepeatedString(p20, 100);  // 2k chars
		String s1280k = getRepeatedString(repeated, 26500) + p2k + "$";
		long startTime = System.currentTimeMillis();
		SuffixTree ST = new SuffixTree(s1280k.toCharArray(), alphabet, false);
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);

	}

}
