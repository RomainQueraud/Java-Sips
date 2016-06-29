package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class Linode extends Provider {
	
	public static Linode singleton = new Linode();

	private Linode() {
		this.name = "Linode";
		this.baseUrl = "https://www.linode.com/pricing";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.billing = URI.hour; 
		this.currency = new Dollar();
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		WebElement button = driver.findElement(By.id("show-larger-plans"));
		button.click();
		Thread.sleep(2000);
		
		WebElement table = driver.findElement(By.id("pricing-larger-plans-table"));
		WebElement tbody = table.findElement(By.tagName("tbody"));
		List<WebElement> trs = tbody.findElements(By.tagName("tr"));
		for(WebElement tr : trs){
			List<WebElement> tds = tr.findElements(By.tagName("td"));
			Configuration config = new Configuration();
			config.setProvider(this);
			config.setConfigName(tds.get(0).getText());
			config.setRam((int)this.extractNumber(tds.get(1).getText()));
			config.setCpu((int)this.extractNumber(tds.get(2).getText()));
			config.setSsd((int)this.extractNumber(tds.get(3).getText()));
			config.setTransferSpeed((int)this.extractNumber(tds.get(4).getText()));
			WebElement price = tr.findElement(By.className("pricing-monthly"));
			config.setPrice(this.extractNumber(price.getText()));
			
			this.configurations.add(config);
			System.out.println(config);
		}
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}
}
