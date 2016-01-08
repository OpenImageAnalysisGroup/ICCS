package support;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculate the square root of the values in the image.
 * input: param 1 input image, param 2 output file
 * output: Square root image
 * 
 * @author Christian Klukas
 */
public class Sqrt {
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 2) {
			System.err.println("Params: [input image (float)] [target file] ! Return Code 1");
			System.exit(1);
		} else {
			File f_a = new File(args[0]);
			File f_out = new File(args[1]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (!f_a.exists()) {
					System.err.println("Input image '" + f_a.getName() + "' could not be found! Return Code 4");
					System.exit(4);
				} else {
					Image imgA = new Image(FileSystemHandler.getURL(f_a));
					float[] imgAf = imgA.getAs1float();
					float[] out = new float[imgAf.length];
					for (int i = 0; i < imgAf.length; i++)
						out[i] = (float) Math.sqrt(imgAf[i]);
					new Image(imgA.getWidth(), imgA.getHeight(), out).saveToFile(f_out.getPath());
				}
		}
	}
}
