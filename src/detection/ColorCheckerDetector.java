package detection;

import iap.blocks.image_analysis_tools.methods.RegionLabeling;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.Vector2d;
import org.Vector2i;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import corner_detection.TestHarrisCornerDetector.HarrisCornerDetector;
import correction.ColorValues;
import de.ipk.ag_ba.image.operation.GrayscaleMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operations.complex_hull.Circle;
import de.ipk.ag_ba.image.operations.complex_hull.ConvexHullCalculator;
import de.ipk.ag_ba.image.operations.complex_hull.Polygon;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 */
public class ColorCheckerDetector {
	
	private final boolean debug;
	private final double scale;
	private final int rescale;
	private final boolean featureDebug = false;
	private final Image img_orig;
	private ColorChecker checker;
	
	public ColorCheckerDetector(Image inp, double scale, boolean debug) {
		this.img_orig = inp;
		this.scale = scale;
		this.debug = debug;
		rescale = (int) (1.0 / scale);
	}
	
	private void findChecker() throws Exception {
		Image img = img_orig.copy();
		img_orig.show("input image", debug);
		
		// prepare
		// color balance
		img = img.io().histogramEqualisation(true, 0.35).getImage();
		img.show("enhanced", debug);
		
		// convert to gray
		img = img.io().resize(scale).convertRGB2Grayscale(GrayscaleMode.LUMINOSITY, false).getImage();
		img.show("gray", debug);
		
		// detect edges
		CannyEdgeDetector ced = new CannyEdgeDetector();
		ced.setContrastNormalized(false);
		img = new Image(ced.process(img.getAsImagePlus())).io().getImage();
		img.show("edge", debug);
		
		// replace background
		img = img.copy().io().replaceColor(-1, ImageOperation.BACKGROUND_COLORint).getImage();
		img = img.io().canvas().drawRectangle(1, 1, img.getWidth() - 2, img.getHeight() - 2, ImageOperation.BACKGROUND_COLOR, 1).getImage();
		img.show("replace", debug);
		
		// enhence edges
		img = img.io().bm().opening(8).erode(8).getImage();
		img.show("enhanced edges", debug);
		
		// labeling
		RegionLabeling rl = new RegionLabeling(img, true, ImageOperation.BACKGROUND_COLORint, -1);
		rl.setNeigbourhoodFour();
		rl.detectClusters();
		Image labeled = rl.getClusterImage();
		labeled.show("labeled", debug);
		LinkedList<ArrayList<PositionAndColor>> cluster = rl.getRegionList();
		
		int numOfColors = 24;
		int lowThresh = (int) (0.00021 * (img_orig.getWidth() * img_orig.getHeight()));
		
		if (debug) {
			System.out.println("Cluster Sizes: " + "\n");
			int[] clusterSizes = rl.getClusterSize();
			Arrays.sort(clusterSizes);
			for (int c : clusterSizes)
				System.out.println(c);
			System.out.println("End Cluster Sizes: ---------------------------------------------------" + "\n");
		}
		
		// detect color rectangles
		// found!! (At least number of segments should be >= number of colors)
		if (cluster.size() >= numOfColors) {
			// identify clusters by greedy
			ArrayList<ArrayList<Vector2i>> trueClusters = identifyClustersByGreedy(cluster, numOfColors, lowThresh);
			// get best candidate using std of region sizes
			ArrayList<Vector2i> bestGroup = getBestUsingStd(trueClusters);
			// identifyClustersByOutlierTest(rl, numOfColors, 1);
			// get initial segmentList
			LinkedList<ColorSegment> segmentList = getSegmentsForColorChecker(rl, bestGroup, 10);
			// get list of best connection to create grid
			estimateInitialGrid(segmentList);
			// calculate some features: circularity factor, check Corners
			getCircularityFactor(segmentList, 10);
			getNumOfCorners(segmentList, 10, scale);
			
			// create checker object
			this.checker = new ColorChecker(segmentList);
			checker.filterByFeatures();
			checker.filterByEdges();
			checker.calcRect();
			checker.samplePositions();
			checker.sampleColors(img_orig.io().resize(scale).getImage(), 5);
			checker.getSampleOrder(false);
		} else {
			throw new Exception("To few color segments, no colorchecker detected!!!");
		}
	}
	
	public Image getDebugImage() {
		return checker.drawArtificialChecker(img_orig.copy(), scale, true);
	}
	
	public ColorValues[] getSamples() throws Exception {
		this.findChecker();
		return checker.getSampleList();
	}
	
