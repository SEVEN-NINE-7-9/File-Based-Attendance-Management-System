import java.io.*;
import java.util.Scanner;

public class AttendanceSystem {

    static final String FILE_NAME = "data/attendance.txt";

    public static void main(String[] args) throws IOException {
        // Ensure data directory exists
        new File("data").mkdirs();
        new File(FILE_NAME).createNewFile();

        Scanner sc = new Scanner(System.in);
        int choice;

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   FILE-BASED ATTENDANCE SYSTEM       ║");
        System.out.println("║         Week 7 - Java Project        ║");
        System.out.println("╚══════════════════════════════════════╝");

        do {
            System.out.println("\n┌─────────────────────────────┐");
            System.out.println("│  1. Mark Attendance          │");
            System.out.println("│  2. View Attendance          │");
            System.out.println("│  3. Search Student           │");
            System.out.println("│  4. Attendance Report        │");
            System.out.println("│  5. Delete Record            │");
            System.out.println("│  6. Exit                     │");
            System.out.println("└─────────────────────────────┘");
            System.out.print("Enter choice: ");

            while (!sc.hasNextInt()) {
                System.out.print("Invalid input. Enter a number: ");
                sc.next();
            }
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    markAttendance(sc);
                    break;
                case 2:
                    viewAttendance();
                    break;
                case 3:
                    searchStudent(sc);
                    break;
                case 4:
                    generateReport();
                    break;
                case 5:
                    deleteRecord(sc);
                    break;
                case 6:
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 6);

        sc.close();
    }

    static void markAttendance(Scanner sc) throws IOException {
        System.out.print("Student Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Roll Number: ");
        String roll = sc.nextLine().trim();
        System.out.print("Subject: ");
        String subject = sc.nextLine().trim();
        System.out.print("Status (Present/Absent/Late): ");
        String status = sc.nextLine().trim();

        String date = new java.util.Date().toString();

        FileWriter fw = new FileWriter(FILE_NAME, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(roll + "|" + name + "|" + subject + "|" + status + "|" + date);
        bw.newLine();
        bw.close();

        System.out.println("✓ Attendance saved for " + name);
    }

    static void viewAttendance() throws IOException {
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.printf("%-10s %-20s %-15s %-10s%n", "Roll No", "Name", "Subject", "Status");
        System.out.println("───────────────────────────────────────────────────────────");

        BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                System.out.printf("%-10s %-20s %-15s %-10s%n",
                    parts[0], parts[1], parts[2], parts[3]);
                count++;
            }
        }
        br.close();

        if (count == 0) System.out.println("No records found.");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("Total Records: " + count);
    }

    static void searchStudent(Scanner sc) throws IOException {
        System.out.print("Enter Name or Roll Number to search: ");
        String query = sc.nextLine().trim().toLowerCase();

        BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
        String line;
        boolean found = false;
        System.out.println("\nSearch Results:");
        System.out.println("───────────────────────────────────────────");
        while ((line = br.readLine()) != null) {
            if (line.toLowerCase().contains(query)) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    System.out.printf("Roll: %-8s Name: %-20s Subject: %-12s Status: %s%n",
                        parts[0], parts[1], parts[2], parts[3]);
                    found = true;
                }
            }
        }
        br.close();
        if (!found) System.out.println("No matching records found.");
    }

    static void generateReport() throws IOException {
        int present = 0, absent = 0, late = 0;
        java.util.Map<String, Integer> subjectCount = new java.util.HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length >= 4) {
                String status = parts[3].toLowerCase();
                if (status.contains("present")) present++;
                else if (status.contains("absent")) absent++;
                else if (status.contains("late")) late++;

                String subj = parts[2];
                subjectCount.put(subj, subjectCount.getOrDefault(subj, 0) + 1);
            }
        }
        br.close();

        int total = present + absent + late;
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║       ATTENDANCE REPORT        ║");
        System.out.println("╠════════════════════════════════╣");
        System.out.printf("║  Total Records : %-13d║%n", total);
        System.out.printf("║  Present       : %-13d║%n", present);
        System.out.printf("║  Absent        : %-13d║%n", absent);
        System.out.printf("║  Late          : %-13d║%n", late);
        if (total > 0)
            System.out.printf("║  Attendance %%  : %-12.1f%%║%n", (present * 100.0 / total));
        System.out.println("╠════════════════════════════════╣");
        System.out.println("║  By Subject:                   ║");
        for (java.util.Map.Entry<String, Integer> e : subjectCount.entrySet()) {
            System.out.printf("║  %-18s : %-8d║%n", e.getKey(), e.getValue());
        }
        System.out.println("╚════════════════════════════════╝");
    }

    static void deleteRecord(Scanner sc) throws IOException {
        System.out.print("Enter Roll Number to delete all records: ");
        String roll = sc.nextLine().trim();

        File inputFile = new File(FILE_NAME);
        File tempFile = new File("data/temp_attendance.txt");

        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));

        String line;
        int deleted = 0;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith(roll + "|")) {
                bw.write(line);
                bw.newLine();
            } else {
                deleted++;
            }
        }
        br.close();
        bw.close();

        inputFile.delete();
        tempFile.renameTo(inputFile);

        System.out.println("✓ Deleted " + deleted + " record(s) for Roll No: " + roll);
    }
}
