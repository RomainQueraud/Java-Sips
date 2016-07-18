package provider;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class DimensionData extends Provider {
	
	public static DimensionData singleton = new DimensionData();
	int cpuClick = 31;
	int ramClick = 255;
	int diskClick = 13990;
	int cpuMin = 1;
	int cpuMax = 32;
	int ramMin = 1;
	int ramMax = 256;
	int diskMin = 10;
	int diskMax = 14000;
	int cpuActual = cpuMin;
	int ramActual = ramMin;
	int diskActual = diskMin;
	
	double crawlSpeed = 0.15;

	private DimensionData() {
		this.name = "DimensionData";
		this.baseUrl = "http://cloud.dimensiondata.com/saas-solutions/services/public-cloud/pricing";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.continents.add(URI.australia);
		this.continents.add(URI.southAmerica);
		this.continents.add(URI.africa);
		this.billing = URI.month; 
		this.currency = new Dollar();
	}
	
	public double extractNumber(String text) throws Exception{
		Pattern p = Pattern.compile("\\d+((\\.|\\,)\\d+)?");
		Matcher m = p.matcher(text);
		if(m.find()){
			return Double.parseDouble(m.group().replace(",", ""));
		}
		else{
			//System.out.println("No number found on text : "+text);
			return 0;
		}
	}
	
	public String getComment(){
		String ret ="";
		ret+= "Choose between economic, standard and high-performance disks<br />";
		ret+= "All prices are given for standard disks";
		return ret;
	}
	
	public int getCpu() throws Exception{
		return this.cpuActual;
	}
	
	public double getRam() throws Exception{
		return this.ramActual;
	}
	
	public int getDisk() throws Exception{
		return this.diskActual;
	}
	
	public double getPrice() throws Exception{
		WebElement price = driver.findElement(By.cssSelector("input#field-total-estimate"));
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		String strPrice = (String) executor.executeScript("return arguments[0].value;", price);
		return this.extractNumber(strPrice);
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
		this.mooveElement(d, "cpu-slider");
		this.cpuActual += d;
		if(this.cpuActual>this.cpuMax){
			this.cpuActual=this.cpuMax;
		}
		else if(this.cpuActual<this.cpuMin){
			this.cpuActual=this.cpuMin;
		}
	}
	
	public void mooveRam(double d) throws InterruptedException{
		this.mooveElement(d, "ram-slider");
		this.ramActual += d;
		if(this.ramActual>this.ramMax){
			this.ramActual=this.ramMax;
		}
		else if(this.ramActual<this.ramMin){
			this.ramActual=this.ramMin;
		}
	}
	
	public void mooveDisk(double d) throws InterruptedException{
		this.mooveElement(d, "storage-slider2");
		this.diskActual += d;
		if(this.diskActual>this.diskMax){
			this.diskActual=this.diskMax;
		}
		else if(this.diskActual<this.diskMin){
			this.diskActual=this.diskMin;
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		WebElement ecoCheckBox = driver.findElement(By.id("storage_options_eco")).findElement(By.tagName("input"));
		executor.executeScript("arguments[0].click();", ecoCheckBox);
		WebElement stdCheckBox = driver.findElement(By.id("storage_options_std")).findElement(By.tagName("input"));
		executor.executeScript("arguments[0].click();", stdCheckBox);
		
		Select select = new Select(driver.findElement(By.cssSelector("select.mcp")));
		select.selectByValue("2");
		
		for(diskActual = diskMin ; diskActual <= this.diskMax ;){ //increment in the mooveDisk
			for(ramActual = ramMin ; ramActual <= this.ramMax ;){
				for(cpuActual = cpuMin ; cpuActual <= this.cpuMax ;){
					Configuration config = new Configuration(); //For linux config
					config.setProvider(this);
					config.setComment(this.getComment());
					config.setCpu(this.getCpu());
					config.setRam(this.getRam());
					config.setHdd(this.getDisk());
					config.setPrice(this.getPrice());
					config.setDate(this.getDate());
					this.configurations.add(config);
					config.println();
					
					if(this.cpuActual>=this.cpuMax){
						break; //Break after the last turn
					}
					this.mooveCpu((this.cpuClick*this.crawlSpeed));
				}
				this.mooveCpu(-this.cpuClick);
				if(this.ramActual>=this.ramMax){
					break;
				}
				this.mooveRam((this.ramClick*this.crawlSpeed)); 
			}
			this.mooveRam(-this.ramClick); 
			if(this.diskActual>=this.diskMax){
				break;
			}
			this.mooveDisk((this.diskClick*this.crawlSpeed));
		}
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
