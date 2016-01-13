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
public class TestDetectionandSimpleCorrection {
	
	static boolean debug = true;
	static double scale = 1.0;
	
	// run test
	public static void main(String[] args) throws Exception {

		File pathTestdata = new File("testdata/testdetectiondata");
		File pathCheckervalues = new File("checkervalues/");
		
		String[] filenames = pathTestdata.list();
		
		for (int i = 0; i < filenames.length; i++) {
			Image img = HelperMethods.readImageAbsPath(pathTestdata.getAbsolutePath() + "/" + filenames[i]); // "color_checker_7.png");//
			ColorCheckerDetector ccd = new ColorCheckerDetector(img, scale, debug);
			ColorValues[] samples = ccd.getSamples(true);
			ccd.getDebugImage().show("debug", true);
			
			// export samples to ARFF
			ccd.exporttoArff(samples, System.getProperty("user.home") + "/Desktop" + "/WB/", "samples");
			
			System.out.println("Finish detection.");
			
			ControlColorCheckerFields[] realValues = ReadColorCheckerValues.readValuesFromCsv(pathCheckervalues.getPath(), new String[] { "XYZ", "LAB", "RGB" });
			ColorCorrection cc = new ColorCorrection(samples, realValues);
			
			Image resWB = cc.correctWBClassicRGB(img).show("WB", false);
			Image resREG = cc.correctImage(img, ColorCorrection.ColorModes.LAB_L, false, 1);
			Image resPOLY = cc.correctImage(img, ColorCorrection.ColorModes.LAB_L, true, 4);
			
			String fname = (String) filenames[i].subSequence(0, filenames[i].length() - 4);
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", fname + "_WB", "png", resWB);
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", fname + "_REG" , "png", resREG);
			HelperMethods.saveImage(System.getProperty("user.home") + "/Desktop" + "/WB/", fname + "_POLY", "png", resPOLY);
			// cc.correctImage(img, ColorModes.LAB_L, false, 2).show("corrected");
			
			System.out.println();
			resWB.show("resWB", debug);
			resREG.show("resREG", debug);
			resPOLY.show("resPOLY", debug);
			
			System.out.println("Finish Correction.");
		}
		
		ImageJ ij = new ImageJ();
		ij.setVisible(true);
		Thread.sleep(1000000);
		System.out.println("Finish");
	}
}
