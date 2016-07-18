package provider;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;
import main.SipsRdf;

/*
 * Crawl the Cpu, Ram and Disk price. The IP price is entered manually in the getIpPrice() function.
 * Calculate the price with the crawled price.
 */
public class SecureRack extends Provider {
	
	public static SecureRack singleton = new SecureRack(); 
	
	int[] availableCpu = {2, 50}; //Min and Max numbers
	int[] availableRam = {2, 77};
	int[] availableDisk = {50, 1500};
	int[] availableIp = {1, 10};
	
	int baseCpuNumber = 2;
	int baseRamNumber = 2;
	int baseDiskNumber = 50;
	int baseIpNumber = 1;
	double basePrice = 5.0;
	
	double speed = 0.25; //If too slow, too many configurations

	private SecureRack() {
		this.name = "SecureRack";
		this.baseUrl = "https://my.securerack.com/index.php?/cart/vdatacenter/";
		this.crawl = true;
		this.billing = URI.month;
		this.currency = new Euro();
	}
	
	public double getCpuPrice() throws Exception{
		WebElement cpu = driver.findElement(By.className("cf_cpu")).findElement(By.xpath(".."));
		double price;
		price = this.extractNumber(cpu.getText());
		//System.out.println("CPU price : "+price);
		return price;
	}
	
	public double getRamPrice() throws Exception{
		WebElement ram = driver.findElement(By.className("cf_memory")).findElement(By.xpath(".."));
		double price;
		price = this.extractNumber(ram.getText());
		//System.out.println("RAM price : "+price);
		return price*1000; //Because the price is given in MB on the website
	}
	
	public double getDiskPrice() throws Exception{
		WebElement disk = driver.findElement(By.className("cf_disk")).findElement(By.xpath(".."));
		double price;
		price = this.extractNumber(disk.getText());
		//System.out.println("Disk price : "+price);
		return price;
	}
	
	public String getComment(){
		String comment = "Free DNS Management <br />";
		return comment;
	}
	
	public double getIpPrice(){
		return 2.50;
	}

	@Override
	public void crawlFillWriteConfigurations() throws Exception {
		this.openFirefox();
		this.loadWebpage();
		
		double cpuPrice = this.getCpuPrice();
		double ramPrice = this.getRamPrice();
		double diskPrice = this.getDiskPrice();
		double ipPrice = this.getIpPrice();
		this.closeFirefox();
		
		System.out.print("Calculating SecureRack configurations...");
		for(double cpuNumber = this.availableCpu[0] ; cpuNumber <= this.availableCpu[1] ; cpuNumber += (this.availableCpu[1] - this.availableCpu[0])*this.speed){
			//System.out.println("cpuNumber : "+cpuNumber);
			for(double ramNumber = this.availableRam[0] ; ramNumber <= this.availableRam[1] ; ramNumber += (this.availableRam[1] - this.availableRam[0])*this.speed){
				//System.out.println("ramNumber : "+ramNumber);
				for(double diskNumber = this.availableDisk[0] ; diskNumber <= this.availableDisk[1] ; diskNumber += (this.availableDisk[1] - this.availableDisk[0])*this.speed){
					//System.out.println("diskNumber : "+diskNumber);
					for(double ipNumber = this.availableIp[0] ; ipNumber <= this.availableIp[1] ; ipNumber += (this.availableIp[1] - this.availableIp[0])*this.speed){
						//System.out.println("ipNumber : "+ipNumber);
						double price = this.basePrice + (cpuNumber - this.baseCpuNumber)*cpuPrice
								+ (ramNumber - this.baseRamNumber)*ramPrice
								+ (diskNumber - this.baseDiskNumber)*diskPrice
								+ (ipNumber - this.baseIpNumber)*ipPrice;
						price = ((price*100))/100; //Arrondi au centième
						Configuration config = new Configuration();
						config.setProvider(this);
						config.setPrice(price);
						config.setCpu(cpuNumber);
						config.setRam(ramNumber);
						config.setSsd(diskNumber);
						config.setComment(this.getComment());
						//TODO add Ip
						config.setDate(this.getDate());
						config.println();
						this.configurations.add(config);
					}
				}
			}
		}
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
