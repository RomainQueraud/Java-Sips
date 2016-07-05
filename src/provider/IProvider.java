package provider;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;

import datas.Configuration;

public interface IProvider {
	public void openFirefox();
	public void closeFirefox();
	public void loadWebpage();
	
	/**
	 * Will extract informations from the provider website.
	 * Then it will append eveything to the configurations ArrayList.
	 * Then it will write everything to the csv file.
	 */
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception;
	
	/**
	 * 
	 * @param text from the WebElement.getText()
	 * @return the first double found in the String
	 * @throws Exception
	 */
	public double extractNumber(String text) throws Exception;
	
	public void addConfiguration(Configuration configuration);
	public Bag toBag(Model model);
	public void writeConfigurationsInCsv() throws IOException;
	public ArrayList<String[]> getConfigurationsLines();
}
