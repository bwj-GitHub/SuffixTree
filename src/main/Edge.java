package main;

public class Edge {
	public int start;
	public int end;
	public Node parentNode;
	public Node childNode;

	public Edge(int start, int end) {
		this.start = start;
		this.end = end;
		this.parentNode = null;
		this.childNode = null;
	}

	public int length() {
		return end - start + 1;
	}
	
	public String toString() {
		return String.format("%s -- (%d, %d) -- %s", parentNode, start, end, childNode);
	}

	public char[] getEdgeLabel(char[] string) {
		char[] edgeLabel = new char[length()];
		for (int i = 0; i < length(); i++) {
			edgeLabel[i] = string[start + i];
		}
		return edgeLabel;
	}
	
	public void setParentNode(Node parentNode, int edgeIndex) {
		this.parentNode = parentNode;
		parentNode.childEdges[edgeIndex] = this;
	}

	public void setChildNode(Node childNode) {
		this.childNode = childNode;
		childNode.parentEdge = this;
	}

	public static void connectNodes(Node parent, Node child, Edge e,
			char[] string, Alphabet alphabet) {
		int ci = alphabet.getIndex(string[e.start]);
		e.setParentNode(parent, ci);
		e.setChildNode(child);
	}

	public static Edge[] insertNode(Node w, Edge e, int offset,
			char[] string, Alphabet alphabet) {
		// i-o -> i-w-o
		Edge inEdge = e;
		Edge outEdge;  // new edge
		Node endNode = e.childNode;

		// Split Edge e:
		int inEnd, outStart, outEnd;
		inEnd = e.start + offset - 1;
		outStart = e.start + offset;
		outEnd = e.end;

		// Create new Edge:
		outEdge = new Edge(outStart, outEnd);

		// Modify parentNode's edge:
		inEdge.childNode = w;  // Edge e
		inEdge.end = inEnd;

		// Set w's edges:
//		System.out.println("outEdge=" + outEdge);
		int ci = alphabet.getIndex(string[outEdge.start]);
		inEdge.setChildNode(w);
		outEdge.setParentNode(w, ci);

		// Modify childNode's edge:
		outEdge.setChildNode(endNode);

		return new Edge[] {inEdge, outEdge};
	}
	
	/** 
	 * Insert a new node w and Edge (w,leaf) into Edge e.
	 * 
	 * Step 4 of Weiner's tree extension algorithm, when head(i) > 0.
	 * @param leaf
	 * @param e
	 * @param headI
	 * @return the node inserted into edge (w).
	 */
	public static Node insertLeafNode(Node leaf, Edge e, int i, int headI, int suffLength,
			char[] string, Alphabet alphabet, int eIts) {
		// FIXME: Check if node w already exists:
		Node w;
		int m = string.length - 1;
		// FIXME: if v3 exists, we might have already traveled some distance
		if (e.length() == headI - eIts) {
//			System.out.println("Node w already exists");
//			System.out.println(m - suffLength);
			// Node w already exists:
			w = e.childNode;
		} else {
			// FIXME: I really need node depth...
			if (headI - eIts > e.length()) {
				int eLength = e.length();
				eIts += eLength;  // count depth traveled thusfar
//				System.out.println("We need to go further!");
//				System.out.println(string[i + headI - eIts]);  // 
				// FIXME
				Node nextNode = e.childNode;
//				System.out.println("nextNode=" + nextNode);
				Edge nextE = nextNode.childEdges[alphabet.getIndex(string[i + eIts])];
//				System.out.println("new e=" + nextE);
				if (nextE == null) {
					// TODO: Sooo much code duplication
					// Just create a new edge and connect it
//					System.out.println("We should be doing something...");
					w = nextNode;
				} else {
					return insertLeafNode(leaf, nextE, i, headI, suffLength, string,
							alphabet, eIts);
				}
			}
			// Create node w:
//			System.out.println("creating node w");
			w = new Node(-1, leaf.childEdges.length);
			Edge[] newEdges = Edge.insertNode(w, e, headI - eIts, string, alphabet);
		}

		Edge edgeWI = new Edge(m - suffLength + headI + 1, m);
		Edge.connectNodes(w, leaf, edgeWI, string, alphabet);
		return w;
	}

	public static Node insertLeafNode(Node leaf, Node source, int i, int headI,
			char[] string, Alphabet alphabet) {
		// Inserts leaf node as edge from Node w, headI from source;
		//  w might already exist:
		// Find or create Node w:
		Node w;
		int ci = alphabet.getIndex(string[i]);
		int suffLength = string.length - i;
		if (headI == 0) {
			// w is source:
			w = source;
			Edge edgeWI = new Edge(i, string.length - 1);
			Edge.connectNodes(w, leaf, edgeWI, string, alphabet);
			return w;
		} else if (source.childEdges[ci].length() < headI) {
			// insert w into edge:
			// TODO: What he said
			Edge e = source.childEdges[ci];
			return insertLeafNode(leaf, e, i, headI, suffLength, string, alphabet, 0);
			
		} else {
			// w is the node at end of childEdges[ci]
			// TODO: Would this ever happen?
			System.out.println("IT HAPPENED!");
			return null;
		}
	}

