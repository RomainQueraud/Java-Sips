package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class DreamHost extends Provider {
	
	public static DreamHost singleton = new DreamHost();

	private DreamHost() {
		this.name = "DreamHost";
		this.baseUrl = "https://www.dreamhost.com/hosting/dedicated/#pricing";
		this.billing = URI.month; 
		this.currency = new Dollar();
	}
	
	public double getPrice() throws Exception{
		WebElement price = driver.findElement(By.className("js-monthly"));
		//System.out.println("price text : "+price.getText());
		double priceValue = this.extractNumber(price.getText());
		return priceValue;
	}
	
	public String getComment(){
		String ret="";
		ret += "Transfer <b>unlimited</b><br />";
		ret += "24/7 support <br />";
		ret += "Save <b>$240</b> billing Yearly ! <br />";
		return ret;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		//Select monthly
		List<WebElement> billingDiv = driver.findElements(By.className("how-youre-paying"));
		for(WebElement billing : billingDiv){
			if(billing.findElement(By.tagName("a")).getText().contains("Monthly")){
				executor.executeScript("arguments[0].click();", billing.findElement(By.tagName("a")));
			}
		}
		
		WebElement cpuDiv = driver.findElement(By.className("cores"));
		WebElement diskDiv = driver.findElement(By.className("storage-type"));
		WebElement ramDiv = driver.findElement(By.className("ram"));
		for(WebElement cpuLi : cpuDiv.findElements(By.tagName("li"))){
			if(!(cpuLi.findElement(By.tagName("a")).getAttribute("class").contains("disabled"))){
				executor.executeScript("arguments[0].click();", cpuLi.findElement(By.tagName("a")));
				for(WebElement diskLi : diskDiv.findElements(By.tagName("li"))){
					if(!(diskLi.findElement(By.tagName("a")).getAttribute("class").contains("disabled"))){
						//System.out.println("class disk : "+diskLi.findElement(By.tagName("a")).getAttribute("class"));
						executor.executeScript("arguments[0].click();", diskLi.findElement(By.tagName("a")));
						for(WebElement ramLi : ramDiv.findElements(By.tagName("li"))){
							if(!(ramLi.findElement(By.tagName("a")).getAttribute("class").contains("disabled"))){
								//System.out.println("class ram: "+ramLi.findElement(By.tagName("a")).getAttribute("class"));
								executor.executeScript("arguments[0].click();", ramLi.findElement(By.tagName("a")));
								Configuration config = new Configuration();
								config.setProvider(this);
								
								config.setCpu((int)this.extractNumber(cpuLi.getText()));
								config.setRam((int)this.extractNumber(ramLi.getText()));
								if(diskLi.getText().contains("SSD")){
									config.setSsd((int)this.extractNumber(diskLi.getText()));
								}
								else{
									config.setHdd((int)this.extractNumber(diskLi.getText())*1000); //Given in TB
								}
								config.setComment(this.getComment());
								config.setOsUri(URI.linux);
								config.setPrice(this.getPrice());
								this.configurations.add(config);
								System.out.println(config);
							}
						}
					}
				}
			}
		}
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}

}
