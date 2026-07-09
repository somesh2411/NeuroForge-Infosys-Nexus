import requests
import psycopg2
import json

def get_user_token():
    token_url = "http://localhost:9000/realms/neuroforge-realm/protocol/openid-connect/token"
    data = {
        "grant_type": "password",
        "client_id": "frontend-client",
        "username": "admin",
        "password": "NeuroForge1!"
    }
    r = requests.post(token_url, data=data)
    if r.status_code == 200:
        return r.json()["access_token"]
    else:
        print("Auth failed:", r.status_code, r.text)
    return None

def get_project_id():
    try:
        conn = psycopg2.connect(
            dbname="project_db",
            user="postgres",
            password="postgres",
            host="localhost",
            port="5432"
        )
        cur = conn.cursor()
        cur.execute("SELECT id, name, key FROM projects LIMIT 1;")
        proj = cur.fetchone()
        cur.close()
        conn.close()
        if proj:
            print(f"Found project in database: {proj[1]} ({proj[2]}) with ID {proj[0]}")
            return proj[0]
    except Exception as e:
        print(f"Failed to query projects from DB: {e}")
    return None

def main():
    token = get_user_token()
    if not token:
        print("Failed to get user token")
        return
        
    proj_id = get_project_id()
    if not proj_id:
        print("No project found to test against")
        return
        
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # Try different date formats
    payloads = [
        # ISO-8601 with T (Standard Jackson)
        {
            "name": "Sprint 3-1",
            "goal": "Testing from Python",
            "startDate": "2026-07-10T12:00:00",
            "endDate": "2026-07-15T12:00:00",
            "capacity": 20,
            "status": "PLANNED"
        },
        # ISO-8601 string from datetime-local element (no seconds)
        {
            "name": "Sprint 3-2",
            "goal": "Testing format 2",
            "startDate": "2026-07-10T12:00",
            "endDate": "2026-07-15T12:00",
            "capacity": 20,
            "status": "PLANNED"
        },
        # Display format shown in screenshot: DD-MM-YYYY HH:mm
        {
            "name": "Sprint 3-3",
            "goal": "Testing screenshot format",
            "startDate": "10-07-2026 12:00",
            "endDate": "15-07-2026 12:00",
            "capacity": 20,
            "status": "PLANNED"
        }
    ]
    
    for i, payload in enumerate(payloads):
        print(f"\n--- Testing Payload {i+1} ---")
        url = f"http://localhost:8080/api/v1/sprints/project/{proj_id}"
        print("POST to", url)
        print("Body:", json.dumps(payload))
        
        r = requests.post(url, headers=headers, json=payload)
        print("Response status:", r.status_code)
        print("Response body:", r.text)

if __name__ == "__main__":
    main()
