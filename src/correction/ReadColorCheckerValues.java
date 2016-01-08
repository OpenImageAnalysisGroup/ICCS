package correction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;

public class ReadColorCheckerValues {
	
	public static ControlColorCheckerValues[] readValuesFromCsv(String path, String[] modes) throws Exception {
		ControlColorCheckerValues[] out = new ControlColorCheckerValues[24];
		
		for (String name : modes) {
			InputStream s = ReadColorCheckerValues.class.getResourceAsStream(path + "color_checker_values_" + name);
			LinkedList<ControlColorCheckerValues> ll = new LinkedList<ControlColorCheckerValues>();
			
			BufferedReader b = new BufferedReader(new InputStreamReader(s));
			while (b.ready()) {
			String line = b.readLine();
			ControlColorCheckerValues v = getValFromLine(line, name);
			ll.add(v);
			}
			
			int i = 0;
			for (ControlColorCheckerValues l : ll)
			out[i++] = l;
		}
		
		return out;
	}
	
	/**
	 * Data File should look like this: name value_1 value_2 value_3
	 */
	private static ControlColorCheckerValues getValFromLine(String line, String name) throws NumberFormatException, IllegalArgumentException,
			IllegalAccessException {
		String[] split = line.split(" ");
		ControlColorCheckerValues cv = new ControlColorCheckerValues();
		Field[] fields = cv.getClass().getDeclaredFields();
		
		for (int idx = 0; idx < fields.length; idx++) {
			// name
			if (fields[idx].getName().endsWith("name") && idx == 0)
			fields[idx].set(cv, split[0]);
			if (fields[idx].getName() == name)
			fields[idx].set(cv, new double[] { Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]) });
		}
		
		return cv;
	}
}
