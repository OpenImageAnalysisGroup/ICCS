package correction;

import java.awt.Color;
import java.lang.reflect.Field;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import colors.Color_Transformer_2;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.structures.Image;
import support.HelperMethods;

public class ColorCorrection {
	
	ColorValues[] samples;
	ControlColorCheckerFields[] controlValues;
	private SimpleRegression regressionL;
	private SimpleRegression regressionA;
	private SimpleRegression regressionB;
	private PolynomialFunction poly1;
	private PolynomialFunction poly2;
	private PolynomialFunction poly3;
	
	public ColorCorrection(ColorValues[] samples, ControlColorCheckerFields[] realValues) {
		this.samples = samples;
		this.controlValues = realValues;
	}
	
	public Image correctImage(Image inp, ColorModes mode, boolean regTypePoly, int degree) {
		calculateTransformation(regTypePoly, mode, degree);
		Image corrected;
		
		if (!regTypePoly)
			corrected = correctImage(regressionL, regressionA, regressionB, inp);
		else
			corrected = correctImage(poly1, poly2, poly3, inp);
		return corrected;
	}
	
	private void calculateTransformation(boolean regTypePoly, ColorModes mode, int degree) {
		
		Vector2D[] samplesL = null;
		Vector2D[] samplesA = null;
		Vector2D[] samplesB = null;
		
		try {
			samplesL = getSamples(ColorModes.LAB_L);
			samplesA = getSamples(ColorModes.LAB_A);
			samplesB = getSamples(ColorModes.LAB_B);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!regTypePoly) {
			regressionL = getSimpleRegression(samplesL);
			regressionA = getSimpleRegression(samplesA);
			regressionB = getSimpleRegression(samplesB);
			
		} else {
			poly1 = getPolyRegression(samplesL, degree);
			poly2 = getPolyRegression(samplesA, degree);
			poly3 = getPolyRegression(samplesB, degree);
		}
	}
	
	/**
	 * Common approach for color balancing using the white patch.
	 * @param inp - input image
	 * @return corrected image
	 */
	public Image correctWBClassicRGB(Image inp) {
		
		double red_f = controlValues[18].RGB[0] / samples[18].rgbAvg.getAverageL();
		double green_f = controlValues[18].RGB[1] / samples[18].rgbAvg.getAverageA();
		double blue_f = controlValues[18].RGB[2] / samples[18].rgbAvg.getAverageB();
		
		System.out.println("WB: red_f = " + controlValues[18].RGB[0] + "  " + samples[18].rgbAvg.getAverageL());
		
		int[] inp1d = inp.getAs1A();
		int[] res = new int[inp1d.length];
		
		for (int x = 0; x < inp1d.length; x++) {
			int c = inp1d[x];
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = c & 0x0000ff;
			
			int corr_r = (int) (r * red_f);
			int corr_g = (int) (g * green_f);
			int corr_b = (int) (b * blue_f);
			
			corr_r = corr_r > 254 ? 254 : corr_r;
			corr_g = corr_g > 254 ? 254 : corr_g;
			corr_b = corr_b > 254 ? 254 : corr_b;
			
			corr_r = corr_r < 0 ? 0 : corr_r;
			corr_g = corr_g < 0 ? 0 : corr_g;
			corr_b = corr_b < 0 ? 0 : corr_b;
			
			res[x] = new Color(corr_r, corr_g, corr_b).getRGB();
		}
		
		return new Image(inp.getWidth(), inp.getHeight(), res);
	}
	
	public Image correctWBVanKries(Image inp) {
		
		int[] inp1d = inp.getAs1A();
		int[] res = new int[inp1d.length];
		ColorSpaceConverter csc = new ColorSpaceConverter();
		
		int[] controlwhite = new int[] {(int) controlValues[18].RGB[0], (int) controlValues[18].RGB[1], (int) controlValues[18].RGB[2]};
		double[] xyzControlWhite = csc.RGBtoXYZ(controlwhite);
		double[] lmsControlWhite = HelperMethods.XYZtoLMS(xyzControlWhite);
		
		for (int x = 0; x < inp1d.length; x++) {
			int c = inp1d[x];
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = c & 0x0000ff;
			
			double[] xyz_sampled = csc.RGBtoXYZ(new int[] {r, g, b});
			double[] lms_sampled = HelperMethods.XYZtoLMS(xyz_sampled);
			
			double l = (255.0 / lmsControlWhite[0]) * lms_sampled[0];
			double m = (255.0 / lmsControlWhite[1]) * lms_sampled[1];
			double s = (255.0 / lmsControlWhite[2]) * lms_sampled[2];
			
			double[] xyz_corr = HelperMethods.LMStoXYZ(new double[] {l, m, s});
			int[] rgb_corr = csc.XYZtoRGB(xyz_corr);
			
			int corr_r = (int) rgb_corr[0];
			int corr_g = (int) rgb_corr[1];
			int corr_b = (int) rgb_corr[2];
			
			corr_r = corr_r > 254 ? 254 : corr_r;
			corr_g = corr_g > 254 ? 254 : corr_g;
			corr_b = corr_b > 254 ? 254 : corr_b;
			
			corr_r = corr_r < 0 ? 0 : corr_r;
			corr_g = corr_g < 0 ? 0 : corr_g;
			corr_b = corr_b < 0 ? 0 : corr_b;
			
			res[x] = new Color(corr_r, corr_g, corr_b).getRGB();
		}
		
		return new Image(inp.getWidth(), inp.getHeight(), res);
	}
	
