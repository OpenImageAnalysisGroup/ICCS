package commands;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import correction.ColorValues;
import de.ipk.ag_ba.image.structures.Image;
import detection.ColorCheckerDetector;
import support.ARFFExport;
import support.HelperMethods;

/**
 * Detects color chart in image and export different kind of data in respect to the parameterization.
 * parms: 1. path to image including color chart; 2. ARFF export checker values, ARFF export for vis, full image to ARFF conversion including color chart values (0,1,2); 3. save debug image (true/false); 4. export color mode (RGB, Lab)
 * @author pape
 *
 */
public class DetectColorChart {
	
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 4) {
			System.err.println("No sufficient parameters given! Return Code 1");
			System.exit(1);
		} else {
			String inputPathStr = args[0];
			File input = new File(inputPathStr);
			int mode = Integer.valueOf(args[1]);
			boolean debug = Boolean.valueOf(args[2]);
			String export_color_mode = args[3];
			
			// options
			double scale = 1.0;
			
			Image img = HelperMethods.readImageAbsPath(input.getAbsolutePath());
			ColorCheckerDetector ccd = new ColorCheckerDetector(img, scale, debug);
			ColorValues[] samples = ccd.getSamples(true);
			ccd.getDebugImage().show("debug", true).saveToFile(System.getProperty("user.home") + "/Desktop" + "/" + FilenameUtils.getBaseName(input.getName()) + "_debug." + FilenameUtils.getExtension(input.getName()));
			
			System.out.println("Finish detection!");
			
			// export samples to ARFF
			ARFFExport ae = new ARFFExport();
			switch(mode) {
				case 0:
					ae.exporttoArff1(samples, System.getProperty("user.home") + "/Desktop" + "/", "samples1");
					break;
				case 1:
					ae.exporttoForVisualization(samples,System.getProperty("user.home") + "/Desktop" + "/", "samplesForVis_" + export_color_mode, export_color_mode);
					break;
				case 2:
					ae.exportImagetoArff(img, samples, System.getProperty("user.home") + "/Desktop" + "/", FilenameUtils.getBaseName(input.getName()));
					break;
			}
			
			System.out.println("Finish export!");
			
		}
	}
	
}
