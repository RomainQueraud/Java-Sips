package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;
import main.SipsRdf;

public class CloudAndHeat extends Provider {
	
	public static CloudAndHeat singleton = new CloudAndHeat();

	private CloudAndHeat() {
		this.name = "CloudAndHeat";
		this.baseUrl = "https://www.cloudandheat.com/en/products.html#iaas";
		this.continents.add(URI.europe); //Germany
		this.billing = URI.month; 
		this.currency = new Euro();
		this.environment = true;
		this.phoneSupport = true;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		List<WebElement> divs = driver.findElements(By.className("col-sm-4"));
		for(WebElement div : divs){
			Configuration config = new Configuration();
			config.setProvider(this);
			config.setConfigName(div.findElement(By.tagName("h3")).getText());
			List<WebElement> spans = div.findElements(By.tagName("span"));
			for(WebElement span : spans){
				if(span.getText().contains("CPU")){
					config.setCpu(this.extractNumber(span.getText()));
				}
				else if(span.getText().contains("RAM")){
					config.setRam(this.extractNumber(span.getText()));
				}
				else if(span.getText().contains("HDD")){
					config.setHdd(this.extractNumber(span.getText()));
				}
				else if(span.getText().contains("SSD")){
					config.setSsd(this.extractNumber(span.getText()));
				}
				else if(span.getText().contains("Traffic")){
					config.setTransferSpeed(this.extractNumber(span.getText()));
					//TODO be carefull because it can take the upper 3...
				}
				else if(span.getText().contains("Month")){
					config.setPrice(this.extractNumber(span.getText()));
					//TODO be carefull because it can take the upper 2...
				}
			}
			config.setOsUri(URI.linux);
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
