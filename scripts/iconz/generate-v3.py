import sys
from PIL import Image, ImageDraw, ImageFont
import io

# Configurations
OUTPUT_WIDTH_DP = 24
OUTPUT_HEIGHT_DP = 16
CANVAS_WIDTH = 48
CANVAS_HEIGHT = 32
FONT_SCALE = 1.0
PADDING = 0.0
VERTICAL_ADJUST = 0

if len(sys.argv) < 3:
    print(f"Usage: {sys.argv[0]} FONT_FILE TEXT [VERTICAL_ADJUST]")
    sys.exit(1)

font_path = sys.argv[1]
text = sys.argv[2]

if len(sys.argv) >= 4:
    try:
        VERTICAL_ADJUST += float(sys.argv[3])
    except ValueError:
        print("Error: VERTICAL_ADJUST must be a float.")
        sys.exit(1)

# Create image
img = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

usable_width = CANVAS_WIDTH * (1 - PADDING * 2)
usable_height = CANVAS_HEIGHT * (1 - PADDING * 2)
font_size = int(min(usable_width, usable_height) * FONT_SCALE)
font = ImageFont.truetype(font_path, font_size)

_, _, text_width, text_height = draw.textbbox((0, 0), text, font=font)

offset_x = CANVAS_WIDTH * PADDING
offset_y = CANVAS_HEIGHT * PADDING
text_x = offset_x + (usable_width - text_width) / 2
text_y = offset_y + (usable_height - text_height) / 2 + (CANVAS_HEIGHT * VERTICAL_ADJUST)

draw.text((text_x, text_y), text, font=font, fill=(255, 255, 255, 255))

# Output XML to STDOUT
output = io.StringIO()
output.write('<?xml version="1.0" encoding="utf-8"?>\n')
output.write('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n')
output.write(f'    android:width="{OUTPUT_WIDTH_DP}dp"\n')
output.write(f'    android:height="{OUTPUT_HEIGHT_DP}dp"\n')
output.write(f'    android:viewportWidth="{CANVAS_WIDTH}"\n')
output.write(f'    android:viewportHeight="{CANVAS_HEIGHT}">\n')

for y in range(CANVAS_HEIGHT):
    x = 0
    while x < CANVAS_WIDTH:
        r, g, b, a = img.getpixel((x, y))
        if a > 128:
            color = (r, g, b)
            start_x = x
            while x < CANVAS_WIDTH:
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

print(output.getvalue())
