# Guide de Configuration Rapide

## 🚀 Démarrage Rapide

### Étape 1 : Préparer MySQL (sans Docker)

1. Crée une base de données nommée `mescaftans.db` (ton choix) dans phpMyAdmin.
2. Importer le fichier SQL : `backend/db/mysql_seed.sql` pour créer le schéma et insérer des catégories/caftans exemples.
3. Note les accès MySQL que tu utilises (host, port, user, password) pour les reporter dans le backend.

### Étape 2 : Configurer le Backend Flask

```bash
cd backend

# Créer et activer l'environnement virtuel
python -m venv venv
venv\Scripts\activate  # Windows
# ou
source venv/bin/activate  # Linux/Mac

# Installer les dépendances
pip install -r requirements.txt

# (Optionnel) Initialiser la base si besoin
# - Si tu as déjà importé le SQL, cette étape peut être sautée.
# - Sinon, tu peux créer les tables et appliquer les migrations.
# Mets bien dans ton env ou dans config.py :
#   MYSQL_DATABASE=mescaftans.db
python init_db.py

# Ajouter les comptes de test (admin et user)
python seed_data.py

# Démarrer le serveur
python run.py
```

Le serveur sera accessible sur `http://localhost:5000`

### Étape 3 : Configurer l'Application Android

1. Ouvrir Android Studio
2. File → Open → Sélectionner le dossier `mobile`
3. Attendre la synchronisation Gradle
4. Modifier l'URL de l'API dans `mobile/app/src/main/java/com/caftans/mobile/data/api/ApiClient.java` si nécessaire
5. Exécuter l'application sur un émulateur ou un appareil

## 🔑 Comptes de Test

- **Admin**: `admin@caftans.com` / `admin123`
- **Utilisateur**: `user@test.com` / `user123`

## 📝 Notes Importantes

1. **URL API pour Android** :
   - Émulateur : `http://10.0.2.2:5000/api/` (déjà configuré)
   - Appareil physique : Remplacez par l'IP de votre ordinateur (ex: `http://192.168.1.100:5000/api/`)

2. **Port MySQL** : Par défaut sur le port 3306

3. **Adminer** : Interface web pour MySQL accessible sur `http://localhost:8080`

## 🐛 Problèmes Courants

### Le serveur Flask ne démarre pas
- Vérifiez que MySQL est démarré
- Vérifiez les credentials dans `backend/config.py`

### L'app Android ne se connecte pas à l'API
- Vérifiez que le serveur Flask est démarré
- Pour appareil physique, vérifiez l'IP et le firewall
- Vérifiez l'URL dans `ApiClient.java`

### Erreur de build Android
- Synchronisez Gradle (File → Sync Project with Gradle Files)
- Nettoyez le projet (Build → Clean Project)

