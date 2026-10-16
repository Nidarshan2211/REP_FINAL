import time
import json
import requests
from selenium import webdriver
from selenium.webdriver.edge.options import Options
from selenium.webdriver.common.by import By

def capture_screens():
    # 1. Login via backend API to get token
    login_url = "http://localhost:8080/api/auth/login"
    login_payload = {
        "email": "michael.resident@gmail.com",
        "password": "Admin@123456"
    }
    resp = requests.post(login_url, json=login_payload)
    if resp.status_code != 200:
        print(f"Login failed: {resp.status_code} {resp.text}")
        return
    login_data = resp.json()
    token = login_data["token"]
    user_data = {
        "userId": login_data.get("userId"),
        "name": login_data.get("name"),
        "email": login_data.get("email"),
        "role": login_data.get("role"),
        "status": login_data.get("status")
    }
    print("Obtained JWT token for Michael Scott.")

    # 2. Setup Selenium Edge Headless
    options = Options()
    options.add_argument("--headless=new")
    options.add_argument("--window-size=1920,1080")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")

    driver = webdriver.Edge(options=options)
    driver.set_window_size(1920, 1080)

    try:
        # Load origin page to set localStorage
        driver.get("http://localhost:5173/login")
        time.sleep(1)

        # Inject token and user into localStorage
        driver.execute_script(f"""
            localStorage.setItem('jwtToken', '{token}');
            localStorage.setItem('user', JSON.stringify({json.dumps(user_data)}));
        """)
        print("Injected auth credentials into localStorage.")

        # 3. Capture Resident Dashboard
        driver.get("http://localhost:5173/resident/dashboard")
        time.sleep(2.5) # Wait for stats & alert banner to render
        dash_img = "Resident_Dashboard_Leak_Alert.png"
        driver.save_screenshot(dash_img)
        print(f"Captured: {dash_img}")

        # 4. Capture Resident Leak Status / Diagnostics Page
        driver.get("http://localhost:5173/resident/leak-status")
        time.sleep(2.5)
        status_img = "Resident_Leak_Diagnostics_Status.png"
        driver.save_screenshot(status_img)
        print(f"Captured: {status_img}")

        # 5. Open Feedback Modal
        try:
            feedback_btn = driver.find_element(By.XPATH, "//button[contains(., 'Report Feedback')]")
            feedback_btn.click()
            time.sleep(1.0)
            modal_img = "Resident_Leak_Feedback_Modal.png"
            driver.save_screenshot(modal_img)
            print(f"Captured: {modal_img}")
        except Exception as ex:
            print(f"Could not open feedback modal: {ex}")

    finally:
        driver.quit()

if __name__ == "__main__":
    capture_screens()
