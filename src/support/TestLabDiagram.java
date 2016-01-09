package support;

import java.util.TreeMap;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import de.ipk.ag_ba.gui.picture_gui.StreamBackgroundTaskHelper;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

public class TestLabDiagram {
	
	public static void main(String[] args) {
		ImageStack is = new ImageStack();
		TreeMap<Integer, Image> l2i = new TreeMap<>();
		new StreamBackgroundTaskHelper<Integer>("L").process(IntStream.range(0, 130), new IntConsumer() {
			@Override
			public void accept(int value) {
				int L = value;
				LabDiagram ld = new LabDiagram(1200, 600, "Test ABC");
				ld.setBaseLabL(L);
				Image i = ld.getImage();
				i.setFilename("L=" + L);
				synchronized (l2i) {
					l2i.put(L, i);
					System.out.println("Finished L=" + L);
				}
			}
		}, null);
		
		for (Image i : l2i.values())
			is.addImage(i.getFileName(), i);
		
		is.show("Test the diagram function...");
	}
	
}
