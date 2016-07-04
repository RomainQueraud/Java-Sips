package provider;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class ZettaGrid extends Provider {
	public static ZettaGrid singleton = new ZettaGrid(); 
	int cpuClick = 31;
	int ramClick = 127;
	int diskClick = 99;
	int transferClick = 398;
	
	double crawlSpeed = 0.60;

	private ZettaGrid() {
		this.name = "ZettaGrid";
		this.baseUrl = "https://account.zettagrid.com/catalog/product/configure/230/3";
		this.crawl = true;
		this.continents.add(URI.australia);
		this.billing = URI.month;
		this.currency = new Dollar();
	}
	
	public double getCpu() throws Exception{
		WebElement cpu = driver.findElement(By.id("option_820_zg-cs-processor"));
		double number = this.extractNumber(cpu.getAttribute("value"));
		return  number;
	}
	
	public double getRam() throws Exception{
		WebElement ram = driver.findElement(By.id("option_820_zg-cs-memory"));
		double number = this.extractNumber(ram.getAttribute("value"));
		return number;
	}
	
	public double getDisk() throws Exception{
		WebElement disk = driver.findElement(By.id("option_820_zg-cs-ios250-storage"));
		double number = this.extractNumber(disk.getAttribute("value"));
		return ( number);
	}
	
	public double getTransfer() throws Exception{
		WebElement transfer = driver.findElement(By.id("option_820_zg-cs-traffic"));
		double number = this.extractNumber(transfer.getAttribute("value"));
		return (number)/1000; //because GB
	}
	
	public double getPrice() throws Exception{
		WebElement price = driver.findElement(By.id("monthly_cost"));
		double number = this.extractNumber(price.getText());
		return number;
	}
	
	public double getWindowsPrice() throws Exception{
		WebElement windowsPrice = driver.findElement(By.id("option_820_zg-cs-operatingsystem"));
		Select select = new Select(windowsPrice);
		List<WebElement> options = select.getOptions();
		for(WebElement option : options){
			if(option.getText().contains("Windows")){
				return this.extractNumber(option.getText(), true);
			}
		}
		throw new Exception("Windows not found");
	}
	
	/*
	 * if n is negative, will perform n mooves for decreasing the number.
	 */
	public void mooveElement(double d, String sliderId) throws InterruptedException{
		WebElement slider = driver.findElement(By.id(sliderId)).findElement(By.className("ui-slider-handle"));
		if(d>=0){
			for(int i=0 ; i<d ; i++){
				slider.sendKeys(Keys.ARROW_RIGHT);
			}
		}
		else{
			for(int i=0 ; i>d ; i--){
				slider.sendKeys(Keys.ARROW_LEFT);
			}
		}
		Thread.sleep(1000);
	}
	
	public void mooveCpu(double d) throws InterruptedException{
		this.mooveElement(d, "slider_820_zg-cs-processor");
	}
	
	public void mooveRam(double d) throws InterruptedException{
		this.mooveElement(d, "slider_820_zg-cs-memory");
	}
	
	public void mooveDisk(double d) throws InterruptedException{
		this.mooveElement(d, "slider_820_zg-cs-ios250-storage");
	}
	
	public void mooveTransfer(double d) throws InterruptedException{
		this.mooveElement(d, "slider_820_zg-cs-traffic");
	}
	
	@Override
	public double extractNumber(String text) throws Exception{
		return this.extractNumber(text, false);
	}
	
	public double extractNumber(String text, boolean windows) throws Exception{
		if(windows){
			Pattern p1 = Pattern.compile("\\$\\d+((\\.|\\,)\\d+)?");
			Matcher m1 = p1.matcher(text);
			if(m1.find()){
				text = m1.group();
			}
			else{
				throw new Exception("No price found in text : "+text);
			}
		}
		
		Pattern p = Pattern.compile("\\d+((\\.|\\,)\\d+)?");
		Matcher m = p.matcher(text);
		if(m.find()){
			return Double.parseDouble(m.group().replace(',', '.'));
		}
		else{
			throw new Exception("No number found in text : "+text);
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		for(int transferActual = 0 ; transferActual < this.transferClick ; transferActual+=(this.transferClick*this.crawlSpeed)){
			for(int diskActual = 0 ; diskActual < this.diskClick ; diskActual+=(this.diskClick*this.crawlSpeed)){
				for(int ramActual = 0 ; ramActual < this.ramClick ; ramActual+=(this.ramClick*this.crawlSpeed)){
					for(int cpuActual = 0 ; cpuActual < this.cpuClick ; cpuActual+=(this.cpuClick*this.crawlSpeed)){
						Configuration config = new Configuration(); //For linux config
						config.setProvider(this);
						config.setCpu(this.getCpu());
						config.setRam(this.getRam());
						config.setHdd(this.getDisk());
						config.setTransferSpeed(this.getTransfer());
						config.setPrice(this.getPrice());
						config.setOsUri(URI.linux);
						this.configurations.add(config);
						System.out.println(config);
						
						Configuration config2 = new Configuration(); //For windows config
						config2.setProvider(this);
						config2.setCpu(this.getCpu());
						config2.setRam(this.getRam());
						config2.setHdd(this.getDisk());
						config2.setTransferSpeed(this.getTransfer());
						config2.setPrice(this.getPrice()+this.getWindowsPrice());
						config2.setOsUri(URI.windows);
						this.configurations.add(config2);
						System.out.println(config2);
						
						this.mooveCpu((this.cpuClick*this.crawlSpeed));
					}
					this.mooveCpu(-this.cpuClick);
					this.mooveRam((this.ramClick*this.crawlSpeed)); 
				}
				this.mooveRam(-this.ramClick); 
				this.mooveDisk((this.diskClick*this.crawlSpeed));
			}
			this.mooveDisk(-this.diskClick);
			this.mooveTransfer((this.transferClick*this.crawlSpeed));
		}
		
		Thread.sleep(3000);
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}
}
