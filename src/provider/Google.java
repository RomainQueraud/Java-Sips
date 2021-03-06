package provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class Google extends Provider {
	
	public static Google singleton = new Google();
	int ssdSize = 375; //Not in the JSON, but seen in the pricing list
	//https://cloud.google.com/compute/pricing#localssdpricing
	double ssdPrice = 81.75; //Same thing, not in the JSON

	private Google() {
		this.name = "Google";
		this.baseUrl = "http://www.cloudpricingcalculator.appspot.com/static/data/pricelist.json";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.billing = URI.minute; 
		this.currency = new Dollar();
		this.freeTrial = true;
		this.detailledSecurity = true;
		this.customizableConfiguration = true;
		this.environment = true;
		this.burstResource = true;
		this.backup = true;
		this.webAccess = true;
		this.terminalAccess = true;
	}
	
	public double getSsdPrice(){
		return this.ssdPrice;
	}
	
	/*
	 * true is High price (0.04 ?) and false is Low price (0.02 ?)
	 */
	public double getWindowsPrice(JSONObject priceObject, boolean high){
		JSONObject osPriceObject = (JSONObject) priceObject.get("CP-COMPUTEENGINE-OS");
		JSONObject windowsPriceObject = (JSONObject) osPriceObject.get("win");
		if(high){
			return Double.parseDouble(""+windowsPriceObject.get("high"));
		}
		else{
			return Double.parseDouble(""+windowsPriceObject.get("low"));
		}
	}
	
	public String getComment(){
		String ret = "";
		ret+= "All prices are given for the <b>Europe</b> continent.";
		return ret;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		URL url = new URL(this.baseUrl);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String l;
        StringBuilder jsonBuilder = new StringBuilder();
        System.out.println(url);
        while((l=in.readLine()) != null){
        	System.out.println(l);
        	jsonBuilder.append(l);
        }
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(jsonBuilder.toString());
        
        JSONObject priceObject = (JSONObject) jsonObject.get("gcp_price_list");

		for(@SuppressWarnings("rawtypes")
		Iterator iterator = priceObject.keySet().iterator(); iterator.hasNext();) {
		    String key = (String) iterator.next();
		    if(key.contains("CP-COMPUTEENGINE-VMIMAGE")){
		    	JSONObject jConfig = (JSONObject) priceObject.get(key);
		    	
		    	Configuration config = new Configuration();
		    	config.setProvider(this);
		    	
		    	config.setConfigName(key);
		    	String cpu = (String) jConfig.get("cores");
		    	if(cpu.equals("shared")){
		    		config.setCpu(0.5); 
		    	}
		    	else{
		    		config.setCpu(Integer.parseInt(cpu));
		    	}
		    	config.setRam((Double.parseDouble(""+jConfig.get("memory"))));
		    	config.setOsUri(URI.linux);
		    	config.setComment(this.getComment());
		    	config.setPrice(Double.parseDouble(""+jConfig.get("europe")) * 24 * (365.0/12.0));
		    	
		    	JSONArray ssdArray = (JSONArray) jConfig.get("ssd");
		    	for(int i=0 ; i<ssdArray.size() ; i++){
		    		long ssdNumber = (long) ssdArray.get(i);
		    		//System.out.println("ssdNumber : "+ssdNumber);
		    		Configuration ssdConfig = new Configuration(config);
		    		ssdConfig.setProvider(this);
		    		
		    		ssdConfig.setSsd( ssdNumber * this.ssdSize); 
		    		ssdConfig.setPrice(ssdConfig.price + ssdNumber*this.getSsdPrice());
		    		
		    		Configuration windowsConfig = new Configuration(ssdConfig);
		    		windowsConfig.setProvider(this);
		    		windowsConfig.setOsUri(URI.windows);
		    		if(windowsConfig.configName.contains("MICRO") || windowsConfig.configName.contains("SMALL")){
		    			windowsConfig.setPrice(windowsConfig.price + 
		    					(this.getWindowsPrice(priceObject, false) * 24 * (365.0/12.0)));
		    		}
		    		else{
		    			windowsConfig.setPrice(windowsConfig.price + 
		    					(this.getWindowsPrice(priceObject, true) * 24 * (365.0/12.0)));
		    		}
		    		
		    		ssdConfig.setDate(this.getDate());
		    		this.configurations.add(ssdConfig);
		    		ssdConfig.println();
			    	windowsConfig.setDate(this.getDate());
			    	this.configurations.add(windowsConfig);
			    	windowsConfig.println();
		    	}
		    }
		}
        
		in.close();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