	public void testLMS() {
		
		int[] rgb = new int[] {100, 90, 130};
		
		ColorSpaceConverter csc = new ColorSpaceConverter();
		
		double[] xyz = csc.RGBtoXYZ(rgb);
		double[] lms = HelperMethods.XYZtoLMS(xyz);
		double[] xyz_back = HelperMethods.LMStoXYZ(lms);
		int[] rgb_back = csc.XYZtoRGB(xyz_back);
		
		System.out.println("in: " + xyz[0] + "|" + xyz[1] + "|" + xyz[2] + " out: " + xyz_back[0] + "|" + xyz_back[1] + "|" + + xyz_back[2]);
		System.out.println("in: " + rgb[0] + "|" + rgb[1] + "|" + rgb[2] + " out: " + rgb_back[0] + "|" + rgb_back[1] + "|" + + rgb_back[2]);
	}
	
	private static Image correctImage(SimpleRegression regressionL, SimpleRegression regressionA, SimpleRegression regressionB, Image inp) {
		int[] img = inp.getAs1A();
		int[] corr = new int[inp.getWidth() * inp.getHeight()];
		Color_CIE_Lab lab;
		
		for (int x = 0; x < img.length; x++) {
			int pix = img[x];
			
			lab = new Color_CIE_Lab(pix);
			
			double predL = regressionL.predict(lab.getL());
			double predA = regressionA.predict(lab.getA());
			double predB = regressionB.predict(lab.getB());
			
			Color crgb = new Color(new Color_CIE_Lab(predL, predA, predB).getRGB());
			corr[x] = crgb.getRGB();
		}
		return new Image(inp.getWidth(), inp.getHeight(), corr);
	}
	
	private SimpleRegression getSimpleRegression(Vector2D[] samples) {
		SimpleRegression reg = new SimpleRegression();
		for (Vector2D s : samples) {
			reg.addData(s.getX(), s.getY());
		}
		return reg;
	}
	
	private static Image correctImage(PolynomialFunction regressionL, PolynomialFunction regressionA, PolynomialFunction regressionB, Image inp) {
		int[] img = inp.getAs1A();
		int[] corr = new int[inp.getWidth() * inp.getHeight()];
		Color_CIE_Lab lab;
		
		for (int x = 0; x < img.length; x++) {
			int pix = img[x];
			
			lab = new Color_CIE_Lab(pix);
			
			double predL = regressionL.value(lab.getL());
			double predA = regressionA.value(lab.getA());
			double predB = regressionB.value(lab.getB());
			
			Color crgb = new Color(new Color_CIE_Lab(predL, predA, predB).getRGB());
			corr[x] = crgb.getRGB();
		}
		return new Image(inp.getWidth(), inp.getHeight(), corr);
	}
	
	private PolynomialFunction getPolyRegression(Vector2D[] samples, int degree) {
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		for (Vector2D s : samples) {
			obs.add(s.getX(), s.getY());
		}
		
		return new PolynomialFunction(fitter.fit(obs.toList()));
	}
	
	private Vector2D[] getSamples(ColorModes mode) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Vector2D[] res = new Vector2D[samples.length];
		String cmode = mode.name();
		String colorSpaceName = cmode.substring(0, 3);
		String channelName = cmode.substring(cmode.length() - 1);
		
		int positionOfMatch = -1;
		for (int i = -1; (i = colorSpaceName.indexOf(channelName, i + 1)) != -1;) {
			positionOfMatch = i;
			if (i == colorSpaceName.length())
				break;
		}
		
		for (int idx = 0; idx < controlValues.length; idx++) {
			Field fieldControl = ControlColorCheckerFields.class.getDeclaredField(colorSpaceName);
			Field fieldName = ControlColorCheckerFields.class.getDeclaredField("name");
			double[] valueControl = (double[]) fieldControl.get(controlValues[idx]);
			// Field fieldM = RealColorCheckerValues.class.getDeclaredField(channel);
			// double valueM = (double) fieldM.get(samples[idx]);
			double valueM = 0.0;
			if (channelName.contains("L"))
				valueM = samples[idx].getLabAvg().getAverageL();
			if (channelName.contains("A"))
				valueM = samples[idx].getLabAvg().getAverageA();
			if (channelName.contains("B"))
				valueM = samples[idx].getLabAvg().getAverageB();
			System.out.println(cmode + "	sampled: " + valueM + "	control: " + valueControl[positionOfMatch] + "	name: " + fieldName.get(controlValues[idx]));
			res[idx] = new Vector2D(valueControl[positionOfMatch], valueM);
		}
		return res;
	}
	
	public enum ColorModes {
		LAB_L, LAB_A, LAB_B, RGB_R, RGB_G, RGB_B, HSV_H, HSV_S, HSV_V, XYZ_X, XYZ_Y, XYZ_Z
	}
}
