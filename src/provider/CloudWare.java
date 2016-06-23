package provider;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import datas.BulgarianLev;
import datas.Configuration;
import datas.Offset;
import datas.URI;

public class CloudWare extends Provider{
	public static CloudWare singleton = new CloudWare();

	private CloudWare() {
		this.name = "CloudWare";
		this.baseUrl = "https://client.cloudware.bg/index.php?/cart/"
				+ "-lang-c_cloudservers-/&step=0&languagechange=English";
		this.crawl = true;
		this.maxOffset = 320;
		this.offset = new Offset(0,0,0,0); //step, cpu, ram, disk, transfer
		double crawlSpeed = 0.3;
		int step = (int) (this.maxOffset*crawlSpeed);
		this.offset.setStep(step);
		this.billing = URI.month;
		this.currency = new BulgarianLev();
	}
	
	public void removeAnnoyingElements(){
		WebElement navBar = driver.findElement(By.id("mainmenu"));
		((JavascriptExecutor)driver).executeScript("arguments[0].style = arguments[1]", navBar, "position:absolute;");
	}
	
	public double getPrice(){
		WebElement price = driver.findElement(By.className("total-price"));
		double priceDouble = Double.parseDouble(price.getText());
		return priceDouble;
	}
	
	public void mooveCpu(int offset) throws InterruptedException{
		WebElement slider = driver.findElement(By.id("custom_slider_3375")).findElement(By.tagName("a"));
		Actions move = new Actions(driver);
		Action action = move.dragAndDropBy(slider, offset, 0).build();
		action.perform();
		if(offset==this.maxOffset){
			this.offset.cpu = this.maxOffset;
		}
		else if(offset==-this.maxOffset){
			this.offset.cpu = 0;
		}
		else{
			this.offset.cpu += offset;
		}
		System.out.println(this.offset.cpu);
	}
	
	public void mooveRam(int offset) throws InterruptedException{
		WebElement slider = driver.findElement(By.id("custom_slider_3376")).findElement(By.tagName("a"));
		Actions move = new Actions(driver);
		Action action = move.dragAndDropBy(slider, offset, 0).build();
		action.perform();
		if(offset==this.maxOffset){
			this.offset.ram = this.maxOffset;
		}
		else if(offset==-this.maxOffset){
			this.offset.ram = 0;
		}
		else{
			this.offset.ram += offset;
		}
	}
	
	public void mooveDisk(int offset) throws InterruptedException{
		WebElement slider = driver.findElement(By.id("custom_slider_3377")).findElement(By.tagName("a"));
		Actions move = new Actions(driver);
		Action action = move.dragAndDropBy(slider, offset, 0).build();
		action.perform();
		if(offset==this.maxOffset){
			this.offset.disk = this.maxOffset;
		}
		else if(offset==-this.maxOffset){
			this.offset.disk = 0;
		}
		else{
			this.offset.disk += offset;
		}
	}
	
	/*
	 * true : select Windows
	 * false : select Linux (arbitrary choice, because there are different linux versions available)
	 */
	public void mooveOs(boolean selectWindows){
		Select select = new Select(driver.findElement(By.id("custom_field_619")));
		if(selectWindows){
			System.out.println("Selecting Windows");
			select.selectByVisibleText("Windows 2012 x64 STD R2 ( 33.86 BGN Monthly )");
		}
		else{
			System.out.println("Selecting Linux");
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
			while(this.offset.disk<this.maxOffset){
				while(this.offset.ram<this.maxOffset){
					while(this.offset.cpu<this.maxOffset){
						Thread.sleep(1000);
						Configuration configuration = new Configuration("", this.getCpu(), this.getRam(), -1, this.getDisk(), -1, this.getOs(os), "bgn","", this.getComment(), this.getPrice());
						configuration.setProvider(this);
						this.configurations.add(configuration);
						this.mooveCpu(this.offset.step);
					}
					this.mooveCpu(-this.maxOffset); //To come back to the origin
					this.mooveRam(this.offset.step);
				}
				this.mooveRam(-this.maxOffset);
				this.mooveDisk(this.offset.step);
			}
			this.mooveDisk(-this.maxOffset);
		}
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}
	
	public void waitForPriceChange(double oldPrice) throws InterruptedException{
		System.out.print("Wait...");
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
		System.out.println("Ok");
	}
}
