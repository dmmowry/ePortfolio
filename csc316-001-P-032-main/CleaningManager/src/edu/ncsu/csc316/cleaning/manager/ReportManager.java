package edu.ncsu.csc316.cleaning.manager;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import edu.ncsu.csc316.cleaning.data.CleaningLogEntry;
import edu.ncsu.csc316.cleaning.dsa.Algorithm;
import edu.ncsu.csc316.cleaning.dsa.DSAFactory;
import edu.ncsu.csc316.cleaning.dsa.DataStructure;
import edu.ncsu.csc316.dsa.list.List;
import edu.ncsu.csc316.dsa.map.Map;
import edu.ncsu.csc316.dsa.map.Map.Entry;

/**
 * Class to handle different reporting and statistics given data.
 * 
 * @author devinmowry
 *
 */
public class ReportManager {

	/** Date formatter we will be using throughout the manager. */
	public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	/** Instance of the manager */
	private CleaningManager manager;

	/** Custom comparator to organize logs by date, from most recent backwards. */
	private static final FrequencyComparator FREQ_COMPARATOR = new FrequencyComparator();
	/** Custom comparator to organize map naturally by room ID. */
	private static final NaturalRoomComparator NAT_ROOM_COMPARATOR = new NaturalRoomComparator();

	/**
	 * Constructs the ReportManager with a given Map data structure.
	 * 
	 * @param pathToRoomFile the path to the room file
	 * @param pathToLogFile  the path to the log file
	 * @param mapType        the map DS to use
	 * @throws FileNotFoundException if either of the files are not found
	 */
	public ReportManager(String pathToRoomFile, String pathToLogFile, DataStructure mapType)
			throws FileNotFoundException {
		manager = new CleaningManager(pathToRoomFile, pathToLogFile, mapType);
		DSAFactory.setListType(DataStructure.ARRAYBASEDLIST);
		DSAFactory.setComparisonSorterType(Algorithm.MERGESORT);
		DSAFactory.setNonComparisonSorterType(Algorithm.QUICKSORT);
		DSAFactory.setMapType(mapType);
	}

	/**
	 * Constructs the ReportManager with a default SkipList
	 * 
	 * @param pathToRoomFile the path to the room file
	 * @param pathToLogFile  the path to the log file
	 * @throws FileNotFoundException if either of the files are not found
	 */
	public ReportManager(String pathToRoomFile, String pathToLogFile) throws FileNotFoundException {
		this(pathToRoomFile, pathToLogFile, DataStructure.SKIPLIST);
	}

	/**
	 * Method to return a string of the vacuum bag report, which tells the date it
	 * was last replaced and the square feet it can vacuum before it is due for
	 * replacement.
	 * 
	 * @param timestamp the time the bag was last replaced
	 * @return a string report about the bag given the replacement time
	 */
	public String getVacuumBagReport(String timestamp) {
		try {
			LocalDateTime ldtime = LocalDateTime.parse(timestamp, DATE_TIME_FORMAT);
			int coverageSince = manager.getCoverageSince(ldtime);

			if (coverageSince > 5280) {
				return "Vacuum Bag Report (last replaced " + timestamp + ") [\n"
						+ "   Bag is overdue for replacement!\n]";
			}
			return "Vacuum Bag Report (last replaced " + timestamp + ") [\n" + "   Bag is due for replacement in "
					+ manager.nextReplacement(ldtime) + " SQ FT\n" + "]";
		} catch (Exception e) {
			return "Date & time must be in the format: MM/DD/YYYY HH:MM:SS";
		}

	}

