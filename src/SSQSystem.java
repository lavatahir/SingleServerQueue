import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;


public class SSQSystem {
	
	private Server server;
	private ArrayList<Event> eventList;
	private ArrayList<Double> IATimes;
	private double clock;
	private int qSize;
	private int departures;
	private ArrayList<Integer> occupancy;
	private final int MAX_DEPARTURES = 100000;

	public SSQSystem() {
		this.server = new Server();
		this.eventList = new ArrayList<Event>();
		this.IATimes = new ArrayList<Double>();
		this.occupancy = new ArrayList<Integer>();
		this.clock = 0;
		qSize = 0;
		departures = 0;
	}

	public void run() {
		initialize();
		while(!eventList.isEmpty()) {	//while there is a next event
			Event e = eventList.get(0);	//take first future event
			clock = e.getTime();
			
			if(e.getType().equals("arrival")) {
				qSize++;
				if(!server.isBusy()) {
					server.setBusy(true);
					Event ev = new Event("departure", server.getServiceTime()+clock);
					int pos = -1;
					
					for(Event event : eventList) {
						if(event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}
					eventList.add(pos+1, ev);
				}
				
				Event ev = new Event("arrival", getIATime() + clock);
				if(ev.getTime()-clock > 0) {
					int pos = -1;
					for (Event event : eventList) {
						if(event.getTime() < ev.getTime()) {
							pos = eventList.indexOf(event);
						}
					}
					eventList.add(pos+1, ev);
				}
			}
			
			else if (e.getType().equals("departure")){
				qSize--;
				departures++;
				occupancy.add(qSize + departures);
				if(departures == MAX_DEPARTURES){
					return;
				}
				
				server.setBusy(false);
				if(qSize>0){
					Event ev = new Event("departure", server.getServiceTime()+clock);
					server.setBusy(true);
					int pos=-1;
					for(Event event : eventList){
						if(event.getTime() < ev.getTime()){
							pos = eventList.indexOf(event);
						}
					}
				eventList.add(pos+1,ev);
				}
			}
			eventList.remove(0);
		}
	}

	public void initialize() {
		importTimes();
		Event e = new Event("arrival", 0);
		IATimes.remove(0);
		eventList.add(e);
	}
	private double getIATime() {
		if(IATimes.isEmpty()) {
			return -1;
		}
		return IATimes.remove(0);
	}
	public void printOccupancy() {
		for(Integer i : occupancy) {
			System.out.println(i);
		}
	}
	public void importTimes() {
		Scanner scaS, scaIA;
		String path = "src/files/";
		String service = "serviceTimes-100K.txt";
		String interArrivals = "interArrivalTimes-100K.txt";
		
		FileReader frS = null;
		FileReader frIA = null;
		
		try {
			frS = new FileReader(path + service);
			frIA = new FileReader(path + interArrivals);
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		
		scaS = new Scanner(frS);
		scaIA = new Scanner(frIA);
		
		ArrayList<Double> serviceTimes = new ArrayList<Double>();
		
		for(int i = 0; i < MAX_DEPARTURES; i++) {
			serviceTimes.add(scaS.nextDouble());
			IATimes.add(scaIA.nextDouble());
		}
		server.setServiceTimes(serviceTimes);
		scaS.close();
		scaIA.close();
	}
	
	public static void main(String[] args) {
		SSQSystem s = new SSQSystem();
		s.run();
		s.printOccupancy();
	}

}
