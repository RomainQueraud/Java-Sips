package provider;

import java.io.IOException;

public class CloudSigma extends Provider{
	public static CloudSigma singleton = new CloudSigma(); 
	/*
	 * TODO (10/06/16) here will be the instructions for the dedicated crawler ?
	 */

	private CloudSigma() {
		this.name = "CloudSigma";
	}

	@Override
	public void crawlFillWriteConfigurations() throws InterruptedException, IOException{
		// TODO Auto-generated method stub
		
	}
}
