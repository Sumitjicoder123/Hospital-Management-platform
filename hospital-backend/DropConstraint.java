import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class DropConstraint {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres?sslmode=require";
        String user = "postgres.lvaymongduklhaaklomk";
        String pass = "SumitDubey@1234";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            
            // Get constraint name
            ResultSet rs = stmt.executeQuery("SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = 'patients' AND constraint_type = 'UNIQUE'");
            while(rs.next()) {
                String cName = rs.getString(1);
                stmt.execute("ALTER TABLE patients DROP CONSTRAINT " + cName);
                System.out.println("Dropped constraint: " + cName);
            }
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
