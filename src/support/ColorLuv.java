package support;

import org.SystemOptions;
import org.color.ColorXYZ;

import de.ipk.ag_ba.image.operation.ColorSpaceConverter;

/**
 * @author klukas
 */
public class ColorLuv {
	private double L;
	private double u;
	private double v;
	
	final static ColorSpaceConverter convert = new ColorSpaceConverter(SystemOptions.getInstance().getStringRadioSelection("IAP",
			"Color Management//White Point",
			ColorSpaceConverter.getWhitePointList(), ColorSpaceConverter.getDefaultWhitePoint(), true));
	
	public ColorLuv(double l, double u, double v) {
		this.L = l;
		this.u = u;
		this.v = v;
	}
	
	public int getRGB(int errorColorValue) {
		ColorXYZ xyz = getXYZ();
		return xyz.getColorRGB(errorColorValue);
	}
	
	private ColorXYZ getXYZ() {
		// http://brucelindbloom.com/index.html?Eqn_XYZ_to_Lab.html
		
		// white point
		double Xr = convert.whitePoint[0];
		double Yr = convert.whitePoint[1];
		double Zr = convert.whitePoint[2];
		
		double uo = 4d * Xr / (Xr + 15 * Yr + 3 * Zr);
		double vo = 9d * Yr / (Xr + 15 * Yr + 3 * Zr);
		
		double ep = 216d / 24389d;
		double k = 24389d / 27d;
		
		double a = 1d / 3d * (52d * L / (u + 13d * L * uo) - 1d);
		
		double Y;
		if (L > k * ep)
			Y = (L + 16d) / 116d * (L + 16d) / 116d * (L + 16d) / 116d;
		else
			Y = L / k;
		
		double d = Y * (39d * L / (v + 13d * L * vo) - 5d);
		
		double b = -5d * Y;
		double c = -1d / 3d;
		double X = (d - b) / (a - c);
		double Z = X * a + b;
		return new ColorXYZ(X * 100d, Y * 100d, Z * 100d);
	}
}
