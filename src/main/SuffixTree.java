package main;

import java.util.ArrayList;
import java.util.Arrays;

public class SuffixTree {

	public char[] string;
	public Node root;
	public Alphabet alphabet;
	public int numLeafNodes;

	/**
	 * Construct suffix-tree for string using Weiner's algorithm.
	 * @param string
	 * @param alphabet
	 */
	public SuffixTree(char[] string, Alphabet alphabet, boolean verbose) {
		// NOTE: string should have an out-of-alphabet character appended to it
		int m = string.length;
		int A = alphabet.length;
		this.alphabet = alphabet;
		this.numLeafNodes = m;
		this.string = string;

		root = new Node(-1, A, 0, 0);

//		Assume we have constructed Ti+1. To construct Ti
//		Start at leaf i+1 in Ti+1 and walk towards the root to identify the first node v (if it exists) such that Iv(S(i)) = 1. If found, continue walking towards root to identify first node v’ such that Lv’(S(i)) is non-null.
//		Cases: In Ti+1, (i) both v and v’ exist, (ii) neither v nor v’ exist, and (iii) v exists but v’ does not.
//		Let li be the number of characters between v’ and v.

		Node curNode = root;  // leaf i+1
		for (int i = m-1; i >= 0; i--) {

//			if (verbose)
//				System.out.println("\n" + i + " " + curNode);

			// Construct SuffixTree Ti for string[i...m]
			int j = alphabet.getIndex(string[i]);

			// Walk towards root looking for v and v'
			Node v = null;
			Node v2 = null;  // v' in notes
			int ti = 0;  // number of characters from root to v.
			int Ii = 0;  // number of characters on path from v' to v
			char c = '_';  // first character on path between v and v'
			while (curNode != null) {
				// Find v:
				if (curNode.I[j] == 1 && v == null) {

					if (verbose)
						System.out.println("\tfound v: " + curNode);

					v = curNode;
				}

				// Find v'
				// TODO: Stop walking after finding V2
				if (v2 == null && curNode.L[j] != null) {

					if (verbose)
						System.out.println("\tfound v2: " + curNode);

					v2 = curNode;
					break;
				}

				// Update Iu(S[i])=1 for every node u on path from root to leaf i+1:
				curNode.I[alphabet.getIndex(string[i])] = 1;  // update (3)

				// break if at root?
				if (curNode.parentEdge == null) {
					break;
				}

				// Add to values ti and Ii:
				if (v != null) {
					ti += curNode.parentEdge.length();
					if (v2 == null) {
						Ii += curNode.parentEdge.length();
					}
				}

				// Keep track of first char on path between cur and next nodes
				if (v2 == null) {
					c = string[curNode.parentEdge.start];
				}

				// Update curNode:
				curNode = curNode.parentEdge.parentNode;
			}

			// Step 3:
			// Find headI, e, z, and v3 (if each exists)
			// Determine Head(i):
			Edge e = null;  // for step 3
			Node v3 = null;  // v'' in notes
			Node z = null;  // for step 3a
			int headI = 0;
			if (v != null) {
				if (v2 == null) {  // Step 3A
					// Head(i) ends exactly ti + 1 characters from root
					// select edge beginning with S(i)
					// edge e = (r, z) exists (and will need to be split)
					headI = ti + 1;
					e = root.childEdges[j];
					z = e.childNode;
				} else {  // Step 3B
					// Both v and v' exist
					v3 = v2.L[j];  // v'' in notes
					headI = Ii + v3.getPathDepth();
					if (verbose) {
						System.out.println("headI=" + headI);
						System.out.println("pathDepth=" + v3.getPathDepth());
						System.out.println("Ii=" + Ii);
					}
					e = v3.childEdges[alphabet.getIndex(c)];

					if (e != null) {  // head(i) == 0
						z = e.childNode;
					}
					// Head(i) ends Ii characters below v'' on edge e
				}
			}

//			if (verbose) {
//				System.out.println("first c. on path from v to v': " + c);
//				System.out.println("headI: " + headI);
//				System.out.println("ti: " + ti);
//				System.out.println("Ii: " + Ii);
//				System.out.println("e: " + e);
//				System.out.println("v3: " + v3);
//			}


			// Step 4: Determine node w and create leafi:
			// TODO: Clean this up!
			Node w;  // node at end of Head(i) (might already exist):
			Node leaf = new Node(i, alphabet.length, -1, m - i + 1);  // don't care about nodeDepth

			if (v != null) {
				if (v2 == null) {  // from 3A
					// Head(i) ends ti+1 characters from root on edge e
					w = findW(e, ti + 1);
				} else {  // from 3B
					// Head(i) ends Ii characters onto edge e (or on node v'')
					if (Ii == 0) {
						w = v3;
					} else {
						w = findW(e, Ii);
					}
				}
			} else {
				// Head(i) ends at root
				w = root;
			}

//			if (verbose)
//				System.out.println("\tw=" + w);
			
			// Create edge connecting Node W to leafi:
			int newEdgeStart = i + headI;
			Edge edgeWI = new Edge(newEdgeStart , m-1);
			Edge.connectNodes(w, leaf, edgeWI, string, alphabet);

//			if (verbose) {
//				System.out.println("\tw=" + w);
//				System.out.println("\tleafi=" + leaf);
//			}

			// Update Indicator and Link Vectors:
			if (z != null)
				w.I = z.I.clone();
			updateLinkVectors(i, v, w);

			// Update curNode:
			curNode = leaf;
			
//			if (verbose) {
//				System.out.println(root.simpleString(alphabet, ""));
//				System.out.println("--------------");
//			}
		}

//		System.out.println(root);
//		System.out.println(root.simpleString(alphabet, ""));
	}

