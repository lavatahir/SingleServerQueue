import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	private String path = "src/files/";
	private ArrayList<Double> arrivalTimes;
	private ArrayList<Double> queueDelayTimes;
	private ArrayList<Double> completionTimes;
	private int probabilityCounter;;
	
	public SSQSystem() {
		server = new Server();
		eventList = new ArrayList<Event>();
		IATimes = new ArrayList<Double>();
		occupancy = new ArrayList<Integer>();
		arrivalTimes = new ArrayList<Double>();
		arrivalTimes.add((double) 0);
		queueDelayTimes = new ArrayList<Double>();
		completionTimes = new ArrayList<Double>();
		clock = 0;
		qSize = 0;
		departures = 0;
		probabilityCounter = 0;
	}

	public void run() {
		initialize();
		while(!eventList.isEmpty()) {	//while there is a next event
			Event e = eventList.get(0);	//take first future event
			clock = e.getTime();
			if((e.getTime() >= (double) 499.9999 && e.getTime() <= (double) 500.0001) ||
			(e.getTime() >= (double) 4999.9999 && e.getTime() <= (double) 5001.0001) ||
			(e.getTime() >= (double) 9999.9999 && e.getTime() <= (double) 10001.0001)){
				System.out.println("CONTENTS OF SYSTEM AT TIME " + e.getTime());
				printEventList();
				System.out.println("Is Server Busy: " + server.isBusy());
				System.out.println("Server Utilization: " + computeAvgServerUte(e.getTime()));
			}
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
				if(server.isBusy()){
					occupancy.add(qSize + 1);	
				}
				else if(!server.isBusy()){
					occupancy.add(qSize);
				}
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

	private void printEventList() {
		for(Event e : eventList){
			e.print();
		}
	}

	public void computeArrivalTimes() {
		System.out.println("WE STARTING");
		for(int i = 1; i < MAX_DEPARTURES; i++) {
			arrivalTimes.add(i, IATimes.get(i) + arrivalTimes.get(i-1));
		}
	}
	public void computeCompletionAndDelayTimes() {
		int i = 0;
		//Since the first customer will arrive at t=0, its completion time will equal its service time
		completionTimes.add(i, server.getServiceTimes().get(0));
		
		while(i < MAX_DEPARTURES-1) {
			i++;
			if(arrivalTimes.get(i) < completionTimes.get(i-1)) {
				queueDelayTimes.add(i-1, (completionTimes.get(i-1) - arrivalTimes.get(i)));
			}
			else {
				queueDelayTimes.add(i-1, (double) 0);
			}
			completionTimes.add(i, arrivalTimes.get(i) + queueDelayTimes.get(i-1) + server.getServiceTimes().get(i));
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
	public ArrayList<Integer> getOccupancy(){
		return occupancy;
	}
	public void computeTotalWaitTimes() {
		
	}
	public double computeAvgServerUte(double time) {
		return -1;
	}
	public void importTimes() {
		Scanner scaS, scaIA;
		
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
		this.computeArrivalTimes();
		this.computeCompletionAndDelayTimes();
	}
	public void exportArrivalTimesToFile() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path + "ArrivalTimes.txt", "UTF-8");
		for(Double i : arrivalTimes) {
			writer.println(i);
		}
		writer.close();
	}
	public void exportOccupancyTimesToFile() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path + "OccupancyTimes.txt", "UTF-8");
		for(Integer i : occupancy) {
			writer.println(i);
		}
		writer.close();
	}
	public void exportCompletionTimesToFile() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path + "CompletionTimes.txt", "UTF-8");
		for(Double i : completionTimes) {
			writer.println(i);
		}
		writer.close();
	}
	public void exportQueueDelayTimesToFile() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(path + "QueueDelayTimes.txt", "UTF-8");
		for(Double i : queueDelayTimes) {
			writer.println(i);
		}
		writer.close();
	}
	public void writeFiles() {
		try {
			exportArrivalTimesToFile();
			exportOccupancyTimesToFile();
			exportCompletionTimesToFile();
			exportQueueDelayTimesToFile();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		SSQSystem s = new SSQSystem();
		s.run();
		s.writeFiles();
	}

}
