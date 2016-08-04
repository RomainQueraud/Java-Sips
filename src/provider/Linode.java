package provider;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class Linode extends Provider {
	
	public static Linode singleton = new Linode();

	private Linode() {
		this.name = "Linode";
		this.baseUrl = "https://www.linode.com/pricing";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.billing = URI.hour; 
		this.currency = new Dollar();
		this.freeTrial = true;
		this.paypal = true;
		this.phoneSupport = true;
		this.webAccess = true;
		this.terminalAccess = true;
		this.multipleIp = true;
		this.backup = true;
	}
	
	@Override
	public double extractNumber(String text) throws Exception{
		Pattern p = Pattern.compile("\\.?\\d+((\\.|\\,)\\d+)?");
		Matcher m = p.matcher(text);
		if(m.find()){
			String tmp = m.group();
			if(tmp.charAt(0) == '.'){
				tmp = "0"+tmp;
			}
			return Double.parseDouble(tmp.replace(',', '.'));
		}
		else{
			return 0;
		}
	}

	/**
	 * This website is responsive, so when it is headless it doesn't show all 
	 *   the informations that we can see usually.
	 */
	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		
		WebElement button = driver.findElement(By.id("show-larger-plans"));
		executor.executeScript("arguments[0].click();", button);
		Thread.sleep(2000);
		
		WebElement table = driver.findElement(By.id("pricing-larger-plans-table"));
		WebElement tbody = table.findElement(By.tagName("tbody"));
		List<WebElement> trs = tbody.findElements(By.tagName("tr"));
		for(WebElement tr : trs){
			List<WebElement> tds = tr.findElements(By.tagName("td"));
			Configuration config = new Configuration();
			config.setProvider(this);
			config.setConfigName(tds.get(0).getText());
			config.setRam(this.extractNumber(tds.get(1).getText()));
			config.setCpu(this.extractNumber(tds.get(2).getText()));
			config.setSsd(this.extractNumber(tds.get(3).getText()));
			config.setTransferSpeed(this.extractNumber(tds.get(4).getText()));
			String price = tds.get(7).getText();
			config.setPrice(this.extractNumber(price)*24*30*0.925); //Given by hour (0.925 is a coeff, because otherwise it isn't the exact values)
			
			config.setDate(this.getDate());
			this.configurations.add(config);
			config.println();
		}
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}
}
