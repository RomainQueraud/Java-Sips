package provider;

import java.io.IOException;

import datas.URI;

public class CloudSigma extends Provider{
	public static CloudSigma singleton = new CloudSigma(); 
	/*
	 * TODO (10/06/16) here will be the instructions for the dedicated crawler ?
	 */

	private CloudSigma() {
		this.name = "CloudSigma";
		this.continentUris.add(URI.europe);
		this.continentUris.add(URI.northAmerica);
		this.continentUris.add(URI.asia);
		this.continentUris.add(URI.australia);
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		// TODO Auto-generated method stub
		
	}
}
