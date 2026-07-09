import requests
import json
import sys

BASE_URL = "http://localhost:8080/api/v1"
KEYCLOAK_TOKEN_URL = "http://localhost:9000/realms/neuroforge-realm/protocol/openid-connect/token"

def get_token(username, password):
    data = {
        "client_id": "frontend-client",
        "username": username,
        "password": password,
        "grant_type": "password"
    }
    r = requests.post(KEYCLOAK_TOKEN_URL, data=data)
    if r.status_code == 200:
        return r.json()["access_token"]
    else:
        print(f"Failed to get token for {username}: {r.status_code} - {r.text}")
        return None

def main():
    import time
    print("=== Start User Flow Integration Test ===")
    
    timestamp = int(time.time())
    username = f"devtest_user_{timestamp}"
    email = f"devtest_user_{timestamp}@neuroforge.com"
    
    # 1. Register a new user
    reg_payload = {
        "username": username,
        "email": email,
        "password": "NeuroForge1!",
        "firstName": "Dev",
        "lastName": "Test",
        "role": "DEVELOPER"
    }
    
    print(f"\n1. Registering user '{username}'...")
    r = requests.post(f"{BASE_URL}/users/register", json=reg_payload)
    if r.status_code == 201:
        user_data = r.json()
        print(f"SUCCESS: User registered! ID: {user_data.get('id')}, Role: {user_data.get('role')}")
        user_id = user_data.get('id')
    else:
        print(f"FAILED to register user: {r.status_code} - {r.text}")
        user_id = None
        
    # 2. Authenticate as the new user
    print(f"\n2. Authenticating as '{username}'...")
    new_user_token = get_token(username, "NeuroForge1!")
    if new_user_token:
        print("SUCCESS: Authenticated successfully!")
    else:
        print("FAILED: Could not authenticate new user")
        
    # 3. Authenticate as admin
    print("\n3. Authenticating as 'admin'...")
    admin_token = get_token("admin", "NeuroForge1!")
    if not admin_token:
        print("FAILED: Could not authenticate admin user")
        sys.exit(1)
    print("SUCCESS: Admin authenticated.")
    
    headers = {"Authorization": f"Bearer {admin_token}"}
    
    # 4. Fetch all users as admin
    print("\n4. Fetching all users as admin...")
    r = requests.get(f"{BASE_URL}/users", headers=headers)
    if r.status_code == 200:
        users = r.json()
        print(f"SUCCESS: Fetched {len(users)} users.")
        for u in users:
            if u["username"] == username:
                print(f"Found '{username}' in list: Role = {u.get('role')}, Team = {u.get('primaryTeamName')}")
    else:
        print(f"FAILED to fetch users: {r.status_code} - {r.text}")
        
    # 5. Update the user as admin
    if user_id:
        print(f"\n5. Updating user {user_id} to Role=TEAM_LEAD...")
        update_payload = {
            "email": f"{username}_updated@neuroforge.com",
            "firstName": "DevUpdated",
            "lastName": "TestUpdated",
            "role": "TEAM_LEAD",
            "primaryTeamId": ""
        }
        r = requests.put(f"{BASE_URL}/users/{user_id}", json=update_payload, headers=headers)
        if r.status_code == 200:
            updated_data = r.json()
            print(f"SUCCESS: User updated! New email: {updated_data.get('email')}, Role: {updated_data.get('role')}")
        else:
            print(f"FAILED to update user: {r.status_code} - {r.text}")
            
        # 6. Delete the user as admin
        print(f"\n6. Deleting/Archiving user {user_id}...")
        r = requests.delete(f"{BASE_URL}/users/{user_id}", headers=headers)
        if r.status_code == 204:
            print("SUCCESS: User deleted.")
        else:
            print(f"FAILED to delete user: {r.status_code} - {r.text}")
            
        # 7. Check that deleted user is no longer active
        print("\n7. Verifying user is deleted from active users list...")
        r = requests.get(f"{BASE_URL}/users", headers=headers)
        if r.status_code == 200:
            users = r.json()
            found = False
            for u in users:
                if u["id"] == user_id:
                    found = True
                    break
            if not found:
                print("SUCCESS: User is no longer in active users list.")
            else:
                print("FAILED: User is still in active users list.")
        else:
            print(f"FAILED to fetch users list: {r.status_code} - {r.text}")

if __name__ == "__main__":
    main()
