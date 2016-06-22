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
	
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception;
	public double extractNumber(String text) throws Exception;
	
	public void addConfiguration(Configuration configuration);
	public Bag toBag(Model model);
	public void writeConfigurationsInCsv() throws IOException;
	public ArrayList<String[]> getConfigurationsLines();
}
