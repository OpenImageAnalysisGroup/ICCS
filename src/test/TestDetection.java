package test;

import java.io.File;

import correction.ColorCorrection;
import correction.ColorValues;
import correction.ControlColorCheckerValues;
import correction.ReadColorCheckerValues;
import de.ipk.ag_ba.image.structures.Image;
import detection.ColorCheckerDetector;
import ij.ImageJ;
import support.HelperMethods;

/**
 * Testclass for extracting colors from color-checker.
 * 
 * @author pape
 */
public class TestDetection {
	
	static boolean debug = true;
	static double scale = 3.0;
	
	// run test
	public static void main(String[] args) throws Exception {
		// int numOfFiles = HelperMethods.getNumOfFiles("/Desktop/test_images/colorchecker/", "");
		File pathTestdata = new File("testdata/");
		File pathCheckervalues = new File("checkervalues/");
		
		// File inp = new File("/home/pape/Desktop/test_timeline/half_size/");
		String[] filenames = pathTestdata.list();
		
		for (int i = 0; i < filenames.length; i++) {
			Image img = HelperMethods.readImageAbsPath(pathTestdata.getAbsolutePath() + "/" + filenames[i]); // "color_checker_7.png");//
			ColorCheckerDetector ccd = new ColorCheckerDetector(img, scale, debug);
			ColorValues[] samples = ccd.getSamples();
			ccd.getDebugImage().show("debug", true);
			
			// export samples to ARFF
			// TODO
			
			System.out.println("Finish detection.");
			
			ControlColorCheckerValues[] realValues = ReadColorCheckerValues.readValuesFromCsv(pathCheckervalues.getPath(), new String[] { "XYZ", "LAB", "RGB" });
			ColorCorrection cc = new ColorCorrection(samples, realValues);
			Image resWB = cc.correctWB(img).show("WB", false);
			// Image resWW = cc.correctImage(img, ColorCorrection., regTypePoly, 2);
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", filenames[i], "png", resWB);
			// cc.correctImage(img, ColorModes.LAB_L, false, 2).show("corrected");
			
			System.out.println();
			Thread.sleep(100);
			// res.show(filenames[i], true);
			// break;
		}
		
		ImageJ ij = new ImageJ();
		ij.setVisible(true);
		Thread.sleep(100000);
	}
}
