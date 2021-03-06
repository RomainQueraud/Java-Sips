package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class VexxHost extends Provider {
	
	public static VexxHost singleton = new VexxHost();

	private VexxHost() {
		this.name = "VexxHost";
		this.baseUrl = "https://vexxhost.com/public-cloud/servers/";
		this.billing = URI.hour; 
		this.currency = new Dollar();
		this.alwaysSupport = true;
		this.uptimeGuarantee = true;
		this.backup = true;
		this.api = true;
		this.webAccess = true;
		this.terminalAccess = true;
		this.multipleIp = true;
		this.dedicatedServer = true;
		this.phoneSupport = true;
	}
	
	public double getWindowsPrice() throws Exception{
		List<WebElement> trs = driver.findElements(By.tagName("tr"));
		for(WebElement tr : trs){
			if(tr.getText().contains("Windows")){
				List<WebElement> tds = tr.findElements(By.tagName("td"));
				double windowsPrice = this.extractNumber(tds.get(1).getText())*24*30; //Given hourly
				return windowsPrice;
			}
		}
		System.out.println("Windows price not found, return 0");
		return 0;
	}
	
	public void addConfigurationsVexxHost(boolean windows) throws Exception{
		List<WebElement> trs = driver.findElements(By.tagName("tr"));
		for(WebElement tr : trs){
			if(tr.getText().contains("cores")){
				List<WebElement> tds = tr.findElements(By.tagName("td"));
				Configuration config = new Configuration();
				config.setProvider(this);
				
				config.setConfigName(tds.get(0).getText());
				config.setCpu(this.extractNumber(tds.get(1).getText()));
				config.setRam(this.extractNumber(tds.get(2).getText()));
				config.setSsd(this.extractNumber(tds.get(3).getText()));
				config.setTransferSpeed(this.extractNumber(tds.get(4).getText()));
				if(windows){
					config.setOsUri(URI.windows);
					config.setPrice(this.extractNumber(tds.get(5).getText())*24*30 + this.getWindowsPrice()); //Given hourly
				}
				else{
					config.setOsUri(URI.linux);
					config.setPrice(this.extractNumber(tds.get(5).getText())*24*30); //Given hourly
				}
				config.setDate(this.getDate());
				this.configurations.add(config);
				config.println();
			}
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		this.addConfigurationsVexxHost(false);
		this.addConfigurationsVexxHost(true);
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
