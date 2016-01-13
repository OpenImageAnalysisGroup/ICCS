package correction;

import java.util.LinkedList;

import de.ipk.ag_ba.image.operations.complex_hull.Point;

public class ColorValues {
	public Point center;
	LinkedList<ColorICCS> avgList = new LinkedList<>();
	public String name;
	
	public ColorValues(Point center) {
		this.center = center;
	}
	
	public void setAvgColor(ColorICCS c) {
		
	}
	
	public ColorICCS getAvgColor(ColorSpaces colorspace) {
		ColorICCS res = null;
		for(ColorICCS color : avgList) {
			if(color.colorSpace == colorspace.name())
				res = color;	
		}
		return res;
	}

	public void setAvgColor(ColorSpaces cs, ColorICCS colorICCS) {
		if(colorICCS.colorSpace == null)
			colorICCS.colorSpace = cs.name();
		avgList.add(colorICCS);
	}
}
