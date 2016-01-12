package correction;

public class ControlColorCheckerFields {
	String name;
	double[] LAB;
	double[] RGB;
	double[] XYZ;
	double[] HSV;
	double[] XYY;
	
	public ControlColorCheckerFields(String name, double l, double a, double b,
			double c, double h, int r, int g, int b2, int aR, int aG, int aB) {
		super();
		this.name = name;
	}
	
	public ControlColorCheckerFields() {
	}
	
	public void setField(String spaceName, String fieldName, double[] vals) {		
		this.name = fieldName;
		ControlColorCheckerSpaces space = ControlColorCheckerSpaces.valueOf(spaceName);
		
		switch (space) {
		case HSV:
			this.HSV = vals;
			break;
		case LAB:
			this.LAB = vals;
			break;
		case RGB:
			this.RGB = vals;
			break;
		case XYY:
			this.XYY = vals;
			break;
		case XYZ:
			this.XYZ = vals;
			break;
		default:
			break;
		
		}
	}
	
	private enum ControlColorCheckerSpaces {
		LAB, RGB, XYZ, HSV, XYY;
	}
}