	/**
	 * Method to return a string of the frequency report, which displays a given
	 * number of the most frequently cleaned rooms.
	 * 
	 * @param number of rooms user wants to include in the report
	 * @return a string report of cleanings
	 */
	@SuppressWarnings("unchecked")
	public String getFrequencyReport(int number) {
		if (number <= 0) {
			return "Number of rooms must be greater than 0.";
		}

		// Lets figure out how many times each thing has been cleaned and create a map
		// of our findings
		Map<String, List<CleaningLogEntry>> map = manager.getEventsByRoom();
		if (number >= map.size()) {
			number = map.size();
		}
		// Array of entries to sort
		Entry<String, List<CleaningLogEntry>>[] freqArr = new Entry[map.size()];

		// Copy entries into array
		int idx = 0;
		for (Entry<String, List<CleaningLogEntry>> ent : map.entrySet()) {
			freqArr[idx++] = ent;
		}

		DSAFactory.getComparisonSorter(FREQ_COMPARATOR).sort(freqArr);

		String report = "Frequency of Cleanings [";

		for (int i = 0; i < number; i++) {
			report += "\n   " + freqArr[i].getKey() + " has been cleaned " + freqArr[i].getValue().size() + " times";
		}

		report += "\n]";
		return report;
	}

	/**
	 * Reports each room and how many times they have been cleaned.
	 * 
	 * @return a string of the report.
	 */
	@SuppressWarnings("unchecked")
	public String getRoomReport() {

		StringBuilder sb = new StringBuilder("Room Report [");

		Map<String, List<CleaningLogEntry>> map = manager.getEventsByRoom();
		// Flag to make sure at least one room has been cleaned.
		boolean beenCleaned = false;
		if (map.size() == 0) {
			return "No rooms have been cleaned.";
		}

		// Sort the map just in case it is unordered
		// Array of entries to sort
		Entry<String, List<CleaningLogEntry>>[] arr = new Entry[map.size()];

		// Copy entries into array
		int idx = 0;
		for (Entry<String, List<CleaningLogEntry>> ent : map.entrySet()) {
			arr[idx++] = ent;
		}

		DSAFactory.getComparisonSorter(NAT_ROOM_COMPARATOR).sort(arr);

		// Iterate over each of the rooms and print the logs.
//		for (String roomId : map) {
		for(int i = 0; i < arr.length; i++) {
			List<CleaningLogEntry> roomLogs = arr[i].getValue();
			sb.append("\n   " + arr[i].getKey() + " was cleaned on [\n");
			String nextRoom = roomReportHelper(roomLogs);
			if (!nextRoom.equals("      (never cleaned)\n   ]")) {
				beenCleaned = true;
			}
			sb.append(nextRoom);
		}
		sb.append("\n]");

		if (beenCleaned) {
			return sb.toString();
		}
		return "No rooms have been cleaned.";
	}

	/**
	 * Helper method to format an individual rooms logs.
	 * 
	 * @param roomLogs of the room
	 * @return a formatted string of the logs
	 */
	private String roomReportHelper(List<CleaningLogEntry> roomLogs) {
		StringBuilder sb = new StringBuilder("");
		if (roomLogs.size() == 0) {
			sb.append("      (never cleaned)\n   ]");
			return sb.toString();
		}

		for (CleaningLogEntry log : roomLogs) {
			sb.append("      " + log.getTimestamp().format(DATE_TIME_FORMAT) + "\n");
		}
		sb.append("   ]");
		return sb.toString();
	}

	/**
	 * Custom comparator that sorts by size of the list of entries associates with
	 * this each room
	 * 
	 * @author devinmowry (dmmowry)
	 *
	 */
	private static class FrequencyComparator implements Comparator<Entry<String, List<CleaningLogEntry>>> {

		@Override
		public int compare(Entry<String, List<CleaningLogEntry>> e1, Entry<String, List<CleaningLogEntry>> e2) {
			if (e1.getValue().size() < e2.getValue().size()) {
				return 1;
			}
			if (e1.getValue().size() > e2.getValue().size()) {
				return -1;
			}
			// If they have been cleaned an equal number of times, order them alphabetically
			return e1.getKey().compareTo(e2.getKey());
		}
	}

	/**
	 * Custom comparator that sorts by size of the list of entries associates with
	 * this each room
	 * 
	 * @author devinmowry (dmmowry)
	 *
	 */
	private static class NaturalRoomComparator implements Comparator<Entry<String, List<CleaningLogEntry>>> {

		@Override
		public int compare(Entry<String, List<CleaningLogEntry>> e1, Entry<String, List<CleaningLogEntry>> e2) {
			return e1.getKey().compareTo(e2.getKey());
		}

	}
}