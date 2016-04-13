package test;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;
import ij.process.FloatProcessor;
import support.HistogramComparision;

/**
 * Test for comparison of two histograms.
 * 
 * @author Jean-Michel Pape
 *
 */
public class TestHistogramComparision {
	public void main(String[]args) throws IOException, Exception {
		File f_a = new File(System.getProperty("user.home") + "/Desktop/hist_test/1.png");
		File f_b = new File(System.getProperty("user.home") + "/Desktop/hist_test/2.png");
		
		Image imgA = new Image(FileSystemHandler.getURL(f_a));
		Image imgB = new Image(FileSystemHandler.getURL(f_b));
		
		FloatProcessor fp_a = (FloatProcessor) imgA.getAsImagePlus().getProcessor();
		FloatProcessor fp_b = (FloatProcessor) imgB.getAsImagePlus().getProcessor();
		
		int[] hist_a = fp_a.getHistogram();
		int[] hist_b = fp_b.getHistogram();
		
		HistogramComparision hc = new HistogramComparision(hist_a, hist_b);
		
		double x_square = hc.chiSquare();
		System.out.println("Chi-square: " + x_square);
	}
}
