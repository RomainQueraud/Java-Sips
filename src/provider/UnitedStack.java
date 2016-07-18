package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import datas.ChineseYuan;
import datas.Configuration;
import datas.URI;
import main.SipsRdf;

public class UnitedStack extends Provider {
	
	public static UnitedStack singleton = new UnitedStack();
	int diskMax = 5000;
	double crawlSpeed = 0.1;

	private UnitedStack() {
		this.name = "UnitedStack";
		this.baseUrl = "https://www.ustack.com/us/uos/price/";
		this.continents.add(URI.asia);
		this.billing = URI.month; 
		this.currency = new ChineseYuan();
	}
	
	public double getPrice() throws Exception{
		return this.getBasePrice() + this.getDiskPrice();
	}
	
	public double getBasePrice() throws Exception{
		WebElement price = driver.findElement(By.className("content"))
				.findElements(By.tagName("li")).get(0).findElement(By.className("month"));
		return this.extractNumber(price.getText());
	}
	
	public double getDiskPrice() throws Exception{
		WebElement disk = driver.findElement(By.cssSelector("div.sataVolumePrice span.month"));
		return this.extractNumber(disk.getText());
	}
	
	/* Click on capacity and set the input value */
	public void setDisk(int value){
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		WebElement capacity = driver.findElement(By.cssSelector("li.disk .tablist"))
				.findElements(By.tagName("span")).get(1);
		executor.executeScript("arguments[0].click();", capacity);
		
		WebElement disk = driver.findElements(By.cssSelector("li.disk .for-tab")).get(1)
				.findElement(By.tagName("input"));
		executor.executeScript("arguments[0].value = arguments[1];", disk, value);
		
		disk.sendKeys(Keys.TAB);
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		List<WebElement> cpus = driver.findElement(By.className("vcpu")).findElements(By.tagName("li"));
		List<WebElement> oss = driver.findElement(By.className("sys")).findElements(By.tagName("li"));
		List<WebElement> rams = driver.findElement(By.className("ram")).findElements(By.tagName("li"));
		
		for(WebElement os : oss){
			WebElement osA = os.findElement(By.tagName("a"));
			if(!(osA.getAttribute("class").contains("hide"))){
				executor.executeScript("arguments[0].click();", osA);
				for(WebElement cpu : cpus){
					WebElement cpuA = cpu.findElement(By.tagName("a"));
					if(!(cpuA.getAttribute("class").contains("hide"))){
						executor.executeScript("arguments[0].click();", cpuA);
						for(WebElement ram : rams){
							WebElement ramA = ram.findElement(By.tagName("a"));
							if(!(ramA.getAttribute("class").contains("hide"))){
								executor.executeScript("arguments[0].click();", ramA);
								for(int diskValue = 10 ; diskValue<=diskMax ; diskValue+=diskMax*this.crawlSpeed){
									this.setDisk(diskValue);
									
									Configuration config = new Configuration();
									config.setProvider(this);
									
									config.setCpu(this.extractNumber(cpu.getText()));
									if(ram.getText().contains("M")){
										config.setRam(this.extractNumber(ram.getText())/1000); //Given in Mb
									}
									else{
										config.setRam(this.extractNumber(ram.getText()));
									}
									config.setHdd(diskValue);
									if(os.getText().contains("Windows")){
										config.setOsUri(URI.windows);
										config.setSsd(50);
									}
									else{
										config.setOsUri(URI.linux);
										config.setSsd(20);
									}
									config.setPrice(this.getPrice());
									
									config.setDate(this.getDate());
									this.configurations.add(config);
									config.println();
								}
							}
						}
					}
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
