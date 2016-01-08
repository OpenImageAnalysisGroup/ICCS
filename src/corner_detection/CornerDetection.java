package corner_detection;

import java.awt.Point;
import java.util.LinkedList;

import de.ipk.ag_ba.image.operation.PositionAndColor;

public class CornerDetection {
	static LinkedList<PositionAndColor> cloud;
	static double quadrilateralRelativeDistortionLimit;
	static LinkedList<Point> corners = new LinkedList<Point>();
	
	/**
	 * @param cloud
	 * @param dim
	 *           int[] = {left, right, top, bottom};
	 */
	public CornerDetection(LinkedList<PositionAndColor> cloud, int[] dim) {
		this.cloud = translate(cloud, dim);
		quadrilateralRelativeDistortionLimit = 0.1; // Math.Max( 0.0f, Math.Min( 0.25f, value ) );
	}
	
	private LinkedList<PositionAndColor> translate(LinkedList<PositionAndColor> cloud2, int[] dim) {
		LinkedList<PositionAndColor> translated = new LinkedList<PositionAndColor>();
		for (PositionAndColor pos : cloud2) {
			translated.add(new PositionAndColor(pos.x - dim[0], pos.y - dim[2], pos.intensityInt));
		}
		return translated;
	}
	
	public static LinkedList<Point> FindQuadrilateralCorners() {
		// quadrilateral's corners
		// LinkedList<Point> corners = new LinkedList<Point>();
		
		// get bounding rectangle of the points list
		Point minXY = new Point(), maxXY = new Point();
		GetBoundingRectangle(cloud, minXY, maxXY);
		// get cloud's size
		Point cloudSize = new Point(maxXY.x - minXY.x, maxXY.y - minXY.y);
		// calculate center point
		Point center = new Point((int) (minXY.x + cloudSize.x * 0.5), (int) (minXY.y + cloudSize.y * 0.5));
		// acceptable deviation limit
		double distortionLimit = quadrilateralRelativeDistortionLimit * (cloudSize.x + cloudSize.y) / 2.0;
		
		// get the furthest point from (0,0)
		Point point1 = GetFurthestPoint(cloud, center);
		// get the furthest point from the first point
		Point point2 = GetFurthestPoint(cloud, point1);
		
		corners.add(point1);
		corners.add(point2);
		
		// get two furthest points from line
		Point point3 = new Point(), point4 = new Point();
		float distance3 = 0, distance4 = 0;
		
		float[] dists = GetFurthestPointsFromLine(cloud, point1, point2, point3, point4);
		distance3 = dists[0];
		distance4 = dists[1];
		
		// ideally points 1 and 2 form a diagonal of the
		// quadrilateral area, and points 3 and 4 form another diagonal
		
		// but if one of the points (3 or 4) is very close to the line
		// connecting points 1 and 2, then it is one the same line ...
		// which means corner was not found.
		// in this case we deal with a trapezoid or triangle, where
		// (1-2) line is one of it sides.
		
		// another interesting case is when both points (3) and (4) are
		// very close the (1-2) line. in this case we may have just a flat
		// quadrilateral.
		
		if (((distance3 >= distortionLimit) && (distance4 >= distortionLimit)) ||
				
				((distance3 < distortionLimit) && (distance3 != 0) &&
						(distance4 < distortionLimit) && (distance4 != 0)))
		{
			// don't add one of the corners, if the point is already in the corners list
			// (this may happen when both #3 and #4 points are very close to the line
			// connecting #1 and #2)
			if (!corners.contains(point3))
			{
				corners.add(point3);
			}
			if (!corners.contains(point4))
			{
				corners.add(point4);
			}
		}
		else
		{
			// it seems that we deal with kind of trapezoid,
			// where point 1 and 2 are on the same edge
			
			Point tempPoint = (distance3 > distance4) ? point3 : point4;
			
			// try to find 3rd point
			float[] dists1 = GetFurthestPointsFromLine(cloud, point1, tempPoint, point3, point4);
			distance3 = dists1[0];
			distance4 = dists1[1];
			
			boolean thirdPointIsFound = false;
			
			if ((distance3 >= distortionLimit) && (distance4 >= distortionLimit))
			{
				if (point4.distance(point2) > point3.distance(point2))
					point3 = point4;
				
				thirdPointIsFound = true;
			}
			else
			{
				float[] dists2 = GetFurthestPointsFromLine(cloud, point2, tempPoint, point3, point4);
				distance3 = dists2[0];
				distance4 = dists2[1];
				
				if ((distance3 >= distortionLimit) && (distance4 >= distortionLimit))
				{
					if (point4.distance(point1) > point3.distance(point1))
						point3 = point4;
					
					thirdPointIsFound = true;
				}
			}
			
			if (!thirdPointIsFound)
			{
				// failed to find 3rd edge point, which is away enough from the temp point.
				// this means that the clound looks more like triangle
				corners.add(tempPoint);
			}
			else
			{
				corners.add(point3);
				
				// try to find 4th point
				float tempDistance = 0;
				
				float[] dists3 = GetFurthestPointsFromLine(cloud, point1, point3, tempPoint, point4);
				tempDistance = dists3[0];
				distance4 = dists3[1];
				
				if ((distance4 >= distortionLimit) && (tempDistance >= distortionLimit))
				{
					if (tempPoint.distance(point2) > point4.distance(point2))
						point4 = tempPoint;
				}
				else
				{
					float[] dists4 = GetFurthestPointsFromLine(cloud, point2, point3, tempPoint, point4);
					tempDistance = dists4[0];
					distance4 = dists4[1];
					
					if ((tempPoint.distance(point1) > point4.distance(point1)) &&
							(tempPoint != point2) && (tempPoint != point3))
					{
						point4 = tempPoint;
					}
				}
				
				if ((point4 != point1) && (point4 != point2) && (point4 != point3))
					corners.add(point4);
			}
		}
		
		// put the point with lowest X as the first
		for (int i = 1, n = corners.size(); i < n; i++)
		{
			if ((corners.get(i).x < corners.getFirst().x) ||
					((corners.get(i).x == corners.getFirst().x) && (corners.get(i).y < corners.getFirst().y)))
			{
				Point temp = corners.get(i);
				corners.set(i, corners.getFirst());
				corners.set(0, temp);
			}
		}
		
		// sort other points in counter clockwise order
		float k1 = (corners.get(1).x != corners.getFirst().x) ?
				((float) (corners.get(1).y - corners.get(0).y) / (corners.get(1).x - corners.get(0).x)) :
				((corners.get(1).y > corners.get(0).y) ? Float.MAX_VALUE : Float.MIN_VALUE);
		
		float k2 = (corners.get(2).x != corners.get(0).x) ?
				((float) (corners.get(2).y - corners.get(0).y) / (corners.get(2).x - corners.get(0).x)) :
				((corners.get(2).y > corners.get(0).y) ? Float.MAX_VALUE : Float.MIN_VALUE);
		
		if (k2 < k1)
		{
			Point temp = corners.get(1);
			corners.set(1, corners.get(2));
			corners.set(2, temp);
			
			float tk = k1;
			k1 = k2;
			k2 = tk;
		}
		
		if (corners.size() == 4)
		{
			float k3 = (corners.get(3).x != corners.get(0).x) ?
					((float) (corners.get(3).y - corners.get(0).y) / (corners.get(3).x - corners.get(0).x)) :
					((corners.get(3).y > corners.get(0).y) ? Float.MAX_VALUE : Float.MIN_VALUE);
			
			if (k3 < k1)
			{
				Point temp = corners.get(1);
				corners.set(1, corners.get(3));
				corners.set(3, temp);
				
				float tk = k1;
				k1 = k3;
				k3 = tk;
			}
			if (k3 < k2)
			{
				Point temp = corners.get(2);
				corners.set(2, corners.get(3));
				corners.set(3, temp);
				
				float tk = k2;
				k2 = k3;
				k3 = tk;
			}
		}
		
		return corners;
	}
	
