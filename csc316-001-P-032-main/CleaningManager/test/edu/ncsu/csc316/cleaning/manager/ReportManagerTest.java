package edu.ncsu.csc316.cleaning.manager;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

public class ReportManagerTest {

	private ReportManager rm;
	private String roomPath = "input/rooms.txt";
	private String logPath = "input/cleaning_events.txt";
	private String emptyLogPath = "input/empty_cleaning_events.txt";

	@Before
	public void setUp() throws FileNotFoundException {
		rm = new ReportManager(roomPath, logPath);

	}

	@Test
	public void testGetVacuumBagReport() {

		assertEquals(rm.getVacuumBagReport("06/04/2021 12:00:00"),
				"Vacuum Bag Report (last replaced 06/04/2021 12:00:00) [\n" + "   Bag is due for replacement in 5280 "
						+ "SQ FT\n" + "]");

		assertEquals(rm.getVacuumBagReport("06/04/2021 11:37:59"),
				"Vacuum Bag Report (last replaced 06/04/2021 11:37:59) [\n" + "   Bag is due for replacement in 5195 "
						+ "SQ FT\n" + "]");

		assertEquals(rm.getVacuumBagReport("06/03/2021 19:21:22"),
				"Vacuum Bag Report (last replaced 06/03/2021 19:21:22) [\n" + "   Bag is due for replacement in 4923 "
						+ "SQ FT\n" + "]");
		
		assertEquals(rm.getVacuumBagReport("FAILURETEST"), "Date & time must be in the format: MM/DD/YYYY HH:MM:SS");
	}

	@Test
	public void testGetFrequencyReport() {

		assertEquals(rm.getFrequencyReport(0), "Number of rooms must be greater than 0.");

		assertEquals(rm.getFrequencyReport(1),
				"Frequency of Cleanings [\n" + "   Living Room has been cleaned 5 times\n]");

		assertEquals(rm.getFrequencyReport(7),
				"Frequency of Cleanings [\n" + "   Living Room has been cleaned 5 times\n"
						+ "   Dining Room has been cleaned 3 times\n" + "   Guest Bedroom has been cleaned 3 times\n"
						+ "   Guest Bathroom has been cleaned 2 times\n" + "   Office has been cleaned 2 times\n"
						+ "   Foyer has been cleaned 1 times\n" + "   Kitchen has been cleaned 0 times\n" + "]");

		assertEquals(rm.getFrequencyReport(8),
				"Frequency of Cleanings [\n" + "   Living Room has been cleaned 5 times\n"
						+ "   Dining Room has been cleaned 3 times\n" + "   Guest Bedroom has been cleaned 3 times\n"
						+ "   Guest Bathroom has been cleaned 2 times\n" + "   Office has been cleaned 2 times\n"
						+ "   Foyer has been cleaned 1 times\n" + "   Kitchen has been cleaned 0 times\n" + "]");
	}

	@Test
	public void testGetRoomReport() {

		assertEquals(rm.getRoomReport(),
				"Room Report [\n" + "   Dining Room was cleaned on [\n" + "      05/31/2021 09:27:45\n"
						+ "      05/23/2021 18:22:11\n" + "      05/21/2021 09:16:33\n" + "   ]\n"
						+ "   Foyer was cleaned on [\n" + "      05/01/2021 10:03:11\n" + "   ]\n"
						+ "   Guest Bathroom was cleaned on [\n" + "      05/17/2021 04:37:31\n"
						+ "      05/08/2021 07:01:51\n" + "   ]\n" + "   Guest Bedroom was cleaned on [\n"
						+ "      06/03/2021 19:21:22\n" + "      05/23/2021 11:51:19\n" + "      05/13/2021 22:20:34\n"
						+ "   ]\n" + "   Kitchen was cleaned on [\n" + "      (never cleaned)\n" + "   ]\n"
						+ "   Living Room was cleaned on [\n" + "      05/30/2021 10:14:41\n"
						+ "      05/28/2021 17:22:52\n" + "      05/12/2021 18:59:12\n" + "      05/11/2021 19:00:12\n"
						+ "      05/09/2021 18:44:23\n" + "   ]\n" + "   Office was cleaned on [\n"
						+ "      06/04/2021 11:37:59\n" + "      06/01/2021 13:39:01\n" + "   ]\n" + "]");

	}

	@Test
	public void testEmptyRoomReport() {
		try {
			rm = new ReportManager(roomPath, emptyLogPath);
			assertEquals(rm.getRoomReport(), "No rooms have been cleaned.");
		} catch (FileNotFoundException e) {
			// Shouldn't throw.
		}
	}

}
