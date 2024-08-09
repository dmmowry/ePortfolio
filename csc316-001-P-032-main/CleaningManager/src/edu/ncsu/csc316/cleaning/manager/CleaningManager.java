package edu.ncsu.csc316.cleaning.manager;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Comparator;

import edu.ncsu.csc316.cleaning.data.CleaningLogEntry;
import edu.ncsu.csc316.cleaning.dsa.Algorithm;
import edu.ncsu.csc316.cleaning.dsa.DSAFactory;
import edu.ncsu.csc316.cleaning.dsa.DataStructure;
import edu.ncsu.csc316.dsa.list.List;
import edu.ncsu.csc316.dsa.map.Map;
import edu.ncsu.csc316.cleaning.io.InputReader;
import edu.ncsu.csc316.cleaning.data.RoomRecord;

/**
 * Class to handle majority of data manipulation.
 * 
 * @author devinmowry
 *
 */
public class CleaningManager {

	/** Array of the rooms, in alphabetical order */
	private RoomRecord[] rooms;
	/** Array of the cleaning log entries */
	private CleaningLogEntry[] logs;
	/** Map of the rooms to cleaning logs */
	private Map<String, List<CleaningLogEntry>> map;
	/** Max square feet vacuum can handle */
	private static final int MAX_SQUARE_FEET = 5280;
	/** Custom comparator to organize logs by date, from most recent backwards. */
	public static final LogTimeComparator LOG_COMPARATOR = new LogTimeComparator();
	/** Custom comparator to organize rooms naturally by roomID */
	public static final RoomComparator ROOM_COMPARATOR = new RoomComparator();
	/** Custom comparator to organize logs by their room */
	public static final LogRoomComparator LOG_ROOM_COMPARATOR = new LogRoomComparator();

	/**
	 * Constructor with given DS
	 * 
	 * @param pathToRoomFile the path to the room file
	 * @param pathToLogFile  the path to the log file
	 * @param mapType        the DS to use to implement the Map ADT
	 * @throws FileNotFoundException if either path is not found
	 */
	public CleaningManager(String pathToRoomFile, String pathToLogFile, DataStructure mapType)
			throws FileNotFoundException {
		DSAFactory.setListType(DataStructure.ARRAYBASEDLIST);
		DSAFactory.setComparisonSorterType(Algorithm.MERGESORT);
		DSAFactory.setNonComparisonSorterType(Algorithm.QUICKSORT);
		DSAFactory.setMapType(mapType);

		// SORT ROOMS
		// Janky solution, but need to sort the rooms now so we can just use them later
		// Put everything into the list initially
		List<RoomRecord> initRooms = InputReader.readRoomFile(pathToRoomFile);
		this.rooms = new RoomRecord[initRooms.size()];
		// Copy everything from logs into the array for sorting
		for (int i = 0; i < initRooms.size(); i++) {
			this.rooms[i] = initRooms.get(i);
		}

		// sort using natural comparator of roomIDs
		DSAFactory.getComparisonSorter(ROOM_COMPARATOR).sort(this.rooms);

		// SORT LOGS
		// Janky solution, but need to sort the logs now so we can just use them later

		// Put everything into the list initially
		List<CleaningLogEntry> initLogs = InputReader.readLogFile(pathToLogFile);
		this.logs = new CleaningLogEntry[initLogs.size()];
		// Copy everything from logs into the array for sorting
		for (int i = 0; i < initLogs.size(); i++) {
			this.logs[i] = initLogs.get(i);
		}

		DSAFactory.getComparisonSorter(LOG_COMPARATOR).sort(this.logs);
		this.map = getEventsByRoom();

	}

	/**
	 * Constructor with default SkipList DS.
	 * 
	 * @param pathToRoomFile the path to the room file
	 * @param pathToLogFile  the path to the log file
	 * @throws FileNotFoundException if either path is not found
	 */
	public CleaningManager(String pathToRoomFile, String pathToLogFile) throws FileNotFoundException {
		this(pathToRoomFile, pathToLogFile, DataStructure.SKIPLIST);
	}

