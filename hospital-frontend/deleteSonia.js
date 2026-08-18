const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = 'https://lvaymongduklhaaklomk.supabase.co';
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx2YXltb25nZHVrbGhhYWtsb21rIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4Njk2OTYwMCwiZXhwIjoyMTAyNTQ1NjAwfQ.zCmGOu_-zk1p0RkHLs21E5R6MYV8I1AHONIo6i6SKvg';
const supabase = createClient(supabaseUrl, supabaseKey);

async function run() {
  console.log('Finding user...');
  const { data: users, error: userErr } = await supabase.from('users').select('*').ilike('name', '%sonia%');
  if (userErr) console.error(userErr);
  
  if (users && users.length > 0) {
    console.log('Found users:', users);
    for (const u of users) {
      console.log('Deleting user:', u.name);
      await supabase.from('users').delete().eq('id', u.id);
    }
  } else {
    console.log('No user named Sonia found in users table.');
  }

  const { data: patients, error: pErr } = await supabase.from('patients').select('*').ilike('name', '%sonia%');
  if (pErr) console.error(pErr);

  if (patients && patients.length > 0) {
    console.log('Found patients:', patients);
    for (const p of patients) {
      console.log('Deleting patient:', p.name);
      await supabase.from('patients').delete().eq('id', p.id);
    }
  } else {
    console.log('No patient named Sonia found in patients table.');
  }
}
run();
