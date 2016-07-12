package provider;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	/*
	 * (non-Javadoc)
	 * @see provider.Provider#extractNumber(java.lang.String)
	 * Necessity to override, because of the presence of the ',' between the thousands and the hundreds
	 */
	@Override
	public double extractNumber(String text) throws Exception{
		Pattern p = Pattern.compile("\\d+((\\.|\\,)\\d+)?");
		Matcher m = p.matcher(text);
		if(m.find()){
			return Double.parseDouble(m.group().replace(",", ""));
		}
		else{
			throw new Exception("No number found");
		}
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException, Exception {
		this.openFirefox();
		this.loadWebpage();
		Thread.sleep(3000);
		
		List<WebElement> boxs = driver.findElements(By.className("pricing-box"));
		//System.out.println("boxs Size : "+boxs.size());
		for(WebElement box : boxs){
			Configuration config = new Configuration();
			config.setProvider(this);
			
			box.click(); //otherwise, elements don't load
			List<WebElement> lis = box.findElements(By.tagName("li"));
			//System.out.println("lis Size : "+lis.size());
			System.out.println("------------------------");
			for(WebElement li : lis){
				//System.out.println("li text : "+li.getText());
				if(li.getText().contains("Core")){
					double cpu = this.extractNumber(li.getText());
					System.out.println("cpu : "+cpu);
					config.setCpu(cpu);
				}
				else if(li.getText().contains("month")){
					double price = this.extractNumber(li.getText());
					System.out.println("price : "+price);
					config.setPrice(price);
				}
				else if(li.getText().contains("RAM")){
					double ram = this.extractNumber(li.getText());
					System.out.println("ram : "+ram);
					config.setRam(ram);
				}
				else if(li.getText().contains("Storage")){
					double disk = this.extractNumber(li.getText());
					System.out.println("disk : "+disk);
					config.setSsd(disk);
				}
			}
			this.configurations.add(config);
		}
		
		this.closeFirefox();
		this.writeConfigurationsInCsv();
	}

}
