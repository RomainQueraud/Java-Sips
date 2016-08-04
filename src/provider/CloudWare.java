package provider;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.BulgarianLev;
import datas.Configuration;
import datas.Offset;
import datas.URI;
import main.SipsRdf;

public class CloudWare extends Provider{
	public static CloudWare singleton = new CloudWare();
	int cpuClick = 15;
	int ramClick = 31;
	int diskClick = 49;
	int cpuMin = 1;
	int cpuMax = 16;
	int ramMin = 0;
	int ramMax = 16;
	int diskMin = 10;
	int diskMax = 500;
	int cpuActualClick = 0;
	int ramActualClick = 0;
	int diskActualClick = 0;
	double crawlSpeed = 0.1;
	
	private CloudWare() {
		this.name = "CloudWare";
		this.baseUrl = "https://client.cloudware.bg/index.php?/cart/"
				+ "-lang-c_cloudservers-/&step=0&languagechange=English";
		this.crawl = true;
		this.maxOffset = 320;
		this.offset = new Offset(0,0,0,0); //step, cpu, ram, disk, transfer
		int step = (int) (this.maxOffset*crawlSpeed);
		this.offset.setStep(step);
		this.billing = URI.month;
		this.currency = new BulgarianLev();
		this.multipleIp = true;
		this.customizableConfiguration = true;
	}
	
	public void removeAnnoyingElements(){
		WebElement navBar = driver.findElement(By.id("mainmenu"));
		((JavascriptExecutor)driver).executeScript("arguments[0].style = arguments[1]", navBar, "position:absolute;");
	}
	
	public double getPrice(){
		WebElement price = driver.findElement(By.className("total-price")); //TODO real line
		double priceDouble = Double.parseDouble(price.getText());
		return priceDouble;
	}
	
	/*
	 * if n is negative, will perform n mooves for decreasing the number.
	 */
	public void mooveElement(int d, String sliderId) throws InterruptedException{
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
	
	public void mooveCpu(int d) throws InterruptedException{
		this.mooveElement(d, "custom_slider_3375");
		this.cpuActualClick += d;
	}
	
	public void mooveRam(int d) throws InterruptedException{
		this.mooveElement(d, "custom_slider_3376");
		this.ramActualClick += d;
	}
	
	public void mooveDisk(int d) throws InterruptedException{
		this.mooveElement(d, "custom_slider_3377");
		this.diskActualClick += d;
	}
	
	/*
	 * true : select Windows
	 * false : select Linux (arbitrary choice, because there are different linux versions available)
	 */
	public void mooveOs(boolean selectWindows){
		Select select = new Select(driver.findElement(By.id("custom_field_619")));
		if(selectWindows){
			select.selectByVisibleText("Windows 2012 x64 STD R2 ( 33.86 BGN Monthly )");
		}
		else{
			select.selectByVisibleText("Ubuntu 15.04 x64");
		}
	}
	
	public int getCpu(){
		WebElement cpu = driver.findElement(By.id("custom_slider_3375_value_indicator"));
		return Integer.parseInt(cpu.getText());
	}
	
	public int getRam(){
		WebElement ram = driver.findElement(By.id("custom_slider_3376_value_indicator"));
		return Integer.parseInt(ram.getText())/1000; //Because ram is in MB on this website
	}
	
	public int getDisk(){
		WebElement disk = driver.findElement(By.id("custom_slider_3377_value_indicator"));
		return Integer.parseInt(disk.getText());
	}
	
	public String getOs(boolean windows){
		if(windows){
			return "windows";
		}
		else{
			return "linux";
		}
	}
	
	public String getComment(){
		return "IT Service (from 110 to 270 BGN)<br />"
				+ "IPv4 addresses (3 BGN each)<br />"
				+ "Choose between HDD & SDD<br />"
				+ "One year subscription (-10%)<br />"
				+ "Two years subscription (-20%)";
	}
	
	/* Fill the configuration ArrayList with the datas obtained from the website */
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		this.openFirefox();
		this.loadWebpage();
		this.removeAnnoyingElements();
		
		assert(this.offset.cpu==0); //Otherwise, moove the cursor to 0 and update offset.cpu
		assert(this.offset.ram==0); 
		assert(this.offset.disk==0);
		boolean windows[] = {true, false}; //windows, linux
		for(Boolean os : windows){
			this.mooveOs(os);
			for(diskActualClick = 0 ; diskActualClick <= this.diskClick ;){
				for(cpuActualClick = 0 ; cpuActualClick <= this.cpuClick || ramActualClick <= this.ramClick;){
					Thread.sleep(1000);
					Configuration configuration = new Configuration("", this.getCpu(), this.getRam(), this.getDisk(), -1, -1, this.getOs(os), "bgn","", this.getComment(), this.getPrice());
					configuration.setProvider(this);
					configuration.setDate(this.getDate());
					this.configurations.add(configuration);
					configuration.println();
					this.mooveCpu((int)(this.crawlSpeed*this.cpuClick));
					this.mooveRam((int)(this.crawlSpeed*this.ramClick));
				}
				this.mooveCpu(-this.cpuClick); //To come back to the origin
				this.mooveRam(-this.ramClick);
				this.mooveDisk((int)(this.crawlSpeed*this.diskClick));
			}
			this.mooveDisk(-this.diskClick);
		}
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}
	
	public void waitForPriceChange(double oldPrice) throws InterruptedException{
		//Thread.sleep(2000); //TODO There is a better way of doing this
		boolean wait=true;
		while(wait){
			double newPrice = this.getPrice();
			if(oldPrice != newPrice){
				wait = false;
			}
			else{
				Thread.sleep(100);
			}
		}
	}
}
