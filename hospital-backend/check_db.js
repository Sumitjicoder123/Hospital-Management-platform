const { Client } = require('pg');

async function run() {
    const client = new Client({
        connectionString: "postgresql://postgres:SumitDubey@1234@db.lvaymongduklhaaklomk.supabase.co:5432/postgres"
    });
    
    await client.connect();
    
    console.log("Finding all connections...");
    const res = await client.query("SELECT pid, state, wait_event_type, wait_event, query FROM pg_stat_activity WHERE state IS NOT NULL AND pid <> pg_backend_pid()");
    
    for (const row of res.rows) {
        console.log(`PID: ${row.pid} | State: ${row.state} | Wait: ${row.wait_event_type}/${row.wait_event} | Query: ${row.query}`);
    }
    
    console.log("Done.");
    await client.end();
}

run().catch(console.error);
