package datas;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import provider.Provider;

public class Configuration {
	int id;
	String configName;
	Provider provider;
	int cpu;
	int ram;
	int hdd;
	int ssd;
	int transferSpeed;
	String osUri;
	String currencyUri;
	String countryUri;
	String comment;
	double price;
	
	public Configuration(String configName, int cpu, int ram, int hdd, int ssd, int transferSpeed,
			String osUri, String currencyUri, String countryUri, String comment, double price) {
		super();
		switch(osUri){
			case "windows" : osUri = URI.windows;
			break;
			case "linux" : osUri = URI.linux;
			break;
			default : break;
		}
		this.id = Counter.getConfigurationCounter(); //Unique id
		this.configName = configName;
		this.cpu = cpu;
		this.ram = ram; //Gb
		this.hdd = hdd; //Gb
		this.ssd = ssd; //Gb
		this.transferSpeed = transferSpeed; //Tb
		this.osUri = osUri;
		this.currencyUri = currencyUri;
		this.countryUri = countryUri;
		this.comment = comment;
		this.price = price; //Monthly
	}
	
	public Configuration(String[] line){
		//line[0] is the name of the provider
		this(line[1],Integer.parseInt(line[2]),Integer.parseInt(line[3]),
				Integer.parseInt(line[4]),Integer.parseInt(line[5]), Integer.parseInt(line[6]),
				line[7],line[8],line[9],line[10],Double.parseDouble(line[11]));
	}
	
	public String[] getLine(){
		String[] line = {this.provider.name, this.configName, ""+this.cpu, ""+this.ram, 
				""+this.hdd, ""+this.ssd,""+this.transferSpeed,this.osUri, 
				this.currencyUri, this.countryUri, this.comment, ""+this.price};
		return line;
	}

	public void setProvider(Provider provider){
		this.provider = provider;
	}

	public Resource toResource(Model model){
		Resource configurationResource = model.createResource(URI.baseURI+this.provider.name+"/"+this.id+"/")
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "id"), ""+this.id)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "providerName"), ""+this.provider.name)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "configName"), ""+this.configName)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "cpu"), ""+this.cpu)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "ram"), ""+this.ram)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "hdd"), ""+this.hdd)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "ssd"), ""+this.ssd)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "transferSpeed"), ""+this.transferSpeed)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "os"), ""+this.osUri)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "currency"), ""+this.currencyUri)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "country"), ""+this.countryUri)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "comment"), ""+this.comment)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "price"), ""+this.price);
		return configurationResource;
	}
	
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		Configuration config = new Configuration("S server Linux", 1, 1, 40, -1, 3000, URI.linux, URI.euro, "", "", 9.93);
		@SuppressWarnings("unused")
		Resource resource = config.toResource(model);
		model.write(System.out);
	}
}
