package support;

/**
 * 
 * @author Jean-Michel Pape
 *
 */
public class HistogramComparision {

	int[] hist_a; 
	int[] hist_b;
	
	public HistogramComparision(int[] hist_a, int[] hist_b) {
		this.hist_a = hist_a;
		this.hist_b = hist_b;
	}

	/*
	 * Chi-squared distance X^2
	 * X^2(P,Q) = 1/2 * Sum((P_i - Q_i)^2 / (P_i + Q_i)^2)
	 */
	public double chiSquare() {
		double x_square = 0.0;
		
		for (int i = 0; i < hist_a.length; i++) {
			int a = (hist_a[i] - hist_b[i]) * (hist_a[i] - hist_b[i]);
			int b = (hist_a[i] + hist_b[i]);
			x_square += a / b;
		}
		return 1/2 * x_square;
	}
	
	/*
	 * quadratic form distance (mahalanobis distance)
	 * QF(P,Q) = sqrt((P - Q)^T A (P - Q))
	 * A = inverse(covariance matrix)
	*/
	public double quadratic() {
		return 0.0;
	}
}
