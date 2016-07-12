package provider;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class Atlantic extends Provider {
	
	public static Atlantic singleton = new Atlantic();

	private Atlantic() {
		this.name = "Atlantic";
		this.baseUrl = "https://www.atlantic.net/cloud-hosting/pricing/";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.billing = URI.second;
		this.currency = new Dollar();
	}
	
	public Configuration getConfiguration(String id, boolean windows) throws Exception{
		WebElement div = driver.findElement(By.id(id));
		Configuration config = new Configuration();
		config.setProvider(this);
		if(windows){
			config.setOsUri(URI.windows);
		}
		else{
			config.setOsUri(URI.linux);
		}
		WebElement price = div.findElement(By.className("cloudServerMonthlyPrice"));
		config.setPrice(this.extractNumber(price.getText()));
		List<WebElement> ps = div.findElements(By.tagName("p"));
		for(WebElement p : ps){
			if(p.getText().contains("Ram")){
				config.setRam(this.extractNumber(p.getText()));
			}
			else if(p.getText().contains("Processor")){
				config.setCpu(this.extractNumber(p.getText()));
			}
			else if(p.getText().contains("SSD")){
				config.setSsd(this.extractNumber(p.getText()));
			}
			else if(p.getText().contains("Transfer")){
				config.setTransferSpeed(this.extractNumber(p.getText()));
			}
		}
		
		System.out.println(config);
		return config;
	}

	@Override
	public void crawlFillWriteConfigurations() throws Exception{
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		this.configurations.add(this.getConfiguration("LS", false));
		this.configurations.add(this.getConfiguration("LM", false));
		this.configurations.add(this.getConfiguration("LL", false));
		this.configurations.add(this.getConfiguration("LXL", false));
		this.configurations.add(this.getConfiguration("LXXL", false));
		
		WebElement osSwitch = driver.findElement(By.id("osSwitch"));
		executor.executeScript("arguments[0].click();", osSwitch);
		
		this.configurations.add(this.getConfiguration("WS", true));
		this.configurations.add(this.getConfiguration("WM", true));
		this.configurations.add(this.getConfiguration("WL", true));
		this.configurations.add(this.getConfiguration("WXL", true));
		this.configurations.add(this.getConfiguration("WXXL", true));
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}
}
