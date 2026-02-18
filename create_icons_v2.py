#!/usr/bin/env python3
"""Generate Android launcher icons from CAPY.png"""
import os
import sys

try:
    from PIL import Image
except ImportError:
    print("Installing Pillow...")
    os.system(f"{sys.executable} -m pip install pillow -q")
    from PIL import Image

source = r"c:\Users\jorvi\Desktop\CalMahAhh\CAPY.png"
base_dir = r"c:\Users\jorvi\Desktop\CalMahAhh\app\src\main\res"

if not os.path.exists(source):
    print(f"ERROR: {source} not found")
    sys.exit(1)

try:
    img = Image.open(source)
    print(f"✓ Opened {source}")
    print(f"  Original size: {img.size}")
    print(f"  Format: {img.format}")
except Exception as e:
    print(f"ERROR opening image: {e}")
    sys.exit(1)

sizes = [(48, "mipmap-mdpi"), (72, "mipmap-hdpi"), (96, "mipmap-xhdpi"), 
         (144, "mipmap-xxhdpi"), (192, "mipmap-xxxhdpi")]

for size, folder in sizes:
    folder_path = os.path.join(base_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    output = os.path.join(folder_path, "ic_launcher.png")
    resized.save(output, "PNG")
    
    file_size = os.path.getsize(output)
    print(f"✓ {size}x{size} → {output} ({file_size} bytes)")

print("\n✓ All icons generated successfully!")
