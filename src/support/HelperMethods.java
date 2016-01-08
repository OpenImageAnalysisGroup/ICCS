package support;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.structures.Image;
import ij.ImagePlus;

public class HelperMethods {
	
	public static Image readImageAbsPath(String inp) {
		inp = inp.replace("\\", "/");
		File fff = new File(inp);
		Image img = null;
		
		try {
			img = new Image(ImageIO.read(fff));
			// if (img.getWidth() < 1000)
			// img = img.io().resize(5).getImage();
		} catch (IOException e) {
			throw new RuntimeException("Can't read image file '" + inp + "'!");
		}
		return img;
	}
	
	public static Image readImage(String inp) {
		inp = inp.replace("\\", "/");
		inp = System.getProperty("user.home") + inp;
		File fff = new File(inp);
		Image img = null;
		
		try {
			img = new Image(ImageIO.read(fff));
			if (img.getWidth() < 1000)
			img = img.io().resize(5).getImage();
		} catch (IOException e) {
			System.err.println("Can't read image file '" + inp + "'!");
		}
		return img;
	}
	
	public static double[] normalize(double[] img) {
		double[] res = new double[img.length];
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		double temp = 0.0;
		double val = 0;
		for (int idx = 0; idx < img.length; idx++) {
			temp = img[idx];
			if (temp < min)
			min = temp;
			if (temp > max)
			max = temp;
		}
		for (int idx = 0; idx < img.length; idx++) {
			val = (255 * ((img[idx] - min) / (max - min)));
			val = (Math.sqrt(val / 255d) * 255d);
			res[idx] = val;
		}
		return res;
	}
	
	public static double[][] normalize(double[][] img) {
		int width = img.length;
		int height = img[0].length;
		double[] temp1d = new double[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
			temp1d[x + width * y] = img[x][y];
			}
		}
		
		temp1d = normalize(temp1d);
		int x, y;
		double[][] res = new double[width][height];
		for (int idx = 0; idx < width * height; idx++) {
			x = idx % width;
			y = idx / width;
			res[x][y] = temp1d[idx];
		}
		
