import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches
from PIL import Image

def generate_slide_image():
    # 16:9 Widescreen ratio at 300 DPI
    fig, ax = plt.subplots(figsize=(16, 9), dpi=200)
    fig.patch.set_facecolor('#0F172A') # Dark Navy background
    ax.set_facecolor('#0F172A')

    # Remove axes
    ax.set_xlim(0, 16)
    ax.set_ylim(0, 9)
    ax.axis('off')

    # Header Bar
    header_rect = patches.FancyBboxPatch(
        (0.6, 7.6), 14.8, 1.1,
        boxstyle="round,pad=0.08,rounding_size=0.15",
        facecolor="#1E293B", edgecolor="#334155", linewidth=1.5
    )
    ax.add_patch(header_rect)

    # Accent tag
    tag_rect = patches.FancyBboxPatch(
        (0.85, 8.2), 2.2, 0.35,
        boxstyle="round,pad=0.04,rounding_size=0.08",
        facecolor="#0EA5E9", edgecolor="none"
    )
    ax.add_patch(tag_rect)
    ax.text(1.95, 8.37, "AQUAFLOW AI CORE", color="#FFFFFF", fontsize=10, fontweight='bold', ha='center', va='center', fontfamily='sans-serif')

    ax.text(3.3, 8.37, "• MACHINE LEARNING LEAK DETECTION PIPELINE", color="#94A3B8", fontsize=11, fontweight='semibold', va='center', fontfamily='sans-serif')
    ax.text(0.85, 7.85, "Cycle-End Quantile Regression & Continuous Anomaly Risk Attribution", color="#FFFFFF", fontsize=16, fontweight='bold', va='center', fontfamily='sans-serif')

    # 4 Pipeline Cards
    cards_data = [
        {
            "step": "STEP 1",
            "title": "Data Normalization",
            "color": "#38BDF8", # Sky
            "bg": "#1E293B",
            "border": "#0284C7",
            "badge_bg": "#0369A1",
            "points": [
                ("Monthly Input (kL)", "Entered per flat (e.g. 48.5 kL)"),
                ("Cycle Duration", "Cycle days = end - start + 1 (30/31d)"),
                ("Daily Rate Derived", "daily_usage = total_kL / cycle_days"),
                ("Demographics", "BHK unit type, floor, family count"),
                ("Away Adjustment", "Vacation days logged by tenant")
            ]
        },
        {
            "step": "STEP 2",
            "title": "21-D Feature Extraction",
            "color": "#818CF8", # Indigo
            "bg": "#1E293B",
            "border": "#4F46E5",
            "badge_bg": "#3730A3",
            "points": [
                ("Past Lags", "lag_1, lag_2, lag_3 daily usage"),
                ("Rolling Window", "6-cycle mean, median, and std-dev"),
                ("Trend Slope", "Linear regression trajectory slope"),
                ("Seasonality", "Cyclical sin & cos of month (1-12)"),
                ("Peer Benchmarks", "Building peer median for same BHK")
            ]
        },
        {
            "step": "STEP 3",
            "title": "Quantile Models (ML)",
            "color": "#34D399", # Emerald
            "bg": "#1E293B",
            "border": "#059669",
            "badge_bg": "#065F46",
            "points": [
                ("Algorithm", "LightGBM Quantile Regressors"),
                ("Loss Function", "Pinball Loss (immune to outliers)"),
                ("Q10 Lower Bound", "Conservative minimum expected usage"),
                ("Q50 Median", "Most probable normal consumption"),
                ("Q95 Upper Ceiling", "Ceiling boundary; leak if exceeded")
            ]
        },
        {
            "step": "STEP 4",
            "title": "Scoring & Attribution",
            "color": "#F87171", # Coral
            "bg": "#1E293B",
            "border": "#DC2626",
            "badge_bg": "#991B1B",
            "points": [
                ("Continuous Risk", "Mapped 0.0 to 1.0 (0% - 100% in UI)"),
                ("Status Levels", "NORMAL (<40%) | WATCH | LEAK (>=70%)"),
                ("Pattern Classifier", "SUDDEN_SPIKE, GRADUAL_DRIFT, etc."),
                ("Natural Reason", "Plain-English diagnostic explanation"),
                ("Cost & Feedback", "Estimated Extra Cost (₹) + Feedback")
            ]
        }
    ]

    card_w = 3.45
    card_h = 4.6
    card_y = 2.7
    spacing = 0.33
    start_x = 0.6

    for i, c in enumerate(cards_data):
        cx = start_x + i * (card_w + spacing)

        # Card Box
        card_box = patches.FancyBboxPatch(
            (cx, card_y), card_w, card_h,
            boxstyle="round,pad=0.08,rounding_size=0.15",
            facecolor=c["bg"], edgecolor=c["border"], linewidth=1.5
        )
        ax.add_patch(card_box)

        # Header Badge
        badge = patches.FancyBboxPatch(
            (cx + 0.2, card_y + card_h - 0.45), 1.05, 0.32,
            boxstyle="round,pad=0.03,rounding_size=0.08",
            facecolor=c["badge_bg"], edgecolor=c["color"], linewidth=1
        )
        ax.add_patch(badge)
        ax.text(cx + 0.725, card_y + card_h - 0.29, c["step"], color="#FFFFFF", fontsize=9, fontweight='bold', ha='center', va='center', fontfamily='sans-serif')

        # Title
        ax.text(cx + 0.2, card_y + card_h - 0.85, c["title"], color=c["color"], fontsize=13, fontweight='bold', va='center', fontfamily='sans-serif')

        # Divider line
        ax.plot([cx + 0.2, cx + card_w - 0.2], [card_y + card_h - 1.15, card_y + card_h - 1.15], color="#334155", linewidth=1)

        # Bullet points
        py = card_y + card_h - 1.45
        for header, sub in c["points"]:
            ax.text(cx + 0.2, py, f"▸ {header}", color="#F8FAFC", fontsize=9.5, fontweight='bold', va='center', fontfamily='sans-serif')
            ax.text(cx + 0.35, py - 0.28, sub, color="#94A3B8", fontsize=8.2, va='center', fontfamily='sans-serif')
            py -= 0.65

        # Flow Arrow between cards
        if i < 3:
            arrow_x = cx + card_w + 0.05
            ax.annotate("", xy=(arrow_x + 0.22, card_y + card_h / 2), xytext=(arrow_x, card_y + card_h / 2),
                        arrowprops=dict(arrowstyle="->", color="#38BDF8", lw=2.5, mutation_scale=15))

    # Bottom Live Case Study Banner
    bot_box = patches.FancyBboxPatch(
        (0.6, 0.45), 14.8, 1.95,
        boxstyle="round,pad=0.08,rounding_size=0.15",
        facecolor="#1E293B", edgecolor="#38BDF8", linewidth=1.5
    )
    ax.add_patch(bot_box)

    # Accent pill
    study_pill = patches.FancyBboxPatch(
        (0.85, 2.05), 3.2, 0.28,
        boxstyle="round,pad=0.03,rounding_size=0.08",
        facecolor="#0369A1", edgecolor="#38BDF8", linewidth=1
    )
    ax.add_patch(study_pill)
    ax.text(2.45, 2.19, "REAL SYSTEM EVALUATION WALKTHROUGH", color="#FFFFFF", fontsize=8.5, fontweight='bold', ha='center', va='center', fontfamily='sans-serif')

    # Case study steps
    ax.text(0.85, 1.68, "Resident: Michael Scott (Flat Apt 4B, 4 Members) | Meter: WM-SKY-4B | Cycle: October 2026", color="#38BDF8", fontsize=11, fontweight='bold', fontfamily='sans-serif')
    
    walkthrough_text = (
        "1. Input Normalization : October Monthly Usage = 48.50 kL (Cycle = 31 days -> 1.56 kL/day). Past baseline = 14.5–15.0 kL/month.\n"
        "2. ML Quantile Bounds   : LightGBM Model predicts Expected Min (Q10) = 13.0 kL  |  Expected Median (Q50) = 14.8 kL  |  Upper Ceiling (Q95) = 16.8 kL.\n"
        "3. Anomaly Evaluation  : 48.50 kL exceeds Q95 by +31.7 kL -> Continuous Risk Score = 0.96 (96% High Risk Meter).\n"
        "4. Actionable Alerts   : Status = POTENTIAL_LEAK | Pattern = SUDDEN_SPIKE | Est. Extra Cost = ₹2,457.00 | Open Incident #5 | Feedback Unacknowledged."
    )
    ax.text(0.85, 0.95, walkthrough_text, color="#CBD5E1", fontsize=9.2, fontfamily='monospace', linespacing=1.45)

    plt.tight_layout()
    output_png = "AquaFlow_ML_Pipeline_Slide.png"
    plt.savefig(output_png, facecolor=fig.get_facecolor(), edgecolor='none', bbox_inches='tight')
    plt.close()

    # Also copy to artifacts directory
    artifact_dir = r"C:\Users\nidar\.gemini\antigravity\brain\f3192eb3-ac4a-4686-b383-83166db376af"
    dest_path = os.path.join(artifact_dir, "AquaFlow_ML_Pipeline_Slide.png")
    Image.open(output_png).save(dest_path)
    print(f"Slide image saved to: {output_png} and {dest_path}")

if __name__ == "__main__":
    generate_slide_image()
