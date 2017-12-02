package main;

public class Node {
	public int index;  // index into string that suffix begins at, -1 for non-leaf nodes
	public Edge parentEdge;
	public Edge[] childEdges;
	
	private int pathDepth;
	private int nodeDepth;

	// For construction:
	public int[] I;  // Indicator vector
	public Node[] L;  // Link vector
	
	public Node(int index, int vocabSize, int nodeDepth, int pathDepth) {
		this.index = index;
		this.childEdges = new Edge[vocabSize];
		this.nodeDepth = nodeDepth;
		this.pathDepth = pathDepth;

		I = new int[vocabSize];  // includes termination character
		L = new Node[vocabSize];
	}

	public Node(int index, int vocabSize) {
		this(index, vocabSize, -1, -1);
	}
	
	public int getNodeDepth() {
		return this.nodeDepth;
	}
	
	public int getPathDepth() {
		return this.pathDepth;
	}

	public boolean isRoot() {
		return parentEdge == null;
	}
	
	public String toString() {
		String s = "{" + index + ": ( ";
		for (Edge childEdge: childEdges) {
			if (childEdge != null) {
				s += childEdge.childNode.toString() + ", ";
			}
		}

		s += " ), I=";
		for (int i: I) {
			s += i + ",";
		}
		s += "}";
		return s;
	}
	
	public String simpleString(Alphabet alphabet, String prepend) {
		// form string for indicator vector:
		String iv = "I=";
		for (int indicator: I) {
			iv += indicator + ",";
		}
		
		String s = String.format("%s%d (%s) {\n", prepend, index, iv);
		for (int i = 0; i < childEdges.length; i++) {
			Edge e = childEdges[i];
			if (e != null) {
				s += String.format("%s\t%c %d %d\n", prepend, alphabet.chars[i], e.start, e.end);
				s += e.childNode.simpleString(alphabet, prepend + "\t");
			} else {
				s += String.format("%s\t%c\n", prepend, alphabet.chars[i]);
			}
		}
		s += prepend + "}\n";
		return s;
	}

	/**
	 * Set this.nodeDepth based on its parent's nodeDepth.
	 */
	public void setNodeDepth() {
		int parentNodeDepth = this.parentEdge.parentNode.getNodeDepth();
		this.nodeDepth = parentNodeDepth + 1;
	}

	public void setNodeDepth(int nodeDepth) {
		this.nodeDepth = nodeDepth;
	}

	public void setPathDepth() {
		int parentPathDepth = this.parentEdge.parentNode.getPathDepth();
		int parentEdgeLength = this.parentEdge.length();
		this.pathDepth = parentPathDepth + parentEdgeLength;
	}

	public void setPathDepth(int pathDepth) {
		this.pathDepth = pathDepth;
	}
}
