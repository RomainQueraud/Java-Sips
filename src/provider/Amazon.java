package provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class Amazon extends Provider {
	
	public static Amazon singleton = new Amazon();

	private Amazon() {
		this.name = "Amazon";
		this.baseUrl = "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/AmazonEC2/current/index.json";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.continents.add(URI.southAmerica);
		this.billing = URI.hour; 
		this.currency = new Dollar();
	}
	
	public void setWindows(){
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		List<WebElement> as = driver.findElement(By.id("aws-element-f601e069-fa5d-44fb-9a04-33e66fbe5252"))
				.findElement(By.className("a-tabs")).findElements(By.tagName("a"));
		for(WebElement a : as){
			if(a.getText().contains("Windows")){
				executor.executeScript("arguments[0].click();", a);
			}
		}
	}
	
	public String getComment(boolean ebs){
		String ret = "";
		ret += "All prices are given for US East";
		if(ebs){
			ret+="<br/><br/>Scalable <b>EBS</b> disk";
		}
		return ret;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		URL url = new URL(this.baseUrl);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(in);
        
        JSONObject priceObject = (JSONObject) jsonObject.get("products");
        
        for(@SuppressWarnings("rawtypes")
		Iterator iterator = priceObject.keySet().iterator(); iterator.hasNext();) {
        	String key = (String) iterator.next();
        	JSONObject jDessus = (JSONObject) priceObject.get(key);
        	JSONObject jConfig = (JSONObject) jDessus.get("attributes");
        	String continent = ""+jConfig.get("location");
        	if(continent.contains("US East")){
        		String family = ""+jDessus.get("productFamily");
        		if(family.equals("Compute Instance")){
	        		String sku = ""+jDessus.get("sku");
	        		JSONObject jTerms = (JSONObject) jsonObject.get("terms");
	        		JSONObject jOnDemand = (JSONObject) jTerms.get("OnDemand");
	        		JSONObject jSku = (JSONObject) jOnDemand.get(sku);
	        		JSONObject jSkuDown = (JSONObject) jSku.get(sku+".JRTCKXETXF");
	        		JSONObject jDimension = (JSONObject) jSkuDown.get("priceDimensions");
	        		JSONObject jDimensionDown = (JSONObject) jDimension.get(sku+".JRTCKXETXF.6YS6EN2CT7");
	        		JSONObject jPricePerUnit = (JSONObject) jDimensionDown.get("pricePerUnit");
	        		double priceHour = Double.parseDouble(""+jPricePerUnit.get("USD")); 
	        		
	        		if(priceHour>0){
		        		Configuration config = new Configuration();
		        		config.setProvider(this);
		        		
		        		config.setCpu((this.extractNumber(""+jConfig.get("vcpu"))));
		        		config.setCpuSpeed((this.extractNumber(""+jConfig.get("clockSpeed"))));
		        		config.setRam((this.extractNumber(""+jConfig.get("memory"))));
		        		String storage = ""+jConfig.get("storage");
		        		if(!storage.contains("EBS") && !storage.equals("null")){
		        			String[] parts = storage.split("x");
		        			double part0 = this.extractNumber(parts[0]);
		        			double part1 = this.extractNumber(parts[1]);
		        			if(storage.contains("SSD")){
		        				config.setSsd(part0 * part1);
		        			}
		        			else{
		        				config.setHdd(part0 * part1);
		        			}
		        			config.setComment(this.getComment(false));
		        		}
		        		if(storage.contains("EBS")){
		        			config.setComment(this.getComment(true));
		        		}
		        		String os = ""+jConfig.get("operatingSystem");
		        		if(os.contains("Windows")){
		        			config.setOsUri(URI.windows);
		        		}
		        		else{
		        			config.setOsUri(URI.linux);
		        		}
		        		config.setConfigName(""+jConfig.get("instanceFamily"));
		        		config.setPrice(priceHour * 24 * 365/12);
		        		
		        		config.setDate(this.getDate());
		        		this.configurations.add(config);
		        		config.println();
	        		}
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
