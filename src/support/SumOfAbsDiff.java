package support;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculate the sum of the absolute differences between a single reference image and a variable number of other images.
 * input: param 1 output file, param 2 reference image, param 3 .. n input files
 * output: Sum of absolute difference of each input image to a given reference image
 * 
 * @author Christian Klukas
 */
public class SumOfAbsDiff {
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length < 3) {
			System.err.println("Params: [target file] [reference image] [input image (float) 1] [more input images (float)] ... ! Return Code 1");
			System.exit(1);
		} else {
			File f_out = new File(args[0]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else {
				File f_ref = new File(args[1]);
				if (!f_ref.exists()) {
					System.err.println("Error - Input reference image '" + f_ref.getName() + "' could not be found! Return Code 3");
					System.exit(3);
				} else {
					float[] refPix = new Image(FileSystemHandler.getURL(f_ref)).getAs1float();
					
					int w = -1, h = -1;
					float[] out = null;
					for (int ai = 2; ai < args.length; ai++) {
						File f_a = new File(args[ai]);
						if (!f_a.exists()) {
							System.err.println("Input image '" + f_a.getName() + "' could not be found! Return Code 4");
							System.exit(4);
						} else {
							Image imgA = new Image(FileSystemHandler.getURL(f_a));
							
							float[] image = imgA.getAs1float();
							
							if (ai == 2) {
								w = imgA.getWidth();
								h = imgA.getHeight();
								out = new float[image.length];
							}
							
							if (ai > 2 && (w != imgA.getWidth() || h != imgA.getHeight())) {
								System.err.println("Input image '" + args[ai] + "' has different image size! Return Code 4");
								System.exit(4);
							}
							
							for (int i = 0; i < out.length; i++) {
								out[i] += Math.abs(image[i] - refPix[i]);
							}
						}
					}
					
					new Image(w, h, out).saveToFile(f_out.getPath());
				}
			}
		}
	}
}
