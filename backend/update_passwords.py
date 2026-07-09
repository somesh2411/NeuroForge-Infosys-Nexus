import requests

KEYCLOAK_URL = "http://localhost:9000"
REALM = "neuroforge-realm"
ADMIN_REALM = "master"
CLIENT_ID = "admin-cli"
ADMIN_USER = "admin"
ADMIN_PASS = "admin"
NEW_PASSWORD = "NeuroForge1!"

def get_admin_token():
    token_url = f"{KEYCLOAK_URL}/realms/{ADMIN_REALM}/protocol/openid-connect/token"
    data = {
        "grant_type": "password",
        "client_id": CLIENT_ID,
        "username": ADMIN_USER,
        "password": ADMIN_PASS
    }
    r = requests.post(token_url, data=data)
    if r.status_code == 200:
        return r.json()["access_token"]
    else:
        print(f"Failed to get admin token: {r.status_code} - {r.text}")
        return None

def main():
    token = get_admin_token()
    if not token:
        return
    
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # 1. Fetch all users in neuroforge-realm
    users_url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/users"
    r = requests.get(users_url, headers=headers)
    if r.status_code != 200:
        print(f"Failed to get users: {r.status_code} - {r.text}")
        return
        
    users = r.json()
    print(f"Found {len(users)} users in realm '{REALM}':")
    for u in users:
        username = u["username"]
        user_id = u["id"]
        print(f"- {username} (ID: {user_id})")
        
        # 2. Reset password to NeuroForge1!
        reset_url = f"{KEYCLOAK_URL}/admin/realms/{REALM}/users/{user_id}/reset-password"
        payload = {
            "type": "password",
            "value": NEW_PASSWORD,
            "temporary": False
        }
        res = requests.put(reset_url, json=payload, headers=headers)
        if res.status_code == 204:
            print(f"  Successfully reset password for '{username}' to '{NEW_PASSWORD}'")
        else:
            print(f"  Failed to reset password for '{username}': {res.status_code} - {res.text}")

if __name__ == "__main__":
    main()
