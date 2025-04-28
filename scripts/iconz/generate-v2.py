import sys
from PIL import Image, ImageDraw, ImageFont
import io

# Configurations
CANVAS_SIZE = 96        # Size of the canvas in pixels
FONT_SCALE = 0.8      # Font size as a percentage of canvas size (before padding)
VERTICAL_ADJUST = -0.225  # Vertical tweak to center text
PADDING = 0.0           # Padding around the text

# Read input arguments
if len(sys.argv) < 3:
    print(f"Usage: {sys.argv[0]} FONT_FILE TEXT")
    sys.exit(1)

# Optional VERTICAL_ADJUST argument
if len(sys.argv) >= 4:
    try:
        VERTICAL_ADJUST = float(sys.argv[3])
    except ValueError:
        print("Error: VERTICAL_ADJUST must be a floating point number.")
        sys.exit(1)

font_path = sys.argv[1]
text = sys.argv[2]

# Create a blank transparent image
img = Image.new('RGBA', (CANVAS_SIZE, CANVAS_SIZE), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Calculate usable area after applying padding
usable_width = CANVAS_SIZE * (1.0 - PADDING * 2)
usable_height = CANVAS_SIZE * (1.0 - PADDING * 2)

# Load the font
font_size = int(min(usable_width, usable_height) * FONT_SCALE)
font = ImageFont.truetype(font_path, font_size)

# Measure text size
text_bbox = draw.textbbox((0, 0), text, font=font)
text_width = text_bbox[2] - text_bbox[0]
text_height = text_bbox[3] - text_bbox[1]

# Calculate position (centered within the padded area)
padding_offset_x = CANVAS_SIZE * PADDING
padding_offset_y = CANVAS_SIZE * PADDING

text_x = padding_offset_x + (usable_width - text_width) / 2
text_y = padding_offset_y + (usable_height - text_height) / 2 + (CANVAS_SIZE * VERTICAL_ADJUST)

# Draw the text
draw.text((text_x, text_y), text, font=font, fill=(255, 255, 255, 255))  # White color

# Convert to VectorDrawable XML format
output = io.StringIO()

output.write('<?xml version="1.0" encoding="utf-8"?>\n')
output.write('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n')
output.write(f'    android:width="24dp"\n')
output.write(f'    android:height="24dp"\n')
output.write(f'    android:viewportWidth="{CANVAS_SIZE}"\n')
output.write(f'    android:viewportHeight="{CANVAS_SIZE}">\n')

# Path compression: group continuous pixels horizontally
for y in range(CANVAS_SIZE):
    x = 0
    while x < CANVAS_SIZE:
        r, g, b, a = img.getpixel((x, y))
        if a > 128:
            color = (r, g, b)
            start_x = x
            while x < CANVAS_SIZE:
                r2, g2, b2, a2 = img.getpixel((x, y))
                if a2 <= 128 or (r2, g2, b2) != color:
                    break
                x += 1
            width = x - start_x
            color_hex = "@android:color/white"
            output.write(f'    <path android:fillColor="{color_hex}" android:pathData="M{start_x},{y}h{width}v1h-{width}z"/>\n')
        else:
            x += 1

output.write('</vector>\n')

# Print the result to STDOUT
print(output.getvalue())
