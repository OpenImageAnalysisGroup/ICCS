package detection;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import org.Vector2d;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import correction.ColorICCS;
import correction.ColorSpaces;
import correction.ColorValues;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operation.Lab;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.complex_hull.Point;
import de.ipk.ag_ba.image.operations.complex_hull.Polygon;
import de.ipk.ag_ba.image.operations.complex_hull.RotatingCalipers;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.plugins.outlier.Outliers;

public class ColorChecker {
	LinkedList<ColorSegment> segments;
	java.awt.geom.Point2D.Double[] rawRectPoints;
	private final Vector2d center = new Vector2d();
	private ColorValues[] sampleList = new ColorValues[24];
	boolean error = false;
	
	public ColorChecker() {
		segments = new LinkedList<ColorSegment>();
	}
	
	public ColorChecker(LinkedList<ColorSegment> segmentList) {
		this.segments = segmentList;
	}
	
	public void filterByEdges() {
		if (segments.size() == 0) {
			System.out.println("SegmentList empty!!");
			return;
		}
		
		Outliers out = new Outliers();
		Vector2d thresholds = out.getThresholds(getAllEdgeLengthsSorted(), 0.05);
		deleteSegments(thresholds.x, thresholds.y);
		double t = calcThresholdsNG(getAllEdgeLengthsSorted());
		deleteSegments(0.0, t * Math.sqrt(2.0) * 1.2);
	}
	
	private double calcThresholdsNG(ArrayList<Double> allEdgeLengthsSorted) {
		int sum = 0, n = 0;
		for (int i = 0; i < allEdgeLengthsSorted.size() * 0.25; i++) {
			sum += allEdgeLengthsSorted.get(i);
			n++;
		}
		return n != 0 ? sum / n : 0;
	}
	
	private void deleteSegments(double x, double y) {
		for (ColorSegment seg : new ArrayList<ColorSegment>(segments)) {
			if (seg.edgeList != null)
				for (Edge edge : new LinkedList<Edge>(seg.edgeList)) {
					if (edge.length <= x || edge.length >= y || !edge.to.isValid)
						seg.edgeList.remove(edge);
				}
			if (seg.edgeList.isEmpty() || seg.edgeList.size() <= 1)
				seg.setValidityFalse();
		}
		
		boolean found = true;
		int idle = 0;
		while (found) {
			if (segments.size() == 0)
				found = false;
			for (ColorSegment seg : segments) {
				for (Edge edge : new LinkedList<Edge>(seg.edgeList)) {
					boolean from = edge.from.isValid;
					boolean to = edge.to.isValid;
					if (!from || !to)
						seg.edgeList.remove(edge);
				}
				if (((seg.edgeList.isEmpty() || seg.edgeList.size() <= 1) && segments.size() > 0) && idle < 100) {
					if (seg.isValid) {
						seg.setValidityFalse();
					} else
						idle++;
				} else
					found = false;
			}
		}
	}
	
	private ArrayList<Double> getAllEdgeLengthsSorted() {
		ArrayList<Double> lengths = new ArrayList<Double>();
		for (ColorSegment segment : segments) {
			if (segment.edgeList != null)
				for (Edge edge : segment.edgeList) {
					if (edge.length > 0.0)
						lengths.add(edge.length);
				}
		}
		Collections.sort(lengths);
		for (Double s : lengths)
			System.out.println(s.toString());
		return lengths;
	}
	
