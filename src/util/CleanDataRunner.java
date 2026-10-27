package util;

public class CleanDataRunner {
    public static void main(String[] args) {
        System.out.println("Clearing all previous data...");
        DBConnection.clearAllPreviousData();
        System.out.println("Data cleanup complete. Database is fresh and pristine!");
    }
}
