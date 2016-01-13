package support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import correction.ColorSpaces;
import correction.ColorValues;

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
	 * Export sampled checker values to same format as reference data for visualization purposes.
	 * @param samples - list of colorvalues for color checker patches.
	 * @throws IOException 
	 */
	public void exporttoForVisualization(ColorValues[] samples, String path, String name) throws IOException {
		FileWriter fw = new FileWriter(new File(path + "/" + name + ""), false);
		
		String line = "";

		// start to add lines
		for(ColorValues s : samples) {
			line = s.name + " " + 
					s.getAvgColor(ColorSpaces.RGB).getA() + " " + s.getAvgColor(ColorSpaces.RGB).getB() + " " + s.getAvgColor(ColorSpaces.RGB).getC() +
					" " + s.getAvgColor(ColorSpaces.HSV).getA() + " " + s.getAvgColor(ColorSpaces.HSV).getB() + " " + s.getAvgColor(ColorSpaces.HSV).getC() + 
					" " + s.getAvgColor(ColorSpaces.Lab).getA() + " " + s.getAvgColor(ColorSpaces.Lab).getB() + " " + s.getAvgColor(ColorSpaces.Lab).getC() + "\n";
				fw.write(line);
		}
		
		fw.write("%");
		fw.close();
	}
}
