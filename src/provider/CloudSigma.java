package provider;

import java.io.IOException;

import datas.Dollar;
import datas.URI;

public class CloudSigma extends Provider{
	public static CloudSigma singleton = new CloudSigma(); 
	/*
	 * TODO (10/06/16) here will be the instructions for the dedicated crawler ?
	 */

	private CloudSigma() {
		this.name = "CloudSigma";
		this.continents.add(URI.europe);
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.asia);
		this.continents.add(URI.australia);
		this.billing = URI.month;
		this.currency = new Dollar();
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		// TODO Auto-generated method stub
		
	}
}