	public static void main(String[] args) {
		char[] string = new char[] {'a', 'b', 'a', 'b', 'c', '$'};
		Alphabet alphabet = new Alphabet(new char[] {'a', 'b', 'c', '$'});
		// Test insertNode and insertLeafNode:
		// Test 1: R-(3,5)-3 ->  R-(3,3)-w-(4,5)-3, w-(2,5)-leaf1
		Node R = new Node(-1, alphabet.length);
		Edge e = new Edge(3, 5);
		Node B = new Node(3, 3);
		Edge.connectNodes(R, B, e, string, alphabet);
		Node leaf = new Node(1, alphabet.length);
		Node w = Edge.insertLeafNode(leaf, e, 1, 1, 5, string, alphabet, 0);
		System.out.println(w.parentEdge);
		System.out.println(w.parentEdge.getEdgeLabel(string));  // b
		System.out.println(w.childEdges[2]);
		System.out.println(w.childEdges[2].getEdgeLabel(string));  // c$
		System.out.println(w.childEdges[0]);
		System.out.println(w.childEdges[0].getEdgeLabel(string));  // abc$
		System.out.println("------");

		// Test 2: R-(2,5)-2 - > R-(2,3)-w-(4,5)-2, w-(2,5)-leaf0
		Edge e2 = new Edge(2, 5);
		Node C = new Node(2, alphabet.length);
		Edge.connectNodes(R, C, e2, string, alphabet);
		Node leaf0 = new Node(0, alphabet.length);
		Node w2 = Edge.insertLeafNode(leaf0, e2, 2, 2, 6, string, alphabet, 0);
		System.out.println(w2.parentEdge);
		System.out.println(w2.parentEdge.getEdgeLabel(string));  // ab
		System.out.println(w2.childEdges[2]);
		System.out.println(w2.childEdges[2].getEdgeLabel(string));  // c$
		System.out.println(w2.childEdges[0]);
		System.out.println(w2.childEdges[0].getEdgeLabel(string));  // abc$
		System.out.println("------");

		// Test 3: R -> R-(5,5)-5:
		R = new Node(-1, alphabet.length);
		Node leaf5 = new Node(5, alphabet.length);
		Node w3 = Edge.insertLeafNode(leaf5, R, 5, 0, string, alphabet);
		System.out.println(leaf5.parentEdge);
		System.out.println(leaf5.parentEdge.getEdgeLabel(string));  // $
		System.out.println("------");

		// Test 4: R -> R-(4,5)-4:
		R = new Node(-1, alphabet.length);
		Node leaf4 = new Node(4, alphabet.length);
		Node w4 = Edge.insertLeafNode(leaf4, R, 4, 0, string, alphabet);
		System.out.println(leaf4.parentEdge);
		System.out.println(leaf4.parentEdge.getEdgeLabel(string));  // c$
		System.out.println("\n------\n");

		// Test 5:
		alphabet = new Alphabet(new char[] {'a', 'b', '$'});
		string = new char[] {'b', 'a', 'b', 'a', 'b', '$'};
		//                    0    1    2    3    4    5
		R = new Node(-1, alphabet.length);
		Node l5 = new Node(5, alphabet.length);
		Edge er5 = new Edge(5, 5);
		Edge.connectNodes(R, l5, er5, string, alphabet);

		Node l4 = new Node(4, alphabet.length);
		Edge er4 = new Edge(4, 5);
		Edge.connectNodes(R, l4, er4, string, alphabet);

		Node l3 = new Node(3, alphabet.length);
		Edge er3 = new Edge(3, 5);
		Edge.connectNodes(R, l3, er3, string, alphabet);
		System.out.println(R);
		
		// Insert leaf2:
		Node l2 = new Node(2, alphabet.length);
		Node w5 = Edge.insertLeafNode(l2, R.childEdges[1], 2, 1, 4, string, alphabet, 0);
		System.out.println(w5.parentEdge);
		System.out.println(w5.parentEdge.getEdgeLabel(string));  // b
		System.out.println(w5.childEdges[2]);
		System.out.println(w5.childEdges[2].getEdgeLabel(string));  // $
		System.out.println(w5.childEdges[0]);
		System.out.println(w5.childEdges[0].getEdgeLabel(string));  // ab$
		System.out.println("------");
		
	}
}
