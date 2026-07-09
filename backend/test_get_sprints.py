import requests

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
    return None

def main():
    token = get_user_token()
    if not token:
        print("Failed to get token")
        return
        
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    proj_id = "55cae245-e5d1-4a5e-84e1-5cb222b9d624"
    url = f"http://localhost:8080/api/v1/sprints/project/{proj_id}"
    print("GET from", url)
    
    r = requests.get(url, headers=headers)
    print("Response status:", r.status_code)
    print("Response body:", r.text)

if __name__ == "__main__":
    main()
