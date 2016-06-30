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
import datas.Euro;
import datas.URI;

public class MicrosoftAzure extends Provider {
	
	public static MicrosoftAzure singleton = new MicrosoftAzure();

	private MicrosoftAzure() {
		this.name = "MicrosoftAzure";
		this.baseUrl = "https://azure.microsoft.com/en-us/pricing/details/virtual-machines/#Linux";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.continents.add(URI.australia);
		this.continents.add(URI.southAmerica);
		this.billing = URI.minute; 
		this.currency = new Dollar();
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
	
	public String getComment(){
		String ret = "";
		ret+= "All prices are for the <b>Central US</b> location";
		return ret;
	}
	
	public void addConfigurationsMicrosoftAzure(boolean windows) throws Exception{
		JavascriptExecutor executor = (JavascriptExecutor)driver;

		List<WebElement> osLinks = driver.findElements(By.cssSelector("a.wa-tab"));
		for(WebElement osLink : osLinks){
			if(windows && osLink.getText().equals("Windows")){
				System.out.println("Click Windows");
				//osLink.click();
				executor.executeScript("arguments[0].click();", osLink);
			}
			else if(!windows && osLink.getText().equals("Linux")){
				System.out.println("Click Linux");
				//osLink.click();
				executor.executeScript("arguments[0].click();", osLink);
			}
		}
		Thread.sleep(2000);

		List<WebElement> trs = driver.findElements(By.cssSelector("tr.wa-row-divider"));
		for(WebElement tr : trs){
			if(tr.getText().contains("$")){
				//System.out.println("display : "+tr.getCssValue("display"));
				Configuration config = new Configuration();
				config.setProvider(this);
				config.setComment(this.getComment());

				List<WebElement> tds = tr.findElements(By.tagName("td"));
				config.setConfigName(tds.get(0).getText());
				config.setCpu((int)this.extractNumber(tds.get(1).getText()));
				config.setRam((int)this.extractNumber(tds.get(2).getText()));
				config.setSsd((int)this.extractNumber(tds.get(3).getText()));
				if(windows){
					config.setOsUri(URI.windows);
				}
				else{
					config.setOsUri(URI.linux);
				}
				List<WebElement> prices = tds.get(4).findElements(By.className("price-data "));
				config.setPrice(this.extractNumber(prices.get(1).getText()));

				this.configurations.add(config);
				System.out.println(config);
			}
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		this.addConfigurationsMicrosoftAzure(true);
		this.addConfigurationsMicrosoftAzure(false);
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}

}
