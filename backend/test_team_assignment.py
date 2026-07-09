import requests
import psycopg2

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

def get_team_id():
    try:
        conn = psycopg2.connect(
            dbname="user_db",
            user="postgres",
            password="postgres",
            host="localhost",
            port="5432"
        )
        cur = conn.cursor()
        cur.execute("SELECT id, name, code FROM teams LIMIT 1;")
        team = cur.fetchone()
        cur.close()
        conn.close()
        if team:
            print(f"Found team in database: {team[1]} ({team[2]}) with ID {team[0]}")
            return team[0]
    except Exception as e:
        print(f"Failed to query teams from DB: {e}")
    return None

def main():
    token = get_user_token()
    if not token:
        print("Failed to get token")
        return
        
    team_id = get_team_id()
    if not team_id:
        print("No team found to test against")
        return
        
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    proj_id = "55cae245-e5d1-4a5e-84e1-5cb222b9d624"
    url = f"http://localhost:8080/api/v1/projects/{proj_id}/teams?teamId={team_id}"
    print("POST to", url)
    
    r = requests.post(url, headers=headers)
    print("Response status:", r.status_code)
    print("Response body:", r.text)

if __name__ == "__main__":
    main()
