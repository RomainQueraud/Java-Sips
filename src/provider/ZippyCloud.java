package provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class ZippyCloud extends Provider {
	public static ZippyCloud singleton = new ZippyCloud();

	private ZippyCloud() {
		this.name = "ZippyCloud";
		this.baseUrl = "https://zippycloud.com/";
		this.continents.add(URI.northAmerica);
		this.billing = URI.month;
		this.currency = new Dollar();
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		List<WebElement> boxs = driver.findElements(By.className("pricing-box"));
		for(WebElement box : boxs){
			Configuration config = new Configuration();
			config.setProvider(this);
			System.out.println(box.getText());
			
			List<WebElement> lis = box.findElements(By.tagName("li"));
			for(WebElement li : lis){
				//li = li.findElement(By.xpath(".//*")); //direct child
				//System.out.println(li.getTagName());
				if(li.getText().contains("Core")){
					double cpu = this.extractNumber(li.getText());
					System.out.println("cpu : "+cpu);
					config.setPrice((int)cpu);
				}
				else if(li.getText().contains("month")){
					double price = this.extractNumber(li.getText());
					System.out.println("price : "+price);
					config.setPrice(price);
				}
				else if(li.getText().contains("RAM")){
					double ram = this.extractNumber(li.getText());
					System.out.println("ram : "+ram);
					config.setRam((int)ram);
				}
				else if(li.getText().contains("Storage")){
					double disk = this.extractNumber(li.getText());
					System.out.println("disk : "+disk);
					config.setSsd((int)disk);
				}
			}
			this.configurations.add(config);
		}
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}

}
