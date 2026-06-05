import sys, base64

def replace_file(filepath, replacements):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    for old_b64, new_b64 in replacements:
        old = base64.b64decode(old_b64).decode('utf-8')
        new = base64.b64decode(new_b64).decode('utf-8')
        content = content.replace(old, new)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

replacements = [
    ('V2lkZ2V0IGJ1aWxkKEJ1aWxkQ29udGV4dCBjb250ZXh0KSB7', 'V2lkZ2V0IGJ1aWxkKEJ1aWxkQ29udGV4dCBjb250ZXh0KSB7CiAgICBmaW5hbCBsMTBuID0gQXBwTG9jYWxpemF0aW9ucy5vZihjb250ZXh0KSE7'),
    ('bGFiZWw6IGNvbnN0IFRleHQoJ1hlbSBnaeG7jyBow6BuZycpLA==', 'bGFiZWw6IFRleHQobDEwbi52aWV3Q2FydCks'),
    ('ZXh0cmE6IHsndGl0bGUnOiAnTOG7i2NoIHPhu6wnfSw=', 'ZXh0cmE6IHsndGl0bGUnOiBsMTBuLmhpc3Rvcnl9LA=='),
    ('bGFiZWw6IGNvbnN0IFRleHQoJ0zhu4tjaCBz4butJyks', 'bGFiZWw6IFRleHQobDEwbi5oaXN0b3J5KSw=')
]
replace_file('lib/features/customer_packages/views/screens/booking_list_screen.dart', replacements)
print('Done booking_list_screen')