	/**
	 * Check if Node w exists offset characters into edge e; otherwise, create it.
	 * @param e
	 * @param offset
	 * @return
	 */
	private Node findW(Edge e, int offset) {
		Node w = null;
		int eLength = e.length();
		if (offset < eLength) {
			// Insert a new Node w into e:
			w = new Node(-1, alphabet.length);
			Edge.insertNode(w, e, offset, string, alphabet);
		} else if (offset == eLength) {
			// Node w already exists:
			w = e.childNode;
		} else {
			System.out.println("I am still not sure if this can happen!!!!!!!");
		}
		return w;
	}

	private void updateLinkVectors(int i, Node v, Node w) {
		// Update link vector for v:
		if (v != null) {
			v.L[alphabet.getIndex(string[i])] = w;  // 1
		}

//		// Set all entries in link vector L for w to null:
//		for (int k=0; k < w.L.length; k++) {
//			w.L[k] = null;
//		}
	}

	/**
	 * Return string[i...j]
	 * @param string: a char[] representing the entire string.
	 * @param i: the start index of the substring.
	 * @param j: the end index of the substring.
	 * @return: a char[] containing string[i...j]
	 */
	public char[] getEdgeLabel(char[] string, int i, int j) {
		int l = j - i + 1;
		char[] substring = new char[l];
		for (int k = 0; k < l; k++) {
			substring[k] = string[i+k];
		}
		return substring;
	}

	/**
	 * Search for pattern in the SuffixTree in O(m+k) time, where k is the
	 * number of matches in string.
	 * @param pattern: the string to search for.
	 * @return: an array containing the start indices of each match; or, null
	 * 	if there are no matches.
	 */
	public ArrayList<Integer> search(char[] pattern, Alphabet alphabet) {
		ArrayList<Integer> matches = new ArrayList<Integer>();
		int n = pattern.length;
		int m = string.length;
		if (n > m) {
			return matches;
		}

		Node curNode = root;
		Edge curEdge = null;
		int j = 0;
		for (int i = 0; i < n; i++) {
//			System.out.println("i=" + i);
			char c = pattern[i];

			if (curEdge == null) {
				// Move on to the next edge:
				int ci = alphabet.getIndex(c);
				curEdge = curNode.childEdges[ci];
				if (curEdge == null) {
					return matches;  // no edge beginning with character c from curNode
				}
				j = 0;
			}
//			System.out.println(curEdge);
			if (c != string[curEdge.start + j]) {
				// No match:
				return matches;
			}
			j += 1;
			if (j >= curEdge.length()) {
				// Finished with edge:
				curNode = curEdge.childNode;
				curEdge = null;
//				System.out.println(curNode);
			}
		}

		// A path matching pattern has been followed, move to next node:
		if (curEdge != null) {
			curNode = curEdge.childNode;
		}
		if (curNode.index == -1) {
			// all terminal nodes in subtree are matches:
			matches.addAll(getAllLeaves(curNode));
		} else {
			// Only a single match:
			matches.add(curNode.index);
		}

		return matches;
	}

