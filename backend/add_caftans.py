"""
Script pour ajouter des caftans avec leurs images en masse
Usage: python add_caftans.py
"""
from app import create_app, db
from app.models import Category, Caftan
from config import Config
import os
from datetime import datetime

def add_caftans_from_folder():
    """
    Ajoute des caftans depuis un dossier d'images
    Structure attendue:
    backend/caftans_images/
        Espace_Mariage/
            image1.jpg
            image2.jpg
        Espace_Fete/
            image1.jpg
        ...
    """
    app = create_app(Config)
    
    with app.app_context():
        # Dossier contenant les images organisées par catégorie
        base_folder = os.path.join(os.path.dirname(__file__), 'caftans_images')
        
        if not os.path.exists(base_folder):
            print(f"⚠️  Le dossier {base_folder} n'existe pas encore.")
            print(f"📁 Crée le dossier et organise tes images comme suit:")
            print(f"   {base_folder}/")
            print(f"   ├── Espace_Mariage/")
            print(f"   │   ├── caftan1.jpg")
            print(f"   │   ├── caftan2.jpg")
            print(f"   │   └── ...")
            print(f"   ├── Espace_Fete/")
            print(f"   ├── Espace_Traditionnel/")
            print(f"   └── Espace_Moderne/")
            return
        
        # Mapping des noms de dossiers aux catégories
        category_mapping = {
            'Espace_Mariage': 'Espace Mariage',
            'Espace_Fete': 'Espace Fête',
            'Espace_Traditionnel': 'Espace Traditionnel',
            'Espace_Moderne': 'Espace Moderne',
            'Espace Fête': 'Espace Fête',  # Alternative
            'Mariage': 'Espace Mariage',
            'Fête': 'Espace Fête',
            'Traditionnel': 'Espace Traditionnel',
            'Moderne': 'Espace Moderne'
        }
        
        # Récupérer toutes les catégories
        categories = {cat.name: cat for cat in Category.query.all()}
        
        total_added = 0
        
        # Parcourir les dossiers de catégories
        for folder_name in os.listdir(base_folder):
            folder_path = os.path.join(base_folder, folder_name)
            
            if not os.path.isdir(folder_path):
                continue
            
            # Trouver la catégorie correspondante
            category_name = category_mapping.get(folder_name, folder_name)
            category = categories.get(category_name)
            
            if not category:
                print(f"⚠️  Catégorie '{category_name}' non trouvée. Création...")
                category = Category(name=category_name, description=f"Caftans {category_name}")
                db.session.add(category)
                db.session.commit()
                categories[category_name] = category
                print(f"✓ Catégorie '{category_name}' créée")
            
            # Parcourir les images dans le dossier
            image_files = [f for f in os.listdir(folder_path) 
                          if f.lower().endswith(('.jpg', '.jpeg', '.png', '.gif', '.webp'))]
            
            if not image_files:
                print(f"⚠️  Aucune image trouvée dans {folder_name}")
                continue
            
            print(f"\n📁 Traitement de {folder_name} ({len(image_files)} images)...")
            
            for idx, image_file in enumerate(image_files, 1):
                image_path = os.path.join(folder_path, image_file)
                
                # Copier l'image dans le dossier uploads
                timestamp = datetime.now().strftime('%Y%m%d_%H%M%S_')
                safe_filename = timestamp + os.path.basename(image_file)
                dest_path = os.path.join(Config.UPLOAD_FOLDER, safe_filename)
                
                # Copier le fichier
                import shutil
                shutil.copy2(image_path, dest_path)
                
                # Créer le nom du caftan depuis le nom du fichier
                caftan_name = os.path.splitext(image_file)[0]
                caftan_name = caftan_name.replace('_', ' ').replace('-', ' ')
                caftan_name = ' '.join(word.capitalize() for word in caftan_name.split())
                
                # Vérifier si le caftan existe déjà
                existing = Caftan.query.filter_by(name=caftan_name, category_id=category.id).first()
                if existing:
                    print(f"  ⏭️  {caftan_name} existe déjà, ignoré")
                    continue
                
                # Créer le caftan
                caftan = Caftan(
                    category_id=category.id,
                    name=caftan_name,
                    description=f"Magnifique caftan {category_name.lower()} - {caftan_name}",
                    price_per_day=300.0 + (idx * 50),  # Prix varié
                    availability_status='available',
                    image_url=f'/api/uploads/{safe_filename}'
                )
                
                db.session.add(caftan)
                total_added += 1
                print(f"  ✓ {caftan_name} ajouté")
            
            db.session.commit()
        
        print(f"\n✅ {total_added} caftans ajoutés avec succès!")
        print(f"\n💡 Pour ajouter plus de caftans:")
        print(f"   1. Place tes images dans {base_folder}/")
        print(f"   2. Organise-les par catégorie (dossiers)")
        print(f"   3. Relance ce script: python add_caftans.py")

if __name__ == '__main__':
    add_caftans_from_folder()

