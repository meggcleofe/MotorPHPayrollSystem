package com.mycompany.motorphpayroll;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


//This class stores the employee information
class Employee {
    String empId;
    String lastName;
    String firstName;
    String birthday;
    double hourlyRate;
    double riceSubsidy;
    double phoneAllowance;
    double clothingAllowance;
    double basicSalary;

    // This is the constructor method
    // A constructor is a special method that runs when we create a new employee
    // It lets us set up all the details of the employee at once

    Employee(String empId,
             String lastName,
             String firstName,
             String birthday,
             double hourlyRate,
             double riceSubsidy,
             double phoneAllowance,
             double clothingAllowance,
             double basicSalary) {

         // Inside the constructor, we use "this" to refer to the current object's variables
        // We're saying: "Set this employee's id to the id we received as input"
        this.empId = empId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthday = birthday;
        this.hourlyRate = hourlyRate;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
        this.basicSalary = basicSalary;
    }
}

// This class stores the attendance or work record of an employee for a specific day
class EmployeeRecord {
    String date; // The date of the work record 
    double hoursWorked;  // Total number of hours the employee worked on that day
    double deductedHours; // Number of hours deducted (example: for being late or absent)

    // This is the constructor of the EmployeeRecord class
    // It allows us to create a new record and set the values immediately
    EmployeeRecord(String date, double hoursWorked, double deductedHours) {

     // Assigns the value passed into the constructor to the class variable
        this.date = date;
     // Assigns how many hours were worked
        this.hoursWorked = hoursWorked;
      // Assign show many hours were deducted
        this.deductedHours = deductedHours;
    }
}

public class MotorPHPayroll { 

 // This sets up a date formatter to format or read dates in the pattern M/d/yy (e.g., 3/31/25)
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");

// This sets up a time formatter to format or read time in the pattern H:mm (e.g., 9:45 or 13:30)
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

    
    // SSS calculation method 
    public static double calculateSSS(double salary) {
 // This is a 2D array that holds salary ranges and the corresponding SSS contribution
    // Each row = {minimum salary, maximum salary, SSS contribution}
        double[][] salaryRanges = {
            {0, 3249, 135}, {3250, 3749, 157.5}, {3750, 4249, 180}, {4250, 4749, 202.5},
            {4750, 5249, 225}, {5250, 5749, 247.5}, {5750, 6249, 270}, {6250, 6749, 292.5},
            {6750, 7249, 315}, {7250, 7749, 337.5}, {7750, 8249, 360}, {8250, 8749, 382.5},
            {8750, 9249, 405}, {9250, 9749, 427.5}, {9750, 10249, 450}, {10250, 10749, 472.5},
            {10750, 11249, 495}, {11250, 11749, 517.5}, {11750, 12249, 540}, {12250, 12749, 562.5},
            {12750, 13249, 585}, {13250, 13749, 607.5}, {13750, 14249, 630}, {14250, 14749, 652.5},
            {14750, 15249, 675}, {15250, 15749, 697.5}, {15750, 16249, 720}, {16250, 16749, 742.5},
            {16750, 17249, 765}, {17250, 17749, 787.5}, {17750, 18249, 810}, {18250, 18749, 832.5},
            {18750, 19249, 855}, {19250, 19749, 877.5}, {19750, 20249, 900}, {20250, 20749, 922.5},
            {20750, 21249, 945}, {21250, 21749, 967.5}, {21750, 22249, 990}, {22250, 22749, 1012.5},
            {22750, 23249, 1035}, {23250, 23749, 1057.5}, {23750, 24249, 1080}, {24250, 24749, 1102.5},
            {24750, Double.MAX_VALUE, 1125}
        };
    // Loop through each range in the salaryRanges table
        for (double[] range : salaryRanges) {
 // Check if the input salary falls within the current range
            if (salary >= range[0] && salary <= range[1]) {
                return range[2];
            }
        }   // In case salary doesn't match (this shouldn't happen), return 0
        return 0;
    }
    
    
    // This is the Withholding Tax Calculation based on the MotorPH data
    private static double calculateWithholdingTax(double basicSalary) {
        double tax; // This will store the result of the tax to be returned 

       // If the salary is 20,833 or less, there's no tax
        if (basicSalary <= 20833) {
            tax = 0; 
 // If the salary is more than 20,833 but less than or equal to 33,333
    // Tax is 20% of the amount over 20,833
        } else if (basicSalary <= 33333) {
            tax = (basicSalary - 20833) * 0.20;

    // If salary is more than 33,333 but less than or equal to 66,667
    // Base tax is 2,500 + 25% of the excess over 33,333
        } else if (basicSalary <= 66667) {
            tax = 2500 + (basicSalary - 33333) * 0.25; 

    // If salary is more than 66,667 but less than or equal to 166,667
    // Base tax is 10,833.33 + 30% of the excess over 66,667
        } else if (basicSalary <= 166667) {
            tax = 10833.33 + (basicSalary - 66667) * 0.30; 

    // If salary is more than 166,667 but less than or equal to 666,667
    // Base tax is 40,833.33 + 32% of the excess over 166,667
        } else if (basicSalary <= 666667) {
            tax = 40833.33 + (basicSalary - 166667) * 0.32; 

    // If salary is more than 666,667
    // Base tax is 200,833.33 + 35% of the excess over 666,667
        } else {
            tax = 200833.33 + (basicSalary - 666667) * 0.35;
        }
        return tax; // This will return the computed tax
    }
    
    
    // For the code to only deduct on the 4th week
    private static boolean applyDeductions(int weekOffset) {
        return (weekOffset == 3);
    }
    
    
    // This part of the code is to get the first Monday of a given month and year
    private static LocalDate getFirstMondayOfMonth(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);

