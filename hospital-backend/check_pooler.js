const { Client } = require('pg');

async function run() {
    const client = new Client({
        connectionString: "postgresql://postgres.lvaymongduklhaaklomk:SumitDubey@1234@aws-0-ap-southeast-1.pooler.supabase.com:6543/postgres"
    });
    
    await client.connect();
    console.log("Connected to pooler successfully!");
    
    const res = await client.query("SELECT 1 AS num");
    console.log("Query result:", res.rows);
    
    await client.end();
}

run().catch(console.error);