	// TODO: Is this method linear?
	/**
	 * Return the indices of all leaf nodes in subtree beginning with ancestor.
	 * @param ancestor
	 * @return
	 */
	private ArrayList<Integer> getAllLeaves(Node ancestor) {
		// FIXME: Something is not working here!
		ArrayList<Integer> leaves = new ArrayList<Integer>();
		if (ancestor.index != -1) {
			leaves.add(ancestor.index);
		} else {
			for (Edge childEdge: ancestor.childEdges) {
				if (childEdge != null) {
					leaves.addAll(getAllLeaves(childEdge.childNode));
				}
			}
		}
		return leaves;
	}

	public static void main(String[] args) {

		Alphabet alphabet;
		// Test 1:
		alphabet = new Alphabet(new char[] {'a', 'b', '$'});
		ArrayList<Integer> expectedValues = new ArrayList<Integer>();
		testOnString("babab$",
				new String[] {"$", "aa", "b$", "ba", "baba", "babab$"},
				new int[][] {{5}, {}, {4}, {0, 2}, {0}, {0}},
				alphabet, false);

		// Test 2
		testOnString("bababab$",
				new String[] {"aa", "b$", "ba", "babab", "ababab", "bababab$"},
				alphabet, false);

		// Test 3:
		alphabet = new Alphabet(new char[] {'a', 'c', 't', 'g', '$'});
		testOnString("tctgacta$",
				new String[] {"tga", "ct", "tctg", "tctgc"},
				alphabet, false);
		
		// Test4:
		alphabet = new Alphabet(new char[] {'m', 'i', 's', 'p', '$'});
		testOnString("mississippi$",
				new String[] {"mip", "miss", "iss", "siss", "pss", "missis"},
				alphabet, false);

		// Test 5:
		alphabet = new Alphabet(new char[] {'a', 'c', 't', 'g', '$'});
		System.out.println("Test 5:");
		testOnString("tttt$",
				new String[] {"tga", "$", "tttt", "ttt", "tt", "t"},
				alphabet, false);
		
		// Test 6a:
		alphabet = new Alphabet(new char[] {'a', 'c', 't', 'g', '$'});
		System.out.println("Test 6a:");
		testOnString("ctttt$",
				new String[] {"ttt", "tttt", "tt", "tcttt", "ct"},
				alphabet, false);

		// Test 6b:
		alphabet = new Alphabet(new char[] {'a', 'c', 't', 'g', '$'});
		System.out.println("Test 6b:");
		testOnString("ttcttt$",
				new String[] {"t", "ttt", "tttt", "tt"},
				alphabet, false);

		// Test 6:
		alphabet = new Alphabet(new char[] {'a', 'c', 't', 'g', '$'});
		testOnString("tttctttt$",
				new String[] {"tga", "ct", "tct", "tcttt", "$", "ttt", "tttt", "tt"},
				alphabet, false);

		// Test7a:
		alphabet = new Alphabet(new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', '$'});
		testOnString("acbaca$",
				new String[] {"$", "c", "a", "ac", "acc", "ab"},
				alphabet, false);
		
		// Test7:
		alphabet = new Alphabet(new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
				'w', 'x', 'y', 'z', '$'});
		testOnString("abbaccbdeachabwab$",
				new String[] {"$", "c", "a", "ac", "acc", "ab"},
				alphabet, false);
		
		// Full alphabet:
		alphabet = new Alphabet("abcdefghijklmnopqrstuvwxyz .$".toCharArray());

		// Test8:
		testOnString("i ate ten whole ghost peppers last friday.$",
				new String[] {"i", "ghost peppers", "ho"},
				alphabet, false);

		// Test 10:
		testOnString("tcctmttctattgcttatgttacctgacctmaaamgmatcgmtaat$",
				new String[] {"tagat", "mn", "tt", "maaam"}, alphabet, false);

		// Test 13:
//		alphabet = new Alphabet(new char[] {'m', 'n', 'o', 'b', '$'});
		testOnString("mnnmnmnn$", new String[] {"m", "mn"}, alphabet, false);

		// Test9:
		testOnString("abbcbcaabcdlmnonponmnbcdabaabcdtqrstmnop$",
				new String[] {"abbc", "bcb", "cdlmnonp", "lmnonp", "mn"},
				alphabet, false);

		// Test9b:
		testOnString("mnonpmnmnop$",
				new String[] {"mn"},
				alphabet, false);

		// Test9c:
		Alphabet shortAlphabet = new Alphabet("mnob$".toCharArray());
		testOnString("mnonmnbmno$",
				new String[] {"mn"},
				shortAlphabet, false);

		// Test 11:
		testOnString("catmatbathatthatwasmattandnotdanbutheythatsthat$",
				new String[] {"cat", "mat", "matt", "that"}, alphabet, false);
		
		// Test 11b:
		testOnString("thatwasahatthatcouldthatbathatwasthatcatthathat$",
				new String[] {"cat", "that"}, alphabet, false);
		
		shortAlphabet = new Alphabet("thay$".toCharArray());
		testOnString("thatthy$",
				new String[] {"that"}, shortAlphabet, false);

		// Test 12:
		testOnString("wthathcthmthdthth$", new String[] {"w", "t", "th", "thth", "mth"},
				alphabet, false);

		// Test 13: 
		testOnString("duck duck goose is a popular nursery rhyme. i love duck duck goose$",
				new String[] {"duck", "goose", "duck duck goose", " ", "i"},
				alphabet, false);

		// Test 14:
		testOnString("apple bannananutcoffe and other stuff will make a sentence that is very long. apple bannana orange apple sunday peanut coffee$",
				new String[] {"$", "apple", "coffe", "coffee", "bannana", "nut"},
				alphabet, false);

		// Test 15:
		shortAlphabet = new Alphabet("anx$".toCharArray());
		testOnString("nanxana$",
				new String[] {"$", "na"}, shortAlphabet, false);
		
	}

