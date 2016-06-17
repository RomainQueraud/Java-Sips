package provider;

public class Atlantic extends Provider {
	public static Atlantic singleton = new Atlantic(); 
	/*
	 * TODO (10/06/16) here will be the instructions for the dedicated crawler ?
	 */

	private Atlantic() {
		this.name = "Atlantic";
	}

	@Override
	public void crawlAndFillConfigurations() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}
}
