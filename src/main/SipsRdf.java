package main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import provider.Atlantic;
import provider.CityCloud;
import provider.CloudAndHeat;
import provider.CloudSigma;
import provider.CloudWare;
import provider.CloudWatt;
import provider.DimensionData;
import provider.DreamHost;
import provider.E24Cloud;
import provider.EApps;
import provider.ElasticHosts;
import provider.ExoScale;
import provider.Gigenet;
import provider.Google;
import provider.Joyent;
import provider.Linode;
import provider.LiquidWeb;
import provider.MicrosoftAzure;
import provider.Numergy;
import provider.Provider;
import provider.RackSpace;
import provider.SecureRack;
import provider.Storm;
import provider.UnitedStack;
import provider.VexxHost;
import provider.VirtualServer;
import provider.VpsNet;
import provider.ZettaGrid;
import provider.ZippyCloud;

/**
 * Main class of the SIPS program.
 * Crawl if needed and write/read the csv files. Then transform everything into an RDF and push to fuseki.
 */
public class SipsRdf {
	public static SipsRdf singleton = new SipsRdf();
	ArrayList<Provider> providers = new ArrayList<Provider>();
	Options options = new Options();
	ResourceBundle bundle = ResourceBundle.getBundle("properties.config");
	
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
	
	/**
	 * Push the RDF format model into the Fuseki server.
	 * Server needs to be running in order to push the model into.
	 */
	public void pushModelToServer(Model model, String serverAdress){
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serverAdress);
		accessor.putModel(model);
	}
	
	/**
	 * Add the providers in the providerList
	 */
	public void loadProvidersInSipsRdf(){
		this.loadProviderInSipsRdf(Atlantic.singleton, Boolean.parseBoolean(bundle.getString("crawler.Atlantic")));
		this.loadProviderInSipsRdf(CloudSigma.singleton, Boolean.parseBoolean(bundle.getString("crawler.CloudSigma")));
		this.loadProviderInSipsRdf(CloudWare.singleton, Boolean.parseBoolean(bundle.getString("crawler.CloudWare")));
		this.loadProviderInSipsRdf(VirtualServer.singleton, Boolean.parseBoolean(bundle.getString("crawler.VirtualServer")));
		this.loadProviderInSipsRdf(SecureRack.singleton, Boolean.parseBoolean(bundle.getString("crawler.SecureRack")));
		this.loadProviderInSipsRdf(EApps.singleton, Boolean.parseBoolean(bundle.getString("crawler.EApps")));
		this.loadProviderInSipsRdf(E24Cloud.singleton, Boolean.parseBoolean(bundle.getString("crawler.E24Cloud")));
		this.loadProviderInSipsRdf(VpsNet.singleton, Boolean.parseBoolean(bundle.getString("crawler.VpsNet")));
		this.loadProviderInSipsRdf(ExoScale.singleton, Boolean.parseBoolean(bundle.getString("crawler.ExoScale")));
		this.loadProviderInSipsRdf(ZippyCloud.singleton, Boolean.parseBoolean(bundle.getString("crawler.ZippyCloud")));
		this.loadProviderInSipsRdf(ZettaGrid.singleton, Boolean.parseBoolean(bundle.getString("crawler.ZettaGrid")));
		this.loadProviderInSipsRdf(RackSpace.singleton, Boolean.parseBoolean(bundle.getString("crawler.RackSpace")));
		this.loadProviderInSipsRdf(ElasticHosts.singleton, Boolean.parseBoolean(bundle.getString("crawler.ElasticHosts")));
		this.loadProviderInSipsRdf(Storm.singleton, Boolean.parseBoolean(bundle.getString("crawler.Storm")));
		this.loadProviderInSipsRdf(CityCloud.singleton, Boolean.parseBoolean(bundle.getString("crawler.CityCloud")));
		this.loadProviderInSipsRdf(DreamHost.singleton, Boolean.parseBoolean(bundle.getString("crawler.DreamHost")));
		this.loadProviderInSipsRdf(CloudWatt.singleton, Boolean.parseBoolean(bundle.getString("crawler.CloudWatt")));
		this.loadProviderInSipsRdf(CloudAndHeat.singleton, Boolean.parseBoolean(bundle.getString("crawler.CloudAndHeat")));
		this.loadProviderInSipsRdf(VexxHost.singleton, Boolean.parseBoolean(bundle.getString("crawler.VexxHost")));
		this.loadProviderInSipsRdf(LiquidWeb.singleton, Boolean.parseBoolean(bundle.getString("crawler.LiquidWeb")));
		this.loadProviderInSipsRdf(Linode.singleton, Boolean.parseBoolean(bundle.getString("crawler.Linode")));
		this.loadProviderInSipsRdf(Joyent.singleton, Boolean.parseBoolean(bundle.getString("crawler.Joyent")));
		this.loadProviderInSipsRdf(Gigenet.singleton, Boolean.parseBoolean(bundle.getString("crawler.Gigenet")));
		this.loadProviderInSipsRdf(MicrosoftAzure.singleton, Boolean.parseBoolean(bundle.getString("crawler.MicrosoftAzure")));
		this.loadProviderInSipsRdf(DimensionData.singleton, Boolean.parseBoolean(bundle.getString("crawler.DimensionData")));
		this.loadProviderInSipsRdf(UnitedStack.singleton, Boolean.parseBoolean(bundle.getString("crawler.UnitedStack")));
		this.loadProviderInSipsRdf(Numergy.singleton, Boolean.parseBoolean(bundle.getString("crawler.Numergy")));
		this.loadProviderInSipsRdf(Google.singleton, Boolean.parseBoolean(bundle.getString("crawler.Google")));
	}
	
	public void loadProviderInSipsRdf(Provider provider, boolean crawl){
		provider.crawl = crawl;
		this.providers.add(provider);
	}
	
	/**
	 * Fill the providers singletons with configurations from the given CSV file
	 */
	public void loadConfigurationsFromCsv() throws Exception{
		for(Provider provider : this.providers){
			provider.loadConfigurationsFromCsv();
		}
	}
	
	/** 
	 * Erase the csv and write configurations in it
	 */
	public void writeConfigurationsInCsv() throws IOException{
		for(Provider provider:this.providers){
				provider.writeConfigurationsInCsv();
		}
	}
	

	/**
	 * Main function of the SIPS project
	 * Based on the config.properties file, will crawl the required providers.
	 *   The crawled data are then written in csv files.
	 * The csv files are loaded into providers configurations.
	 * The providers configurations are turned into rdf and then pushed into the fuseki server.
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Starting SIPS... use -help for documentation");
		Model model = ModelFactory.createDefaultModel();
		SipsRdf.singleton.loadProvidersInSipsRdf();
		ArrayList<Provider> failed = new ArrayList<Provider>();
		
		Options options = new Options();
		Options providerOptions = new Options();
		
		ArrayList<String> cmdList = new ArrayList<String>();
		ArrayList<String> providerCmdList = new ArrayList<String>();
		for(String a : args){
			if(Character.isUpperCase(a.charAt(1))){
				providerCmdList.add(a);
			}
			else{
				cmdList.add(a);
			}
		}
		String[] cmdArgs = new String[0];
		cmdArgs = cmdList.toArray(cmdArgs);
		String[] providerCmdArgs = new String[0];
		providerCmdArgs = providerCmdList.toArray(providerCmdArgs);
		
		options.addOption("help", false, "display the help");
		options.addOption("providers", false, "display the list of providers crawl cmd");
		options.addOption("all", false, "crawl all Providers");
		options.addOption("localhost", false, "fuseki-server connect to localhost");
		options.addOption("dns", true, "fuseki-server connect to given DNS");
		for(Provider provider:SipsRdf.singleton.providers){
			providerOptions.addOption(provider.name, false, "crawl "+provider.name);
		}
		CommandLine cmd = null;
		CommandLine providerCmd = null;
		try{
			CommandLineParser parser = new DefaultParser();
			cmd = parser.parse(options, cmdArgs);
			providerCmd = parser.parse(providerOptions, providerCmdArgs);
		}
		catch(Exception e){
			System.err.println("Error : Unknown option\nuse -help for general information\nuse -providers for information about available providers");
			System.exit(1);
		}
		
		if(cmd.hasOption("help") || cmd.hasOption("providers")){
			if(cmd.hasOption("help")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("SIPS", options);
			}
			if(cmd.hasOption("providers")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Crawler", providerOptions);
			}
		}
		else{
			System.out.println("Starting crawl/fill...");
			for(Provider provider:SipsRdf.singleton.providers){
				if(provider.crawl || providerCmd.hasOption(provider.name) || cmd.hasOption("all")){ //from config.properties or from cmd line
					try{
						provider.crawlFillWriteConfigurations();
					}
					catch(Exception e){
						System.err.print("Failed to crawl "+provider.name);
						e.printStackTrace();
						System.out.println("Closing Firefox...");
						provider.closeFirefox();
						System.out.println("...and trying another time");
						try{
							provider.crawlFillWriteConfigurations();
						}
						catch(Exception e2){
							e2.printStackTrace();
							failed.add(provider);
							provider.closeFirefox();
							System.err.println("Error in crawler for "+provider.name);
							System.out.println("Going to next provider");
						}
					}
				}
				else{
					provider.loadConfigurationsFromCsv();
				}
			}
			
			if(failed.size()>0){
				System.out.println("Crawl finished with "+failed.size()+" errors : "+failed);
			}
			else{
				System.out.println("Crawl finished without errors");
			}
			
			System.out.println("Creating bag");
			@SuppressWarnings("unused")
			Bag bag = SipsRdf.singleton.toBag(model);
			
			/* Push to the server */
			if(cmd.hasOption("dns")){
				System.out.println("Sending rdf to "+cmd.getOptionValue("dns"));
				SipsRdf.singleton.pushModelToServer(model, "http://"+cmd.getOptionValue("dns")+":3030/ds/data");
			}
			else if(cmd.hasOption("localhost")){
				System.out.println("Sending rdf to localHost");
				SipsRdf.singleton.pushModelToServer(model, "http://localhost:3030/ds/data");
			}
	
			OutputStream out;
			try{
				System.out.println("Writing model to ../Fuseki-server/ontology.owl");
				out = new FileOutputStream("../Fuseki-server/ontology.owl");
			}
			catch(java.io.FileNotFoundException e){
				System.out.println("Fuseki folder not found, writing model to ontology.owl");
				out = new FileOutputStream("ontology.owl");
			}
			RDFDataMgr.write(out, model, Lang.RDFXML);
			out.close();
			//model.write(System.out);
		}
	}
}
