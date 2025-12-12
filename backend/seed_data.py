"""
Script to seed initial data into the database
Run this after creating the database and running migrations
"""
from app import create_app, db
from app.models import User, Category, Caftan
from config import Config
from datetime import datetime

def seed_data():
    app = create_app(Config)
    
    with app.app_context():
        # Create admin user
        admin = User.query.filter_by(email='admin@caftans.com').first()
        if not admin:
            admin = User(
                email='admin@caftans.com',
                full_name='Admin User',
                phone='+212600000000',
                role='admin'
            )
            admin.set_password('admin123')
            db.session.add(admin)
            print("✓ Admin user created")
        
        # Create test user
        test_user = User.query.filter_by(email='user@test.com').first()
        if not test_user:
            test_user = User(
                email='user@test.com',
                full_name='Test User',
                phone='+212611111111',
                role='user'
            )
            test_user.set_password('user123')
            db.session.add(test_user)
            print("✓ Test user created")
        
        # Create categories
        categories_data = [
            {'name': 'Espace Mariage', 'description': 'Caftans élégants pour les mariages'},
            {'name': 'Espace Fête', 'description': 'Caftans festifs pour les occasions spéciales'},
            {'name': 'Espace Traditionnel', 'description': 'Caftans traditionnels marocains'},
            {'name': 'Espace Moderne', 'description': 'Caftans modernes et tendance'}
        ]
        
        categories = {}
        for cat_data in categories_data:
            category = Category.query.filter_by(name=cat_data['name']).first()
            if not category:
                category = Category(**cat_data)
                db.session.add(category)
                print(f"✓ Category created: {cat_data['name']}")
            categories[cat_data['name']] = category
        
        # Create sample caftans
        caftans_data = [
            {
                'name': 'Caftan Mariage Doré',
                'category': 'Espace Mariage',
                'description': 'Magnifique caftan de mariage en soie dorée avec broderies traditionnelles',
                'price_per_day': 500.00,
                'availability_status': 'available'
            },
            {
                'name': 'Caftan Mariage Blanc Perle',
                'category': 'Espace Mariage',
                'description': 'Caftan élégant blanc perle avec perles et sequins',
                'price_per_day': 600.00,
                'availability_status': 'available'
            },
            {
                'name': 'Caftan Fête Bleu Royal',
                'category': 'Espace Fête',
                'description': 'Caftan festif bleu royal avec motifs géométriques',
                'price_per_day': 300.00,
                'availability_status': 'available'
            },
            {
                'name': 'Caftan Traditionnel Vert',
                'category': 'Espace Traditionnel',
                'description': 'Caftan traditionnel vert émeraude avec broderies manuelles',
                'price_per_day': 250.00,
                'availability_status': 'available'
            },
            {
                'name': 'Caftan Moderne Noir',
                'category': 'Espace Moderne',
                'description': 'Caftan moderne noir avec coupe contemporaine',
                'price_per_day': 350.00,
                'availability_status': 'available'
            },
            {
                'name': 'Caftan Moderne Rose',
                'category': 'Espace Moderne',
                'description': 'Caftan moderne rose poudré avec finitions élégantes',
                'price_per_day': 320.00,
                'availability_status': 'available'
            }
        ]
        
        for caftan_data in caftans_data:
            category_name = caftan_data.pop('category')
            category = categories[category_name]
            
            existing = Caftan.query.filter_by(name=caftan_data['name']).first()
            if not existing:
                caftan = Caftan(
                    category_id=category.id,
                    **caftan_data
                )
                db.session.add(caftan)
                print(f"✓ Caftan created: {caftan_data['name']}")
        
        db.session.commit()
        print("\n✓ Database seeded successfully!")
        print("\nDefault credentials:")
        print("  Admin: admin@caftans.com / admin123")
        print("  User:  user@test.com / user123")

if __name__ == '__main__':
    seed_data()

