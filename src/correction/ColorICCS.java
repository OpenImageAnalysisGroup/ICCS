package correction;

public class ColorICCS {

	double[] values;
	String colorname;
	String colorSpace;
	
	public ColorICCS(String name, double[] values) {
		this.colorname = name;
		this.values = values;
	}
	
	public ColorICCS(String colorSpace, String name, double[] values) {
		this.colorSpace = colorSpace;
		this.colorname = name;
		this.values = values;
	}
	
	public ColorICCS(String colorSpace, String name, double a, double b, double c) {
		this.colorSpace = colorSpace;
		this.colorname = name;
		this.values = new double[] {a, b, c};
	}

	public ColorICCS(double a, double b, double c) {
		this.values = new double[] {a, b, c};
	}
	
	public double getA() {
		return values[0];
	}
	
	public double getB() {
		return values[1];
	}
	
	public double getC() {
		return values[2];
	}
}
