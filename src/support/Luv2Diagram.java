package support;

import java.awt.Color;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import de.ipk.ag_ba.gui.picture_gui.StreamBackgroundTaskHelper;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operation.canvas.TextJustification;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Draws a Luv u-v diagram with axes and highlights points/lines of interest.
 * 
 * @author klukas
 */
public class Luv2Diagram {
	private int w, h;
	private String title;
	private Color background = Color.LIGHT_GRAY;
	private Color titleTextColor = Color.BLACK;
	private int titleTextFontSize = 20;
	
	private Color axisTextColor = Color.BLACK;
	private int axisTextFontSize = 15;
	
	private int textInset = 5;
	private int inset = 50;
	private Color axisColor = Color.BLACK;
	private int axisThickness = 1;
	private int axisGridPointLineLength = 5;
	
	private int minA = -200;
	private int maxA = 200;
	private int stepA = 10;
	
	private int minB = -200;
	private int maxB = 200;
	private int stepB = 10;
	private int invalidColor = Color.WHITE.getRGB();
	private double baseLabL = 75;
	
	public Luv2Diagram(int w, int h, String title) {
		this.w = w;
		this.h = h;
		this.title = title;
	}
	
	public Image getImage() {
		ImageCanvas ic = new Image(w, h, new int[w * h]).io().canvas();
		
		ic = ic.fillRect(0, 0, w, h, getBackground().getRGB(), 0);
		ic = ic.text(w / 2, getTextInset() + getTitleTextFontSize(), title + " (L*=" + (int) getBaseLabL() + ")", getTitleTextColor(), getTitleTextFontSize(),
				TextJustification.CENTER);
		
		{ // draw colored background
			int dw = getDiagramAreaWidth();
			int dh = getDiagramAreaHeight();
			int offX = getDiagramAreaX();
			int offY = getDiagramAreaY();
			
			ic.fillRect(offX, offY, dw, dh, invalidColor);
			
			double labL = getBaseLabL();
			
			final ImageCanvas icf = ic;
			IntConsumer c = new IntConsumer() {
				@Override
				public void accept(int y) {
					double b = getMinB() + y / (double) dh * (getMaxB() - getMinB());
					for (int x = 0; x < dw; x++) {
						double L = labL;
						double a = getMinA() + x / (double) dw * (getMaxA() - getMinA());
						
						double l = L;
						double u = a / 100d;
						double v = b / 100d;
						
						ColorLuv luv = new ColorLuv(l, u, v);
						
						icf.fillRect(x + offX, (dh - y) + offY, 1, 1, luv.getRGB(invalidColor));
					}
				}
			};
			new StreamBackgroundTaskHelper<Integer>("Fill Luv background").process(IntStream.range(0, dh), c, null);
		}
		
		ic = ic.drawRectangle(getDiagramAreaX(), getDiagramAreaY(), getDiagramAreaWidth(), getDiagramAreaHeight(), getAxisColor(), getAxisThickness());
		
		// X-Axis grid point lines
		for (int a = getMinA(); a < getMaxA(); a += getStepA()) {
			int offX = (int) (getDiagramAreaWidth() * (a - getMinA()) / (double) (getMaxA() - getMinA()));
			ic = ic.drawLine(
					getDiagramAreaX() + offX, getDiagramAreaY() + getDiagramAreaHeight() - getAxisGridPointLineLength(),
					getDiagramAreaX() + offX, getDiagramAreaY() + getDiagramAreaHeight(),
					getAxisColor().getRGB(), 0, getAxisThicknessForSteps());
			if (a > getMinA())
				ic = ic.text(getDiagramAreaX() + offX, getDiagramAreaY() + getDiagramAreaHeight() + getAxisTextFontSize() + 2 * getTextInset(),
						a / 100d + "", getAxisTextColor(), getAxisTextFontSize(), TextJustification.CENTER);
		}
		
		// Y-Axis grid point lines
		for (int b = getMinB(); b < getMaxB(); b += getStepB()) {
			int offY = getDiagramAreaHeight() - (int) (getDiagramAreaHeight() * (b - getMinB()) / (double) (getMaxB() - getMinB()));
			ic = ic.drawLine(
					getDiagramAreaX(), getDiagramAreaY() + offY,
					getDiagramAreaX() + getAxisGridPointLineLength(), getDiagramAreaY() + offY,
					getAxisColor().getRGB(), 0, getAxisThicknessForSteps());
			if (b > getMinB())
				ic = ic.text(getDiagramAreaX() - getTextInset(), getDiagramAreaY() + getAxisTextFontSize() / 2 + 2 + offY,
						b / 100d + "", getAxisTextColor(), getAxisTextFontSize(), TextJustification.RIGHT);
		}
		
		return ic.getImage();
	}
	