	public Image drawArtificialChecker(Image inp, double scale, boolean debug) {
		if (scale != 1.0)
			inp = inp.copy().io().resize(scale).getImage();
		
		int width = inp.getWidth();
		int height = inp.getHeight();
		
		int[] colorArray = inp.getAs1A();
		// draw center
		for (ColorSegment segment : segments) {
			if (segment.isValid) {
				for (PositionAndColor pix : segment.pixels) {
					colorArray[pix.x + width * pix.y] = Color.RED.getRGB();
				}
			} else
				if (error) {
					for (PositionAndColor pix : segment.pixels) {
						colorArray[pix.x + width * pix.y] = Color.GRAY.getRGB();
					}
				}
		}
		Image img = new Image(width, height, colorArray);
		
		ImageCanvas ic = new ImageCanvas(img);
		
		for (ColorSegment segment : segments) {
			if (segment.isValid) {
				if (debug)
					for (Edge edge : segment.edgeList) {
						if (edge.to.isValid) {
							ic.drawLine(edge.a, edge.b, Color.GREEN.getRGB(), 0.0, 1);
						}
					}
				if (segment.numberOfCorners == 4) {
					ic.drawCircle(segment.center.x, segment.center.y, 5, Color.GREEN.getRGB(), 0.0, 2);
				}
				if (segment.circularityOk) {
					ic.drawCircle(segment.center.x, segment.center.y, 7, Color.BLUE.getRGB(), 0.0, 2);
				}
			}
		}
		
		int iii = 0;
		for (ColorValues seg : sampleList) {
			if (seg.getAvgColor(ColorSpaces.RGB) == null)
				continue;
			Point p = seg.center;
			DecimalFormat f = new DecimalFormat("#0");
			if (p != null) {
				ic.drawRectangle((int) (p.x) - 2, (int) (p.y) - 2, 14, 14, Color.BLACK, 3);
				ic.drawRectanglePoints((int) (p.x), (int) (p.y), 10, 10, new Color((int) seg.getAvgColor(ColorSpaces.RGB).getA(), (int) seg.getAvgColor(ColorSpaces.RGB).getB(),
						(int) seg.getAvgColor(ColorSpaces.RGB).getC()), 3);
				ic.text((int) (p.x) - 40, (int) (p.y) - 40,
						"No: " + iii++ + "\n" + f.format(seg.getAvgColor(ColorSpaces.Lab).getA()) + "\n" + f.format(seg.getAvgColor(ColorSpaces.Lab).getB())
								+ "\n" + f.format(seg.getAvgColor(ColorSpaces.Lab).getC()), Color.YELLOW);
				System.out.println(iii + " Lab: " + seg.getAvgColor(ColorSpaces.Lab).getA() + " | " + seg.getAvgColor(ColorSpaces.Lab).getB() + " | "
						+ seg.getAvgColor(ColorSpaces.Lab).getC());
			}
		}
		
		if (rawRectPoints != null)
			for (int i = 0; i < rawRectPoints.length; i++)
				ic.drawLine((int) rawRectPoints[i].x, (int) rawRectPoints[i].y, (int) rawRectPoints[(i + 1) % rawRectPoints.length].x, (int) rawRectPoints[(i + 1)
						% rawRectPoints.length].y,
						Color.PINK.getRGB(), 0.0, 2);
		
		return ic.getImage();
	}
	
	public void filterByFeatures() {
		for (ColorSegment seg : new LinkedList<ColorSegment>(segments)) {
			if (seg.circularityOk == false) // || seg.numberOfCorners != 4
				seg.setValidityFalse();
		}
	}
	
	public void calcRect() throws Exception {
		if (segments.size() < 4)
			return;
		
		int validSegments = 0;
		for (ColorSegment seg : segments)
			if (seg.isValid)
				validSegments++;
		
		if (validSegments < 10) {
			System.out.println("To few segments!! Abort.");
			error = true;
			return;
		}
		
		Point[] points = new Point[validSegments];
		int idx = 0;
		for (ColorSegment seg : segments) {
			if (seg.isValid) {
				points[idx] = new Point(seg.center.x, seg.center.y);
				idx++;
			}
		}
		Polygon poly = new Polygon(points);
		java.awt.geom.Point2D.Double[] mr = null;
		mr = RotatingCalipers.getMinimumBoundingRectangle(poly.getPoints());
		this.rawRectPoints = mr;
		calcCenter();
	}
	
