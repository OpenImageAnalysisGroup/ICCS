package detection;

public enum CCColorNames {
	Darkskin, Lightskin, Bluesky, Foliage, Blueflower, Bluishgreen, Orange, Purplishblue, Moderatered, Purple, Yellowgreen, Orangeyellow, Blue, Green, Red, Yellow, Magenta, Cyan, White, Neutral8, Neutral6_5, Neutral5, Neutral3_5, Black;

	public static String[] getNames() {
		CCColorNames vals[] = CCColorNames.values();
		String names[] = new String[CCColorNames.values().length];
		
		int idx=0;
		for(CCColorNames val : vals)
			names[idx] = vals[idx++].name();
		
		return names;
	}
}
