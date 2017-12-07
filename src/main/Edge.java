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
		
		// Set w's pathDepth
		w.setPathDepth();

		return new Edge[] {inEdge, outEdge};
	}
}
