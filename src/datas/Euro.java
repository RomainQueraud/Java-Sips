package datas;

public class Euro implements Currency {
	private double dollar = 1.13825; // One Euro <=> 1.13825 Dollar
	
	@Override
	public double toDollar(double value){
		return value*this.dollar;
	}

	@Override
	public double toEuro(double value) {
		return value;
	}
}
