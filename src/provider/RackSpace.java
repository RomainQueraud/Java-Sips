package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class RackSpace extends Provider {
	public static RackSpace singleton = new RackSpace();

	private RackSpace() {
		this.name = "RackSpace";
		this.baseUrl = "https://www.rackspace.com/cloud/servers/pricing";
		this.billing = URI.month;
		this.currency = new Dollar();
	}
	
	public void addConfigurationsRackSpace() throws Exception{		
		//TODO Click on the specified tabs, maybe set a parameter for the function
		//TODO Be carefull because it selects the display none
		WebElement divGeneral = driver.findElement(By.id("general"));
		WebElement divHorizontal = divGeneral.findElement(By.className("horizontal-scroll"));
		List<WebElement> tables = divHorizontal.findElements(By.tagName("table"));
		for(WebElement table : tables){
			List<WebElement> trs = table.findElements(By.className("pricing-row"));
			for(WebElement tr : trs){
				Configuration config = new Configuration();
				config.setProvider(this);
				List<WebElement> tds = tr.findElements(By.tagName("td"));
				System.out.println("tds Size : "+tds.size());
				
				config.setConfigName(tds.get(0).getText());
				config.setRam((int)this.extractNumber(tds.get(1).getText()));
				config.setCpu((int)this.extractNumber(tds.get(2).getText()));
				config.setSsd((int) (this.extractNumber(tds.get(3).getText())+this.extractNumber(tds.get(4).getText())));
				config.setTransferSpeed((int)this.extractNumber(tds.get(5).getText())/1000); //Given in GB
				config.setPrice(this.extractNumber(tds.get(6).getText())+this.extractNumber(tds.get(8).getText())); //tds[7] is the +
			
				System.out.println(config);
				this.configurations.add(config);
			}
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		
		this.addConfigurationsRackSpace();
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}

}
