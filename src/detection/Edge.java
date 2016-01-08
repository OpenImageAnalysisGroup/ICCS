package detection;

import org.Vector2i;

public class Edge {
	ColorSegment from;
	ColorSegment to;
	Vector2i a;
	Vector2i b;
	double length;
	
	public Edge(Vector2i a, Vector2i b, double length, ColorSegment from, ColorSegment to) {
		super();
		this.a = a;
		this.b = b;
		this.length = length;
		this.from = from;
		this.to = to;
	}
}