	public static void testOnString(String string, String[] patterns,
			int[][] expectedValues, Alphabet alphabet, boolean verbose) {
		// Build SuffixTree:
		SuffixTree ST = new SuffixTree(string.toCharArray(), alphabet, verbose);

		// Create reference index string:
		System.out.println(
				"Testing on '" + string + "'");
		String referenceIndex = "            ";
		for (int i = 0; i < string.length(); i++) {
			referenceIndex += i % 10;
		}
		System.out.println(referenceIndex);

		// Search for Patterns:
		ArrayList<Integer> searchResults;
		for (int i = 0; i < patterns.length; i++) {
			String pattern = patterns[i];
			searchResults = ST.search(pattern.toCharArray(),
					alphabet);
			searchResults.sort(null);
			System.out.println(String.format("pattern=%s: %s", pattern,
					searchResults.toString()));

			// Compare searchResults to expectedValues:
			if (expectedValues != null) {

				boolean success = true;
				if (searchResults.size() == expectedValues[i].length) {
					for (int j = 0; j < searchResults.size(); j++) {
						if (searchResults.get(j) != expectedValues[i][j]) {
							success = false;
						}
					}
				} else {
					success = false;
				}

				// Display results:
				if (success) {
					System.out.println("\tSuccess!");
				} else {
					System.out.println("\tFailure!");
				}
			}
		}

		System.out.println("-------------");
	}

	public static void testOnString(String string, String[] patterns,
			Alphabet alphabet, boolean verbose) {
		testOnString(string, patterns, null, alphabet, verbose);
	}
}
