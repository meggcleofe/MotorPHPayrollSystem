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

class Employee {
    String id;
    String lastName;
    String firstName;
    String birthday;
    double hourlyRate;
    double riceSubsidy;
    double phoneAllowance;
    double clothingAllowance;
    double basicSalary;

    Employee(String id,
             String lastName,
             String firstName,
             String birthday,
             double hourlyRate,
             double riceSubsidy,
             double phoneAllowance,
             double clothingAllowance,
             double basicSalary) {

        this.id = id;
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

class EmployeeRecord {
    String date;
    double hoursWorked;
    double deductedHours;

    EmployeeRecord(String date, double hoursWorked, double deductedHours) {
        this.date = date;
        this.hoursWorked = hoursWorked;
        this.deductedHours = deductedHours;
    }
}

public class MotorPHPayroll {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M/d/yy");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

    
    // SSS calculation
    public static double calculateSSS(double salary) {
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

        for (double[] range : salaryRanges) {
            if (salary >= range[0] && salary <= range[1]) {
                return range[2];
            }
        }
        return 0;
    }
    
    
    // This is the Withholding Tax Calculation based on the MotorPH data
    private static double calculateWithholdingTax(double basicSalary) {
        double tax;
        if (basicSalary <= 20833) {
            tax = 0;
        } else if (basicSalary <= 33333) {
            tax = (basicSalary - 20833) * 0.20;
        } else if (basicSalary <= 66667) {
            tax = 2500 + (basicSalary - 33333) * 0.25;
        } else if (basicSalary <= 166667) {
            tax = 10833.33 + (basicSalary - 66667) * 0.30;
        } else if (basicSalary <= 666667) {
            tax = 40833.33 + (basicSalary - 166667) * 0.32;
        } else {
            tax = 200833.33 + (basicSalary - 666667) * 0.35;
        }
        return tax;
    }
    
    
    // For the code to only deduct on the 4th week
    private static boolean applyDeductions(int weekOffset) {
        return (weekOffset == 3);
    }
    
    
    // This part of the code is to get the first Monday of a given month and year
    private static LocalDate getFirstMondayOfMonth(int year, int month) {
        LocalDate date = LocalDate.of(year, month, 1);
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    
    // This will be used to load the employee data from CSV i used (OpenCSV)
    private static HashMap<String, Employee> loadEmployeeData(String filePath) {
        HashMap<String, Employee> employeeMap = new HashMap<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] row;
            boolean firstLine = true;
            try {
                while ((row = reader.readNext()) != null) {
                    
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    
                    //this is the total columns for the csv to read
                    if (row.length < 19) {
                        continue;
                    }
                    
                    
                    String id              = row[0].trim();
                    String lastName        = row[1].trim();
                    String firstName       = row[2].trim();
                    String birthday        = row[3].trim();
                    double basicSalary     = parseDoubleOrZero(row[13]);
                    double riceSubsidy     = parseDoubleOrZero(row[14]);
                    double phoneAllowance  = parseDoubleOrZero(row[15]);
                    double clothingAllowance = parseDoubleOrZero(row[16]);
                    double hourlyRate      = parseDoubleOrZero(row[18]);
                    
                    Employee emp = new Employee(
                            id,
                            lastName,
                            firstName,
                            birthday,
                            hourlyRate,
                            riceSubsidy,
                            phoneAllowance,
                            clothingAllowance,
                            basicSalary
                    );
                    employeeMap.put(id, emp);
                }
            } catch (CsvValidationException ex) {
                Logger.getLogger(MotorPHPayroll.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            System.err.println("Error reading employee file: " + e.getMessage());
        }
        return employeeMap;
    }

    
    // I used OpenCSV to read csv file
    private static HashMap<String, List<EmployeeRecord>> loadAttendanceData(String filePath) {
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
                    
                    //this is information about columns
                    if (row.length < 6) {
                        continue;
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
        System.out.println("\nEmployee #: " + employee.id);
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
