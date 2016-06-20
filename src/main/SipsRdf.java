package main;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import datas.Configuration;
import datas.URI;
import provider.Atlantic;
import provider.CloudSigma;
import provider.CloudWare;
import provider.Provider;
import provider.VirtualServer;

public class SipsRdf {
	public static SipsRdf singleton = new SipsRdf();
	ArrayList<Provider> providers = new ArrayList<Provider>();
	
	private SipsRdf(){
	}
	
	public void addProvider(Provider provider){
		this.providers.add(provider);
	}
	
	public Bag toBag(Model model){
		Bag sipsBag = model.createBag();
		for(Provider provider : providers){
			sipsBag.add(provider.toBag(model));
		}
		return sipsBag;
	}
	
	/*
	 * Push the RDF format model into the Fuseki server.
	 * Server needs to be running in order to push the model into.
	 */
	public void pushModelToServer(Model model, String serverAdress){
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serverAdress);
		accessor.putModel(model);
	}
	
	/*
	 * Add the providers in the providerList
	 */
	public void loadProvidersInSipsRdf(){
		this.providers.add(Atlantic.singleton);
		this.providers.add(CloudSigma.singleton);
		this.providers.add(CloudWare.singleton);
		this.providers.add(VirtualServer.singleton);
	}
	
	private void setProvidersCrawlSpeed(double crawlSpeed) {
		for(Provider provider : this.providers){
			provider.setCrawlSpeed(crawlSpeed);
		}
	}
	
	/*
	 * Fill the providers singletons with configurations from the given CSV file
	 */
	public void loadConfigurationsFromCsv(String csvLocation) throws Exception{
		CSVReader reader = new CSVReader(new FileReader(csvLocation));
	    List<String[]> myEntries = reader.readAll();
	    
	    for(String[] line : myEntries){
	    	if(!line[0].equals("Provider")){ //First line are the titles
		    	Configuration configuration = new Configuration(line);
		    	switch(line[0]){
		    	case "Atlantic" : Atlantic.singleton.addConfiguration(configuration);
		    	break;
		    	case "CloudSigma" : CloudSigma.singleton.addConfiguration(configuration);
		    	break;
		    	case "CloudWare" : CloudWare.singleton.addConfiguration(configuration);
		    	break;
		    	case "VirtualServer" : VirtualServer.singleton.addConfiguration(configuration);
		    	break;
		    	default : throw new Exception("CSV file error : unknown provider "+line[0]);
		    	}
	    	}
	    }
	    
		reader.close();
	}
	
	
	public void writeConfigurationsInCsv(String csvLocation) throws IOException{
		this.writeConfigurationsInCsv(csvLocation, false);
	}
	
	/* Erase the csv and write configurations in it
	 * If crawlOnly is selected, only Providers with crawl boolean will be written */
	public void writeConfigurationsInCsv(String csvLocation, boolean crawlOnly) throws IOException{
		CSVWriter writer = new CSVWriter(new FileWriter(csvLocation), ',');
		for(Provider provider:this.providers){
			if(!crawlOnly || provider.crawl){
				provider.writeConfigurationInCsv(writer);
			}
		}
		writer.close();
	}
	
	public static void test(){
		Model model = ModelFactory.createDefaultModel();
		
		/****** Provider part ******/
		Provider atlantic = Atlantic.singleton;
		
		Configuration configuration = new Configuration("S server Linux", 1, 1, 40, -1, 3000, URI.linux, URI.euro, "", "", 9.93);
		atlantic.addConfiguration(configuration);
		
		Configuration configuration2 = new Configuration("fake server", 1, 1, 50, 3, 2000, URI.windows, URI.euro, "", "", 14);
		atlantic.addConfiguration(configuration2);
		/***************************/
		/****** Provider2 part ******/
		Provider cloudSigma = CloudSigma.singleton;
		
		Configuration configuration3 = new Configuration("S server Linux", 1, 1, 40, -1, 3000, URI.linux, URI.euro, "", "", 9.93);
		cloudSigma.addConfiguration(configuration3);
		
		Configuration configuration4 = new Configuration("fake server", 1, 1, 50, 3, 2000, URI.windows, URI.euro, "", "", 14);
		cloudSigma.addConfiguration(configuration4);
		/***************************/
		
		SipsRdf sips = new SipsRdf();
		sips.addProvider(atlantic);
		sips.addProvider(cloudSigma);
		
		@SuppressWarnings("unused")
		Bag bag= sips.toBag(model);
		
		model.write(System.out);
	}
	
	/*
	 * args[0] : boolean to know if we want to crawl
	 * args[1] : double to know the speed of the crawl (1 = fast and un-precise, 0.10 = slow and precise) 
	 */
	public static void main(String[] args) throws Exception {
		boolean crawl = Boolean.parseBoolean(args[0]); 
		System.out.println("arg[0] crawl : "+crawl);
		double crawlSpeed = Double.parseDouble(args[1]);
		System.out.println("arg[1] crawlSpeed : "+crawlSpeed);
		
		Model model = ModelFactory.createDefaultModel();
		SipsRdf.singleton.loadProvidersInSipsRdf();
		SipsRdf.singleton.setProvidersCrawlSpeed(crawlSpeed);
		
		if(crawl){
			for(Provider provider:SipsRdf.singleton.providers){
				if(provider.crawl){
					provider.crawlAndFillConfigurations();
				}
			}
			/*
			 * writeConfigurationsInCsv(..., true) because we want only crawled configurations to be written
			 */
			SipsRdf.singleton.writeConfigurationsInCsv("resources/datasCrawl.csv", true);
		}
		else{
			SipsRdf.singleton.loadConfigurationsFromCsv("resources/datasCrawl.csv");
		}
		
		SipsRdf.singleton.loadConfigurationsFromCsv("resources/datas.csv");
		
		@SuppressWarnings("unused")
		Bag bag = SipsRdf.singleton.toBag(model);
		
		/* Push to the server */
		SipsRdf.singleton.pushModelToServer(model, "http://localhost:3030/ds/data");
		System.out.println("Rdf sent to server");
		
		model.write(System.out);
	}
}
