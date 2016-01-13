package correction;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.LinkedList;

public class ReadColorCheckerValues {
	
	public static ControlColorCheckerFields[] readValuesFromCsv(String path, String[] modes) throws Exception {
		ControlColorCheckerFields[] out = new ControlColorCheckerFields[24];
		
		for (String spaceName : modes) {
			System.out.println("ReadMode: " + spaceName);
			FileInputStream s = new FileInputStream(path + "/color_checker_values_" + spaceName);
			LinkedList<ColorICCS> ll = new LinkedList<ColorICCS>();
			
			BufferedReader b = new BufferedReader(new InputStreamReader(s));
			while (b.ready()) {
				String line = b.readLine();
				ColorICCS v = getValFromLine(line, spaceName);
				ll.add(v);
			}
			
			b.close();
			
			int i = 0;
			for (ColorICCS l : ll) {
				if(out[i] == null) {
					out[i] = new ControlColorCheckerFields();
				}
				out[i++].setField(spaceName, l.colorname,  l.values);
			}
		}
		
		return out;
	}
	
	/**
	 * Data File should look like this: name value_1 value_2 value_3
	 */
	private static ColorICCS getValFromLine(String line, String colorspacename) throws NumberFormatException, IllegalArgumentException,
			IllegalAccessException {
		String[] split = line.split(" ");
		ControlColorCheckerFields cv = new ControlColorCheckerFields();
		Field[] fields = cv.getClass().getDeclaredFields();
		ColorICCS col = null;
		
		for (int idx = 0; idx < fields.length; idx++) {
			// name
			if (fields[idx].getName().endsWith("name") && idx == 0)
			fields[idx].set(cv, split[0]);

			if (fields[idx].getName() == colorspacename)
				col = new ColorICCS(colorspacename, cv.name, new double[] { Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]) });
		}
		
		return col;
	}
}
