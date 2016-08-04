package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class Gigenet extends Provider {

	public static Gigenet singleton = new Gigenet();

	private Gigenet() {
		this.name = "Gigenet";
		this.baseUrl = "http://gigenet.com/dedicated-servers/all-servers/";
		this.continents.add(URI.northAmerica);
		this.billing = URI.month; 
		this.currency = new Dollar();
		this.uptimeGuarantee = true;
		this.dedicatedServer = true;
		this.alwaysSupport = true;
		this.detailledSecurity = true;
		this.backup = true;
		this.webAccess = true;
		this.phoneSupport = true;
	}
	
	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		Select select = new Select(driver.findElement(By.id("tablepress-4_length")).findElement(By.tagName("select")));
		select.selectByVisibleText("100");
		Thread.sleep(2000);
		
		WebElement table = driver.findElement(By.id("tablepress-4"));
		List<WebElement> trs = table.findElement(By.tagName("tbody")).findElements(By.tagName("tr"));
		for(WebElement tr : trs){
			Configuration config = new Configuration();
			config.setProvider(this);
			
			List<WebElement> tds = tr.findElements(By.tagName("td"));
			config.setConfigName(tds.get(0).getText());
			config.setCpu(this.extractNumber(tds.get(2).getText()));
			config.setRam(this.extractNumber(tds.get(3).getText()));
			if(tds.get(4).getText().contains("TB")){
				config.setHdd(this.extractNumber(tds.get(4).getText())*1000); //Given in TB
			}
			else{
				config.setHdd(this.extractNumber(tds.get(4).getText()));
			}
			config.setTransferSpeed(this.extractNumber(tds.get(5).getText()));
			config.setPrice(this.extractNumber(tds.get(6).getText()));
			
			config.setDate(this.getDate());
			this.configurations.add(config);
			config.println();
		}
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		System.gc();
		this.writeConfigurationsInCsv();
	}

}
