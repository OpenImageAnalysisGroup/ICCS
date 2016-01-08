package support;

import java.io.File;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * Calculates the average intensity value, standard deviation and other
 * statistics for a given input image.
 * 
 * @author Christian Klukas
 */
public class AnalyzeLevels {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 1) {
			System.err.println("No input image filenames provided! Return Code 1");
			System.exit(1);
		} else {
			for (String fn : args) {
				File f = new File(fn);
				if (!f.exists()) {
					System.err.println("File '" + f + "' could not be found! Return Code 2");
					System.exit(2);
				}
				
				Image i = new Image(FileSystemHandler.getURL(f));
				DescriptiveStatistics stat = new DescriptiveStatistics();
				
				float[] p = i.getAs1float();
				
				for (float v : p)
					stat.addValue(v);
				
				TextFile tf = new TextFile();
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "mean" + "\t" + stat.getMean());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "min" + "\t" + stat.getMin());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "max" + "\t" + stat.getMax());
				// not sensible, I would guess:
				// tf.add(f.getParent() + File.separator + f.getName() + "\t" + "geom_mean" + "\t" + stat.getGeometricMean());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "kurtosis" + "\t" + stat.getKurtosis());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_50" + "\t" + stat.getPercentile(50));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_01" + "\t" + stat.getPercentile(1));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_99" + "\t" + stat.getPercentile(99));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_05" + "\t" + stat.getPercentile(5));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_95" + "\t" + stat.getPercentile(95));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_10" + "\t" + stat.getPercentile(10));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "percentile_90" + "\t" + stat.getPercentile(90));
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "n" + "\t" + stat.getN());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "population_variance" + "\t" + stat.getPopulationVariance());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "standard_deviation" + "\t" + stat.getStandardDeviation());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "sum" + "\t" + stat.getSum());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "sum_of_squares" + "\t" + stat.getSumsq());
				tf.add(f.getParent() + File.separator + f.getName() + "\t" + "variance" + "\t" + stat.getVariance());
				tf.write(f.getParent() + File.separator + f.getName() + "_levels.csv");
			}
		}
	}
}
