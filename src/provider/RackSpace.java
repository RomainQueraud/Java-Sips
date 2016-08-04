package provider;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class RackSpace extends Provider {
	public static RackSpace singleton = new RackSpace();

	private RackSpace() {
		this.name = "RackSpace";
		this.baseUrl = "https://www.rackspace.com/cloud/servers/pricing";
		this.billing = URI.month;
		this.currency = new Dollar();
		this.alwaysSupport = true;
		this.burstResource = true;
		this.detailledSecurity = true;
		this.backup = true;
		this.api = true;
		this.phoneSupport = true;
	}
	
	/*
	 * Remove the "redirect to UK" popup
	 */
	public void removeAnnoyingElement() throws InterruptedException{
		WebElement link = driver.findElement(By.id("geoip-continue"));
		link.click();
	}
	
	/*
	 * Select the element number index that have the display property.
	 */
	public WebElement getDisplayedElement(List<WebElement> elements, int index) throws Exception{
		int cpt = 0;
		for(WebElement element : elements){
			if(!(element.getCssValue("display").equals("none"))){
				//System.out.println("display : "+element.getCssValue("display"));
				if(cpt == index){
					//System.out.println("Select : "+element.getText());
					return element;
				}
				else{
					//System.out.println("Reject : "+element.getText());
					cpt++;
				}
			}
		}
		throw new Exception("Element not found at index : "+index);
	}
	
	public void addConfigurationsRackSpace(WebElement div, boolean windows) throws Exception{		
		//TODO Click on the specified tabs, maybe set a parameter for the function
		//TODO Be carefull because it selects the display none
		
		WebElement divHorizontal = div.findElement(By.className("horizontal-scroll"));
		List<WebElement> tables = divHorizontal.findElements(By.tagName("table"));
		for(WebElement table : tables){
			if(!(table.getCssValue("display").equals("none"))){
				List<WebElement> trs = table.findElements(By.className("pricing-row"));
				for(WebElement tr : trs){
					if(!(tr.getCssValue("display").equals("none"))){
						Configuration config = new Configuration();
						config.setProvider(this);
						List<WebElement> tds = tr.findElements(By.tagName("td"));
						//System.out.println("tds Size : "+tds.size());
						
						config.setConfigName(this.getDisplayedElement(tds, 0).getText());
						config.setRam(this.extractNumber(this.getDisplayedElement(tds, 1).getText()));
						config.setCpu(this.extractNumber(this.getDisplayedElement(tds, 2).getText()));
						config.setSsd( (this.extractNumber(this.getDisplayedElement(tds, 3).getText())));
						config.setHdd(this.extractNumber(this.getDisplayedElement(tds, 4).getText()));
						config.setTransferSpeed(this.extractNumber(this.getDisplayedElement(tds, 5).getText())/1000); //Given in GB
						config.setPrice(this.extractNumber(this.getDisplayedElement(tds, 6).getText())+this.extractNumber(this.getDisplayedElement(tds, 8).getText())); //tds[7] is the +
						if(windows){
							config.setOsUri(URI.windows);
						}
						else{
							config.setOsUri(URI.linux);
						}
						
						config.println();
						config.setDate(this.getDate());
						this.configurations.add(config);
					}
				}
			}
		}
	}
	
	@Override
	public double extractNumber(String text) throws Exception{
		Pattern p = Pattern.compile("\\d+((\\.|\\,)\\d+)?");
		Matcher m = p.matcher(text);
		if(m.find()){
			return Double.parseDouble(m.group().replace(",", ""));
		}
		else{
			return 0;
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		Thread.sleep(1000);
		
		this.removeAnnoyingElement();
		Thread.sleep(1000);
		Select select = new Select(driver.findElement(By.className("pricing-table-select-time")));
		select.selectByValue("monthly");
		Select selectOs;
		WebElement tab;
		WebElement div;
		Thread.sleep(1000);
		
		tab = driver.findElement(By.cssSelector("a[href*='tab-0']"));
		executor.executeScript("arguments[0].click();", tab);
		div = driver.findElement(By.id("tab-0"));
		selectOs = new Select(div.findElement(By.className("pricing-table-select-os")));
		selectOs.selectByValue("linux");
		this.addConfigurationsRackSpace(div, false);
		selectOs.selectByValue("windows");
		this.addConfigurationsRackSpace(div, true);
		
		tab = driver.findElement(By.cssSelector("a[href*='tab-1']"));
		executor.executeScript("arguments[0].click();", tab);
		div = driver.findElement(By.id("tab-1"));
		selectOs = new Select(div.findElement(By.className("pricing-table-select-os")));
		selectOs.selectByValue("linux");
		this.addConfigurationsRackSpace(div, false);
		selectOs.selectByValue("windows");
		this.addConfigurationsRackSpace(div, true);
		
		tab = driver.findElement(By.cssSelector("a[href*='tab-2']"));
		executor.executeScript("arguments[0].click();", tab);
		div = driver.findElement(By.id("tab-2"));
		selectOs = new Select(div.findElement(By.className("pricing-table-select-os")));
		selectOs.selectByValue("linux");
		this.addConfigurationsRackSpace(div, false);
		selectOs.selectByValue("windows");
		this.addConfigurationsRackSpace(div, true);
		
		tab = driver.findElement(By.cssSelector("a[href*='tab-3']"));
		executor.executeScript("arguments[0].click();", tab);
		div = driver.findElement(By.id("tab-3"));
		selectOs = new Select(div.findElement(By.className("pricing-table-select-os")));
		selectOs.selectByValue("linux");
		this.addConfigurationsRackSpace(div, false);
		selectOs.selectByValue("windows");
		this.addConfigurationsRackSpace(div, true);
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}

}
