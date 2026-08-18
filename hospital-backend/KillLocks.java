import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class KillLocks {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://db.lvaymongduklhaaklomk.supabase.co:5432/postgres";
        String user = "postgres";
        String password = "SumitDubey@1234";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Finding locked connections...");
            ResultSet rs = stmt.executeQuery("SELECT pid, state, query FROM pg_stat_activity WHERE state LIKE 'idle in transaction%'");
            
            while (rs.next()) {
                int pid = rs.getInt("pid");
                String state = rs.getString("state");
                String query = rs.getString("query");
                System.out.println("Killing PID " + pid + " (State: " + state + ", Query: " + query + ")");
                
                try (Statement killStmt = conn.createStatement()) {
                    killStmt.execute("SELECT pg_terminate_backend(" + pid + ")");
                }
            }
            System.out.println("Done.");
        }
    }
}
