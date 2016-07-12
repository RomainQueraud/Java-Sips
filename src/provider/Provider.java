package provider;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import datas.Configuration;
import datas.Currency;
import datas.Offset;
import datas.URI;

/**
 * Abstract class that needs to implements the basics for every providers
 */
public abstract class Provider implements IProvider {
	/**
	 * Will be used by both URI, csv and FrontEnd to identify the provider.
	 */
	public String name = "Provider";
	/**
	 * No longer used, except for some providers
	 */
	public boolean crawl = false;
	/**
	 * Specify which currency is used by the provider, it will automatically convert to USD
	 */
	public Currency currency;
	/**
	 * Once filled, will be written in csv and/or converted into rdf format
	 */
	ArrayList<Configuration> configurations = new ArrayList<Configuration>();
	/**
	 * List of URIs 
	 */
	ArrayList<String> continents = new ArrayList<String>();
	/**
	 * minimum time required to purchase the service (usually URI.month)
	 */
	String billing = URI.nothing; 
	
	/**
	 * from Selenium
	 */
	WebDriver driver;
	
	/**
	 * No longer used
	 */
	Offset offset = new Offset(0,0,0,0); //step, cpu, ram, disk, transfer

	/**
	 * Used by the crawler to go on to the pricing page
	 */
	String baseUrl = "";
	
	/**
	 * No longer used
	 */
	int maxOffset = 0;
	
	/***********Selenium**************/
	/**
	 * Open Firefox
	 */
	public void openFirefox(){
		System.out.print("Opening Firefox for "+this.name+"...");
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
	
	public double extractNumber(String text) throws Exception{
		Pattern p = Pattern.compile("\\d+((\\.|\\,)\\d+)?");
		Matcher m = p.matcher(text);
		if(m.find()){
			return Double.parseDouble(m.group().replace(',', '.'));
		}
		else{
			//System.out.println("No number found on text : "+text);
			return 0;
		}
	}
	/*********************************/
	
	/***********Jena*****************/
	public void addConfiguration(Configuration configuration){
		configuration.setProvider(this);
		this.configurations.add(configuration);
	}
	
	/**
	 * @Deprecated Use the toResource function instead.
	 */
	@Deprecated public Bag toBag(Model model){
		Bag providerBag = model.createBag();
		System.out.print(this.name+" to bag : ("+this.configurations.size()+") ");
		for(int i=0 ; i<this.configurations.size() ; i++){
			if(i%1000 == 0){
				System.out.print("."); //print one dot every thousand configs
			}
			providerBag.add(this.configurations.get(i).toResource(model));
		}
		System.out.println("");
		return providerBag;
	}
	
	/**
	 * 
	 * @return Resource for provider that also contains the configurations resource	
	 * @throws Exception
	 */
	public Resource toResource(Model model) throws Exception{
		Resource providerResource = model.createResource(URI.baseURI+this.name+"/");
		System.out.print(this.name+" to resource : ("+this.configurations.size()+") ");
		for(int i=0 ; i<this.configurations.size() ; i++){
			if(i%1000 == 0){
				System.out.print("."); //print one dot every thousand configs
			}
			providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "config"),this.configurations.get(i).toResource(model));
		}
		System.out.println("");

		if(this.continents.size()!=0){
			for(int i=0 ; i<this.continents.size() ; i++){
				providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "continent"), model.createResource(this.continents.get(i)));
			}
		}
		else{
			providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "continent"), "");
		}
		
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "billing"), model.createResource(this.billing));
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "billingDuration"), ""+URI.getBillingDuration(this.billing));
		
		return providerResource;
	}
	
	public void loadConfigurationsFromCsv() throws IOException{
		//ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		//InputStream is = classloader.getResourceAsStream(this.name+".csv");
		CSVReader reader = new CSVReader(new FileReader("csv/"+this.name+".csv"));
	    List<String[]> myEntries = reader.readAll();
	    
	    for(String[] line : myEntries){
	    	if(!line[0].equals("Provider")){ //First line are the titles
		    	Configuration configuration = new Configuration(line);
		    	this.addConfiguration(configuration);
	    	}
	    }
	    
	    reader.close();
	}
	
	public void writeConfigurationsInCsv() throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter("csv/"+this.name+".csv"), ',');
		for(Configuration configuration:this.configurations){
			String[] line = configuration.getLine();
	    	writer.writeNext(line);
		}
		writer.close();
	}
	
	public ArrayList<String[]> getConfigurationsLines(){
		ArrayList<String[]> lines = new ArrayList<String[]>();
		for(Configuration configuration : this.configurations){
			lines.add(configuration.getLine());
		}
		return lines;
	}
	
	@Override
	public String toString(){
		return this.name;
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
