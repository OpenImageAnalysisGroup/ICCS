package support;

import java.io.File;
import java.util.LinkedList;

/**
 * Merges two or more ARFF files (concatinates the column data)
 * 
 * @author Christian Klukas
 */
public class MergeArffFiles {
	
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 2) {
			System.err.println("No [targetfile] [filenames] for ARFF files to be merged specified! Return Code 1");
			System.exit(1);
		} else {
			LinkedList<File> fl = new LinkedList<>();
			boolean first = true;
			for (String a : args) {
				if (!first)
					fl.add(new File(a));
				first = false;
			}
			ARFFcontent ac = new ARFFcontent();
			for (File f : fl)
				ac.appendColumnData(f);
			ac.writeTo(new File(args[0]));
		}
	}
}
