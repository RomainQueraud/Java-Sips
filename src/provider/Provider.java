package provider;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.opencsv.CSVWriter;

import datas.Configuration;
import datas.Offset;
import datas.URI;

public abstract class Provider implements IProvider {
	public String name = "Provider";
	public boolean crawl = false;
	ArrayList<Configuration> configurations = new ArrayList<Configuration>();
	
	WebDriver driver;
	Offset offset = new Offset(0,0,0,0); //step, cpu, ram, disk, transfer

	String baseUrl = "";
	int maxOffset = 0;
	
	/***********Selenium**************/
	/* Open Firefox*/
	public void openFirefox(){
		System.out.print("Opening Firefox...");
		driver = new FirefoxDriver();
		System.out.println("Ok");
	}
	
	public void closeFirefox(){
		driver.close();
	}
	
	public void loadWebpage(){
		System.out.print("Loading Webpage...");
		driver.get(baseUrl);
		System.out.println("Ok");
	}
	
	/*
	 * if close to 1, speed is maximum
	 * if close to 0, speed is minimum
	 */
	public void setCrawlSpeed(double crawlSpeed){
		//TODO if crawlSpeed is too close to 0, there will be to many configurations, so we need to warn the user
		if(crawlSpeed<=0){
			crawlSpeed = 0.01;
		}
		if(crawlSpeed>=1){
			crawlSpeed = 1;
		}
		int step = (int) (this.maxOffset*crawlSpeed);
		this.offset.setStep(step);
	}
	/*********************************/
	
	/***********Jena*****************/
	public void addConfiguration(Configuration configuration){
		configuration.setProvider(this);
		this.configurations.add(configuration);
	}
	
	public Bag toBag(Model model){
		Bag providerBag = model.createBag();
		for(int i=0 ; i<this.configurations.size() ; i++){
			providerBag.add(this.configurations.get(i).toResource(model));
		}
		return providerBag;
	}
	
	public void writeConfigurationInCsv(CSVWriter writer){
		for(Configuration configuration:this.configurations){
			String[] line = configuration.getLine();
	    	writer.writeNext(line);
		}
	}
	
	public ArrayList<String[]> getConfigurationsLines(){
		ArrayList<String[]> lines = new ArrayList<String[]>();
		for(Configuration configuration : this.configurations){
			lines.add(configuration.getLine());
		}
		return lines;
	}
	/********************************/
	
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		Provider provider = Atlantic.singleton;
		
		Configuration configuration = new Configuration("S server Linux", 1, 1, 40, -1, 3000, URI.linux, URI.euro, "", "", 9.93);
		provider.addConfiguration(configuration);
		
		Configuration configuration2 = new Configuration("fake server", 1, 1, 50, 3, 2000, URI.windows, URI.euro, "", "", 14);
		provider.addConfiguration(configuration2);
		
		@SuppressWarnings("unused")
		Bag bag = provider.toBag(model);
		model.write(System.out);
	}
}