	public static void GetBoundingRectangle(LinkedList<PositionAndColor> cloud, Point minXY, Point maxXY)
	{
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		
		for (PositionAndColor pt : cloud) {
			int x = pt.x;
			int y = pt.y;
			
			// check X coordinate
			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			
			// check Y coordinate
			if (y < minY)
				minY = y;
			if (y > maxY)
				maxY = y;
		}
		
		if (minX > maxX) // if no point appeared to set either minX or maxX
			throw new IllegalArgumentException("List of points can not be empty.");
		
		minXY.setLocation(minX, minY);
		maxXY.setLocation(maxX, maxY);
	}
	
	public static Point GetFurthestPoint(LinkedList<PositionAndColor> cloud, Point referencePoint) {
		Point furthestPoint = referencePoint;
		float maxDistance = -1;
		
		int rx = referencePoint.x;
		int ry = referencePoint.y;
		
		for (PositionAndColor point : cloud) {
			int dx = rx - point.x;
			int dy = ry - point.y;
			// we are not calculating square root for finding "real" distance,
			// since it is really not important for finding furthest point
			float distance = dx * dx + dy * dy;
			
			if (distance > maxDistance)
			{
				maxDistance = distance;
				furthestPoint = new Point(point.x, point.y);
			}
		}
		
		return furthestPoint;
	}
	
	public static float[] GetFurthestPointsFromLine(LinkedList<PositionAndColor> cloud, Point linePoint1, Point linePoint2,
			Point furthestPoint1, Point furthestPoint2) {
		furthestPoint1 = linePoint1;
		float distance1 = 0;
		
		furthestPoint2 = linePoint2;
		float distance2 = 0;
		
		if (linePoint2.x != linePoint1.x) {
			// line's equation y(x) = k * x + b
			float k = (float) (linePoint2.y - linePoint1.y) / (linePoint2.x - linePoint1.x);
			float b = linePoint1.y - k * linePoint1.x;
			
			float div = (float) Math.sqrt(k * k + 1);
			float distance = 0;
			
			for (PositionAndColor point : cloud) {
				distance = (k * point.x + b - point.y) / div;
				
				if (distance > distance1)
				{
					distance1 = distance;
					furthestPoint1 = new Point(point.x, point.y);
				}
				if (distance < distance2)
				{
					distance2 = distance;
					furthestPoint2 = new Point(point.x, point.y);
				}
			}
		}
		else
		{
			int lineX = linePoint1.x;
			float distance = 0;
			
			for (PositionAndColor point : cloud) {
				distance = lineX - point.x;
				
				if (distance > distance1)
				{
					distance1 = distance;
					furthestPoint1 = new Point(point.x, point.y);
				}
				if (distance < distance2)
				{
					distance2 = distance;
					furthestPoint2 = new Point(point.x, point.y);
				}
			}
		}
		
		distance2 = -distance2;
		return new float[] { distance1, distance2 };
	}
}
