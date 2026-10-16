import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from PIL import Image

def render_slide_image():
    # 16:9 Widescreen ratio at 200 DPI
    fig, ax = plt.subplots(figsize=(16, 9), dpi=200)
    fig.patch.set_facecolor('#FFFFFF')
    ax.set_facecolor('#FFFFFF')
    ax.set_xlim(0, 16)
    ax.set_ylim(0, 9)
    ax.axis('off')

    # 1. Slide Title & Subtitle
    ax.text(0.8, 8.35, "Explainable Alerts: Reason Codes & Severity", color="#1E293B", fontsize=22, fontweight='bold', fontfamily='Georgia', va='center')
    ax.text(0.8, 7.85, "Residents see why an alert fired, not just that it fired", color="#64748B", fontsize=12, fontfamily='sans-serif', va='center')

    # 2. Severity Levels (Top Right)
    ax.text(14.8, 8.45, "SEVERITY LEVELS", color="#D97706", fontsize=9.5, fontweight='bold', fontfamily='sans-serif', ha='right', va='center')
    
    levels = [
        ("LOW", "#F59E0B"),
        ("MEDIUM", "#EA580C"),
        ("HIGH", "#DC2626"),
        ("CRITICAL", "#451A03")
    ]
    badge_w = 0.92
    badge_h = 0.52
    start_bx = 11.25
    for i, (lvl, bg_col) in enumerate(levels):
        bx = start_bx + i * (badge_w + 0.10)
        badge_box = patches.FancyBboxPatch(
            (bx, 7.72), badge_w, badge_h,
            boxstyle="round,pad=0.04,rounding_size=0.08",
            facecolor=bg_col, edgecolor="none"
        )
        ax.add_patch(badge_box)
        ax.text(bx + badge_w / 2, 7.72 + badge_h / 2, lvl, color="#FFFFFF", fontsize=9, fontweight='bold', ha='center', va='center', fontfamily='sans-serif')

    # 3. Two Reason Cards (Middle Section)
    card_y = 6.0
    card_h = 1.35
    card_w = 6.95

    # Card 1: Abnormal Spike
    rc1 = patches.FancyBboxPatch(
        (0.8, card_y), card_w, card_h,
        boxstyle="round,pad=0.06,rounding_size=0.15",
        facecolor="#FFF7ED", edgecolor="#FED7AA", linewidth=1.2
    )
    ax.add_patch(rc1)
    # Circle 1 with vector icon
    c1_x, c1_y = 1.35, card_y + card_h / 2
    circle1 = patches.Circle((c1_x, c1_y), 0.35, facecolor="#EA580C", edgecolor="none")
    ax.add_patch(circle1)
    # Draw vector line trend inside circle
    ax.plot([c1_x - 0.20, c1_x - 0.05, c1_x + 0.05, c1_x + 0.20],
            [c1_y - 0.12, c1_y + 0.02, c1_y - 0.05, c1_y + 0.16],
            color="#FFFFFF", linewidth=2.5, solid_capstyle='round')
    ax.annotate("", xy=(c1_x + 0.22, c1_y + 0.18), xytext=(c1_x + 0.08, c1_y + 0.06),
                arrowprops=dict(arrowstyle="->", color="#FFFFFF", lw=2, mutation_scale=10))

    # Text 1
    ax.text(1.95, card_y + 0.92, "ABNORMAL_SPIKE_FOR_HOUSEHOLD_SIZE", color="#C2410C", fontsize=10.5, fontweight='bold', fontfamily='sans-serif')
    ax.text(1.95, card_y + 0.42, "Usage far above what the declared household size explains (e.g. 48.5 kL\nvs 16.8 kL upper ceiling threshold for 4 members).", color="#475569", fontsize=9, fontfamily='sans-serif', linespacing=1.3)

    # Card 2: Away Consumption
    rc2 = patches.FancyBboxPatch(
        (8.25, card_y), card_w, card_h,
        boxstyle="round,pad=0.06,rounding_size=0.15",
        facecolor="#FFF7ED", edgecolor="#FED7AA", linewidth=1.2
    )
    ax.add_patch(rc2)
    # Circle 2 with warning triangle
    c2_x, c2_y = 8.80, card_y + card_h / 2
    circle2 = patches.Circle((c2_x, c2_y), 0.35, facecolor="#DC2626", edgecolor="none")
    ax.add_patch(circle2)
    # Draw warning triangle
    tri = patches.Polygon([[c2_x, c2_y + 0.18], [c2_x - 0.18, c2_y - 0.14], [c2_x + 0.18, c2_y - 0.14]],
                          closed=True, facecolor="#FFFFFF", edgecolor="none")
    ax.add_patch(tri)
    ax.text(c2_x, c2_y - 0.04, "!", color="#DC2626", fontsize=11, fontweight='black', ha='center', va='center')

    # Text 2
    ax.text(9.40, card_y + 0.92, "UNEXPLAINED_AWAY_CONSUMPTION", color="#B91C1C", fontsize=10.5, fontweight='bold', fontfamily='sans-serif')
    ax.text(9.40, card_y + 0.42, "Significant water consumption detected while flat occupancy is marked as\nvacant or resident logged an active vacation period.", color="#475569", fontsize=9, fontfamily='sans-serif', linespacing=1.3)

    # 4. Bottom UI Screenshot
    img_path = "Cropped_Resident_Dashboard_Alert.png"
    if os.path.exists(img_path):
        ui_img = Image.open(img_path)
        # Clean white card frame with shadow effect
        frame_box = patches.FancyBboxPatch(
            (0.8, 0.70), 14.4, 5.0,
            boxstyle="round,pad=0.04,rounding_size=0.12",
            facecolor="#FFFFFF", edgecolor="#CBD5E1", linewidth=1.5
        )
        ax.add_patch(frame_box)

        # Place image cleanly inside the frame
        ax.imshow(ui_img, extent=[0.85, 15.15, 0.75, 5.65], aspect='auto', zorder=2)

    # 5. Bottom Footer
    ax.text(0.8, 0.32, "AQATRAC | Web-Based Water Billing & Management", color="#94A3B8", fontsize=9, fontfamily='sans-serif', va='center')
    ax.text(15.2, 0.32, "19", color="#94A3B8", fontsize=9.5, fontfamily='sans-serif', ha='right', va='center')

    plt.tight_layout()
    output_png = "AquaFlow_Explainable_Alerts_Slide.png"
    plt.savefig(output_png, facecolor=fig.get_facecolor(), edgecolor='none', bbox_inches='tight')
    plt.close()

    # Save to artifacts directory as well
    artifact_dir = r"C:\Users\nidar\.gemini\antigravity\brain\f3192eb3-ac4a-4686-b383-83166db376af"
    dest_path = os.path.join(artifact_dir, "AquaFlow_Explainable_Alerts_Slide.png")
    Image.open(output_png).save(dest_path)
    print(f"Slide image saved to: {output_png} and {dest_path}")

if __name__ == "__main__":
    render_slide_image()
