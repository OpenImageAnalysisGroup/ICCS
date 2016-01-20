package support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import correction.ColorSpaces;
import correction.ColorValues;
import de.ipk.ag_ba.image.structures.Image;
import detection.CCColorNames;

public class ARFFExport {
	
	/**
	 * Export sampled checker values to Arff format (RGBHSVLAB for each patch).
	 * @param samples - list of colorvalues for color checker patches.
	 * @throws IOException 
	 */
	public void exporttoArff1(ColorValues[] samples, String path, String name) throws IOException {
		FileWriter fw = new FileWriter(new File(path + "/" + name + ".arff"), false);
		
		String header = "";
		String line = "";
		
		// create header
		String colors[] = new String[] {"R", "G", "B", "H", "S", "V", "L", "A", "B"};
		for(String col : colors)
			header += "@attribute " + col
			+ "\tNUMERIC\n";
		
		header = "%\n" + "@relation '" + name + "'\n" + header
				+ "@data\n";
				
		fw.write(header);
	
			
		// start to add lines
		for(ColorValues s : samples) {
			line = s.getAvgColor(ColorSpaces.RGB).getA() + " " + s.getAvgColor(ColorSpaces.RGB).getB() + " " + s.getAvgColor(ColorSpaces.RGB).getC() +
					" " + s.getAvgColor(ColorSpaces.HSV).getA() + " " + s.getAvgColor(ColorSpaces.HSV).getB() + " " + s.getAvgColor(ColorSpaces.HSV).getC() + 
					" " + s.getAvgColor(ColorSpaces.Lab).getA() + " " + s.getAvgColor(ColorSpaces.Lab).getB() + " " + s.getAvgColor(ColorSpaces.Lab).getC() + "\n";
				fw.write(line);
		}
		
		fw.write("%");
		fw.close();
	}
	
	/**
	 * Export of RGB colorchecker values including Arff header with color names.
	 */
	public void exportColorChartValuestoArff(ColorValues[] samples, String path, String name) throws IOException {
		FileWriter fw = new FileWriter(new File(path + "/" + name + ".arff"), false);
		
		String header = "";
		String line = "";
		
		// create header
		String colorNames[] = CCColorNames.getNames();
		String colorSpaces[] = new String[] {"R", "G", "B"};
		
		for(String colorName : colorNames)
			for(String col : colorSpaces)
				header += "@attribute " + colorName + "_" + col
				+ "\tNUMERIC\n";
		
		header = "%\n" + "@relation '" + name + "'\n" + header
				+ "@data\n";
				
		fw.write(header);
	
			
		// start to add lines
		for(ColorValues s : samples) {
			line += s.getAvgColor(ColorSpaces.RGB).getA() + " " + s.getAvgColor(ColorSpaces.RGB).getB() + " " + s.getAvgColor(ColorSpaces.RGB).getC() + " ";
		}
		
		fw.write(line);
		fw.write("%");
		fw.close();
	}

	/**
	 * Export sampled checker values to same format as reference data for visualization purposes, in defined color space.
	 * @param samples - list of colorvalues for color checker patches.
	 * @throws IOException 
	 */
	public void exporttoForVisualization(ColorValues[] samples, String path, String name, String colorspace) throws IOException {
		FileWriter fw = new FileWriter(new File(path + "/" + name + ""), false);
		
		String line = "";
		
		ColorSpaces cs = ColorSpaces.valueOf(colorspace);

		// start to add lines
		for(ColorValues s : samples) {
			line = s.name + " " + 
					s.getAvgColor(cs).getA() + " " + s.getAvgColor(cs).getB() + " " + s.getAvgColor(cs).getC() + "\n";
				fw.write(line);
		}
		
		fw.close();
	}

	public void exportImagetoArff(Image img, ColorValues[] samples, String path, String name) throws IOException {
		
		String attributes = "";
		attributes += "@attribute " + "RGB_R" + "\n";
		attributes += "@attribute " + "RGB_G" + "\n";
		attributes += "@attribute " + "RGB_B" + "\n";
		
		String[] rgb = new String[] {"R", "G" ,"B"};
		
		for (int i = 0; i < 24; i++) {
			for(String s : rgb) {
			attributes += "@attribute " + samples[i].name + "_" + s
				+ "\tNUMERIC\n";
			}
		}
		
//		int numberOfDiseaseClasses = Settings.numberOfClasses;
//		
//		attributes += "@attribute class\t{";
//		for (int idx = 0; idx < numberOfDiseaseClasses; idx++) {
//			if (idx < numberOfDiseaseClasses - 1)
//			attributes += ("class" + idx + ",");
//			else
//			attributes += ("class" + idx);
//		}
//		attributes += "}\n";
		
		String header = "%\n" + "@relation '" + name  + "'\n" + attributes
			+ "@data\n";
		
		String line = "";
		
		FileWriter fw = new FileWriter(new File(path + "/" + name + ".arff"),
			false);
			
		fw.write(header);
		
		int[] img1d = img.getAs1A();
		
		for (int x = 0; x < img1d.length; x++) {
			
			int c = img1d[x];
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = c & 0x0000ff;
			
			line = r + " " + g + " " + b + " ";
			
			for (int i = 0; i < 24; i++) {
				line += " " + samples[i].getAvgColor(ColorSpaces.RGB).getA() +
						" " + samples[i].getAvgColor(ColorSpaces.RGB).getB() +
						" " + samples[i].getAvgColor(ColorSpaces.RGB).getC();
			}
			
			if (line.length() > 0) {
				// appends the string to the file
				fw.write(line + " ?" + "\n");
				line = "";
			}
		}
		
		fw.write("%");
		fw.close();
	}
}
