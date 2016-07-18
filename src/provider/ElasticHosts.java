package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class ElasticHosts extends Provider {
	public static ElasticHosts singleton = new ElasticHosts();
	int cpuMax = 20000;
	int ramMax = 8;
	int diskMax = 1862;
	int windowsPrice = 15;
	double crawlSpeed = 0.2;

	private ElasticHosts() {
		this.name = "ElasticHosts";
		this.baseUrl = "https://www.elastichosts.com/pricing/#64+0,128,0,0,/,0,/,-1,-1,0,/,/,0,0,0dal-a";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.continents.add(URI.australia);
		this.billing = URI.hour; //Verified
		this.currency = new Dollar();
	}
	
	public double getMaxPrice(String s) throws Exception{
		List<WebElement> elements = driver.findElements(By.className("server-component"));
		for(WebElement element : elements){
			if(element.getText().contains(s)){
				WebElement slider = element.findElement(By.className("slider-handle"));
				Action move = new Actions(driver).dragAndDropBy(slider, 1000, 0).build();
				move.perform();
				WebElement price = element.findElement(By.className("slider-price"));
				return this.extractNumber(price.getText());
			}
		}
		throw new Exception("Element not found : "+s);
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		double cpuMaxPrice = this.getMaxPrice("CPU");
		double ramMaxPrice = this.getMaxPrice("RAM");
		double diskMaxPrice = this.getMaxPrice("HDD");
		
		for(int disk = 1 ; disk<diskMax ; disk+=diskMax*this.crawlSpeed){
			for(int cpu = 1000 ; cpu<cpuMax ; cpu+=cpuMax*this.crawlSpeed){
				for(int ram = 1 ; ram<ramMax ; ram+=ramMax*this.crawlSpeed){
					double cpuPrice = (cpuMaxPrice / cpuMax)*cpu;
					double ramPrice = (ramMaxPrice / ramMax)*ram;
					double diskPrice = (diskMaxPrice / diskMax)*disk;
							
					Configuration config = new Configuration();
					config.setProvider(this);
					config.setCpu(cpu/1000); //Given in MHz, not sure about the conversion
					config.setRam(ram);
					config.setSsd(disk);
					config.setPrice(cpuPrice + ramPrice + diskPrice);
					config.setOsUri(URI.linux);
					config.setDate(this.getDate());
					this.configurations.add(config);
					config.println();
					
					Configuration config2 = new Configuration();
					config2.setProvider(this);
					config2.setCpu(cpu/1000); //Given in MHz, not sure about the conversion
					config2.setRam(ram);
					config2.setSsd(disk);
					config2.setPrice(cpuPrice + ramPrice + diskPrice + this.windowsPrice);
					config2.setOsUri(URI.windows);
					config2.setDate(this.getDate());
					this.configurations.add(config2);
					config2.println();
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
