# Application Mobile de Location de Caftans Marocains

Application mobile Android développée avec Java et Android Studio, avec un backend Flask et une base de données MySQL.

## 📋 Architecture du Projet

```
caftans_mobile/
├── backend/                 # Backend Flask
│   ├── app/
│   │   ├── __init__.py     # Configuration Flask
│   │   ├── models.py       # Modèles de données SQLAlchemy
│   │   └── routes/         # Endpoints API
│   │       ├── auth.py
│   │       ├── users.py
│   │       ├── categories.py
│   │       ├── caftans.py
│   │       ├── reservations.py
│   │       └── uploads.py
│   ├── config.py           # Configuration
│   ├── run.py              # Point d'entrée
│   ├── seed_data.py        # Données initiales
│   └── requirements.txt    # Dépendances Python
│
├── mobile/                 # Application Android
│   └── app/
│       └── src/main/
│           ├── java/com/caftans/mobile/
│           │   ├── data/           # Modèles et API
│           │   ├── ui/             # Écrans
│           │   └── utils/          # Utilitaires
│           └── res/                # Ressources Android
│
└── docker-compose.yml       # Configuration MySQL
```

## 🗄️ Structure de la Base de Données MySQL

### Tables principales :

1. **users** : Utilisateurs (clients et admins)
   - id, email, password_hash, full_name, phone, role, created_at, updated_at

2. **categories** : Catégories de caftans
   - id, name, description, created_at, updated_at

3. **caftans** : Caftans disponibles
   - id, category_id, name, description, price_per_day, availability_status, image_url, created_at, updated_at

4. **reservations** : Réservations
   - id, user_id, caftan_id, start_date, end_date, status, notes, created_at, updated_at

5. **favorites** (optionnel) : Favoris utilisateurs
   - id, user_id, caftan_id, created_at

## 🚀 Installation et Démarrage

### Prérequis

- Python 3.8+
- MySQL 8.0+ (ou Docker)
- Android Studio
- JDK 11+

### 1. Backend Flask

#### Installation

```bash
cd backend

# Créer un environnement virtuel
python -m venv venv

# Activer l'environnement virtuel
# Sur Windows:
venv\Scripts\activate
# Sur Linux/Mac:
source venv/bin/activate

# Installer les dépendances
pip install -r requirements.txt
```

#### Configuration de la base de données

1. Démarrer MySQL avec Docker :
```bash
docker-compose up -d
```

Ou utiliser MySQL local et mettre à jour les variables d'environnement dans `config.py`.

2. Créer le fichier `.env` (optionnel) :
```bash
cp .env.example .env
# Modifier les valeurs selon votre configuration
```

3. Initialiser la base de données :
```bash
# Dans le répertoire backend
flask db init
flask db migrate -m "Initial migration"
flask db upgrade

# Charger les données initiales
python seed_data.py
```

#### Démarrer le serveur Flask

```bash
python run.py
```

Le serveur sera accessible sur `http://localhost:5000`

**Comptes par défaut :**
- Admin: `admin@caftans.com` / `admin123`
- User: `user@test.com` / `user123`

### 2. Application Android

#### Configuration

1. Ouvrir le projet dans Android Studio :
   - File → Open → Sélectionner le dossier `mobile`

2. Modifier l'URL de l'API dans `ApiClient.java` :
   ```java
   // Pour émulateur Android
   private static final String BASE_URL = "http://10.0.2.2:5000/api/";
   
   // Pour appareil physique, utiliser l'IP de votre ordinateur
   // Exemple: "http://192.168.1.100:5000/api/"
   ```

3. Synchroniser Gradle et construire le projet

#### Exécution

- Connecter un appareil Android ou démarrer un émulateur
- Exécuter l'application depuis Android Studio

## 📡 Endpoints API Flask

### Authentification
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion
- `GET /api/auth/me` - Utilisateur actuel
- `POST /api/auth/logout` - Déconnexion

### Utilisateurs
- `GET /api/users/me` - Profil utilisateur
- `PUT /api/users/me` - Mettre à jour le profil