	private int getAxisThicknessForSteps() {
		return getAxisThickness();
	}
	
	public void showDiagram(String windowTitle) {
		getImage().show(windowTitle);
	}
	
	private int getDiagramAreaX() {
		return getInset();
	}
	
	private int getDiagramAreaY() {
		return 2 * getTextInset() + getTitleTextFontSize();
	}
	
	private int getDiagramAreaWidth() {
		return w - 2 * getDiagramAreaX();
	}
	
	private int getDiagramAreaHeight() {
		return h - getDiagramAreaY() - getDiagramAreaX();
	}
	
	public Color getBackground() {
		return background;
	}
	
	public void setBackground(Color background) {
		this.background = background;
	}
	
	public Color getTitleTextColor() {
		return titleTextColor;
	}
	
	public void setTitleTextColor(Color titleTextColor) {
		this.titleTextColor = titleTextColor;
	}
	
	public int getTitleTextFontSize() {
		return titleTextFontSize;
	}
	
	public void setTitleTextFontSize(int titleTextFontSize) {
		this.titleTextFontSize = titleTextFontSize;
	}
	
	public int getTextInset() {
		return textInset;
	}
	
	public void setTextInset(int textInset) {
		this.textInset = textInset;
	}
	
	public int getInset() {
		return inset;
	}
	
	public Color getAxisColor() {
		return axisColor;
	}
	
	public void setAxisColor(Color axisColor) {
		this.axisColor = axisColor;
	}
	
	public int getAxisThickness() {
		return axisThickness;
	}
	
	public void setAxisThickness(int axisThickness) {
		this.axisThickness = axisThickness;
	}
	
	public int getAxisGridPointLineLength() {
		return axisGridPointLineLength;
	}
	
	public void setAxisGridPointLineLength(int axisGridPointLineLength) {
		this.axisGridPointLineLength = axisGridPointLineLength;
	}
	
	public int getMinA() {
		return minA;
	}
	
	public void setMinA(int minA) {
		this.minA = minA;
	}
	
	public int getMaxA() {
		return maxA;
	}
	
	public void setMaxA(int maxA) {
		this.maxA = maxA;
	}
	
	public int getStepA() {
		return stepA;
	}
	
	public void setStepA(int stepA) {
		this.stepA = stepA;
	}
	
	public int getMinB() {
		return minB;
	}
	
	public void setMinB(int minB) {
		this.minB = minB;
	}
	
	public int getMaxB() {
		return maxB;
	}
	
	public void setMaxB(int maxB) {
		this.maxB = maxB;
	}
	
	public int getStepB() {
		return stepB;
	}
	
	public void setStepB(int stepB) {
		this.stepB = stepB;
	}
	
	public Color getAxisTextColor() {
		return axisTextColor;
	}
	
	public void setAxisTextColor(Color axisTextColor) {
		this.axisTextColor = axisTextColor;
	}
	
	public int getAxisTextFontSize() {
		return axisTextFontSize;
	}
	
	public void setAxisTextFontSize(int axisTextFontSize) {
		this.axisTextFontSize = axisTextFontSize;
	}
	
	public double getBaseLabL() {
		return baseLabL;
	}
	
	public void setBaseLabL(double baseLabL) {
		this.baseLabL = baseLabL;
	}
}
