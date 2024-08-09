package edu.ncsu.csc316.cleaning.ui;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import edu.ncsu.csc316.cleaning.manager.ReportManager;

public class CleaningManagerUI {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter a file path for the room file.\n");
		String roomPath = scan.next();
		System.out.println("Enter a file path for the log file.\n");
		String logPath = scan.next();
		try {
			ReportManager manager = new ReportManager(roomPath, logPath);

			while (true) {
				String str = scan.nextLine();
				// The user is done
				if (str.equals("quit")) {
					return;
				}
				if (str.equals("report")) {
					System.out.println(manager.getRoomReport());
					continue;
				}
				try {
					// Is it a date?
					LocalDateTime.parse(str, DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));
					System.out.println(manager.getVacuumBagReport(str));
					continue;
				} catch (Exception n) {
					//Do nothing, just move along
				}
				// if not, check if its a number
				try {
					int num = Integer.parseInt(str);
					System.out.println(manager.getFrequencyReport(num));
					continue;
				} catch (Exception e) {
					// Go to next iteration
				}
			}

		} catch (FileNotFoundException e) {
			System.out.println("One or more of the files you input were not valid.");
		}
		scan.close();
	}
}
