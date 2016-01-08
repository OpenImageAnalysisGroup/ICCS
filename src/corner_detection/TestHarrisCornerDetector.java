package corner_detection;

import iap.blocks.image_analysis_tools.imageJ.externalPlugins.CannyEdgeDetector;
import iap.blocks.image_analysis_tools.imageJ.externalPlugins.MaximumFinder;
import ij.measure.ResultsTable;

import java.awt.Color;

import org.Vector2d;

import Jama.Matrix;
import de.ipk.ag_ba.image.operation.GrayscaleMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Test function for the Harris Corner Detector (aka Plessey algorithm). The operator is useful to detect interest points in an image. TODO Uses the non-maximum
 * suppression algorithm to remove the number of interest points.
 * Input: BW image (B - background, w - foreground)
 * TODO test blur option, set default to 0.0!!!!
 * 
 * @author pape
 */
public class TestHarrisCornerDetector {
	
	// @Test
	// public void testHarris() throws Exception {
	// // Image img = new Image(GravistoService.getIOurl(TestDistanceMap.class, "barley_top.png", null));
	// Image img = HelperMethods.readImage("/Desktop/test_images/harris_test.png");
	//
	// HarrisCornerDetector hcd = new HarrisCornerDetector(img);
	// hcd.setDebug(true);
	// hcd.calcIntrestPoints(true, false, 1.0, 3.0);
	// Vector2d[] results = hcd.getIntrestPoints();
	// img = hcd.markImage(img);
	// System.out.println("number of intrest points: " + results.length);
	// img.show("res");
	//
	// Thread.sleep(500000);
	// }
	
	public static class HarrisCornerDetector {
		
		Image img;
		Vector2d[] results;
		boolean debug = false;
		double scale = 1.0;
		
		public HarrisCornerDetector(Image img) {
			this.img = img;
		}
		
		public Image markImage(Image img) {
			ImageCanvas ic2 = img.io().canvas();
			if (results != null)
				for (Vector2d v : results) {
					ic2.drawCircle((int) (v.getX() * (1.0 / scale)), (int) (v.getY() * (1.0 / scale)), 3, Color.RED.getRGB(), 0.0, 1);
				}
			return img = ic2.getImage();
		}
		
		public Vector2d[] getIntrestPoints() {
			return results;
		}
		
		public void setDebug(boolean b) {
			debug = b;
		}
		
		public void calcIntrestPoints(boolean gray, boolean useEdges, double scale, double blur) {
			this.scale = scale;
			// prepare
			if (gray) {
				img = img.io().convertRGB2Grayscale(GrayscaleMode.LUMINOSITY, false).getImage();
				img.show("gray", debug);
			}
			if (useEdges) {
				CannyEdgeDetector ced = new CannyEdgeDetector();
				img = new Image(ced.process(img.getAsImagePlus())).io().getImage();
				img.show("edge", debug);
			}
			
			// calculate derivatives
			int w = img.getWidth();
			int h = img.getHeight();
			int[][] img2d = img.getAs2A();
			
			int[][] imgfx;
			int[][] imgfy;
			int[][] imgfxy;
			
			boolean ng = false;
			
			if (!ng) {
				// masks for sobel edge detection
				int[][] sobelx = new int[][] { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
				int[][] sobely = new int[][] { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
				
				// old methods by convolution (sobel)
				imgfx = convolution3x3(img.copy().getAs2A(), sobelx);
				imgfy = convolution3x3(img.copy().getAs2A(), sobely);
				imgfxy = multiply(imgfx, imgfy);
			} else {
				// better approximation
				int[][] grayscale = convertToGrayscaleImage(img2d.clone());
				imgfx = centralDerivate_x(grayscale);
				imgfy = centralDerivate_y(grayscale);
				imgfxy = centralDerivate_xy(grayscale);
			}
			
			new Image(imgfx, imgfx, imgfx).show("fx", debug);
			new Image(imgfy, imgfy, imgfy).show("fy", debug);
			new Image(imgfxy, imgfxy, imgfxy).show("fxy", debug);
			
			int[][] intrestMap = calcIntrestMap(w, h, imgfx, imgfy, imgfxy);
			Image grayMap = new Image(intrestMap, intrestMap, intrestMap).show("intrest map", debug);
			
			Image smoothImage = grayMap.io().blurImageJ(blur).getImage();
			smoothImage.show("intrest map blur", debug);
			
			results = findMaxima(smoothImage);
		}
		
		private Vector2d[] findMaxima(Image img) {
			iap.blocks.image_analysis_tools.imageJ.externalPlugins.MaximumFinder mf = new MaximumFinder();
			mf.findMaxima(img.getAsImagePlus().getProcessor(), 1d, 1d, MaximumFinder.LIST, false, false);
			ResultsTable rt = mf.getRt();
			Vector2d[] results = null;
			
			if (rt != null && rt.getColumnAsDoubles(0) != null && rt.getColumnAsDoubles(1) != null) {
				double[] firstrow = rt.getColumnAsDoubles(0);
				double[] secondrow = rt.getColumnAsDoubles(1);
				
				results = new Vector2d[firstrow.length];
				
				for (int i = 0; i < firstrow.length; i++) {
					results[i] = new Vector2d(firstrow[i], secondrow[i]);
				}
			}
			return results;
		}
		
		private int[][] calcIntrestMap(int w, int h, int[][] imgfx, int[][] imgfy, int[][] imgfxy) {
			int fx = 0, fy = 0, fxy = 0, rank = 0;
			double k = 0.04, pointStrength = 0.0, det = 0.0, trace = 0, max = 0; // k = 0.04 for interest points, edges are negative
			int[][] intrestMap = new int[w][h];
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					// autocorrelation matrix
					fx = imgfx[x][y];
					fy = imgfy[x][y];
					if (fx != 0 || fy != 0) {
						fxy = imgfxy[x][y];
						double[][] A = { { fx * fx, fxy }, { fxy, fy * fy } };
						Matrix auto = new Matrix(A);
						rank = auto.rank();
						trace = auto.trace();
						det = auto.det();
						pointStrength = Math.abs(det - (k * trace * trace));
						// TODO threshold estimation for k, rank is ignored
						if (pointStrength > 1.0 && rank > 0) {
							intrestMap[x][y] = (int) pointStrength;
							if (pointStrength > max)
								max = pointStrength;
						}
					}
				}
			}
			
			// System.out.println("max: " + max);
			return intrestMap;
		}
		
