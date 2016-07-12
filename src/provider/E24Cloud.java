package provider;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.Configuration;
import datas.Euro;
import datas.URI;

public class E24Cloud extends Provider {
	public static E24Cloud singleton = new E24Cloud(); 
	int cpuClick = 15;
	int ramClick = 63;
	int ramMax = 32;
	int diskClick = 50;
	int transferClick = 5000;
	
	double crawlSpeed = 0.20;

	private E24Cloud() {
		this.name = "E24Cloud";
		this.baseUrl = "https://www.e24cloud.com/en/price-list/";
		this.crawl = true;
		this.continents.add(URI.europe);
		this.billing = URI.hour;
		this.currency = new Euro();
	}
	
	public double getCpu() throws Exception{
		WebElement cpu = driver.findElement(By.id("cloud-cores-amount"));
		double number = this.extractNumber(cpu.getText());
		return number;
	}
	
	public double getRam() throws Exception{
		WebElement ram = driver.findElement(By.id("cloud-ram-amount"));
		double number = this.extractNumber(ram.getText());
		return number;
	}
	
	/*
	 * Add 40 because of the base offer
	 */
	public double getDisk() throws Exception{
		WebElement disk = driver.findElement(By.id("cloud-storage-amount"));
		double number = this.extractNumber(disk.getText());
		return (number)+40;
	}
	
	/*
	 * Add 50 because of the base offer
	 * Divide 1000 because it is given in GB
	 */
	public double getTransfer() throws Exception{
		WebElement transfer = driver.findElement(By.id("cloud-link-amount"));
		double number = this.extractNumber(transfer.getText());
		return (number+50)/1000;
	}
	
	public String getComment(){
		return "Expect to be charged more for Windows";
	}
	
	/*
	 * 30 times to have it monthly
	 */
	public double getPrice() throws Exception{
		WebElement price = driver.findElement(By.id("price-month"));
		double number = this.extractNumber(price.getText()); //Maybe check the extract number because of the comma
		return number*30;
	}
	
	/*
	 * if n is negative, will perform n clicks for decreasing the number.
	 */
	public void mooveElement(double d, String minusClass, String plusClass) throws InterruptedException{
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		WebElement minus = driver.findElement(By.className(minusClass));
		WebElement plus = driver.findElement(By.className(plusClass));
		if(d>=0){
			for(int i=0 ; i<d ; i++){
				//plus.click();
				executor.executeScript("arguments[0].click();", plus);
			}
		}
		else{
			for(int i=0 ; i>d ; i--){
				//minus.click();
				executor.executeScript("arguments[0].click();", minus);
			}
		}
		Thread.sleep(1000);
	}
	
	public void mooveCpu(double d) throws InterruptedException{
		this.mooveElement(d, "core-minus", "core-plus");
	}
	
	public void mooveRam(double d) throws InterruptedException{
		this.mooveElement(d, "ram-minus", "ram-plus");
	}
	
	public void mooveDisk(double d) throws InterruptedException{
		this.mooveElement(d, "hdd-minus", "hdd-plus");
	}
	
	public void mooveTransfer(double d) throws InterruptedException{
		this.mooveElement(d, "b-minus", "b-plus");
	}
	
	public void mooveOs(boolean selectWindows) throws InterruptedException{
		Select select = new Select(driver.findElement(By.id("sys")));
		if(selectWindows){
			System.out.println("Selecting Windows");
			select.selectByVisibleText("Windows 2012 R2");
		}
		else{
			System.out.println("Selecting Linux");
			select.selectByVisibleText("Ubuntu 15.04");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see provider.IProvider#crawlFillWriteConfigurations()
	 * No need for Ram loop because ram already with cpu.
	 */
	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		//boolean[] windows = {true, false};
		for(int transferActual = 0 ; transferActual <= this.transferClick ; transferActual+=(this.transferClick*this.crawlSpeed)){
			for(int diskActual = 0 ; diskActual <= this.diskClick ; diskActual+=(this.diskClick*this.crawlSpeed)){
				for(int cpuActual = 0 ; cpuActual <= this.cpuClick ; cpuActual+=(this.cpuClick*this.crawlSpeed)){
					int ramActual = 0;
					while(ramActual <= this.ramClick && this.getRam()<this.ramMax){
					//for(int ramActual = 0 ; ramActual <= this.ramClick ; ramActual+=(this.ramClick*this.crawlSpeed)){
						if(cpuActual <= (this.cpuClick-(this.cpuClick*this.crawlSpeed))){
							ramActual = this.ramClick + 1; //This way, ram doesn't increase while cpu isn't at it's maximum
						}
						else{
							this.mooveRam((this.ramClick*this.crawlSpeed));
						}
						System.out.println("ramActual : "+ramActual);
						Configuration config = new Configuration();
						config.setProvider(this);
						config.setCpu(this.getCpu());
						config.setRam(this.getRam());
						config.setSsd(this.getDisk());
						config.setTransferSpeed(this.getTransfer());
						config.setComment(this.getComment());
						config.setPrice(this.getPrice());
						this.configurations.add(config);
						System.out.println(config);
					}
					this.mooveCpu((this.cpuClick*this.crawlSpeed)); //Ram follow
				}
				this.mooveCpu(-this.cpuClick); //Ram follow
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