	public void samplePositions() {
		if (rawRectPoints == null || rawRectPoints.length == 0)
			return;
		
		double length_ab = rawRectPoints[0].distance(rawRectPoints[1]);
		double length_bc = rawRectPoints[1].distance(rawRectPoints[2]);
		double length_cd = rawRectPoints[2].distance(rawRectPoints[3]);
		double length_da = rawRectPoints[3].distance(rawRectPoints[0]);
		
		// ab & cd main axes
		if (length_ab > length_bc && length_ab > length_da && length_cd > length_bc && length_cd > length_da) {
			Vector2D vecA = new Vector2D(rawRectPoints[1].x - rawRectPoints[0].x, rawRectPoints[1].y - rawRectPoints[0].y);
			Vector2D vecB = new Vector2D(rawRectPoints[3].x - rawRectPoints[0].x, rawRectPoints[3].y - rawRectPoints[0].y);
			int n = 0;
			for (int b = 0; b < 4; b++) {
				for (int a = 0; a < 6; a++) {
					Vector2D seg = vecA.scalarMultiply(a * 1.0 / 5).add(vecB.scalarMultiply(b * 1.0 / 3));
					sampleList[n] = new ColorValues(new Point(seg.getX() + rawRectPoints[0].x, seg.getY() + rawRectPoints[0].y));
					n++;
				}
			}
		}
		// bc & da main axes
		if (length_bc > length_ab && length_bc > length_cd && length_da > length_ab && length_da > length_cd) {
			Vector2D vecB = new Vector2D(rawRectPoints[1].x - rawRectPoints[0].x, rawRectPoints[1].y - rawRectPoints[0].y);
			Vector2D vecA = new Vector2D(rawRectPoints[3].x - rawRectPoints[0].x, rawRectPoints[3].y - rawRectPoints[0].y);
			int n = 0;
			for (int b = 0; b < 4; b++) {
				for (int a = 0; a < 6; a++) {
					Vector2D seg = vecA.scalarMultiply(a * 1.0 / 5).add(vecB.scalarMultiply(b * 1.0 / 3));
					sampleList[n] = new ColorValues(new Point(seg.getX() + rawRectPoints[0].x, seg.getY() + rawRectPoints[0].y));
					n++;
				}
			}
		}
	}
	
	public void sampleColors(Image orig, int r) {
		int w = orig.getWidth();
		
		int[] red_img = orig.io().channels().getR().getAs1D();
		int[] gre_img = orig.io().channels().getG().getAs1D();
		int[] blu_img = orig.io().channels().getB().getAs1D();
		
		int[] hue_img = orig.io().channels().getH().getAs1D();
		int[] sat_img = orig.io().channels().getS().getAs1D();
		int[] val_img = orig.io().channels().getV().getAs1D();
		
		for (ColorValues s : sampleList) {
			int x = (int) s.center.x;
			int y = (int) s.center.y;
			
			double red_sample = sampleColor(x, y, r, red_img, w);
			double gre_sample = sampleColor(x, y, r, gre_img, w);
			double blu_sample = sampleColor(x, y, r, blu_img, w);
			
			s.setAvgColor(ColorSpaces.RGB, new ColorICCS(red_sample, gre_sample, blu_sample));
			
			double hue_sample = sampleColor(x, y, r, hue_img, w);
			double sat_sample = sampleColor(x, y, r, sat_img, w);
			double val_sample = sampleColor(x, y, r, val_img, w);
			
			s.setAvgColor(ColorSpaces.HSV, new ColorICCS(hue_sample, sat_sample, val_sample));
			
			double lum_sample = sampleColorLabL(x, y, r, orig.getAs1A(), w);
			double a_sample = sampleColorLabA(x, y, r, orig.getAs1A(), w);
			double b_sample = sampleColorLabB(x, y, r, orig.getAs1A(), w);
			
			s.setAvgColor(ColorSpaces.Lab, new ColorICCS(lum_sample, a_sample, b_sample));
		}
	}
	
	/**
	 * Samples Average color values in a circle defined by {x,y,r}.
	 * @param x
	 * @param y
	 * @param r
	 * @param img
	 * @param w
	 * @return
	 */
	private double sampleColor(int x, int y, int r, int[] img, int w) {
		int sum = 0;
		int n = 0;
		for (int i = -r; i < r; i++) {
			for (int j = -r; j < r; j++) {
				if ((i * i + j * j) < (r * r)) {
					int c = img[(y + i) * w + (x + j)];
					sum += (c & 0xff0000) >> 16;
					n++;
				}
			}
		}
		return sum / (double) n;
	}
	
	private double sampleColorLabL(int x, int y, int r, int[] img, int w) {
		int sum = 0;
		int n = 0;
		for (int i = -r; i < r; i++) {
			for (int j = -r; j < r; j++) {
				if ((i * i + j * j) < (r * r)) {
					int c = img[(y + i) * w + (x + j)];
					Color_CIE_Lab lab = new Color_CIE_Lab(c);
					sum += lab.getL();
					n++;
				}
			}
		}
		return sum / (double) n;
	}
	
