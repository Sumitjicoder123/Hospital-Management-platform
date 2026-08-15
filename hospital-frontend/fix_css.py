import re
import os

css_file = 'src/app/app.css'
with open(css_file, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix .action-btn
content = re.sub(r'\.action-btn\s*\{[^}]*\}', '''.action-btn {
  background: #f8fafc;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  padding: 9px 18px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}''', content)

# Fix .action-btn:hover
content = re.sub(r'\.action-btn:hover\s*\{[^}]*\}', '''.action-btn:hover {
  background: #e2e8f0;
  border-color: #cbd5e1;
}''', content)

# Fix .btn-secondary
content = re.sub(r'\.btn-secondary\s*\{[^}]*\}', '''.btn-secondary {
  background: #f8fafc;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}''', content)

# Fix .btn-secondary:hover
content = re.sub(r'\.btn-secondary:hover\s*\{[^}]*\}', '''.btn-secondary:hover {
  background: #e2e8f0;
}''', content)

# Fix .wait-display
content = re.sub(r'\.wait-display\s*\{[^}]*\}', '''.wait-display {
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}''', content)

# Fix .toast-bar
content = re.sub(r'\.toast-bar\s*\{[^}]*\}', '''.toast-bar {
  background: #ffffff;
  border-left: 4px solid #0284c7;
  color: var(--text-primary);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 12px 20px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.875rem;
  font-weight: 500;
  animation: toast-in 0.25s ease;
}''', content)

# Replace all occurrences of 'border: 1px solid var(--text-primary);' or 'border-bottom: ...' with var(--border-color)
content = content.replace('border: 1px solid var(--text-primary);', 'border: 1px solid var(--border-color);')
content = content.replace('border: 1px solid var(--text-secondary);', 'border: 1px solid var(--border-color);')
content = content.replace('border-bottom: 1px solid var(--text-primary);', 'border-bottom: 1px solid var(--border-color);')
content = content.replace('border-top: 1px solid var(--text-primary);', 'border-top: 1px solid var(--border-color);')

# Fix tag priority
content = re.sub(r'\.tag-priority\s*\{[^}]*\}', '''.tag-priority {
  background: #f1f5f9;
  color: var(--text-primary);
}''', content)

with open(css_file, 'w', encoding='utf-8') as f:
    f.write(content)

print("CSS Fixed.")
