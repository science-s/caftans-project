# 📸 Guide pour Ajouter des Caftans avec Images

## 📁 Structure des Dossiers

Organise tes images dans le dossier `backend/caftans_images/` comme suit:

```
backend/caftans_images/
├── Espace_Mariage/
│   ├── caftan-mariage-1.jpg
│   ├── caftan-mariage-2.jpg
│   ├── caftan-dore.jpg
│   └── ...
├── Espace_Fete/
│   ├── caftan-fete-1.jpg
│   ├── caftan-bleu-royal.jpg
│   └── ...
├── Espace_Traditionnel/
│   ├── caftan-traditionnel-1.jpg
│   ├── caftan-vert-emeraude.jpg
│   └── ...
└── Espace_Moderne/
    ├── caftan-moderne-1.jpg
    ├── caftan-noir.jpg
    └── ...
```

## 🚀 Étapes pour Ajouter les Caftans

### 1. Placer les Images

1. Ouvre le dossier : `C:\Users\PC\caftans_mobile\backend\caftans_images\`
2. Place tes images dans le bon dossier selon la catégorie :
   - **Espace_Mariage** : pour les caftans de mariage
   - **Espace_Fete** : pour les caftans de fête
   - **Espace_Traditionnel** : pour les caftans traditionnels
   - **Espace_Moderne** : pour les caftans modernes

### 2. Nommer les Images

- Le nom du fichier sera utilisé pour créer le nom du caftan
- Exemple : `caftan-dore.jpg` → "Caftan Dore"
- Exemple : `caftan_mariage_blanc.jpg` → "Caftan Mariage Blanc"

### 3. Exécuter le Script

```bash
cd backend
.\venv\Scripts\Activate.ps1
python add_caftans.py
```

Le script va :
- ✅ Copier toutes les images dans le dossier `uploads/`
- ✅ Créer automatiquement les caftans dans la base de données
- ✅ Associer chaque caftan à sa catégorie
- ✅ Générer un prix automatique (300-800 MAD/jour)
- ✅ Créer une description automatique

## 📝 Format des Images

- **Formats acceptés** : `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`
- **Taille recommandée** : 800x1200 pixels ou plus
- **Poids** : Moins de 5MB par image

## ⚙️ Personnalisation

Si tu veux modifier les prix ou descriptions, édite le fichier `backend/add_caftans.py` :

```python
# Modifier le prix (ligne ~80)
price_per_day=300.0 + (idx * 50),  # Prix varié entre 300 et 800

# Modifier la description (ligne ~75)
description=f"Magnifique caftan {category_name.lower()} - {caftan_name}",
```

## 🔄 Ajouter Plus de Caftans Plus Tard

1. Ajoute simplement de nouvelles images dans les dossiers
2. Relance le script : `python add_caftans.py`
3. Les nouveaux caftans seront ajoutés (les existants seront ignorés)

## 💡 Astuce

Tu peux aussi ajouter des caftans manuellement via l'API ou directement dans la base de données MySQL si tu préfères.

