const { Client } = require('pg');

async function run() {
    const client = new Client({
        connectionString: "postgresql://postgres:SumitDubey@1234@db.lvaymongduklhaaklomk.supabase.co:5432/postgres"
    });
    
    await client.connect();
    
    console.log("Finding locked connections...");
    const res = await client.query("SELECT pid, state, query FROM pg_stat_activity WHERE state LIKE 'idle in transaction%'");
    
    for (const row of res.rows) {
        console.log(`Killing PID ${row.pid} (State: ${row.state}, Query: ${row.query})`);
        await client.query(`SELECT pg_terminate_backend(${row.pid})`);
    }
    
    console.log("Done. Locks cleared.");
    await client.end();
}

run().catch(console.error);