### Catégories
- `GET /api/categories` - Liste des catégories
- `GET /api/categories/{id}` - Détails d'une catégorie
- `POST /api/categories` - Créer (admin)
- `PUT /api/categories/{id}` - Modifier (admin)
- `DELETE /api/categories/{id}` - Supprimer (admin)

### Caftans
- `GET /api/caftans` - Liste (filtres: category_id, search, availability)
- `GET /api/caftans/{id}` - Détails
- `POST /api/caftans` - Créer (admin)
- `PUT /api/caftans/{id}` - Modifier (admin)
- `DELETE /api/caftans/{id}` - Supprimer (admin)

### Réservations
- `GET /api/reservations` - Liste des réservations
- `GET /api/reservations/{id}` - Détails
- `POST /api/reservations` - Créer une réservation
- `PUT /api/reservations/{id}` - Modifier
- `PATCH /api/reservations/{id}/status` - Changer statut (admin)

### Upload
- `POST /api/uploads` - Upload d'image (admin)
- `GET /api/uploads/{filename}` - Récupérer une image

## 📱 Écrans Android

### Flux d'authentification
1. **SplashActivity** - Écran de démarrage
2. **LoginActivity** - Connexion

### Espace utilisateur
3. **MainActivity** - Activité principale avec navigation
   - **CategoriesFragment** - Liste des catégories
   - **MyReservationsFragment** - Mes réservations
   - **ProfileFragment** - Profil utilisateur

4. **CaftansListActivity** - Liste des caftans par catégorie
5. **CaftanDetailActivity** - Détails d'un caftan
6. **ReservationActivity** - Formulaire de réservation

## 🎨 Fonctionnalités

### ✅ Implémentées
- Authentification avec JWT
- Gestion des utilisateurs
- Affichage des catégories
- Liste et détails des caftans
- Création de réservations
- Consultation des réservations
- Profil utilisateur
- Upload d'images (backend)

### 🔄 À améliorer/ajouter
- Inscription depuis l'app mobile
- Système de favoris
- Panier de réservation
- Recherche avancée
- Notifications push
- Paiement en ligne
- Galerie d'images multiples

## 🔧 Technologies Utilisées

### Backend
- Flask 3.0
- Flask-SQLAlchemy
- Flask-JWT-Extended
- Flask-CORS
- PyMySQL
- Flask-Migrate

### Mobile
- Android SDK
- Java
- Retrofit 2
- OkHttp
- Glide (chargement d'images)
- Material Design Components

### Base de données
- MySQL 8.0

## 📝 Notes Importantes

1. **Sécurité** : En production, changez les clés secrètes dans `config.py` et utilisez HTTPS.

2. **URL API** : Pour tester sur un appareil physique, assurez-vous que :
   - Le téléphone et l'ordinateur sont sur le même réseau WiFi
   - L'URL dans `ApiClient.java` pointe vers l'IP de votre ordinateur
   - Le firewall autorise les connexions sur le port 5000

3. **Images** : Les images uploadées sont stockées dans `backend/uploads/`. En production, utilisez un service de stockage cloud (AWS S3, Cloudinary, etc.).

4. **Base de données** : Les migrations Flask sont configurées. Utilisez `flask db migrate` et `flask db upgrade` pour les mises à jour du schéma.

## 🐛 Dépannage

### Erreur de connexion à la base de données
- Vérifiez que MySQL est démarré
- Vérifiez les credentials dans `config.py`
- Vérifiez que la base de données `caftans_db` existe

### Erreur de connexion API depuis Android
- Vérifiez l'URL dans `ApiClient.java`
- Vérifiez que le serveur Flask est démarré
- Pour appareil physique, vérifiez l'IP et le firewall

### Erreur de build Android
- Synchronisez Gradle (File → Sync Project with Gradle Files)
- Nettoyez le projet (Build → Clean Project)
- Vérifiez que toutes les dépendances sont installées

## 📄 Licence

Ce projet est un exemple éducatif.

## 👤 Auteur

Développé pour la location de caftans marocains.

