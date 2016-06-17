package datas;

public class Offset {
	public int step = 50;
	
	public int cpu;
	public int ram;
	public int disk;
	public int transfer;
	
	public Offset(int cpu, int ram, int disk, int transfer){
		this.cpu = cpu;
		this.ram = ram;
		this.disk = disk;
		this.transfer = transfer;
	}
	
	public void setStep(int step){
		this.step = step;
	}
}