	private double sampleColorLabA(int x, int y, int r, int[] img, int w) {
		int sum = 0;
		int n = 0;
		for (int i = -r; i < r; i++) {
			for (int j = -r; j < r; j++) {
				if ((i * i + j * j) < (r * r)) {
					int c = img[(y + i) * w + (x + j)];
					Color_CIE_Lab lab = new Color_CIE_Lab(c);
					sum += lab.getA();
					n++;
				}
			}
		}
		return sum / (double) n;
	}
	
	private double sampleColorLabB(int x, int y, int r, int[] img, int w) {
		int sum = 0;
		int n = 0;
		for (int i = -r; i < r; i++) {
			for (int j = -r; j < r; j++) {
				if ((i * i + j * j) < (r * r)) {
					int c = img[(y + i) * w + (x + j)];
					Color_CIE_Lab lab = new Color_CIE_Lab(c);
					sum += lab.getB();
					n++;
				}
			}
		}
		return sum / (double) n;
	}
	
	private void calcCenter() {
		int xSum = 0;
		int ySum = 0;
		int n = 0;
		for (ColorSegment seg : segments) {
			if (seg.isValid) {
				xSum += seg.center.x;
				ySum += seg.center.y;
				n++;
			}
		}
		center.x = xSum / n;
		center.y = ySum / n;
	}
	
	/**
	 * Tries to identify the bottom line of color values (less saturated) and orders the samples beginning with the brown segment: 0 - brown, ..., 18 - white,
	 * ..., 23 - black. (based on HSV colors)
	 */
	public void getSampleOrder(boolean useSat) {
		// find bottom row black -> white, must be the first 6 or the last 6 entries
		boolean swap;
		
		if (useSat) {
			double firstAvgSat = 0;
			double lastAvgSat = 0;
			int n = 0;
			for (ColorValues s : sampleList) {
				if (n < 6)
					firstAvgSat += s.getAvgColor(ColorSpaces.HSV).getC();
				if (n > 17)
					lastAvgSat += s.getAvgColor(ColorSpaces.HSV).getC();
				n++;
			}
			
			firstAvgSat = firstAvgSat / 6.0;
			lastAvgSat = lastAvgSat / 6.0;
			
			swap = lastAvgSat > firstAvgSat;
			
		} else {
			double ratio1 = Math.abs(sampleList[23].getAvgColor(ColorSpaces.HSV).getC() - sampleList[18].getAvgColor(ColorSpaces.HSV).getC());
			double ratio2 = Math.abs(sampleList[5].getAvgColor(ColorSpaces.HSV).getC() - sampleList[0].getAvgColor(ColorSpaces.HSV).getC());
			
			swap = ratio2 > ratio1;
		}
		
		// if firstrow less saturated -> flip
		if (swap) {
			ColorValues[] tempList = new ColorValues[24];
			for (int i = 0; i < 24; i++)
				tempList[23 - i] = sampleList[i];
			sampleList = tempList;
		}
		
		// if #18 more dark than #23 -> flip rows
		if (sampleList[23].getAvgColor(ColorSpaces.HSV).getC() > sampleList[18].getAvgColor(ColorSpaces.HSV).getC()) {
			ColorValues[] tempList = new ColorValues[24];
			for (int i = 0; i < 24; i++) {
				tempList[i] = sampleList[(i / 6) * 6 + 5 - (i % 6)];
			}
			sampleList = tempList;
		}
	}
	
	public ColorValues[] getSampleList() {
		return sampleList;
	}

	public void addNamestoSampleList() {
		String names[] = new String[] {"Darkskin", "Lightskin", "Bluesky", "Foliage", "Blueflower", "Bluishgreen", "Orange", "Purplishblue", "Moderatered", "Purple", "Yellowgreen", "Orangeyellow", "Blue", "Green", "Red", "Yellow", "Magenta", "Cyan", "White", "Neutral8", "Neutral6.5", "Neutral5", "Neutral3.5", "Black"};
		for(int idx = 0; idx < sampleList.length; idx++) {
			sampleList[idx].name = names[idx];
		}
	}
}
