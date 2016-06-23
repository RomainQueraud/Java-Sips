package provider;

import java.io.IOException;

import datas.Dollar;
import datas.URI;

public class Atlantic extends Provider {
	public static Atlantic singleton = new Atlantic(); 
	/*
	 * TODO (10/06/16) here will be the instructions for the dedicated crawler ?
	 */

	private Atlantic() {
		this.name = "Atlantic";
		this.continents.add(URI.northAmerica);
		this.continents.add(URI.europe);
		this.continents.add(URI.asia);
		this.billing = URI.second;
		this.currency = new Dollar();
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		// TODO Auto-generated method stub
		
	}
}
