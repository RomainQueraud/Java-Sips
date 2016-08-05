package provider;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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
import main.SipsRdf;

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
	 * Complementary information
	 */
	boolean freeTrial = false;
	/**
	 * Is a number provider for, at least, configuration help. In the contact page for example.
	 */
	boolean phoneSupport = false;
	boolean multipleIp = false;
	/**
	 * Does the provider provides a monitoring for his cloud from his website
	 */
	boolean webAccess = false;
	/**
	 * Do resources increase automatically
	 */
	boolean burstResource = false;
	/**
	 * Accessing the CPU speed, not only the CPU number
	 */
	boolean customizableCpu = false;
	/**
	 * Do the provider says that he has an api
	 */
	boolean api = false;
	/**
	 * Basically, using sliders
	 */
	boolean customizableConfiguration = false;
	/**
	 * The provider provide something to save VM datas
	 */
	boolean backup = false;
	boolean payAsYouGo = false;
	boolean prepaid = false;
	/**
	 * Multiple users per account, as for amazon
	 */
	boolean multipleUsers = false;
	/**
	 * Does the provider gives details about its security scheme
	 */
	boolean detailledSecurity = false;
	boolean terminalAccess = false;
	/**
	 * Does the provider provide a 100% uptime guarantee on every plans
	 */
	boolean uptimeGuarantee = false;
	/**
	 * Does the provider provide a real computer for a whole use by the user. Physical server, not virtual
	 */
	boolean dedicatedServer = false;
	boolean paypal = false;
	/**
	 * 24/7 support
	 */
	boolean alwaysSupport = false;
	boolean environment = false;
	
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
		System.gc();
	}
	
	public void loadWebpage(){
		System.out.print("Loading Webpage...");
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES); //10 minutes Timeout
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
		
		if(this.configurations.size()>=1){
			providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "date"), ""+this.configurations.get(0).date);
		}
		else{
			providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "date"), "");
		}
		
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "billing"), model.createResource(this.billing));
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "billingDuration"), ""+URI.getBillingDuration(this.billing));
		
		String freeTrial = this.freeTrial ? URI.freeTrial : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "freeTrial"), ""+freeTrial);
		String phoneSupport = this.phoneSupport ? URI.phoneSupport : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "phoneSupport"), ""+phoneSupport);
		String multipleIp = this.multipleIp ? URI.multipleIp : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "multipleIp"), ""+multipleIp);
		String webAccess = this.webAccess ? URI.webAccess : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "webAccess"), ""+webAccess);
		String burstResource = this.burstResource ? URI.burstResource : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "burstResource"), ""+burstResource);
		String customizableCpu = this.customizableCpu ? URI.customizableCpu : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "customizableCpu"), ""+customizableCpu);
		String api = this.api ? URI.api : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "api"), ""+api);
		String customizableConfiguration = this.customizableConfiguration ? URI.customizableConfiguration : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "customizableConfiguration"), ""+customizableConfiguration);
		String backup = this.backup ? URI.backup : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "backup"), ""+backup);
		String payAsYouGo = this.payAsYouGo ? URI.payAsYouGo : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "payAsYouGo"), ""+payAsYouGo);
		String prepaid = this.prepaid ? URI.prepaid : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "prepaid"), ""+prepaid);
		String multipleUsers = this.multipleUsers ? URI.multipleUsers : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "multipleUsers"), ""+multipleUsers);
		String detailledSecurity = this.detailledSecurity ? URI.detailledSecurity : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "detailledSecurity"), ""+detailledSecurity);
		String terminalAccess = this.terminalAccess ? URI.terminalAccess : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "terminalAccess"), ""+terminalAccess);
		String uptimeGuarantee = this.uptimeGuarantee ? URI.uptimeGuarantee : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "uptimeGuarantee"), ""+uptimeGuarantee);
		String dedicatedServer = this.dedicatedServer ? URI.dedicatedServer : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "dedicatedServer"), ""+dedicatedServer);
		String paypal = this.paypal ? URI.paypal : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "paypal"), ""+paypal);
		String alwaysSupport = this.alwaysSupport ? URI.alwaysSupport : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "alwaysSupport"), ""+alwaysSupport);
		String environment = this.environment ? URI.environment : "";
		providerResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "environment"), ""+environment);
		
		return providerResource;
	}
	
	public void loadConfigurationsFromCsv() throws IOException{
		//ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		//InputStream is = classloader.getResourceAsStream(this.name+".csv");
		this.configurations.clear();
		CSVReader reader = null;
		try{
			reader = new CSVReader(new FileReader(SipsRdf.dir+"/csv/"+this.name+".csv"));
		}
		catch(Exception e){
			reader = new CSVReader(new FileReader("csv/"+this.name+".csv"));
		}
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
		CSVWriter writer = new CSVWriter(new FileWriter(SipsRdf.dir+"/csv/"+this.name+".csv"), ',');
		for(Configuration configuration:this.configurations){
			String[] line = configuration.getLine();
	    	writer.writeNext(line);
		}
		writer.close();
		System.gc();
	}
	
	public ArrayList<String[]> getConfigurationsLines(){
		ArrayList<String[]> lines = new ArrayList<String[]>();
		for(Configuration configuration : this.configurations){
			lines.add(configuration.getLine());
		}
		return lines;
	}
	
	public String getDate(){
		Date date = Calendar.getInstance().getTime();
	    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
	    return sdf.format(date);
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
