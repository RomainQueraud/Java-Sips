package datas;

public class Counter {
	private static int configurationCounter = 0;
	public static int getConfigurationCounter(){
		int ret = Counter.configurationCounter;
		Counter.configurationCounter++;
		return ret;
	}
}