		return res;
	}
	
	public static int[][] normalizeToInt(double[][] img) {
		int width = img.length;
		int height = img[0].length;
		double[] temp1d = new double[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
			temp1d[x + width * y] = img[x][y];
			}
		}
		
		temp1d = normalize(temp1d);
		int x, y;
		int[][] res = new int[width][height];
		for (int idx = 0; idx < width * height; idx++) {
			x = idx % width;
			y = idx / width;
			res[x][y] = (int) temp1d[idx];
		}
		
		return res;
	}
	
	public static double[][][] normalize(double[][][] img) {
		int width = img.length;
		int height = img[0].length;
		int depth = img[0][0].length;
		
		double[] temp1d = new double[width * height * depth];
		for (int ix = 0; ix < width; ix++) {
			for (int iy = 0; iy < height; iy++) {
			for (int iz = 0; iz < depth; iz++) {
				temp1d[ix + width * iy + height * depth * iz] = img[ix][iy][iz];
			}
			}
		}
		
		temp1d = normalize(temp1d);
		int x, y, z;
		double[][][] res = new double[width][height][depth];
		for (int idx = 0; idx < width * height * depth; idx++) {
			x = idx % width;
			y = (idx / width) % depth;
			z = idx / (width * depth);
			res[x][y][z] = temp1d[idx];
		}
		
		return res;
	}
	
	public static int[][] getGrayImageAs2dArray(Image grayImage) {
		int[] img1d = grayImage.getAs1A();
		int c, r, y = 0;
		int w = grayImage.getWidth();
		int h = grayImage.getHeight();
		int[][] res = new int[w][h];
		
		for (int idx = 0; idx < img1d.length; idx++) {
			c = img1d[idx];
			r = ((c & 0xff0000) >> 16);
			if (idx % w == 0 && idx > 0)
			y++;
			res[idx % w][y] = r;
		}
		return res;
	}
	
	/**
	 * @param filter
	 *           : Empty String "" ignores Filter.
	 */
	public static int getNumOfFilesAbsPath(String pathname, String filter) {
		File directory = new File(pathname);
		String[] list;
		
		if (!filter.equals("")) {
			list = directory.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (filter.contains(name))
					return true;
				else
					return false;
			}
			});
		} else
			list = directory.list();
			
		int count = list.length;
		return count;
	}
	
	public static int getNumOfFiles(String pathname, String filter) {
		pathname = System.getProperty("user.home") + pathname;
		File directory = new File(pathname);
		String[] list = directory.list(); // optional: filter
		int count = list.length;
		return count;
	}
	
	public static void write(String pathname, String filename, String data) {
		write(pathname, filename, data, ".txt");
	}
	
	public static void write(String pathname, String filename, String data, String format) {
		File path = new File(pathname);
		if (!path.exists())
			path.mkdirs();
			
		if (!pathname.startsWith("/home"))
			pathname = System.getProperty("user.home") + pathname;
			
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(pathname + "/" + filename + format)));
			out.write(data);
			out.close();
		} catch (IOException e) {
			System.out.println("Fail to write File: " + filename);
		}
	}
	
	public static void write(String pathname, String filename, double[] data) {
		String str = Arrays.toString(data);
		write(pathname, filename, str);
	}
	
	/**
	 * Test: Location of 3 points
	 * return: - 1, if p2 is "right/left" to p1 and p3 else 1 (0 = points are collinear)
	 **/
	public static int ccw(Point2d p1, Point2d p2, Point2d p3) {
		int val = (int) ((p3.x - p1.x) * (p2.y - p1.y) - (p2.x - p1.x) * (p3.y - p1.y));
		return val > 1 ? 1 : (val < 1 ? -1 : 0);
	}
	
	public static int[][] crop(int[][] img, int w, int h, int pLeft, int pRight, int pTop,
			int pBottom) {
		int[][] res = new int[pRight - pLeft][pBottom - pTop];
		pLeft = Math.max(pLeft, 0);
		pRight = Math.min(pRight, w);
		pTop = Math.max(pTop, 0);
		pBottom = Math.min(pBottom, h);
		
		for (int x = pLeft; x < pRight; x++) {
			for (int y = pTop; y < pBottom; y++) {
			res[x - pLeft][y - pTop] = img[x][y];
			}
		}
		return res;
	}
	
	public static double[] convert(Object[] array) {
		double[] out = new double[array.length];
		int idx = 0;
		for (Object num : array) {
			out[idx] = Double.parseDouble(String.valueOf(num));
			idx++;
		}
		return out;
	}
	
	/**
	 * Includes white background.
	 */
	public static int[] colorize(int[] in) {
		float hue = 0f, sat = 1f, val = 1f;
		int rgb;
		int[] res = new int[in.length];
		int count = 0;
		for (int i : in) {
			double gray = (i & 0xff0000) >> 16;
			hue = (float) (0.3 - (0.3 * (gray / 255d)));
			if (hue == 0.3f)
			sat = 0f;
			else
			sat = 1f;
			rgb = Color.HSBtoRGB(hue, sat, val);
			res[count] = rgb;
			count++;
		}
		return res;
	}
	
	public static synchronized void saveImage(String outputPath, String name, String format, Image img) {
		saveImage(outputPath, name, format, img.getAsBufferedImage(false));
	}
	
	public static synchronized void saveImage(String outputPath, String name, String format, ImagePlus img) {
		saveImage(outputPath, name, format, img.getBufferedImage());
	}
	
	public static synchronized void saveImage(String outputPath, String name, String format, BufferedImage img) {
		File path = new File(outputPath);
		boolean pathOK = true;
		if (!path.exists())
			pathOK = path.mkdirs();
		if (pathOK == false) {
			System.out.println("Path incorrect, no image has been written!");
			return;
		}
		if (!outputPath.endsWith("/"))
			outputPath = outputPath + "/";
		File outputfile = new File(outputPath + name + "." + format);
		try {
			ImageIO.write(img, format, outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static boolean windows = SystemAnalysis.isWindowsRunning();
	
	/**
	 * @param outputPath
	 *           - absolute path
	 */
	public static synchronized void writeOrAppend(String outputPath, String filename, String data) {
		File path = new File(outputPath);
		if (!path.exists())
			path.mkdirs();
		// file already present -> append else create new
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(outputPath + filename, true)));
			if (windows)
			out.write(data + "\r\n");
			else
			out.write(data + "\n");
			out.close();
		} catch (IOException e) {
			System.out.println("File could not be written.");
		}
	}
	
	public static int[][] normalize(int[][] img) {
		int width = img.length;
		int height = img[0].length;
		double[] temp1d = new double[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
			temp1d[x + width * y] = img[x][y];
			}
		}
		
		temp1d = normalize(temp1d);
		int x, y;
		int[][] res = new int[width][height];
		for (int idx = 0; idx < width * height; idx++) {
			x = idx % width;
			y = idx / width;
			res[x][y] = (int) temp1d[idx];
		}
		
		return res;
	}
}
