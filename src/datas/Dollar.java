package datas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Dollar implements Currency {
	private String jsonUrl = "http://api.fixer.io/latest?base=USD";
	private double euro = 0.87835;
	
	public Dollar(){
		try {
			JSONObject ratesObject = this.getJsonData();
			this.euro = (double) ratesObject.get("EUR");
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject getJsonData() throws IOException, ParseException{
		URL url = new URL(jsonUrl);
        BufferedReader in = new BufferedReader(
        new InputStreamReader(url.openStream()));
        
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(in);
        
        String base = (String) jsonObject.get("base");
        assert(base=="USD");
        JSONObject ratesObject = (JSONObject) jsonObject.get("rates");
        
        return ratesObject;
	}
	
	@Override
	public double toEuro(double value){
		return value*this.euro;
	}

	@Override
	public double toDollar(double value) {
		return value;
	}
}
