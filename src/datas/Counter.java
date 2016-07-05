package datas;

/**
 * Class used to make sure that every configuration is unique by giving it a unique id
 * This id can change from run to run 
 */
public class Counter {
	private static int configurationCounter = 0;
	public static int getConfigurationCounter(){
		int ret = Counter.configurationCounter;
		Counter.configurationCounter++;
		return ret;
	}
}
