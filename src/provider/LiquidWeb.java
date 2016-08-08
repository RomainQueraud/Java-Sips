package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class LiquidWeb extends Provider {
	
	public static LiquidWeb singleton = new LiquidWeb();

	private LiquidWeb() {
		this.name = "LiquidWeb";
		this.baseUrl = "https://www.liquidweb.com/dedicated/";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.billing = URI.month; 
		this.currency = new Dollar();
		this.phoneSupport = true;
		this.dedicatedServer = true;
		this.alwaysSupport = true;
		this.uptimeGuarantee = true;
		this.detailledSecurity = true;
		this.backup = true;
	}
	
	/*
	 * Return the li.get(index).getText() without getting the crossed elements
	 */
	public String lisGetText(List<WebElement> lis, int index){
		WebElement li = lis.get(index);
		List<WebElement> spans = li.findElements(By.tagName("span"));
		if(spans.size()>=2){
			return spans.get(1).getText();
		}
		else{
			return li.getText();
		}
	}
	
	public void addConfigurationsLiquidWeb(String containerClass) throws Exception{
		WebElement container = driver.findElement(By.className(containerClass));
		List<WebElement> divs = container.findElements(By.className("col-1-4"));
		for(WebElement div : divs){
			Configuration config = new Configuration();
			config.setProvider(this);
			List<WebElement> lis = div.findElement(By.tagName("ul")).findElements(By.tagName("li"));
			config.setConfigName(this.lisGetText(lis, 0));
			config.setCpu(this.extractNumber(this.lisGetText(lis, 1)));
			config.setRam(this.extractNumber(this.lisGetText(lis, 2)));
			config.setSsd(this.extractNumber(this.lisGetText(lis, 3)));
			if(this.lisGetText(lis, 3).contains("TB")){
				config.setSsd(config.ssd*1000); //Given in TB
			}
			if(this.lisGetText(lis, 4).contains("Bandwidth")){
				config.setTransferSpeed(this.extractNumber(this.lisGetText(lis, 4)));
			}
			else{
				config.setHdd(this.extractNumber(this.lisGetText(lis, 4)));
				if(this.lisGetText(lis, 4).contains("TB")){
					config.setHdd(config.hdd*1000); //Given in TB
				}
				if(this.lisGetText(lis, 5).contains("Backups")){
					if(this.lisGetText(lis, 5).contains("TB")){
						config.setHdd(config.hdd + 1000*this.extractNumber(this.lisGetText(lis, 5)));
					}
					else{
						config.setHdd(config.hdd + this.extractNumber(this.lisGetText(lis, 5)));
					}
					config.setTransferSpeed(this.extractNumber(this.lisGetText(lis, 6)));
				}
				else{
					config.setTransferSpeed(this.extractNumber(this.lisGetText(lis, 5)));
				}
			}
			WebElement price = div.findElement(By.className("price-monthly"));
			config.setPrice(this.extractNumber(price.getText()));
			
			config.setDate(this.getDate());
			config.setDedicated(true);
			this.configurations.add(config);
			config.println();
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		this.addConfigurationsLiquidWeb("dedi-plans-smaller");
		this.addConfigurationsLiquidWeb("dedi-plans-larger");
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
