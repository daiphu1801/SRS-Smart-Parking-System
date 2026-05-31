import psycopg2
conn = psycopg2.connect('postgresql://postgres.rrmhwltgofgtvxrmfxpl:Ksupabase05NTz@aws-1-ap-southeast-1.pooler.supabase.com:5432/postgres?sslmode=require')
cur = conn.cursor()
cur.execute('DROP POLICY "Cho_phep_tat_ca_user_upload_anh" ON storage.objects;')
conn.commit()
print('Policy dropped successfully!')
conn.close()
