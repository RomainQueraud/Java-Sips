package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;
import main.SipsRdf;

public class Numergy extends Provider {
	
	public static Numergy singleton = new Numergy();
	
	int diskMin = 100;
	int diskMax = 1800;
	double crawlSpeed = 0.1;

	private Numergy() {
		this.name = "Numergy";
		this.baseUrl = "https://www.numergy.com/tarifs-cloud-simulateur-prix";
		this.continents.add(URI.europe);
		this.billing = URI.month; 
		this.currency = new Euro();
	}
	
	public double getCpu() throws Exception{
		WebElement cpu = driver.findElement(By.className("js-server-size-detail"));
		String cpuText = cpu.getText().split("-")[0];
		double cpuValue = this.extractNumber(cpuText);
		return cpuValue;
	}
	
	public double getRam() throws Exception{
		WebElement ram = driver.findElement(By.className("js-server-size-detail"));
		String ramText = ram.getText().split("-")[1];
		double ramValue = this.extractNumber(ramText);
		return ramValue;
	}
	
	public double getPrice() throws Exception{
		WebElement price = driver.findElement(By.className("js-price-ttc"));
		return this.extractNumber(price.getText());
	}
	
	public void sendKey(WebElement input, char c) throws Exception {
		switch (c){
		case '0' : input.sendKeys(Keys.NUMPAD0);
		break;
		case '1' : input.sendKeys(Keys.NUMPAD1);
		break;
		case '2' : input.sendKeys(Keys.NUMPAD2);
		break;
		case '3' : input.sendKeys(Keys.NUMPAD3);
		break;
		case '4' : input.sendKeys(Keys.NUMPAD4);
		break;
		case '5' : input.sendKeys(Keys.NUMPAD5);
		break;
		case '6' : input.sendKeys(Keys.NUMPAD6);
		break;
		case '7' : input.sendKeys(Keys.NUMPAD7);
		break;
		case '8' : input.sendKeys(Keys.NUMPAD8);
		break;
		case '9' : input.sendKeys(Keys.NUMPAD9);
		break;
		default :
			throw new Exception("Unknown char number");
		}
	}
	
	public void setDisk(int value) throws Exception{
		int sleepTime = 100;
		
		WebElement disk = driver.findElement(By.id("drive-1"));
		Thread.sleep(sleepTime);
		for(int i=0 ; i<4 ; i++){
			disk.sendKeys(Keys.BACK_SPACE);
			Thread.sleep(sleepTime);
		}
		
		String strValue = ""+value;
		for(int i=0 ; i<strValue.length() ; i++){
			this.sendKey(disk, strValue.charAt(i));
			Thread.sleep(sleepTime);
		}
		disk.sendKeys(Keys.ENTER);
		Thread.sleep(sleepTime);
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		List<WebElement> oss = driver.findElement(By.className("flex5050")).findElements(By.className("column"));
		List<WebElement> cpusRams = driver.findElement(By.className("ram")).findElements(By.tagName("label"));
		
		for(WebElement os : oss){
			WebElement osLink = os.findElement(By.className("ez-radio"));
			executor.executeScript("arguments[0].click();", osLink);
			for(WebElement cpuRam : cpusRams){
				executor.executeScript("arguments[0].click();", cpuRam);
				for(int diskValue = this.diskMin ; diskValue<=this.diskMax ; diskValue+=this.diskMax*this.crawlSpeed){
					this.setDisk(diskValue);
					
					Configuration config = new Configuration();
					config.setProvider(this);
					config.setCpu(this.getCpu());
					config.setRam(this.getRam());
					config.setHdd(diskValue);
					if(os.getText().contains("Windows")){
						config.setOsUri(URI.windows);
					}
					else{
						config.setOsUri(URI.linux);
					}
					config.setPrice(this.getPrice());
					
					config.setDate(this.getDate());
					this.configurations.add(config);
					config.println();
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
