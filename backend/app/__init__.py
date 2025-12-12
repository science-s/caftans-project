from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from flask_jwt_extended import JWTManager
from flask_cors import CORS
from config import Config

db = SQLAlchemy()
migrate = Migrate()
jwt = JWTManager()

def create_app(config_class=Config):
    app = Flask(__name__)
    app.config.from_object(config_class)
    
    # Initialize extensions
    db.init_app(app)
    migrate.init_app(app, db)
    jwt.init_app(app)
    CORS(app, resources={r"/api/*": {"origins": "*"}})
    
    # Create upload folder
    import os
    upload_folder = app.config['UPLOAD_FOLDER']
    if not os.path.exists(upload_folder):
        os.makedirs(upload_folder)
    
    # Register blueprints
    from app.routes.auth import bp as auth_bp
    app.register_blueprint(auth_bp, url_prefix='/api/auth')
    
    from app.routes.users import bp as users_bp
    app.register_blueprint(users_bp, url_prefix='/api/users')
    
    from app.routes.categories import bp as categories_bp
    app.register_blueprint(categories_bp, url_prefix='/api/categories')
    
    from app.routes.caftans import bp as caftans_bp
    app.register_blueprint(caftans_bp, url_prefix='/api/caftans')
    
    from app.routes.reservations import bp as reservations_bp
    app.register_blueprint(reservations_bp, url_prefix='/api/reservations')
    
    from app.routes.uploads import bp as uploads_bp
    app.register_blueprint(uploads_bp, url_prefix='/api/uploads')
    
    # Health check endpoint
    @app.route('/api/health')
    def health():
        return {'status': 'ok', 'message': 'Caftans API is running'}, 200
    
    return app

