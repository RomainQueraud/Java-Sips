package main;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import provider.Atlantic;
import provider.CloudSigma;
import provider.CloudWare;
import provider.E24Cloud;
import provider.EApps;
import provider.ExoScale;
import provider.Provider;
import provider.SecureRack;
import provider.VirtualServer;
import provider.VpsNet;
import provider.ZippyCloud;

public class SipsRdf {
	public static SipsRdf singleton = new SipsRdf();
	ArrayList<Provider> providers = new ArrayList<Provider>();
	
	private SipsRdf(){
	}
	
	public void addProvider(Provider provider){
		this.providers.add(provider);
	}
	
	public Bag toBag(Model model) throws Exception{
		Bag sipsBag = model.createBag();
		for(Provider provider : providers){
			sipsBag.add(provider.toResource(model));
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
		this.loadProviderInSipsRdf(Atlantic.singleton, false);
		this.loadProviderInSipsRdf(CloudSigma.singleton, false);
		this.loadProviderInSipsRdf(CloudWare.singleton, false);
		this.loadProviderInSipsRdf(VirtualServer.singleton, false);
		this.loadProviderInSipsRdf(SecureRack.singleton, false); //long to getBag if too many elements
		this.loadProviderInSipsRdf(EApps.singleton, false);
		this.loadProviderInSipsRdf(E24Cloud.singleton, false);
		this.loadProviderInSipsRdf(VpsNet.singleton, false);
		this.loadProviderInSipsRdf(ExoScale.singleton, false);
		this.loadProviderInSipsRdf(ZippyCloud.singleton, true);
	}
	
	public void loadProviderInSipsRdf(Provider provider, boolean crawl){
		provider.crawl = crawl;
		this.providers.add(provider);
	}
	
	/*
	 * Fill the providers singletons with configurations from the given CSV file
	 */
	public void loadConfigurationsFromCsv() throws Exception{
		for(Provider provider : this.providers){
			provider.loadConfigurationsFromCsv();
		}
	}
	
	/* Erase the csv and write configurations in it
	 * If crawlOnly is selected, only Providers with crawl boolean will be written */
	public void writeConfigurationsInCsv() throws IOException{
		for(Provider provider:this.providers){
				provider.writeConfigurationsInCsv();
		}
	}
	
	/*
	 * args[0] : boolean to know if we want to crawl
	 * args[1] : double to know the speed of the crawl (1 = fast and un-precise, 0.10 = slow and precise) 
	 */
	public static void main(String[] args) throws Exception {

		Model model = ModelFactory.createDefaultModel();
		SipsRdf.singleton.loadProvidersInSipsRdf();
		
		System.out.println("Start Crawling and loading configurations");
		for(Provider provider:SipsRdf.singleton.providers){
			if(provider.crawl){
				provider.crawlFillWriteConfigurations();
			}
			else{
				provider.loadConfigurationsFromCsv();
			}
		}
		
		System.out.println("Creating bag");
		@SuppressWarnings("unused")
		Bag bag = SipsRdf.singleton.toBag(model);
		
		/* Push to the server */
		System.out.println("Sending rdf to server");
		SipsRdf.singleton.pushModelToServer(model, "http://localhost:3030/ds/data");
		
		//model.write(System.out);
	}
}
