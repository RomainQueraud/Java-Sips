package provider;

import java.io.IOException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class VpsNet extends Provider {
	
	public static VpsNet singleton = new VpsNet();
	
	double windowsPrice = 7.50;

	private VpsNet() {
		this.name = "VpsNet";
		this.baseUrl = "https://www.vps.net/products/ssd-vps#slider-ssd";
		this.continents.add(URI.asia);
		this.continents.add(URI.europe);
		this.continents.add(URI.australia);
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.southAmerica);
		this.billing = URI.month;
		this.currency = new Dollar();
		this.alwaysSupport = true;
		this.phoneSupport = true;
		this.backup = true;
	}
	
	public Configuration getConfiguration(WebElement click, boolean windows) throws Exception{
		Configuration config = new Configuration();
		config.setProvider(this);
		
		click.click();
		Thread.sleep(2000);
		config.setConfigName(click.getText());
		
		WebElement cpu = driver.findElement(By.className("cores-field"));
		int cpuNumber = Integer.parseInt(cpu.getText());
		config.setCpu(cpuNumber);
		
		WebElement ram = driver.findElement(By.className("ram-field"));
		double ramNumber = this.extractNumber(ram.getText())/1000;//Given in Mb
		config.setRam( ramNumber);
		
		WebElement disk = driver.findElement(By.className("disk-field"));
		double diskNumber = this.extractNumber(disk.getText());
		config.setSsd( diskNumber);
		
		WebElement transfer = driver.findElement(By.className("traffic-field"));
		double transferNumber = this.extractNumber(transfer.getText());
		config.setTransferSpeed( transferNumber);
		
		WebElement price = driver.findElement(By.className("price-val"));
		double priceNumber = this.extractNumber(price.getText());
		if(windows){
			priceNumber += windowsPrice;
			config.setOsUri(URI.windows);
		}
		else{
			config.setOsUri(URI.linux);
		}
		config.setPrice(priceNumber);
		
		config.setDate(this.getDate());
		config.println();
		return config;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		
		WebElement click = driver.findElement(By.className("slider-links-in")).findElement(By.className("active"));
		this.configurations.add(this.getConfiguration(click, false));
		this.configurations.add(this.getConfiguration(click, true));
		
		click = driver.findElement(By.className("slider-links-in")).findElement(By.id("slider-links2"));
		this.configurations.add(this.getConfiguration(click, false));
		this.configurations.add(this.getConfiguration(click, true));
		
		click = driver.findElement(By.className("slider-links-in")).findElement(By.id("slider-links3"));
		this.configurations.add(this.getConfiguration(click, false));
		this.configurations.add(this.getConfiguration(click, true));
		
		click = driver.findElement(By.className("slider-links-in")).findElement(By.id("slider-links4"));
		this.configurations.add(this.getConfiguration(click, false));
		this.configurations.add(this.getConfiguration(click, true));
		
		click = driver.findElement(By.className("slider-links-in")).findElement(By.id("slider-links5"));
		this.configurations.add(this.getConfiguration(click, false));
		this.configurations.add(this.getConfiguration(click, true));
		
		click = driver.findElement(By.className("slider-links-in")).findElement(By.id("slider-links6"));
		this.configurations.add(this.getConfiguration(click, false));
		this.configurations.add(this.getConfiguration(click, true));
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}
	
}
