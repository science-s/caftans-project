"""
Script simple pour tester les endpoints de l'API
Utilisez ce script pour vérifier que l'API fonctionne correctement
"""
import requests
import json

BASE_URL = "http://localhost:5000/api"

def test_health():
    """Test du endpoint health"""
    print("Testing /api/health...")
    response = requests.get(f"{BASE_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}\n")

def test_login():
    """Test de connexion"""
    print("Testing /api/auth/login...")
    data = {
        "email": "user@test.com",
        "password": "user123"
    }
    response = requests.post(f"{BASE_URL}/auth/login", json=data)
    print(f"Status: {response.status_code}")
    result = response.json()
    print(f"Response: {json.dumps(result, indent=2)}\n")
    
    if response.status_code == 200 and result.get('access_token'):
        return result['access_token']
    return None

def test_get_categories(token=None):
    """Test de récupération des catégories"""
    print("Testing /api/categories...")
    headers = {}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    response = requests.get(f"{BASE_URL}/categories", headers=headers)
    print(f"Status: {response.status_code}")
    result = response.json()
    print(f"Categories count: {len(result.get('categories', []))}\n")
    return result.get('categories', [])

def test_get_caftans(token=None, category_id=None):
    """Test de récupération des caftans"""
    print("Testing /api/caftans...")
    headers = {}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    params = {}
    if category_id:
        params['category_id'] = category_id
    response = requests.get(f"{BASE_URL}/caftans", headers=headers, params=params)
    print(f"Status: {response.status_code}")
    result = response.json()
    print(f"Caftans count: {len(result.get('caftans', []))}\n")
    return result.get('caftans', [])

if __name__ == '__main__':
    print("=" * 50)
    print("Test de l'API Caftans")
    print("=" * 50 + "\n")
    
    try:
        # Test health
        test_health()
        
        # Test login
        token = test_login()
        
        # Test categories
        categories = test_get_categories(token)
        
        # Test caftans
        if categories:
            test_get_caftans(token, categories[0]['id'])
        else:
            test_get_caftans(token)
        
        print("=" * 50)
        print("Tests terminés!")
        print("=" * 50)
        
    except requests.exceptions.ConnectionError:
        print("ERREUR: Impossible de se connecter au serveur Flask.")
        print("Assurez-vous que le serveur est démarré (python run.py)")
    except Exception as e:
        print(f"ERREUR: {e}")

