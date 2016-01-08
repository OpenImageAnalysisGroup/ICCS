package support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculate the median value of a series of images. Requires more memory (all images need to be loaded at once),
 * in comparison to the mean calculation command.
 * input: param 1 output file, param 2 .. n input files
 * output: Median value image
 * 
 * @author Christian Klukas
 */
public class Median {
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
				float[][] images = null;
				int w = -1, h = -1;
				for (int ai = 1; ai < args.length; ai++) {
					File f_a = new File(args[ai]);
					if (!f_a.exists()) {
						System.err.println("Input image '" + f_a.getName() + "' could not be found! Return Code 4");
						System.exit(4);
					} else {
						Image imgA = new Image(FileSystemHandler.getURL(f_a));
						
						if (images == null) {
							images = new float[args.length - 1][];
							w = imgA.getWidth();
							h = imgA.getHeight();
						}
						
						images[ai - 1] = imgA.getAs1float();
						
						if (ai > 1 && images[ai - 1].length != images[0].length) {
							System.err.println("Input image '" + args[ai] + "' has different image size! Return Code 4");
							System.exit(4);
						}
					}
				}
				float[] out = new float[images[0].length];
				float[] pixVec = new float[args.length - 1];
				
				for (int pixelIdx = 0; pixelIdx < images[0].length; pixelIdx++) {
					for (int i = 0; i < args.length - 1; i++) {
						pixVec[i] = images[i][pixelIdx];
					}
					out[pixelIdx] = median(pixVec);
				}
				
				new Image(w, h, out).saveToFile(f_out.getPath());
			}
		}
	}
	
	private static float median(float[] pixVec) {
		ArrayList<Float> m = new ArrayList<Float>();
		for (float v : pixVec)
			m.add(v);
		java.util.Collections.sort(m);
		int middle = m.size() / 2;
		if (m.size() % 2 == 1) {
			return m.get(middle);
		} else {
			return (m.get(middle - 1) + m.get(middle)) / 2.0f;
		}
	}
}