     // Loop forward day by day until we find a Monday
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        } 

      // Once a Monday is found, return it as the result
        return date;
    }

    
    //This method is used to load employee data from a CSV file
    private static HashMap<String, Employee> loadEmployeeData(String filePath) { 

     // This part is where the HashMap stores all employee info
    // The key is the employee's ID (String), and the value is the Employee object
        HashMap<String, Employee> employeeMap = new HashMap<>(); 

      // Try to read the CSV file using OpenCSV's CSVReader
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] row;
            boolean firstLine = true;

 
            try { 
          // This will keep reading the file one line at a time until there are no more lines
                while ((row = reader.readNext()) != null) {
                    

          // If it reads on the first line (which is the header), it will skip it
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }


                    
    for (int i = 0; i < 9; i++) { // Only checking the first 9 fields needed for Employee
    if (row[i] == null || row[i].trim().isEmpty()) {
        throw new IllegalArgumentException("❌ Missing required field at column " + i +
                                           " in row: " + Arrays.toString(row));
    }
}
                    
                    String empId              = row[0].trim();
                    String lastName        = row[1].trim();
                    String firstName       = row[2].trim();
                    String birthday        = row[3].trim(); 

           // This will convert the basic salary to a number (double)
           // If the value is empty or invalid, it becomes 0.0 using parseDoubleOrZero()
                    double basicSalary     = parseDoubleOrZero(row[13]); 

                 // This line converts rice subsidy amount to a number
                    double riceSubsidy     = parseDoubleOrZero(row[14]);
                  // This line will convert phone allowance amount to a number
                    double phoneAllowance  = parseDoubleOrZero(row[15]); 

                  // This line will convert clothing allowance amount to a number
                    double clothingAllowance = parseDoubleOrZero(row[16]); 

                   // This will convert hourly rate to a number
                    double hourlyRate      = parseDoubleOrZero(row[18]); 
                  
                    // This will create an Employee object using the values above
                    Employee emp = new Employee(
                            empId,
                            lastName,
                            firstName,
                            birthday,
                            hourlyRate,
                            riceSubsidy,
                            phoneAllowance,
                            clothingAllowance,
                            basicSalary
                    ); 

               //After the employee data above, this line stores them in a map (like a mini database in memory) using the employee ID as the key.
                    employeeMap.put(empId, emp);
                } 

             // This line catches errors related to CSV validation (for example if the format is wrong)
            } catch (CsvValidationException ex) {
                Logger.getLogger(MotorPHPayroll.class.getName()).log(Level.SEVERE, null, ex);
            } 
         // This block catches file-related errors, like if the CSV file doesn't exist or can't be read
        } catch (IOException e) {
            System.err.println("Error reading employee file: " + e.getMessage());
        }
        return employeeMap;
    }

    
    // I used OpenCSV for it to read csv file
    private static HashMap<String, List<EmployeeRecord>> loadAttendanceData(String filePath) { 

    // This will store all employee attendance records
    // The key (String) is the employee ID
    // The value is a list of EmployeeRecord (one per day)
        HashMap<String, List<EmployeeRecord>> attendanceMap = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] row;
            boolean firstLine = true;
            try {
                while ((row = reader.readNext()) != null) {
                    
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    
                    if (row.length < 6) {
    throw new IllegalArgumentException("Missing data in CSV row: " + Arrays.toString(row));
}

// This will throw an error if any of the first 6 columns is empty
for (int i = 0; i < 6; i++) {
    if (row[i] == null || row[i].trim().isEmpty()) {
        throw new IllegalArgumentException("Empty column " + i + " in CSV row: " + Arrays.toString(row));
    }
}
                    
                    
                    String employeeId   = row[0].trim();
                    String dateStr      = row[3].trim();
                    String loginTimeStr = row[row.length - 2].trim();
                    String logoutTimeStr= row[row.length - 1].trim();
                    
                    try {
                        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
                        LocalTime loginTime = LocalTime.parse(loginTimeStr, timeFormatter);
                        LocalTime logoutTime = LocalTime.parse(logoutTimeStr, timeFormatter);
                        
                        // Grace period check
                        LocalTime gracePeriod = LocalTime.of(8, 10);
                        double deductedHours = 0.0;
                        if (loginTime.isAfter(gracePeriod)) {
                            long minutesLate = Duration.between(gracePeriod, loginTime).toMinutes();
                            deductedHours = minutesLate / 60.0;
                        }
                        
                        double totalHours = Duration.between(loginTime, logoutTime).toMinutes() / 60.0;
                        double adjustedHours = Math.max(totalHours - deductedHours, 0);
                        
                        attendanceMap.putIfAbsent(employeeId, new ArrayList<>());
                        attendanceMap.get(employeeId).add(new EmployeeRecord(dateStr, adjustedHours, deductedHours));
                        
                    } catch (Exception e) {
                        System.out.println("Skipping invalid data for Employee ID: " + employeeId + " on date: " + dateStr);
                    }
                }
            } catch (CsvValidationException ex) {
                Logger.getLogger(MotorPHPayroll.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            System.err.println("Error reading attendance file: " + e.getMessage());
        }
        return attendanceMap;
    }

    
    private static double parseDoubleOrZero(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    //this is the main 
    public static void main(String[] args) {
        String employeeFile = "C:\\Users\\Michiko\\Documents\\NetBeansProjects\\MotorPHPayroll\\src\\main\\java\\resources\\employee_data.csv";
        String attendanceFile = "C:\\Users\\Michiko\\Documents\\NetBeansProjects\\MotorPHPayroll\\src\\main\\java\\resources\\attendance_record.csv";

        // This is for loading data
        HashMap<String, Employee> employeeData = loadEmployeeData(employeeFile);
        HashMap<String, List<EmployeeRecord>> attendanceData = loadAttendanceData(attendanceFile);

        Scanner scanner = new Scanner(System.in);

        // This is for the output prompt
        System.out.print("Enter Employee ID (or 'exit' to quit): ");
        String inputEmployeeId = scanner.nextLine().trim();
        if (inputEmployeeId.equalsIgnoreCase("exit")) {
            scanner.close();
            return;
        }

        Employee employee = employeeData.get(inputEmployeeId);
        if (employee == null) {
            System.out.println("Employee not found.");
            scanner.close();
            return;
        }

        // This is for displaying employee information
        System.out.println("\nEmployee #: " + employee.empId);
        System.out.println("Last Name: " + employee.lastName);
        System.out.println("First Name: " + employee.firstName);
        System.out.println("Birthday: " + employee.birthday);
        System.out.println("Hourly Rate: " + employee.hourlyRate);
        System.out.println("Basic Salary: " + employee.basicSalary);
        System.out.println("Rice Subsidy: " + employee.riceSubsidy);
        System.out.println("Phone Allowance: " + employee.phoneAllowance);
        System.out.println("Clothing Allowance: " + employee.clothingAllowance + "\n");

        // After the employee id it will prompt the month
        System.out.print("Enter Month (e.g., june, july): ");
        String inputMonth = scanner.nextLine().trim();
        Month month;
        try {
            month = Month.valueOf(inputMonth.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid month. Please restart the program and try again.");
            scanner.close();
            return;
        }

        int year = 2024;  // since the data started in 2024
        LocalDate firstMonday = getFirstMondayOfMonth(year, month.getValue());
        System.out.println("\nPayroll for " + month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year + ":\n");

        double totalNet = 0.0;  // for accumulation of netpay

        for (int weekOffset = 0; weekOffset < 4; weekOffset++) {
            LocalDate startDate = firstMonday.plusWeeks(weekOffset);
            LocalDate endDate = startDate.plusDays(4);  // For the weekly calculation Monday to Friday

            double weeklyHours = 0;
            double totalDeductedHours = 0;

            // Sum up hours based on attendance
            if (attendanceData.containsKey(inputEmployeeId)) {
                for (EmployeeRecord record : attendanceData.get(inputEmployeeId)) {
                    LocalDate recordDate = LocalDate.parse(record.date, dateFormatter);
                    if (!recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)) {
                        weeklyHours += record.hoursWorked;
                        totalDeductedHours += record.deductedHours;
                    }
                }
            }

            double grossSalary = weeklyHours * employee.hourlyRate;
            double deductionForLate = totalDeductedHours * employee.hourlyRate;

            double sss = 0, pagibig = 0, philhealth = 0, tax = 0;
            if (applyDeductions(weekOffset)) {
                // For the code to only deduct from SSS, Pag-IBIG, PhilHealth on the 4th week only 
                sss = calculateSSS(grossSalary * 2);  // semi-monthly rate 
                pagibig = 100;
                philhealth = grossSalary * 0.03;
                // Withholding tax is calculated but only deducted once the netpay for the month is shown
                tax = calculateWithholdingTax(employee.basicSalary);
            }

            double totalDeduction = sss + pagibig + philhealth + deductionForLate;
            double netPay = grossSalary - totalDeduction;
            totalNet += netPay;

            // Weekly breakdown
            System.out.println("Week " + startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " "
                    + startDate.getDayOfMonth() + " - " + endDate.getDayOfMonth());
            System.out.printf("Hours Worked: %.2f%n", weeklyHours);
            System.out.printf("Gross Salary (Weekly): %.2f%n", grossSalary);
            System.out.printf("Total Late Hours Deducted: %.2f%n", totalDeductedHours);
            System.out.println("Deductions:");
            System.out.printf("    Late Deduction (Monetary): %.2f%n", deductionForLate);
            System.out.printf("    SSS: %.2f%n", sss);
            System.out.printf("    Pag-IBIG: %.2f%n", pagibig);
            System.out.printf("    Philhealth: %.2f%n", philhealth);
            System.out.printf("Net Pay (This Week): %.2f%n", netPay);
            System.out.println("------------------------");

            // For the 4th week, this is where i subtracted withholding tax from the total net, and then add allowances
            if (applyDeductions(weekOffset)) {
                double allowances = employee.riceSubsidy + employee.phoneAllowance + employee.clothingAllowance;
                double finalNet = totalNet - tax + allowances;
                System.out.printf("TOTAL NET (Weeks 1-4): %.2f%n", totalNet);
                System.out.printf("Withholding Tax (Based on Basic Salary): %.2f%n", tax);
                System.out.printf("Total Allowances (Rice, Phone, Clothing): %.2f%n", allowances);
                System.out.printf("FINAL NET AFTER WITHHOLDING TAX & ALLOWANCES: %.2f%n", finalNet);
            }
        }

        scanner.close();
    }
}
