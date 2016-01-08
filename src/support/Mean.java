package support;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculate the mean value of a series of images. Requires few memory, in comparison to the median calculation command.
 * input: param 1 output file, param 2 .. n input files
 * output: Mean value image
 * 
 * @author Christian Klukas
 */
public class Mean {
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length < 2) {
			System.err.println("Params: [target file] [input image (float) 1] [more input images (float)] ... ! Return Code 1");
			System.exit(1);
		} else {
			File f_out = new File(args[0]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else {
				double[] sum = null;
				int w = -1, h = -1;
				for (int ai = 1; ai < args.length; ai++) {
					File f_a = new File(args[ai]);
					if (!f_a.exists()) {
						System.err.println("Input image '" + f_a.getName() + "' could not be found! Return Code 4");
						System.exit(4);
					} else {
						Image imgA = new Image(FileSystemHandler.getURL(f_a));
						float[] imgAf = imgA.getAs1float();
						
						if (sum == null) {
							sum = new double[imgAf.length];
							for (int idx = 0; idx < sum.length; idx++)
								sum[idx] = 0;
							w = imgA.getWidth();
							h = imgA.getHeight();
						}
						for (int i = 0; i < imgAf.length; i++)
							sum[i] += imgAf[i];
					}
				}
				float[] out = new float[sum.length];
				double n = args.length - 1;
				for (int i = 0; i < sum.length; i++)
					out[i] = (float) (sum[i] / n);
				
				new Image(w, h, out).saveToFile(f_out.getPath());
			}
		}
	}
}
