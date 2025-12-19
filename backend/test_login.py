"""
Script simple pour tester le login
"""
import requests
import json

BASE_URL = "http://localhost:5000/api"

def test_login():
    print("=" * 50)
    print("Test de connexion")
    print("=" * 50)
    
    # Test avec admin
    print("\n1. Test avec admin@caftans.com / admin123")
    data = {
        "email": "admin@caftans.com",
        "password": "admin123"
    }
    try:
        response = requests.post(f"{BASE_URL}/auth/login", json=data)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get('access_token'):
                print("✅ Login réussi!")
                return result['access_token']
            else:
                print("❌ Pas de token dans la réponse")
        else:
            print(f"❌ Erreur: {response.text}")
    except requests.exceptions.ConnectionError:
        print("❌ ERREUR: Impossible de se connecter au serveur Flask")
        print("   Assurez-vous que le serveur est démarré (python run.py)")
    except Exception as e:
        print(f"❌ ERREUR: {e}")
    
    # Test avec user
    print("\n2. Test avec user@test.com / user123")
    data = {
        "email": "user@test.com",
        "password": "user123"
    }
    try:
        response = requests.post(f"{BASE_URL}/auth/login", json=data)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get('access_token'):
                print("✅ Login réussi!")
                return result['access_token']
            else:
                print("❌ Pas de token dans la réponse")
        else:
            print(f"❌ Erreur: {response.text}")
    except Exception as e:
        print(f"❌ ERREUR: {e}")
    
    return None

if __name__ == '__main__':
    test_login()

