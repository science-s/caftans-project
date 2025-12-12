"""
Script pour tester différentes combinaisons d'identifiants MySQL
et trouver celle qui fonctionne
"""
import pymysql

# Combinaisons courantes à tester
test_combinations = [
    {'user': 'root', 'password': ''},
    {'user': 'root', 'password': 'root'},
    {'user': 'root', 'password': 'rootpassword'},
    {'user': 'root', 'password': 'password'},
]

host = 'localhost'
port = 3306
database = 'mescaftans.db'

print("=" * 60)
print("Test de connexion MySQL")
print("=" * 60)
print(f"\nBase de données: {database}")
print(f"Host: {host}")
print(f"Port: {port}\n")

success = False

for combo in test_combinations:
    user = combo['user']
    password = combo['password']
    password_display = '(vide)' if password == '' else password
    
    print(f"Test avec user='{user}' / password='{password_display}'...", end=' ')
    
    try:
        connection = pymysql.connect(
            host=host,
            port=port,
            user=user,
            password=password,
            database=database
        )
        connection.close()
        print("✓ SUCCÈS!")
        print(f"\n✅ Identifiants qui fonctionnent:")
        print(f"   User: {user}")
        print(f"   Password: {password_display}")
        print(f"   Database: {database}")
        success = True
        break
    except pymysql.err.OperationalError as e:
        if e.args[0] == 1045:  # Access denied
            print("✗ Accès refusé")
        elif e.args[0] == 1049:  # Unknown database
            print("✗ Base de données introuvable")
            print(f"\n⚠️  La base '{database}' n'existe pas encore.")
            print("   Crée-la d'abord dans phpMyAdmin, puis réessaye.")
            break
        else:
            print(f"✗ Erreur: {e.args[1]}")
    except Exception as e:
        print(f"✗ Erreur: {e}")

if not success:
    print("\n" + "=" * 60)
    print("❌ Aucune combinaison n'a fonctionné.")
    print("\nVérifie dans phpMyAdmin:")
    print("1. Quel nom d'utilisateur tu utilises pour te connecter")
    print("2. Quel mot de passe tu utilises")
    print("3. Que la base 'mescaftans.db' existe")
    print("\nEnsuite, modifie backend/config.py avec tes identifiants.")
    print("=" * 60)

