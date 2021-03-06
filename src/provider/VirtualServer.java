package provider;

import java.io.IOException;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;
import main.SipsRdf;

public class VirtualServer extends Provider {

	public static VirtualServer singleton = new VirtualServer(); 

	private VirtualServer() {
		this.name = "VirtualServer";
		this.baseUrl = "https://www.virtual-server.net/home/";
		this.crawl = true;
		this.continents.add(URI.europe);
		this.billing = URI.month;
		this.currency = new Euro();
		this.detailledSecurity = true;
		this.paypal = true;
		this.terminalAccess = true;
	}
	
	public Configuration getConfiguration(String id) throws Exception{
		Configuration config = new Configuration();
		config.setProvider(this);
		
		List<WebElement> lis = driver.findElement(By.id(id)).findElements(By.tagName("li"));
		for(WebElement li : lis){
			if(li.getText().contains("CPU")){
				config.setCpu(this.extractNumber(li.getText()));
			}
			else if(li.getText().contains("RAM")){
				config.setRam(this.extractNumber(li.getText()));
			}
			else if(li.getText().contains("storage")){
				config.setSsd(this.extractNumber(li.getText()));
			}
		}
		
		WebElement configName = driver.findElement(By.id(id)).findElement(By.className("offer-head"));
		config.setConfigName(configName.getText());
		if(configName.getText().contains("Linux")){
			config.setOsUri(URI.linux);
		}
		else if(configName.getText().contains("Windows")){
			config.setOsUri(URI.windows);
		}
		
		List<WebElement> price = driver.findElement(By.id(id)).findElements(By.className("value"));
		config.setPrice(Double.parseDouble(price.get(1).getText())); //The 0th is the cost, the 1st is the value
		
		config.setDate(this.getDate());
		config.println();
		return config;
	}
	
	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		this.openFirefox();
		this.loadWebpage();
		try {
			this.configurations.add(this.getConfiguration("c2088"));
			this.configurations.add(this.getConfiguration("c2090"));
			this.configurations.add(this.getConfiguration("c2092"));
			this.configurations.add(this.getConfiguration("c2082"));
			this.configurations.add(this.getConfiguration("c2084"));
			this.configurations.add(this.getConfiguration("c2086"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}
}
