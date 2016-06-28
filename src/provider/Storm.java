package provider;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.Configuration;
import datas.Dollar;
import datas.URI;

public class Storm extends Provider {
	
	public static Storm singleton = new Storm();
	
	private Storm() {
		this.name = "Storm";
		this.baseUrl = "https://www.stormondemand.com/manage/signup/configure.html?product=XD.VM&config_id=517&zone_id=27#ssd";
		this.continents.add(URI.northAmerica);
		this.billing = URI.hour;
		this.currency = new Dollar();
	}
	
	public String getComment(boolean ssd){
		String ret = "Transfer <b>0.05 / GB</b>\n";
		if(ssd){
			ret+= "Disk <b>SSD</b>\n";
		}
		else{
			ret+= "Disk <b>HDD</b>\n";
		}
		return ret;
	}
	
	public void mooveOs(boolean windows) throws InterruptedException{
		List<WebElement> spans = driver.findElements(By.tagName("span"));
		for(WebElement span : spans){
			if(windows && span.getText().equals("Windows Servers")){
				span.click();
			}
			else if(!windows && span.getText().equals("Linux Servers")){
				span.click();
			}
		}
		Thread.sleep(2000);
		Select select = new Select(driver.findElement(By.className("zone-selector")));
		select.selectByVisibleText("Zone B - US Central");
		Thread.sleep(2000);
	}
	
	public void addConfigurationsSsd(boolean windows) throws Exception{
		List<WebElement> tabss = driver.findElements(By.className("tabs"));
		List<WebElement> lis = tabss.get(0).findElements(By.tagName("li"));
		lis.get(1).click();
		List<WebElement> tabs = driver.findElements(By.className("tab"));
		for(WebElement tab : tabs){
			if(tab.getAttribute("hash").equals("ssd")){
				List<WebElement> trs = tab.findElements(By.tagName("tr"));
				for(WebElement tr : trs){
					Configuration config = new Configuration();
					config.setProvider(this);
					WebElement label = tr.findElement(By.tagName("label"));
					config.setConfigName(label.getText());
					config.setComment(this.getComment(true));
					if(windows){
						config.setOsUri(URI.windows);
					}
					else{
						config.setOsUri(URI.linux);
					}
					
					List<WebElement> tds = tr.findElements(By.tagName("td"));
					for(WebElement td : tds){
						if(td.getText().contains("$")){
							config.setPrice(this.extractNumber(td.getText()));
						}
						else if(td.getText().contains("GB")){
							config.setRam((int)this.extractNumber(td.getText()));
						}
						else if(td.getText().contains("CPU")){
							config.setCpu((int)this.extractNumber(td.getText()));
						}
						else{
							config.setSsd((int)this.extractNumber(td.getText()));
						}
					}
					System.out.println(config);
					this.configurations.add(config);
				}
			}
		}
	}
	
	public void addConfigurationsStorm(boolean windows) throws Exception{
		List<WebElement> tabss = driver.findElements(By.className("tabs"));
		List<WebElement> lis = tabss.get(0).findElements(By.tagName("li"));
		lis.get(0).click();
		List<WebElement> tabs = driver.findElements(By.className("tab"));
		for(WebElement tab : tabs){
			if(tab.getAttribute("hash").equals("storm")){
				List<WebElement> trs = tab.findElements(By.tagName("tr"));
				for(WebElement tr : trs){
					Configuration config = new Configuration();
					config.setProvider(this);
					WebElement label = tr.findElement(By.tagName("label"));
					config.setConfigName(label.getText());
					config.setComment(this.getComment(false));
					if(windows){
						config.setOsUri(URI.windows);
					}
					else{
						config.setOsUri(URI.linux);
					}
					
					List<WebElement> tds = tr.findElements(By.tagName("td"));
					for(WebElement td : tds){
						if(td.getText().contains("$")){
							config.setPrice(this.extractNumber(td.getText()));
						}
						else if(td.getText().contains("GB")){
							config.setRam((int)this.extractNumber(td.getText()));
						}
						else if(td.getText().contains("CPU")){
							config.setCpu((int)this.extractNumber(td.getText()));
						}
						else{
							config.setHdd((int)this.extractNumber(td.getText()));
						}
					}
					System.out.println(config);
					this.configurations.add(config);
				}
			}
		}
	}
	
	public void addConfigurationsBareMetal(boolean windows) throws Exception{
		List<WebElement> tabss = driver.findElements(By.className("tabs"));
		List<WebElement> lis = tabss.get(0).findElements(By.tagName("li"));
		lis.get(2).click();
		List<WebElement> tabs = driver.findElements(By.className("tab"));
		for(WebElement tab : tabs){
			if(tab.getAttribute("hash").equals("bare_metal")){
				List<WebElement> trs = tab.findElements(By.tagName("tr"));
				for(WebElement tr : trs){
					if(!(tr.getAttribute("class").equals("subheadings"))){
						Configuration config = new Configuration();
						config.setProvider(this);
						WebElement label = tr.findElement(By.tagName("label"));
						config.setConfigName(label.getText());
						if(windows){
							config.setOsUri(URI.windows);
						}
						else{
							config.setOsUri(URI.linux);
						}
						
						List<WebElement> tds = tr.findElements(By.tagName("td"));
						config.setCpu((int)this.extractNumber(tds.get(3).getText()));
						config.setRam((int)this.extractNumber(tds.get(4).getText()));
						if(tds.get(7).getText().equals("SSD")){
							config.setSsd((int)this.extractNumber(tds.get(6).getText()));
						}
						else{
							config.setHdd((int)this.extractNumber(tds.get(6).getText()));
						}
						config.setPrice(this.extractNumber(tds.get(9).getText()));
						
						System.out.println(config);
						this.configurations.add(config);
					}
				}
			}
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		this.mooveOs(false);
		System.out.println("Storm Linux");
		this.addConfigurationsStorm(false);
		System.out.println("Ssd Linux");
		this.addConfigurationsSsd(false);
		System.out.println("BareMetal Linux");
		this.addConfigurationsBareMetal(false);
		
		this.mooveOs(true);
		System.out.println("Storm Windows");
		this.addConfigurationsStorm(true);
		System.out.println("Ssd Windows");
		this.addConfigurationsSsd(true);
		System.out.println("BareMetal Windows");
		this.addConfigurationsBareMetal(true);
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}

}
