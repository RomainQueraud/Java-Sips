package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;
import main.SipsRdf;

public class CloudWatt extends Provider {
	
	public static CloudWatt singleton = new CloudWatt();

	private CloudWatt() {
		this.name = "CloudWatt";
		this.baseUrl = "https://www.cloudwatt.com/en/pricing.html";
		this.continents.add(URI.europe);
		this.billing = URI.hour; 
		this.currency = new Euro();
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		List<WebElement> containers = driver.findElements(By.className("container"));
		for(WebElement container : containers){
			if(container.getText().contains("Compute")){
				WebElement normandie = driver.findElement(By.cssSelector("div #normandie2"));
				List<WebElement> trs = normandie.findElements(By.tagName("tr"));
				for(WebElement tr : trs){
					if(tr.isDisplayed() && tr.getText().contains("€")){
						//System.out.println("--------------");
						//System.out.println(tr.getText());
						List<WebElement> tds = tr.findElements(By.tagName("td"));
						Configuration config = new Configuration();
						config.setProvider(this);
						
						config.setConfigName(tds.get(0).getText());
						config.setCpu(this.extractNumber(tds.get(1).getText()));
						config.setRam(this.extractNumber(tds.get(2).getText()));
						config.setSsd(this.extractNumber(tds.get(3).getText())); //System storage ?
						config.setHdd(this.extractNumber(tds.get(4).getText())); //Data storage ?
						if(tds.get(6).getText() != ""){
							Configuration configLinux = new Configuration(config);
							configLinux.setProvider(this);
							configLinux.setOsUri(URI.linux);
							configLinux.setPrice(this.extractNumber(tds.get(6).getText()));
							configLinux.setDate(this.getDate());
							this.configurations.add(configLinux);
							configLinux.println();
						}
						if(tds.get(7).getText()!=""){
							Configuration configWindows = new Configuration(config);
							configWindows.setProvider(this);
							configWindows.setOsUri(URI.windows);
							configWindows.setPrice(this.extractNumber(tds.get(7).getText()));
							configWindows.setDate(this.getDate());
							this.configurations.add(configWindows);
							configWindows.println();
						}
						//System.out.println("--------------");
					}
				}
			}
		}
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
