package edu.ncsu.csc316.cleaning.manager;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import edu.ncsu.csc316.cleaning.data.CleaningLogEntry;
import edu.ncsu.csc316.cleaning.dsa.DataStructure;
import edu.ncsu.csc316.dsa.list.List;
import edu.ncsu.csc316.dsa.map.Map;

public class CleaningManagerTest {

	private CleaningManager cm;
	private String roomPath = "input/rooms.txt";
	private String logPath = "input/cleaning_events.txt";
	private Map<String, List<CleaningLogEntry>> map;

	/**
	 * Create a new instance of CleaningManager.
	 * 
	 * @throws FileNotFoundException if given file path cannot be opened
	 */
	@Before
	public void setUp() throws FileNotFoundException {
		cm = new CleaningManager(roomPath, logPath, DataStructure.UNORDEREDLINKEDMAP);
		map = cm.getMap();
	}

	/**
	 * Tests the getEventsByRoom functionality.
	 */
	@Test
	public void testGetEventsByRoom() {

		assertEquals(map.size(), 7);
		// Checking that most recent event for this room is at front of each list
		assertEquals(map.get("Office").first().getPercentCompleted(), 51);
		assertEquals(map.get("Dining Room").first().getPercentCompleted(), 89);
		assertEquals(map.get("Guest Bedroom").first().getPercentCompleted(), 100);
		assertEquals(map.get("Living Room").first().getPercentCompleted(), 68);
		assertEquals(map.get("Guest Bathroom").first().getPercentCompleted(), 91);
		assertEquals(map.get("Foyer").first().getPercentCompleted(), 93);
		// Kitchen didn't have any events associated with it, so size is 0 which throws
		assertThrows(IndexOutOfBoundsException.class, () -> map.get("Kitchen").first().getPercentCompleted());
	}

	@Test
	public void tetsGetCoverageSince() {

		try {
			cm = new CleaningManager(roomPath, logPath);
		} catch (FileNotFoundException e) {
			fail("Should not have thrown exception");
		}

		assertEquals(map.size(), 7);
		LocalDateTime after = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
		LocalDateTime last1 = LocalDateTime.of(2021, 6, 4, 11, 37, 59);
		LocalDateTime last2 = LocalDateTime.of(2021, 6, 3, 19, 21, 22);
		LocalDateTime all = LocalDateTime.of(2021, 5, 1, 10, 0, 0);
		
		
		assertEquals(cm.getCoverageSince(after), 0);
		assertEquals(cm.getCoverageSince(last1), 85);
		assertEquals(cm.getCoverageSince(last2), 357);
		assertEquals(cm.getCoverageSince(all), 5279);

	}

}