		private int[][] multiply(int[][] imgfx, int[][] imgfy) {
			int w = imgfx.length;
			int h = imgfx[0].length;
			int[][] out = new int[w][h];
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					out[x][y] = ((imgfx[x][y] * imgfy[x][y]));
				}
			}
			return out;
		}
		
		private int[][] add(int[][] intrestMapfx, int[][] intrestMapfy) {
			int w = intrestMapfx.length;
			int h = intrestMapfx[0].length;
			int[][] out = new int[w][h];
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					out[x][y] = ((intrestMapfx[x][y] + intrestMapfy[x][y]) / 2);
				}
			}
			return out;
		}
		
		private int[][] nonMaxSuspression(int[][] intrestMap, int[][] gradientMap) {
			int w = intrestMap.length;
			int h = intrestMap[0].length;
			int[][] res = new int[w][h];
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					// not at border
					if (x - 1 > 0 && x + 1 < w && y - 1 > 0 && y + 1 < h) {
						if (intrestMap[x][y] != 0) {
							// all possible directions (8-neighborhood) north, north-east, east, south-east and all must be > zero
							// if (intrestMap[x][y - 1] != 0 && intrestMap[x][y + 1] != 0 && intrestMap[x + 1][y - 1] != 0 && intrestMap[x - 1][y + 1] != 0
							// && intrestMap[x + 1][y] != 0 && intrestMap[x - 1][y] != 0 && intrestMap[x + 1][y + 1] != 0 && intrestMap[x - 1][y - 1] != 0) {
							int nDi = 0, noDi = 0, oDi = 0, soDi = 0;
							
							nDi = Math.abs(intrestMap[x][y - 1] + intrestMap[x][y + 1]);
							
							noDi = Math.abs(intrestMap[x + 1][y - 1] + intrestMap[x - 1][y + 1]);
							
							oDi = Math.abs(intrestMap[x + 1][y] + intrestMap[x - 1][y]);
							
							soDi = Math.abs(intrestMap[x + 1][y + 1] + intrestMap[x - 1][y - 1]);
							
							// north
							if (nDi > noDi && nDi > oDi && nDi > soDi) {
								res[x][y - 1] = 1;
								res[x][y + 1] = 1;
							}
							// north-east
							else
								if (noDi > oDi && noDi > soDi) {
									res[x + 1][y - 1] = 1;
									res[x - 1][y + 1] = 1;
								}
								// east
								else
									if (oDi > soDi) {
										res[x + 1][y] = 1;
										res[x - 1][y] = 1;
									}
									// south-east
									else {
										res[x + 1][y + 1] = 1;
										res[x - 1][y - 1] = 1;
									}
							// }
						}
					}
				}
			}
			return res;
		}
		
		private int[][] convolution3x3(int[][] img, int[][] kernel) {
			
			int[][] img2d = img;
			int w = img.length;
			int h = img[0].length;
			int[][] out = new int[w][h];
			int r = 0, g = 0, b = 0, c = 0, gray = 0;
			double sum = 0.0;
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (img2d[x][y] != ImageOperation.BACKGROUND_COLORint && img2d[x][y] != -16777216)
						for (int u = -1; u < 2; u++) {
							for (int i = -1; i < 2; i++) {
								if (x + u > 0 && x + u < w && y + i > 0 && y + i < h) {
									c = img2d[x + u][y + i];
									r = ((c & 0xff0000) >> 16);
									g = ((c & 0x00ff00) >> 8);
									b = (c & 0x0000ff);
									gray = (Math.max(Math.max(r, g), b) + Math.min(Math.min(r, g), b)) / 2;
									// gray = r;
									sum += gray * kernel[u + 1][i + 1];
								}
								
							}
						}
					out[x][y] = (int) (Math.abs(sum) / 9.0);
					sum = 0;
				}
			}
			return out;
		}
		
		/**
		 * Calculation of the derivative fx at point p(x,y), by using 3x3 neighborhood and central step method.
		 * 
		 * @return
		 */
		private int[][] centralDerivate_x(int[][] img2d) {
			
			int w = img2d.length;
			int h = img2d[0].length;
			int[][] out = new int[w][h];
			double fx = 0;
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (img2d[x][y] != 0) {
						if (x - 1 >= 0 && x + 1 < w) {
							fx = (img2d[x + 1][y] - img2d[x - 1][y]) / 2d;
						}
						out[x][y] = (int) (fx * 255);
						// if (fx > 0)
						// System.out.println(fx + " | " + x + " : " + y);
					}
				}
			}
			return out;
		}
		
		/**
		 * Calculation of the derivative fy at point p(x,y), by using 3x3 neighborhood and central step method.
		 * 
		 * @return
		 */
		private int[][] centralDerivate_y(int[][] img2d) {
			
			int w = img2d.length;
			int h = img2d[0].length;
			int[][] out = new int[w][h];
			double fy = 0;
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (img2d[x][y] != 0) {
						if (y - 1 >= 0 && y + 1 < h) {
							fy = (img2d[x][y + 1] - img2d[x][y - 1]) / 2;
						}
						out[x][y] = (int) (fy * 255);
					}
				}
			}
			return out;
		}
		
		/**
		 * Calculation of the derivative fxy at point p(x,y), by using 3x3 neighborhood and central step method.
		 * 
		 * @return
		 */
		private int[][] centralDerivate_xy(int[][] img2d) {
			
			int w = img2d.length;
			int h = img2d[0].length;
			int[][] out = new int[w][h];
			double fxy = 0;
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (img2d[x][y] != 0) {
						if (x - 1 >= 0 && x + 1 < w && y - 1 >= 0 && y + 1 < h) {
							fxy = (img2d[x + 1][y + 1] - img2d[x + 1][y] - img2d[x][y + 1] + 2 * img2d[x][y] - img2d[x - 1][y] - img2d[x][y - 1] + img2d[x - 1][y - 1]) / 2;
						}
						out[x][y] = (int) (fxy * 255);
					}
				}
			}
			return out;
		}
		
		private int[][] convertToGrayscaleImage(int[][] img2d) {
			
			int w = img2d.length;
			int h = img2d[0].length;
			int[][] out = new int[w][h];
			int r = 0, g = 0, b = 0, c = 0, gray = 0;
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (img2d[x][y] != ImageOperation.BACKGROUND_COLORint) {
						c = img2d[x][y];
						r = ((c & 0xff0000) >> 16);
						g = ((c & 0x00ff00) >> 8);
						b = (c & 0x0000ff);
						gray = (Math.max(Math.max(r, g), b) + Math.min(Math.min(r, g), b)) / 2;
						out[x][y] = 254 - gray;
					} else {
						out[x][y] = 0;
					}
				}
			}
			return out;
		}
	};
}
// first get gradient image of intrest map
// int[][] intrestMapfx = convolution3x3(intrestMap, sobelx);
// int[][] intrestMapfy = convolution3x3(intrestMap, sobely);
// int[][] gradientMap = add(intrestMapfx, intrestMapfy);

// new Image(gradientMap).show("blub1");
// int[][] intrestMapFiltered = nonMaxSuspression(intrestMap, gradientMap);

// mark non sus... TODO fix this approach, produces more interest points
// int count1 = 0;
// ImageCanvas ic1 = gray.copy().io().canvas();
// for (int x = 0; x < w; x++) {
// for (int y = 0; y < h; y++) {
// if (intrestMapFiltered[x][y] != 0)
// // System.out.println("sdsdsdsd");
// ic1.drawCircle(x, y, 3, Color.RED.getRGB(), 0.5, 1);
// count1++;
// }
// }
// System.out.println("number of intrest points non max sus: " + count1);
// ic1.getImage().show("non max");