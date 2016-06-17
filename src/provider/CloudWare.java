package provider;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import datas.Configuration;
import datas.Offset;

public class CloudWare extends Provider{
	public static CloudWare singleton = new CloudWare(); 

	private CloudWare() {
		this.name = "CloudWare";
		this.baseUrl = "https://client.cloudware.bg/index.php?/cart/"
				+ "-lang-c_cloudservers-/&step=0&languagechange=English";
		this.crawl = true;
		this.maxOffset = 320;
		this.offset = new Offset(0,0,0,0); //step, cpu, ram, disk, transfer
	}
	
	public void removeAnnoyingElements(){
		WebElement navBar = driver.findElement(By.id("mainmenu"));
		((JavascriptExecutor)driver).executeScript("arguments[0].style = arguments[1]", navBar, "position:absolute;");
	}
	
	public double getPrice(){
		WebElement price = driver.findElement(By.className("total-price"));
		return Double.parseDouble(price.getText());
	}
	
	public void mooveCpu(int offset) throws InterruptedException{
		double oldPrice = this.getPrice();
		System.out.print("Mooving cpu slider...");
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
		System.out.println("Ok");
		this.waitForPriceChange(oldPrice);
	}
	
	public void mooveRam(int offset) throws InterruptedException{
		double oldPrice = this.getPrice();
		System.out.print("Mooving ram slider...");
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
		System.out.println("Ok");
		this.waitForPriceChange(oldPrice);
	}
	
	public void mooveDisk(int offset) throws InterruptedException{
		double oldPrice = this.getPrice();
		System.out.print("Mooving disk slider...");
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
		System.out.println("Ok");
		this.waitForPriceChange(oldPrice);
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
	
	/* Fill the configuration ArrayList with the datas obtained from the website */
	public void crawlAndFillConfigurations() throws InterruptedException{
		this.openFirefox();
		this.loadWebpage();
		this.removeAnnoyingElements();
		
		assert(this.offset.cpu==0); //Sinon, deplacer le curseur pour le remettre à 0, puis mettre à jour offset.cpu
		assert(this.offset.ram==0);
		assert(this.offset.disk==0);
		while(this.offset.disk<this.maxOffset){
			while(this.offset.ram<this.maxOffset){
				while(this.offset.cpu<this.maxOffset){
					Configuration configuration = new Configuration("", this.getCpu(), this.getRam(), -1, this.getDisk(), -1, "", "bgn","", "", this.getPrice());
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
		
		this.closeFirefox();
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
				Thread.sleep(1000);
			}
		}
		System.out.println("Ok");
	}

	public static void main(String[] args){
		try {
			CloudWare.singleton.crawlAndFillConfigurations();
		} catch (InterruptedException e) {
			System.out.println("Error crawling");
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void example() throws InterruptedException{
		System.out.print("Opening Firefox...");
		driver = new FirefoxDriver();
		System.out.println("Ok");
		driver.get(baseUrl);

		/****Remove annoying elements****/
		WebElement navBar = driver.findElement(By.id("mainmenu"));
		((JavascriptExecutor)driver).executeScript("arguments[0].style = arguments[1]", navBar, "position:absolute;");
		/********************************/

		/********Test*************/
		//WebElement slider = driver.findElement(By.id("custom_slider_3377")).findElement(By.tagName("a"));
		WebElement slider = driver.findElement(By.id("custom_slider_3377")).findElement(By.tagName("a"));
		System.out.println("Slider value = "+slider.getCssValue("left"));

		WebElement price = driver.findElement(By.className("total-price"));
		System.out.println("Price value = "+price.getText());
		/************************/

		//((JavascriptExecutor)driver).executeScript("arguments[0].style = arguments[1]", slider, "width : 100%");
		/*********Action to move the slider ******/
		System.out.print("Mooving slider...");
		Actions move = new Actions(driver);
		Action action = move.dragAndDropBy(slider, 320, 0).build();
		action.perform();
		System.out.println("Ok");
		/*****************************************/

		/********Wait for price update******/
		System.out.print("Wait...");
		//Thread.sleep(2000); //TODO There is a better way of doing this
		boolean wait=true;
		double oldPrice = Double.parseDouble(price.getText());
		while(wait){
			price = driver.findElement(By.className("total-price"));
			double newPrice = Double.parseDouble(price.getText());
			if(oldPrice != newPrice){
				wait = false;
			}
			else{
				Thread.sleep(100);
			}
		}
		System.out.println("Ok");
		/**********************************/

		/********** Get information after moving *******/
		System.out.println("Slider value = "+slider.getCssValue("left"));
		price = driver.findElement(By.className("total-price"));
		System.out.println("Price value = "+price.getText());
		/***********************************************/


		driver.close();
		System.exit(0);
	}
}
