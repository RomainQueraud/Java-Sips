package provider;

import java.io.IOException;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import datas.Configuration;
import datas.Dollar;
import datas.URI;
import main.SipsRdf;

public class EApps extends Provider{
	
	public static EApps singleton = new EApps(); 

	private EApps() {
		this.name = "EApps";
		this.baseUrl = "https://portal.eapps.com/order/index.php?pid=74&skip=true";
		this.crawl = true;
		this.continents.add(URI.northAmerica);
		this.billing = URI.month;
		this.currency = new Dollar();
	}
	
	public Configuration getConfiguration(String className) throws Exception{
		Configuration config = new Configuration();
		config.setProvider(this);
	
		WebElement block = driver.findElement(By.className(className));
		block.click();
		Thread.sleep(3000);
		config.setConfigName(block.findElement(By.tagName("b")).getText());
		
		List<WebElement> ps = block.findElements(By.tagName("p"));
		for(WebElement p : ps){
			if (p.getText().contains("Cores")){
				config.setCpu(this.extractNumber(p.getText()));
			}
			else if(p.getText().contains("RAM")){
				config.setRam((this.extractNumber(p.getText())/1000)); // divide 1000 because of MB
			}
			else if(p.getText().contains("Disk")){
				config.setSsd(this.extractNumber(p.getText()));
			}
		}
		
		WebElement price = driver.findElement(By.className("totaldue"));
		config.setPrice(this.extractNumber(price.getText()));
		
		config.setDate(this.getDate());
		config.println();
		return config;
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		Thread.sleep(3000);
		this.loadWebpage();
		
		Thread.sleep(20000);
		
		this.configurations.add(this.getConfiguration("predef-0"));
		this.configurations.add(this.getConfiguration("predef-1"));
		this.configurations.add(this.getConfiguration("predef-2"));
		this.configurations.add(this.getConfiguration("predef-3"));
		this.configurations.add(this.getConfiguration("predef-4"));
		
		this.closeFirefox();
		if(!SipsRdf.verbose){
			System.out.println("");
		}
		this.writeConfigurationsInCsv();
	}
}
