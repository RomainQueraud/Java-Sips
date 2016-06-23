package datas;

public class BulgarianLev implements Currency {
	
	private double euro = 0.51169;
	private double dollar = 0.58281;

	@Override
	public double toDollar(double value) {
		return value*this.dollar;
	}

	@Override
	public double toEuro(double value) {
		return value*this.euro;
	}

}
