import os
import time
import json
import urllib.request
from selenium import webdriver
from selenium.webdriver.edge.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import pymupdf

def get_auth_context(email, password):
    url = 'http://localhost:8080/api/auth/login'
    data = json.dumps({'email': email, 'password': password}).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as resp:
        res = json.loads(resp.read().decode('utf-8'))
        token = res['token']
        user_data = {
            'userId': res['userId'],
            'name': res['name'],
            'email': res['email'],
            'role': res['role'],
            'status': res['status']
        }
        return token, user_data

def setup_browser():
    options = Options()
    options.add_argument('--headless=new')
    options.add_argument('--window-size=1440,920')
    options.add_argument('--disable-gpu')
    options.add_argument('--hide-scrollbars')
    driver = webdriver.Edge(options=options)
    driver.set_window_size(1440, 920)
    return driver

def inject_auth(driver, token, user_data):
    driver.get('http://localhost:5173')
    time.sleep(0.5)
    driver.execute_script(f"""
        localStorage.setItem('jwtToken', '{token}');
        localStorage.setItem('user', JSON.stringify({json.dumps(user_data)}));
    """)

def capture_all():
    print("Capturing all project presentation UI screenshots...")
    driver = setup_browser()
    wait = WebDriverWait(driver, 10)

    try:
        # =====================================================================
        # 1. 11_Register_Owner.png: Owner Registration Page
        # =====================================================================
        print("Capturing 11_Register_Owner.png...")
        driver.get('http://localhost:5173/register-owner')
        time.sleep(1.5)
        driver.save_screenshot('11_Register_Owner.png')
        print("  -> Saved 11_Register_Owner.png")

        # =====================================================================
        # 2. 11_Admin_History.png: Super Admin Approval History / Pending Approvals
        # =====================================================================
        print("Capturing 11_Admin_History.png...")
        admin_token, admin_user = get_auth_context('admin@smartwater.com', 'Admin@123456')
        inject_auth(driver, admin_token, admin_user)
        driver.get('http://localhost:5173/admin/history')
        time.sleep(2.0)
        driver.save_screenshot('11_Admin_History.png')
        print("  -> Saved 11_Admin_History.png")

        # =====================================================================
        # 3. 12_Owner_Buildings.png: Owner Buildings, Flats & Meter Pairing
        # =====================================================================
        print("Capturing 12_Owner_Buildings.png...")
        owner_token, owner_user = get_auth_context('owner.robert@gmail.com', 'Admin@123456')
        inject_auth(driver, owner_token, owner_user)
        driver.get('http://localhost:5173/owner/buildings')
        time.sleep(2.0)
        driver.save_screenshot('12_Owner_Buildings.png')
        print("  -> Saved 12_Owner_Buildings.png")

        # =====================================================================
        # 4. 14_Bulk_Purchases.png: Owner Billing -> Bulk Water Purchases tab
        # =====================================================================
        print("Capturing 14_Bulk_Purchases.png...")
        driver.get('http://localhost:5173/owner/billing')
        time.sleep(2.0)
        # Click on '2. Bulk Water Purchases' tab
        try:
            buttons = driver.find_elements(By.TAG_NAME, 'button')
            for b in buttons:
                if 'Bulk Water Purchases' in b.text:
                    b.click()
                    time.sleep(1.5)
                    break
        except Exception as ex:
            print("  Note clicking purchases tab:", ex)
        driver.save_screenshot('14_Bulk_Purchases.png')
        print("  -> Saved 14_Bulk_Purchases.png")

        # =====================================================================
        # 5. 16_Owner_Billing.png: Owner Billing (Cycle + Generate Bills)
        # =====================================================================
        print("Capturing 16_Owner_Billing.png...")
        driver.get('http://localhost:5173/owner/billing')
        time.sleep(2.0)
        driver.save_screenshot('16_Owner_Billing.png')
        print("  -> Saved 16_Owner_Billing.png")

        # =====================================================================
        # 6. 17_Resident_Dashboard.png: Resident Dashboard
        # =====================================================================
        print("Capturing 17_Resident_Dashboard.png...")
        res_token, res_user = get_auth_context('michael.resident@gmail.com', 'Admin@123456')
        inject_auth(driver, res_token, res_user)
        driver.get('http://localhost:5173/resident/dashboard')
        time.sleep(2.0)
        driver.save_screenshot('17_Resident_Dashboard.png')
        print("  -> Saved 17_Resident_Dashboard.png")

        # =====================================================================
        # 7. 18_Bill_Payment.png: Resident Bills & Payment Modal
        # =====================================================================
        print("Capturing 18_Bill_Payment.png...")
        driver.get('http://localhost:5173/resident/bills')
        time.sleep(2.0)
        # Click 'Pay' button to display the simulated payment modal
        try:
            pay_buttons = driver.find_elements(By.XPATH, "//button[contains(text(), 'Pay') or contains(text(), 'Pay Bill')]")
            if pay_buttons:
                pay_buttons[0].click()
                time.sleep(1.5)
        except Exception as ex:
            print("  Note opening pay modal:", ex)
        driver.save_screenshot('18_Bill_Payment.png')
        print("  -> Saved 18_Bill_Payment.png")

    finally:
        driver.quit()

    # =========================================================================
    # 8. 18_PDF_Invoice.png: Rendered PDF Invoice
    # =========================================================================
    print("Generating 18_PDF_Invoice.png from actual backend PDF...")
    try:
        res_token, _ = get_auth_context('michael.resident@gmail.com', 'Admin@123456')
        req_pdf = urllib.request.Request('http://localhost:8080/api/bills/1/pdf', headers={'Authorization': f'Bearer {res_token}'})
        pdf_bytes = urllib.request.urlopen(req_pdf).read()
        with open('18_PDF_Invoice.pdf', 'wb') as f:
            f.write(pdf_bytes)

        doc = pymupdf.open('18_PDF_Invoice.pdf')
        page = doc[0]
        pix = page.get_pixmap(dpi=200)
        pix.save('18_PDF_Invoice.png')
        print("  -> Saved 18_PDF_Invoice.png")
    except Exception as ex:
        print("  Error generating PDF invoice image:", ex)

    print("\nALL PRESENTATION IMAGES CAPTURED AND GENERATED SUCCESSFULLY!")

if __name__ == '__main__':
    capture_all()
