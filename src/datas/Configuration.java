package datas;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import main.SipsRdf;
import provider.Provider;

/**
 * A configuration represents one particular set of cpu, ram, price,... for a given provider
 */
public class Configuration {
	public int id;
	public String configName;
	public Provider provider;
	public double cpu;
	public double cpuSpeed;
	public double ram; /* Gb */
	public double hdd;
	public double ssd; /* Gb */
	public double transferSpeed; /* Tb */
	public String osUri;
	public String currencyUri;
	public String countryUri;
	public String comment;
	public double price; /* Monthly */ /* $Dollar */
	public String date;
	public String dedicated = "";
	
	public Configuration(){
		this("", -1, -1, -1, -1, -1, "", "", "", "", -1);
		this.date = "";
	}
	
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	
	public void setCpuSpeed(double cpuSpeed) {
		this.cpuSpeed = cpuSpeed;
	}

	public void setRam(double ram) {
		this.ram = ram;
	}

	public void setHdd(double hdd) {
		this.hdd = hdd;
	}

	public void setSsd(double ssd) {
		this.ssd = ssd;
	}

	public void setTransferSpeed(double transferSpeed) {
		this.transferSpeed = transferSpeed;
	}

	public void setOsUri(String osUri) {
		this.osUri = osUri;
	}

	public void setCurrencyUri(String currencyUri) {
		this.currencyUri = currencyUri;
	}

	public void setCountryUri(String countryUri) {
		this.countryUri = countryUri;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public void setDedicated(boolean bool){
		if(bool){
			this.dedicated = URI.dedicatedServer;
		}
		else{
			this.dedicated = "";
		}
	}
	
	/**
	 * Deep copy
	 */
	public Configuration(Configuration config){
		this(config.configName, config.cpu, config.ram, config.hdd, config.ssd, config.transferSpeed,
				config.osUri, config.currencyUri, config.countryUri, config.comment, config.price);
		this.setProvider(config.provider);
	}

	public Configuration(String configName, double cpu, double ram, double hdd, double ssd, double transferSpeed,
			String osUri, String currencyUri, String countryUri, String comment, double price) {
		super();
		switch(osUri){
			case "windows" : osUri = URI.windows;
			break;
			case "linux" : osUri = URI.linux;
			break;
			case "" : osUri = "";
			break;
			case "http://dbpedia.org/page/Linux" : osUri = URI.linux;
			break;
			case "http://dbpedia.org/page/Microsoft_Windows" : osUri = URI.windows;
			break;
			default : try {
				throw new Exception("Problem with osURI");
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		this.id = Counter.getConfigurationCounter(); //Unique id
		this.configName = configName;
		this.cpu = cpu;
		this.cpuSpeed = -1;
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
	
	/**
	 * Aim is to create a Configuration object from the reading of the csv file
	 * @param line comes from the csv file
	 */
	public Configuration(String[] line){
		//line[0] is the name of the provider
		this(line[1],Double.parseDouble(line[2]),Double.parseDouble(line[3]),
				Double.parseDouble(line[4]),Double.parseDouble(line[5]), Double.parseDouble(line[6]),
				line[7],line[8],line[9],line[10],Double.parseDouble(line[11]));
		if(line.length>12){ //Otherwise, date may not have been added yet
			this.setDate(line[12]);
		}
		else{
			this.setDate("7 June 2016"); //Anterior to all crawlers
		}
		if(line.length>13){ //Otherwise, dedicated may not have been added yet
			if(line[13]!=""){
				this.setDedicated(true);
			}
			else{
				this.setDedicated(false);
			}
		}
	}
	
	/**
	 * @return line to be written in the csv file
	 */
	public String[] getLine(){
		String[] line = {this.provider.name, this.configName, ""+this.cpu, ""+this.ram, 
				""+this.hdd, ""+this.ssd,""+this.transferSpeed,this.osUri, 
				this.currencyUri, this.countryUri, this.comment, ""+this.price, this.date, this.dedicated};
		return line;
	}

	public void setProvider(Provider provider){
		this.provider = provider;
	}
	
	public double roundPrice(double price){
		return ((int)(price*100))/100.0;
	}

	/**
	 * @return a Resource object that can be written in the rdf file
	 */
	public Resource toResource(Model model){
		Resource configurationResource = model.createResource(URI.baseURI+this.provider.name+"/"+this.id+"/")
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "id"), ""+this.id)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "providerName"), ""+this.provider.name)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "cpu"), ""+this.cpu)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "ram"), ""+this.ram)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "hdd"), ""+this.hdd)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "ssd"), ""+this.ssd)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "transferSpeed"), ""+this.transferSpeed)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "comment"), ""+this.comment)
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "priceEuro"), ""+this.roundPrice(this.provider.currency.toEuro(this.price)))
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "price"), ""+this.roundPrice(this.provider.currency.toDollar(this.price)))
				.addProperty(ResourceFactory.createProperty(URI.baseURI, "dedicated"), ""+this.dedicated);
		if(this.osUri!=""){
			configurationResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "os"), model.createResource(this.osUri));
		}
		else{
			configurationResource.addProperty(ResourceFactory.createProperty(URI.baseURI, "os"), "");
		}
		return configurationResource;
	}
	
	public String toString(){
		String ret = "";
		ret+="Cpu : "+this.cpu+" --- ";
		ret+="Ram : "+this.ram+" --- ";
		ret+="Ssd : "+this.ssd+" --- ";
		ret+="Hdd : "+this.hdd+" --- ";
		ret+="Transfer : "+this.transferSpeed+" --- ";
		if(this.osUri.equals(URI.windows)){
			ret+="Os : Windows --- ";
		}
		else if(this.osUri.equals(URI.linux)){
			ret+="Os : Linux --- ";
		}
		else{
			ret+="Os : Unknown --- ";
		}
		ret+="Price : "+this.price+" --- ";
		return ret;
	}
	
	/**
	 * Custom print, to be managed by the -options
	 */
	public void println(){
		if(SipsRdf.verbose){
			System.out.println(this);
		}
		else{
			System.out.print(".");
		}
	}
	
	/**
	 * test function
	 */
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		Configuration config = new Configuration("S server Linux", 1, 1, 40, -1, 3000, URI.linux, URI.euro, "", "", 9.93);
		@SuppressWarnings("unused")
		Resource resource = config.toResource(model);
		model.write(System.out);
	}
}
