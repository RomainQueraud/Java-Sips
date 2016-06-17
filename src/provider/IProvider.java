package provider;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;

import com.opencsv.CSVWriter;

import datas.Configuration;

public interface IProvider {
	public void openFirefox();
	public void closeFirefox();
	public void loadWebpage();
	public void setCrawlSpeed(double crawlSpeed);
	
	public void crawlAndFillConfigurations() throws InterruptedException;
	
	public void addConfiguration(Configuration configuration);
	public Bag toBag(Model model);
	public void writeConfigurationInCsv(CSVWriter writer);
	public ArrayList<String[]> getConfigurationsLines();
}
