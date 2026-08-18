const SUPABASE_URL = "https://lvaymongduklhaaklomk.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx2YXltb25nZHVrbGhhYWtsb21rIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY5Njk2MDAsImV4cCI6MjEwMjU0NTYwMH0.LxJ7Xwi_McjdFo-769Lc7QotSijaPWtkHrPLhT3YUdE";

async function run() {
    const email = `testuser${Date.now()}@example.com`;
    const signupRes = await fetch(`${SUPABASE_URL}/auth/v1/signup`, {
        method: "POST",
        headers: {
            "apikey": SUPABASE_ANON_KEY,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ email, password: "password123" })
    });
    
    const signupData = await signupRes.json();
    const token = signupData.access_token;
    
    const syncRes = await fetch("http://localhost:8080/api/auth/me", {
        headers: { "Authorization": `Bearer ${token}` }
    });
    const syncData = await syncRes.json();
    
    // Now test /api/transfers
    const transfersRes = await fetch("http://localhost:8080/api/transfers", {
        method: "GET",
        headers: {
            "Authorization": `Bearer ${syncData.token}`
        }
    });
    console.log("Transfers response status:", transfersRes.status);
    console.log("Transfers response body:", await transfersRes.text());
}
run().catch(console.error);