	private LinkedList<ColorSegment> getSegmentsForColorChecker(RegionLabeling rl, ArrayList<Vector2i> bestGroup, int i) {
		LinkedList<ColorSegment> segments = new LinkedList<ColorSegment>();
		
		Vector2i[] centerPoints = rl.getClusterCenterPoints();
		
		for (Vector2i index : bestGroup) {
			// if (rl.getCluster(index.x).size() < i || rl.getCluster(index.x).size() > (i / 0.00021) * 0.25)
			// continue;
			if (rl.getCluster(index.x).size() < i)
				continue;
			ArrayList<PositionAndColor> segmentPixels = rl.getCluster(index.x);
			segments.add(new ColorSegment(segmentPixels, centerPoints[index.x]));
		}
		return segments;
	}
	
	private void getCircularityFactor(LinkedList<ColorSegment> segmentList, int lowThresh) {
		for (ColorSegment colorSeg : segmentList) {
			ArrayList<PositionAndColor> segment = colorSeg.pixels;
			int[] dim = findDimensions(segment);
			int[][] segmentArray = copyRegiontoArray(dim, segment);
			Image segmentImage = new Image(segmentArray);
			ConvexHullCalculator chc = new ConvexHullCalculator(segmentImage.io());
			
			chc.find(false, true, true, true, true, Color.BLUE.getRGB(), Color.RED.getRGB(), Color.RED.getRGB(), Color.RED.getRGB(), 1.0, 1.0)
					.getImage();
			
			Polygon poly = chc.getPolygon();
			double circularityfactor = 0;
			
			if (poly != null) {
				Circle circumcircle = poly.calculateminimalcircumcircle();
				double circlearea = circumcircle.area();
				double perimeter = 2.0 * 3.14159 * Math.sqrt(circlearea / 3.14159);
				double area = segment.size();
				circularityfactor = (4 * 3.14159 * area) / (perimeter * perimeter);
				
				if (featureDebug)
					System.out.println("Circularityfactor: " + circularityfactor);
				
				if (circularityfactor < 0.95 && circularityfactor > 0.50)
					circularityfactor = 1;
				else
					circularityfactor = 0;
			}
			colorSeg.setCircularity(circularityfactor);
		}
	}
	
	private void getNumOfCorners(LinkedList<ColorSegment> segmentList, int lowThresh, double scale) {
		double radius = 20d * scale;
		
		// for all segments
		for (ColorSegment seg : segmentList) {
			ArrayList<PositionAndColor> segment = seg.pixels;
			// LinkedList<PositionAndColor> lsegment = new LinkedList<PositionAndColor>(segment);
			int[] dim = findDimensions(segment);
			int[][] segmentArray = copyRegiontoArray(dim, segment);
			
			// CornerDetection cd = new CornerDetection(lsegment, dim);
			// cd.FindQuadrilateralCorners();
			// LinkedList<Point> corners = cd.corners;
			//
			// ImageCanvas markcor = new ImageCanvas(new Image(segmentArray));
			// for (Point p : corners)
			// markcor.drawCircle(p.x, p.y, 4, Color.CYAN.getRGB(), 0.0, 3);
			// markcor.getImage().show("new!!!!");
			
			Image imgBorder = new Image(segmentArray).io().addBorder((int) radius, 0, 0, ImageOperation.BACKGROUND_COLORint)
					.replaceColor(Color.BLACK.getRGB(), Color.WHITE.getRGB()).replaceColor(ImageOperation.BACKGROUND_COLORint, Color.BLACK.getRGB()).getImage()
					.show("border", false);
			HarrisCornerDetector hcd = new HarrisCornerDetector(imgBorder);
			hcd.setDebug(false);
			hcd.calcIntrestPoints(true, true, 1.0, 5.0);
			Vector2d[] results = hcd.getIntrestPoints();
			
			if (Math.abs(results.length - 4) < 1) {
				hcd.markImage(imgBorder).show("corners", featureDebug);
			}
			if (featureDebug)
				System.out.println(results.length);
			
			if (results != null)
				seg.setCornerNum(results.length);
		}
	}
	
