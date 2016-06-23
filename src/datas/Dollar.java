package datas;

public class Dollar implements Currency {
	private double euro = 0.87835;
	
	@Override
	public double toEuro(double value){
		return value*this.euro;
	}

	@Override
	public double toDollar(double value) {
		return value;
	}
}
