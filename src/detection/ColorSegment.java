package detection;

import java.util.ArrayList;
import java.util.LinkedList;

import org.Vector2i;

import de.ipk.ag_ba.image.operation.PositionAndColor;

public class ColorSegment {
	ArrayList<PositionAndColor> pixels;
	Vector2i center;
	LinkedList<Edge> edgeList;
	boolean circularityOk;
	int numberOfCorners;
	boolean isValid;
	
	public ColorSegment(ArrayList<PositionAndColor> pixels, Vector2i center) {
		super();
		this.pixels = pixels;
		this.center = center;
		this.edgeList = new LinkedList<Edge>();
		isValid = true;
	}
	
	public void setEdge(double bestDist, ColorSegment colorSegment) {
		this.edgeList.add(new Edge(this.center, colorSegment.center, bestDist, this, colorSegment));
	}
	
	public void setCircularity(double circularityfactor) {
		if (circularityfactor == 1.0)
			circularityOk = true;
		else
			circularityOk = false;
	}
	
	public void setCornerNum(int n) {
		numberOfCorners = n;
	}
	
	public void setValidityFalse() {
		isValid = false;
	}
	
}
