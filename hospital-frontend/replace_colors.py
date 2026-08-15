import re
import os

css_file = 'src/app/app.css'
with open(css_file, 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    r'#111827': 'var(--bg-card)',
    r'#0f172a': '#f8fafc',
    r'#0b1120': 'var(--bg-body)',
    r'#1e293b': '#e2e8f0',
    r'#334155': '#cbd5e1',
    r'#e2e8f0': 'var(--text-primary)',
    r'#f1f5f9': 'var(--text-primary)',
    r'#94a3b8': 'var(--text-muted)',
    r'#64748b': 'var(--text-secondary)',
    r'#475569': '#334155',
    r'#cbd5e1': 'var(--text-secondary)',
    r'rgba\(255,\s*255,\s*255,\s*0\.08\)': 'var(--border-color)',
    r'rgba\(255,\s*255,\s*255,\s*0\.06\)': 'var(--border-color)',
    r'rgba\(0,\s*0,\s*0,\s*0\.7\)': 'rgba(0, 0, 0, 0.3)',
    r'color:\s*white;': 'color: #ffffff;'
}

for pattern, repl in replacements.items():
    content = re.sub(pattern, repl, content, flags=re.IGNORECASE)

with open(css_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("Colors updated successfully.")
