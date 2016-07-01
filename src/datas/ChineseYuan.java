package datas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ChineseYuan implements Currency {
	
	private String jsonUrl = "http://api.fixer.io/latest?base=CNY";
	private double euro;
	private double dollar;
	
	public ChineseYuan(){
		try {
			JSONObject ratesObject = this.getJsonData();
			this.euro = (double) ratesObject.get("EUR");
			this.dollar = (double) ratesObject.get("USD");
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
        assert(base=="CNY");
        JSONObject ratesObject = (JSONObject) jsonObject.get("rates");
        
        return ratesObject;
	}

	@Override
	public double toDollar(double value) {
		return value*this.dollar;
	}

	@Override
	public double toEuro(double value) {
		return value*this.euro;
	}

}
