from PIL import Image

# 1. Clean crop of Resident Dashboard down to stat cards only (no cutoff line below)
dash = Image.open("Resident_Dashboard_Leak_Alert.png")
# x1=275, y1=125, x2=1885, y2=642
cropped_dash = dash.crop((275, 125, 1885, 642))
cropped_dash.save("Cropped_Resident_Dashboard_Alert.png")
print(f"Clean cropped dashboard saved: {cropped_dash.size}")

# 2. Also crop a clean version of the Leak Diagnostics Hero Card
diag = Image.open("Resident_Leak_Diagnostics_Status.png")
# x1=275, y1=125, x2=1885, y2=870
cropped_diag = diag.crop((275, 125, 1885, 870))
cropped_diag.save("Cropped_Resident_Diagnostics_Hero.png")
print(f"Clean cropped diagnostics saved: {cropped_diag.size}")