	private void estimateInitialGrid(LinkedList<ColorSegment> segmentList) {
		int mainIdx = 0;
		for (ColorSegment segment : segmentList) {
			Vector2i centerPos = segment.center;
			ArrayList<Vector2i> templist = new ArrayList<Vector2i>();
			templist.add(centerPos);
			// search for 4 best connections
			HashSet<Vector2i> added = new HashSet<Vector2i>();
			added.add(centerPos);
			for (int i = 0; i < 4; i++) {
				double bestDist = Double.MAX_VALUE;
				int bestPosInList = -1;
				int tempIdx = 0;
				Vector2i bestPos = null;
				for (ColorSegment tocomp : segmentList) {
					Vector2i comparePos = tocomp.center;
					double dist = centerPos.distance(comparePos);
					if (!added.contains(comparePos) && dist < bestDist) {
						bestDist = dist;
						bestPosInList = tempIdx;
						bestPos = comparePos;
					}
					tempIdx++;
				}
				if (bestPosInList != -1) {
					added.add(bestPos);
					segmentList.get(mainIdx).setEdge(bestDist, segmentList.get(bestPosInList));
				}
			}
			mainIdx++;
		}
	}
	
	private ArrayList<Vector2i> getBestUsingStd(ArrayList<ArrayList<Vector2i>> trueClusters) {
		// calc std for all groups
		double stdBest = Double.MAX_VALUE;
		int posBest = -1;
		
		int idx = 0;
		for (ArrayList<Vector2i> group : trueClusters) {
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for (Vector2i vec : group) {
				ds.addValue(vec.y);
			}
			double std = ds.getStandardDeviation();
			if (std < stdBest) {
				stdBest = std;
				posBest = idx;
			}
			idx++;
		}
		return trueClusters.get(posBest);
	}
	
	/**
	 * Get best mappings by greedy like algorithm. Ignores clusters with size < ignoreSize.
	 * 
	 * @return List[List [vector [real position, cluster size], ...], ...]
	 **/
	private ArrayList<ArrayList<Vector2i>> identifyClustersByGreedy(LinkedList<ArrayList<PositionAndColor>> cluster, int numOfColors, int ignoreSize) {
		ArrayList<ArrayList<Vector2i>> bestMapping = new ArrayList<ArrayList<Vector2i>>();
		int activeClu = 0;
		for (ArrayList<PositionAndColor> c : cluster) {
			boolean[] added = new boolean[cluster.size() + 1];
			ArrayList<Vector2i> tempMapped = new ArrayList<Vector2i>();
			for (int idx = 0; idx < numOfColors; idx++) {
				int posBest = -1;
				int diffBest = Integer.MAX_VALUE;
				int sizeBest = 0;
				int position = 0;
				for (int innerIdx = 0; innerIdx < cluster.size(); innerIdx++) {
					if (innerIdx != activeClu && cluster.get(innerIdx).size() > ignoreSize)
						if (Math.abs(cluster.get(innerIdx).size() - c.size()) < diffBest && added[position] == false) {
							posBest = position;
							diffBest = Math.abs(cluster.get(innerIdx).size() - c.size());
							sizeBest = cluster.get(innerIdx).size();
						}
					position++;
				}
				if (posBest != -1) {
					tempMapped.add(new Vector2i(posBest, sizeBest));
					added[posBest] = true;
				}
			}
			bestMapping.add(tempMapped);
			activeClu++;
		}
		return bestMapping;
	}
	
	/**
	 * Find maximal dimensions of a region. TODO duplicate method from class border detection.
	 * 
	 * @param list
	 * @return int[] = {left, right, top, bottom}
	 */
	private static int[] findDimensions(ArrayList<PositionAndColor> list) {
		int[] dim = { Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE };
		if (list == null)
			return null;
		for (Iterator<PositionAndColor> i = list.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			if (temp.x < dim[0])
				dim[0] = temp.x;
			if (temp.x > dim[1])
				dim[1] = temp.x;
			if (temp.y < dim[2])
				dim[2] = temp.y;
			if (temp.y > dim[3])
				dim[3] = temp.y;
		}
		return dim;
	}
	
	/**
	 * Extract region into 2d array. TODO duplicate method from class border detection.
	 * 
	 * @param dim
	 * @param region
	 * @return
	 */
	private static int[][] copyRegiontoArray(int[] dim, ArrayList<PositionAndColor> region) {
		int[][] res = new int[(dim[1] - dim[0]) + 1][(dim[3] - dim[2]) + 1];
		ImageOperation.fillArray(res, ImageOperation.BACKGROUND_COLORint);
		for (Iterator<PositionAndColor> i = region.iterator(); i.hasNext();) {
			PositionAndColor temp = i.next();
			res[temp.x - dim[0]][temp.y - dim[2]] = temp.intensityInt;
		}
		return res;
	}
}
