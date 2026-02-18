#!/usr/bin/env python3
"""
Generate Android launcher icons from a source image.
Creates icons at all required densities for Android.
"""
import os
import sys

try:
    from PIL import Image
except ImportError:
    print("Installing Pillow...")
    os.system(f"{sys.executable} -m pip install pillow -q")
    from PIL import Image

# Source image
source = r"c:\Users\jorvi\Desktop\CalMahAhh\CAPY.png"
base_dir = r"c:\Users\jorvi\Desktop\CalMahAhh\app\src\main\res"

# Android icon sizes: (size_px, folder_name)
sizes = [
    (48, "mipmap-mdpi"),
    (72, "mipmap-hdpi"),
    (96, "mipmap-xhdpi"),
    (144, "mipmap-xxhdpi"),
    (192, "mipmap-xxxhdpi"),
]

if not os.path.exists(source):
    print(f"Error: {source} not found")
    sys.exit(1)

# Open source image
img = Image.open(source)
print(f"Opened {source}: {img.size}")

# Generate icons
for size, folder in sizes:
    folder_path = os.path.join(base_dir, folder)
    os.makedirs(folder_path, exist_ok=True)
    
    # Resize image
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    
    # Save as ic_launcher.png
    output = os.path.join(folder_path, "ic_launcher.png")
    resized.save(output, "PNG")
    print(f"✓ Created {size}x{size} → {output}")

print("\nDone! Icons created successfully.")
