package correction;

import de.ipk.ag_ba.image.operation.Lab;
import de.ipk.ag_ba.image.operations.complex_hull.Point;

public class ColorValues {
	public Point center;
	Lab labAvg;
	Lab rgbAvg;
	Lab hsvAvg;
	public String name;
	
	public ColorValues(Point center) {
		this.center = center;
	}
	
	public Lab getLabAvg() {
		return labAvg;
	}
	
	public void setLabAvg(Lab labAvg) {
		this.labAvg = labAvg;
	}
	
	public Lab getRgbAvg() {
		return rgbAvg;
	}
	
	public void setRgbAvg(Lab rgbAvg) {
		this.rgbAvg = rgbAvg;
	}
	
	public Lab getHsvAvg() {
		return hsvAvg;
	}
	
	public void setHsvAvg(Lab hsvAvg) {
		this.hsvAvg = hsvAvg;
	}
	
}
