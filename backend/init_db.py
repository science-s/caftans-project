"""
Script d'initialisation de la base de données
Exécute les migrations et charge les données initiales
"""
from app import create_app, db
from config import Config
from flask_migrate import upgrade

def init_database():
    app = create_app(Config)
    
    with app.app_context():
        print("Initialisation de la base de données...")
        
        # Créer toutes les tables
        db.create_all()
        print("✓ Tables créées")
        
        # Exécuter les migrations
        try:
            upgrade()
            print("✓ Migrations appliquées")
        except Exception as e:
            print(f"Note: {e}")
        
        print("\n✓ Base de données initialisée!")
        print("\nPour charger les données initiales, exécutez:")
        print("  python seed_data.py")

if __name__ == '__main__':
    init_database()

