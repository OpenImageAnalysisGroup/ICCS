package test;

import java.io.File;

import correction.ColorCorrection;
import correction.ColorValues;
import correction.ControlColorCheckerFields;
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
	static double scale = 1.0;
	
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
			
			ControlColorCheckerFields[] realValues = ReadColorCheckerValues.readValuesFromCsv(pathCheckervalues.getPath(), new String[] { "XYZ", "LAB", "RGB" });
			ColorCorrection cc = new ColorCorrection(samples, realValues);
			Image resWB = cc.correctWB(img).show("WB", false);
			Image resREG = cc.correctImage(img, ColorCorrection.ColorModes.LAB_L, false, 1);
			Image resPOLY = cc.correctImage(img, ColorCorrection.ColorModes.LAB_L, true, 4);
			
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", (String) filenames[i].subSequence(0, filenames[i].length() - 4), "_WB.png", resWB);
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", (String) filenames[i].subSequence(0, filenames[i].length() - 4), "_REG.png", resREG);
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", (String) filenames[i].subSequence(0, filenames[i].length() - 4), "_POLY.png", resPOLY);
			// cc.correctImage(img, ColorModes.LAB_L, false, 2).show("corrected");
			
			System.out.println();
			resWB.show("resWB");
			resREG.show("resREG");
			resPOLY.show("resPOLY");
			
			Thread.sleep(100000);
		}
		
		ImageJ ij = new ImageJ();
		ij.setVisible(true);
		Thread.sleep(100);
		System.out.println("Finish");
	}
}