	/**
	 * Method to create a map of roomIDs to a list of their cleaning log entries.
	 * 
	 * @return a map of roomIDs to a list of their cleaning log entries.
	 */
	public Map<String, List<CleaningLogEntry>> getEventsByRoom() {
		// Create the map instance with a natural order comparator
		map = DSAFactory.getMap(null);
		List<CleaningLogEntry> roomLogs = DSAFactory.getIndexedList();
		// Lets re-sort the logs by the room id, and make sure rooms are sorted naturally
		DSAFactory.getComparisonSorter(ROOM_COMPARATOR).sort(this.rooms);
		DSAFactory.getComparisonSorter(LOG_ROOM_COMPARATOR).sort(this.logs);

		// For each room, for each log, if the log is for this room, add it to this
		// rooms logs
		int i = 0;
		for (RoomRecord room : rooms) {
			while (i < logs.length && logs[i].getRoomID().equals(room.getRoomID())) {
				roomLogs.addLast(logs[i++]);
			}
			// Add this rooms logs to the map, then get a new instance of room logs for the next room
			map.put(room.getRoomID(), roomLogs);
			
			//Sort the map, 
			roomLogs = DSAFactory.getIndexedList();
		}
		return map;
	}

	/**
	 * Method to get the square feet the vacuum has cleaned since the given
	 * LocalDateTime.
	 * 
	 * @param time the earliest LocalDateTime we want to consider
	 * @return and int representing the square feet the vacuum has cleaned
	 */
	public int getCoverageSince(LocalDateTime time) {
		int squareFeet = 0;
		map = getEventsByRoom();

		// For each of the rooms, get the logs associated and add the square feet for
		// each log that was since the given time
		for (RoomRecord room : rooms) {
			List<CleaningLogEntry> roomlogs = map.get(room.getRoomID());
			// This room does not have associated logs, lets not go out of bounds!
			if (roomlogs.size() == 0) {
				continue;
			}
			int idx = 0;
			CleaningLogEntry log = roomlogs.get(idx);
			while (idx < roomlogs.size()) {
				log = roomlogs.get(idx);
				if (log.getTimestamp().compareTo(time) < 0) {
					break;
				}
				squareFeet = squareFeet + (room.getLength() * room.getWidth() * log.getPercentCompleted()) / 100;
				idx++;

			}
		}
		return squareFeet;
	}

	/**
	 * Method to get the square feet the vacuum can clean from now until the bag
	 * needs to be replaced.
	 * 
	 * @param ldtime the time the bag was last replaced.
	 * @return an int representing the square feet to clean before bag needs to be
	 *         replaced.
	 */
	public int nextReplacement(LocalDateTime ldtime) {
		return MAX_SQUARE_FEET - getCoverageSince(ldtime);
	}

	/**
	 * Getter for the map of room IDs to List of associated CleaningLogs.
	 * 
	 * @return the map of room IDs to List of associated CleaningLogs
	 */
	public Map<String, List<CleaningLogEntry>> getMap() {
		return map;
	}
	
	/**
	 * Getter for the array of RoomRecords
	 * 
	 * @return the map of room IDs to List of associated CleaningLogs
	 */
	public RoomRecord[] getRooms() {
		return rooms;
	}

	/**
	 * Custom comparator for CleaningLogEntry, sorts in descending time.
	 * 
	 * @author devinmowry (dmmowry)
	 *
	 */
	private static class LogTimeComparator implements Comparator<CleaningLogEntry> {

		@Override
		public int compare(CleaningLogEntry c1, CleaningLogEntry c2) {
			return -1 * c1.compareTo(c2);
		}
	}

	/**
	 * Custom comparator for CleaningLogEntry, sorts by Room ID. 
	 * If roomID the same, sort by timestamp.
	 * 
	 * @author devinmowry (dmmowry)
	 *
	 */
	private static class LogRoomComparator implements Comparator<CleaningLogEntry> {

		@Override
		public int compare(CleaningLogEntry c1, CleaningLogEntry c2) {
			int res = c1.getRoomID().compareTo(c2.getRoomID());
			return res != 0 ? res : -1 * c1.getTimestamp().compareTo(c2.getTimestamp());
		}
	}

	/**
	 * Custom comparator for RoomRecord, sorts in natural order of IDs.
	 * 
	 * @author devinmowry (dmmowry)
	 *
	 */
	private static class RoomComparator implements Comparator<RoomRecord> {

		@Override
		public int compare(RoomRecord r1, RoomRecord r2) {
			return r1.compareTo(r2);
		}
	}
}