import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropAppointmentsConstraint {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require";
        String user = "postgres.lvaymongduklhaaklomk";
        String pass = "SumitDubey@1234";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_status_check");
            System.out.println("Dropped constraint: appointments_status_check");
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
