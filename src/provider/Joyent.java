package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class Joyent extends Provider {
	
	public static Joyent singleton = new Joyent();

	private Joyent() {
		this.name = "Joyent";
		this.baseUrl = "https://www.joyent.com/pricing";
		this.billing = URI.minute; 
		this.currency = new Dollar();
		this.phoneSupport = true;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		List<WebElement> modals = driver.findElements(By.className("modal"));
		modals.remove(0);
		for(WebElement modal : modals){
			executor.executeScript("arguments[0].removeAttribute('class')",modal);
			//System.out.println(modal.getText());
			Configuration config = new Configuration();
			config.setProvider(this);
			List<WebElement> lis = modal.findElements(By.tagName("li"));
			for(WebElement li : lis){
				if(li.getText().contains("RAM")){
					config.setRam(this.extractNumber(li.getText()));
				}
				else if(li.getText().contains("CPU")){
					config.setCpu(this.extractNumber(li.getText()));
				}
				else if(li.getText().contains("Disk")){
					config.setHdd(this.extractNumber(li.getText())); //Not precised
				}
				else if(li.getText().contains("API Name")){
					config.setConfigName(li.getText());
				}
			}
			//System.out.println("config : "+config);
			List<WebElement> pricesLinux = modal.findElements(By.cssSelector("li.s"));
			//System.out.println("pricesLinux Size : "+pricesLinux.size());
			for(WebElement priceLinux : pricesLinux){
				executor.executeScript("arguments[0].setAttribute('style', 'display: list-item')",priceLinux);
				if(priceLinux.getText().contains("Monthly")){
					Configuration configLinux = new Configuration(config);
					configLinux.setOsUri(URI.linux);
					configLinux.setPrice(this.extractNumber(priceLinux.getText()));
					configLinux.setDate(this.getDate());
					this.configurations.add(configLinux);
					configLinux.println();
				}
			}
			List<WebElement> pricesWindows = modal.findElements(By.cssSelector("li.ws")); 
			//System.out.println("pricesWindows Size : "+pricesWindows.size());
			for(WebElement priceWindows : pricesWindows){
				executor.executeScript("arguments[0].setAttribute('style', 'display: list-item')",priceWindows);
				if(priceWindows.getText().contains("Monthly")){
					Configuration configWindows = new Configuration(config);
					configWindows.setOsUri(URI.windows);
					configWindows.setPrice(this.extractNumber(priceWindows.getText()));
					configWindows.setDate(this.getDate());
					this.configurations.add(configWindows);
					configWindows.println();
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
