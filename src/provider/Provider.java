package provider;

import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Bag;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.opencsv.CSVWriter;

import datas.Configuration;
import datas.Offset;
import datas.URI;

public abstract class Provider {
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
	
	public void removeAnnoyingElements(){
		//Implemented in sons
		//TODO replace by an interface
	}
	
	public void crawlAndFillConfigurations()throws InterruptedException{
		//Implemented in sons
		//TODO replace by an interface
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
	/********************************/
	
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		Provider provider = Atlantic.singleton;
		
		Configuration configuration = new Configuration("S server Linux", 1, 1, 40, -1, 3000, URI.linux, URI.euro, "", "", 9.93);
		provider.addConfiguration(configuration);
		
		Configuration configuration2 = new Configuration("fake server", 1, 1, 50, 3, 2000, URI.windows, URI.euro, "", "", 14);
		provider.addConfiguration(configuration2);
		
		Bag bag = provider.toBag(model);
		model.write(System.out);
	}

	/* ============================================================== */
	
	String uri = "http://myurl.com/";
	String uriName = uri + name + "/";
	
	Model model = ModelFactory.createDefaultModel();
	Bag configurationBag = model.createBag();
	
	public Model getModel(){
		return this.model;
	}
	
	public void printModel(){
		// list the statements in the Model
		StmtIterator iter = model.listStatements();

		// print out the predicate, subject and object of each statement
		while (iter.hasNext()) {
		    Statement stmt      = iter.nextStatement();  // get next statement
		    Resource  subject   = stmt.getSubject();     // get the subject
		    Property  predicate = stmt.getPredicate();   // get the predicate
		    RDFNode   object    = stmt.getObject();      // get the object

		    System.out.print(subject.toString());
		    System.out.print(" " + predicate.toString() + " ");
		    if (object instanceof Resource) {
		       System.out.print(object.toString());
		    } else {
		        // object is a literal
		        System.out.print(" \"" + object.toString() + "\"");
		    }
		    System.out.println(" .");
		} 
	}
	
	public void writeModel(OutputStream arg0){
		this.model.write(arg0);
	}
	
	/*
	 * Create and add the given configuration to the given model
	 * @param configurationName The number of the configuration in the resource bag/seq
	 */
	public void addConfiguration(String configurationName, int cpu, int ram, int disk, int transfer, double price){
		Resource configurationResource = model.createResource(this.uriName+"_"+configurationName+"/")
			.addProperty(ResourceFactory.createProperty(this.uriName, "cpu"), ""+cpu)
			.addProperty(ResourceFactory.createProperty(this.uriName, "ram"), ""+ram)
			.addProperty(ResourceFactory.createProperty(this.uriName, "disk"), ""+disk)
			.addProperty(ResourceFactory.createProperty(this.uriName, "transfer"), ""+transfer)
			.addProperty(ResourceFactory.createProperty(this.uriName, "price"), ""+price);
		//TODO add the os and the country property
		this.configurationBag.add(configurationResource);
	}
	
	/*
	 * Example on how to request an existing model and showing the results
	 */
	public void exampleRequest(){
		String queryString = "SELECT ?subject \n"
				+ "WHERE {?subject <http://myurl.com/Provider/price> \"14.97\"} \n"
				+ "LIMIT 100";
		Query query = QueryFactory.create(queryString) ;
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
		    ResultSet results = qexec.execSelect() ;
		    ResultSetFormatter.out(System.out, results, query) ;
		    //Be carefull, we can't iterate 2 times (maybe it's needed to start the iterator again ?
		    for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      RDFNode x = soln.get("varName") ;       // Get a result variable by name.
		      Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
		      Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
		    }
		    System.out.println("Result : "+ results.getRowNumber());
		}
	}
	
	/*
	 * Example on how to get a model from an http existing endpoint.
	 */
	public void exampleEndpointRDF(){
		String myUrl = "http://www.geonames.org/ontology/ontology_v3.1.rdf";
		Model model = ModelFactory.createDefaultModel();
		model.read(myUrl);
		model.write(System.out);
	}
	
	/*
	 * Push the rdf model to the running fusuki server.
	 * The datas then can be asked from the fusuki window.
	 */
	public void exampleInsertModeleToFusukiServer(){
		String serviceURI = "http://localhost:3030/ds/data";
		DatasetAccessor accessor;
		accessor = DatasetAccessorFactory.createHTTP(serviceURI);
		accessor.putModel(model);
	}
	
	public Model createOldModelRDF(){
		// create an empty Model
		Resource provider = model.createResource(this.uriName)
				.addProperty(ResourceFactory.createProperty(this.uriName, "os"), 
						model.createResource(this.uriName+"os-bag") //TODO define it to be a seq and do a foreach os
						.addProperty(ResourceFactory.createProperty(this.uriName, "_1"),
								model.createResource(this.uriName+"windows-bundle")
								.addProperty(ResourceFactory.createProperty(this.uriName, "contains"), "Should point on a windows rdf")
								.addProperty(ResourceFactory.createProperty(this.uriName, "price"), "12")));
		return model;
	}
}
