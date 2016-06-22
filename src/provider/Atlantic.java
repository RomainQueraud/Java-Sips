package provider;

import java.io.IOException;

import datas.URI;

public class Atlantic extends Provider {
	public static Atlantic singleton = new Atlantic(); 
	/*
	 * TODO (10/06/16) here will be the instructions for the dedicated crawler ?
	 */

	private Atlantic() {
		this.name = "Atlantic";
		this.continentUris.add(URI.northAmerica);
		this.continentUris.add(URI.europe);
		this.continentUris.add(URI.asia);
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		// TODO Auto-generated method stub
		
	}
}
