package provider;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class CloudSigma extends Provider{
	public static CloudSigma singleton = new CloudSigma(); 

	private CloudSigma() {
		this.name = "CloudSigma";
		this.baseUrl = "https://www.cloudsigma.com/pricing/";
		this.continents.add(URI.europe);
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.asia);
		this.continents.add(URI.australia);
		this.billing = URI.month;
		this.currency = new Dollar();
		this.freeTrial = true;
		this.multipleIp = true;
		this.customizableCpu = true;
		this.api = true;
		this.alwaysSupport = true;
		this.paypal = true;
	}
	
	public String getComment(){
		return "Unlimited IOPS";
	}

	@Override
	public void crawlFillWriteConfigurations() throws Exception{
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		List<WebElement> divs = driver.findElements(By.className("x-pricing-column"));
		for(WebElement div : divs){
			Configuration config = new Configuration();
			config.setProvider(this);
			
			WebElement price = div.findElement(By.className("x-price"));
			config.setPrice(this.extractNumber(price.getText()));
			
			config.setComment(this.getComment());
			
			List<WebElement> lis = div.findElements(By.tagName("li"));
			for(WebElement li : lis){
				if(li.getText().contains("CPU")){
					config.setCpu(this.extractNumber(li.getText()));
				}
				else if(li.getText().contains("RAM")){
					config.setRam(this.extractNumber(li.getText()));
				}
				else if(li.getText().contains("SSD")){
					config.setSsd(this.extractNumber(li.getText()));
				}
				else if(li.getText().contains("Data")){
					config.setTransferSpeed(this.extractNumber(li.getText()));
				}
			}
			
			config.setDate(this.getDate());
			this.configurations.add(config);
			config.println();
		}
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}
}
