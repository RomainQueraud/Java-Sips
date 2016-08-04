package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Euro;
import datas.URI;
import main.SipsRdf;

public class CityCloud extends Provider {
	
	public static CityCloud singleton = new CityCloud();
	double windowsValue = 19;

	private CityCloud() {
		this.name = "CityCloud";
		this.baseUrl = "https://www.citycloud.com/pricing-gen-one/";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.continents.add(URI.australia);
		this.continents.add(URI.southAmerica);
		this.billing = URI.hour; 
		this.currency = new Euro();
		this.phoneSupport = true;
	}
	
	public String getComment(){
		String ret = "";
		ret += "Full IPv6 support<br />";
		ret += "100% uptime guarantee<br />";
		
		return ret;
	}
	
	public void addConfigurationsCityCloud(boolean windows) throws Exception{
		List<WebElement> columns = driver.findElements(By.className("price-table"));
		for(WebElement column : columns){
			if(column.getText().contains("System disk")){
				Configuration config = new Configuration();
				config.setProvider(this);
				
				WebElement title = column.findElement(By.className("title"));
				config.setConfigName(title.getText());
				
				WebElement price = column.findElement(By.className("price"));
				double priceValue = this.extractNumber(price.getText()) * 24 * 30; //Given in hour
				if(windows){
					priceValue =+ windowsValue;
					config.setOsUri(URI.windows);
				}
				else{
					config.setOsUri(URI.linux);
				}
				config.setPrice(priceValue);
				
				List<WebElement> lis = column.findElements(By.tagName("li"));
				for(WebElement li : lis){
					//System.out.println("<-- "+li.getText());
					if(li.getText().contains("core")){
						config.setCpu(this.extractNumber(li.getText()));
						//System.out.println(config.cpu+" cpu -->");
					}
					else if(li.getText().contains("RAM")){
						config.setRam(this.extractNumber(li.getText()));
						//System.out.println(config.ram+" ram -->");
					}
					else if(li.getText().contains("disk")){
						config.setHdd(this.extractNumber(li.getText()));
						//System.out.println(config.hdd+" hdd -->");
					}
					else if(li.getText().contains("transfer")){
						config.setTransferSpeed(this.extractNumber(li.getText()));
						//System.out.println(config.transferSpeed+" transfer -->");
					}
				}
				config.setComment(this.getComment());
				config.setDate(this.getDate());
				this.configurations.add(config);
				config.println();
			}
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		this.addConfigurationsCityCloud(true);
		this.addConfigurationsCityCloud(false);
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
