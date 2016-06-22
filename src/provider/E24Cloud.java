package provider;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class E24Cloud extends Provider {
	public static E24Cloud singleton = new E24Cloud(); 
	int cpuClick = 15;
	int ramClick = 63;
	int diskClick = 50;
	int transferClick = 5000;

	private E24Cloud() {
		this.name = "E24Cloud";
		this.baseUrl = "https://www.e24cloud.com/en/price-list/";
		this.crawl = true;
	}
	
	public int getCpu() throws Exception{
		WebElement cpu = driver.findElement(By.id("cloud-cores-amount"));
		double number = this.extractNumber(cpu.getText());
		return (int) number;
	}
	
	public double getRam() throws Exception{
		WebElement ram = driver.findElement(By.id("cloud-ram-amount"));
		double number = this.extractNumber(ram.getText());
		return number;
	}
	
	/*
	 * Add 40 because of the base offer
	 */
	public int getDisk() throws Exception{
		WebElement disk = driver.findElement(By.id("cloud-storage-amount"));
		double number = this.extractNumber(disk.getText());
		return ((int) number)+40;
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
	public void mooveElement(int n, String minusClass, String plusClass){
		WebElement plus = driver.findElement(By.className("core-plus"));
		WebElement minus = driver.findElement(By.className("core-minus"));
		if(n>=0){
			for(int i=0 ; i<n ; i++){
				plus.click();
			}
		}
		else{
			for(int i=0 ; i>n ; i++){
				minus.click();
			}
		}
	}
	
	public void mooveCpu(int n){
		this.mooveElement(n, "core-minus", "core-plus");
	}
	
	public void mooveRam(int n){
		this.mooveElement(n, "ram-minus", "ram-plus");
	}
	
	public void mooveDisk(int n){
		this.mooveElement(n, "hdd-minus", "hdd-plus");
	}
	
	public void mooveTransfer(int n){
		this.mooveElement(n, "b-minus", "b-plus");
	}
	
	public void mooveOs(boolean selectWindows){
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

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		this.mooveTransfer(4000);
		
		Thread.sleep(3000);
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}
}
