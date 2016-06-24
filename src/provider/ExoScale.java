package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;

public class ExoScale extends Provider {
	
	public static ExoScale singleton = new ExoScale();

	private ExoScale() {
		this.name = "ExoScale";
		this.baseUrl = "https://www.exoscale.ch/pricing/";
		this.continents.add(URI.europe);
		this.billing = URI.hour;
		this.currency = new Euro();
	}
	
	/*
	 * Return the configurations of only one config
	 * Example : all the disks and os configs of the "Extra-Large" config
	 */
	public void addConfigurationsExoScale(String configName, boolean windows) throws Exception{
		List<WebElement> articles = driver.findElements(By.className("vm-card"));
		for(WebElement article : articles){
			WebElement h3 = article.findElement(By.tagName("h3"));
			if(h3.getText().equals(configName)){
				WebElement parent = h3.findElement(By.xpath(".."));
				parent.click();
			}
		}
		
		if(windows){
			WebElement windowsButton = driver.findElement(By.className("win"));
			windowsButton.click();
		}
		
		WebElement minusButton = driver.findElement(By.className("stepper-subtract"));
		while(!(minusButton.getAttribute("class").contains("disabled"))){
			minusButton.click();
		}
		//System.out.println("minusButton-class : "+minusButton.getAttribute("class"));
		
		WebElement cpu = driver.findElement(By.className("summary")).findElement(By.className("cpu")).findElement(By.className("spec-value"));
		int cpuValue = Integer.parseInt(cpu.getText());
		//System.out.println("cpu : "+cpuValue);
		
		WebElement ram = driver.findElement(By.className("summary")).findElement(By.className("ram")).findElement(By.className("spec-value"));
		double ramValue = this.extractNumber(ram.getText());
		if(ram.getText().contains("MB")){
			ramValue /= 1000; //Given in MB
		}
		//System.out.println("ram : "+ramValue);
		
		WebElement plusButton = driver.findElement(By.className("stepper-add"));
		//System.out.println("plusButton-class : "+plusButton.getAttribute("class"));
		
		//Inside the while will be one configuration. It has to be added to the configs arrayList
		while(!(plusButton.getAttribute("class").contains("disabled"))){
			this.addConfigurationExoScale(configName, cpuValue, ramValue, windows);
			plusButton.click();
		}
		
		//For the last one that can't be inside the while because of the disabled plusButton
		this.addConfigurationExoScale(configName, cpuValue, ramValue, windows);
	}
	
	public void addConfigurationExoScale(String configName, int cpuValue, double ramValue, boolean windows) throws Exception{
		Configuration config = new Configuration();
		config.setProvider(this);
		config.setConfigName(configName);
		config.setCpu(cpuValue);
		config.setRam((int)ramValue);
		
		WebElement disk = driver.findElement(By.className("summary")).findElement(By.className("hdd")).findElement(By.className("spec-value"));
		double diskValue = this.extractNumber(disk.getText());
		//System.out.println("disk : "+diskValue);
		
		config.setSsd((int) diskValue);
		if(windows){
			config.setOsUri(URI.windows);
		}
		else{
			config.setOsUri(URI.linux);
		}
		
		Thread.sleep(500);
		WebElement price = driver.findElement(By.className("summary")).findElement(By.className("price-month")).findElement(By.className("price-value"));
		double priceValue = Double.parseDouble(price.getText());
		//System.out.println("price : "+priceValue);
		config.setPrice(priceValue);
		
		this.configurations.add(config);
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		//driver.manage().window().maximize();
		//Thread.sleep(3000);
		
		this.addConfigurationsExoScale("Micro", false);
		
		this.addConfigurationsExoScale("Tiny", false);
		this.addConfigurationsExoScale("Tiny", true);
		
		this.addConfigurationsExoScale("Small", false);
		this.addConfigurationsExoScale("Small", true);
		
		this.addConfigurationsExoScale("Medium", false);
		this.addConfigurationsExoScale("Medium", true);
		
		this.addConfigurationsExoScale("Large", false);
		this.addConfigurationsExoScale("Large", true);
		
		this.addConfigurationsExoScale("Extra-large", false);
		this.addConfigurationsExoScale("Etra-large", true);
		
		this.addConfigurationsExoScale("Huge", false);
		this.addConfigurationsExoScale("Huge", true);
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}
}
