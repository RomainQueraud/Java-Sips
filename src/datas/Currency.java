package datas;

/* http://www.xe.com/currency/eur-euro */
public interface Currency {
	/**
	 * 
	 * @param value price in the current Currency
	 * @return the price in Dollar
	 */
	public double toDollar(double value);
	
	/**
	 * 
	 * @param value price in the current Currency
	 * @return the price in Euro
	 */
	public double toEuro(double value);
	
}
