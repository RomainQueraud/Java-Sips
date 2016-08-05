package datas;

/**
 * List of URI
 */
public class URI {
	
	/* Url of the website */
	public static String baseURI = "http://www.ic4.ie/SIPS/";
	
	/* Null = Nothing */
	public static String nothing = "http://dbpedia.org/page/Nothing";
	
	/* Continents */
	public static String europe = "http://dbpedia.org/page/Europe";
	public static String northAmerica = "http://dbpedia.org/page/North_America";
	public static String southAmerica = "http://dbpedia.org/page/South_America";
	public static String africa = "http://dbpedia.org/page/Africa";
	public static String asia = "http://dbpedia.org/page/Asia";
	public static String australia = "http://dbpedia.org/page/Australia";
	public static String antartica = "http://dbpedia.org/page/Antarctica";
	
	/* Billing + duration (everything is in month) */
	public static String year = "http://dbpedia.org/page/Year";
	private static double yearDuration = 12;
	public static String month = "http://dbpedia.org/page/Month";
	private static double monthDuration = 1;
	public static String week = "http://dbpedia.org/page/Week";
	private static double weekDuration = (1/30.416)*7;
	public static String day = "http://dbpedia.org/page/Day";
	private static double dayDuration = 1/30.416;
	public static String hour = "http://dbpedia.org/page/Hour";
	private static double hourDuration = URI.dayDuration/24;
	public static String minute ="http://dbpedia.org/page/Minute";
	private static double minuteDuration = URI.hourDuration/60;
	public static String second = "http://dbpedia.org/page/Second";
	private static double secondDuration = URI.minuteDuration/60;	
	
	/* Currency */
	public static String dollar = "http://dbpedia.org/page/Dollar";
	public static String euro = "http://dbpedia.org/page/Euro";
	
	/* Os */
	public static String linux = "http://dbpedia.org/page/Linux";
	public static String windows = "http://dbpedia.org/page/Microsoft_Windows";
	
	/* Properties */
	public static String freeTrial = "http://dbpedia.org/page/Freeware";
	public static String phoneSupport ="http://dbpedia.org/page/Telephone";
	public static String multipleIp ="http://dbpedia.org/page/IP_address";
	public static String webAccess ="http://dbpedia.org/page/World_Wide_Web";
	public static String burstResource ="http://dbpedia.org/page/Resource";
	public static String customizableCpu ="http://dbpedia.org/page/Central_processing_unit";
	public static String api ="http://dbpedia.org/page/Application_programming_interface";
	public static String customizableConfiguration ="http://dbpedia.org/page/Computer_configuration";
	public static String backup ="http://dbpedia.org/page/Backup";
	public static String payAsYouGo ="http://dbpedia.org/page/Pay_as_you_go";
	public static String prepaid ="http://dbpedia.org/page/Prepayment";
	public static String multipleUsers ="http://dbpedia.org/page/Multi-user";
	public static String detailledSecurity ="http://dbpedia.org/page/Application_security";
	public static String terminalAccess ="http://dbpedia.org/page/Cmd.exe";
	public static String uptimeGuarantee ="http://dbpedia.org/page/Uptime";
	public static String dedicatedServer ="http://dbpedia.org/page/Dedicated_hosting_service";
	public static String paypal ="http://dbpedia.org/page/PayPal";
	public static String alwaysSupport ="http://dbpedia.org/page/Technical_support";
	public static String environment ="http://dbpedia.org/page/Environmentalism";
	
	public static double getBillingDuration(String billingUri) throws Exception{
		if(billingUri == URI.second){
			return URI.secondDuration;
		}
		else if(billingUri == URI.minute){
			return URI.minuteDuration;
		}
		else if(billingUri == URI.hour){
			return URI.hourDuration;
		}
		else if(billingUri == URI.day){
			return URI.dayDuration;
		}
		else if(billingUri == URI.week){
			return URI.weekDuration;
		}
		else if(billingUri == URI.month){
			return URI.monthDuration;
		}
		else if(billingUri == URI.year){
			return URI.yearDuration;
		}
		else{
			throw new Exception("Billing Uri unknown");
		}
	}
}
