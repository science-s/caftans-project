from flask import Blueprint, request, jsonify
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
from app import db
from app.models import User
from datetime import datetime

bp = Blueprint('auth', __name__)

@bp.route('/register', methods=['POST'])
def register():
    try:
        data = request.get_json()
        
        if not data or not data.get('email') or not data.get('password'):
            return jsonify({'error': 'Email and password are required'}), 400
        
        # Check if user already exists
        if User.query.filter_by(email=data['email']).first():
            return jsonify({'error': 'User already exists'}), 400
        
        # Create new user
        user = User(
            email=data['email'],
            full_name=data.get('full_name'),
            phone=data.get('phone'),
            role=data.get('role', 'user')
        )
        user.set_password(data['password'])
        
        db.session.add(user)
        db.session.commit()
        
        # Generate token
        access_token = create_access_token(identity=str(user.id))
        
        return jsonify({
            'message': 'User registered successfully',
            'access_token': access_token,
            'user': user.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@bp.route('/login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        print(f"Login request received: {data}")
        
        if not data:
            return jsonify({'error': 'Request body is required'}), 400
        
        email = data.get('email')
        password = data.get('password')
        
        if not email or not password:
            return jsonify({'error': 'Email and password are required'}), 400
        
        email = email.strip().lower()
        print(f"Searching for user with email: {email}")
        
        # Search user by email (case-insensitive using func.lower)
        from sqlalchemy import func
        user = User.query.filter(func.lower(User.email) == email).first()
        
        if not user:
            print(f"User not found with email: {email}")
            return jsonify({'error': 'Invalid email or password'}), 401
        
        print(f"User found: {user.email}, checking password...")
        
        if not user.check_password(password):
            print("Password incorrect")
            return jsonify({'error': 'Invalid email or password'}), 401
        
        print(f"Login successful for user: {user.email}")
        
        # Generate token
        access_token = create_access_token(identity=str(user.id))
        
        return jsonify({
            'message': 'Login successful',
            'access_token': access_token,
            'user': user.to_dict()
        }), 200
        
    except Exception as e:
        import traceback
        error_msg = str(e)
        print(f"Login error: {error_msg}")
        print(traceback.format_exc())
        return jsonify({'error': f'An error occurred during login: {error_msg}'}), 500

@bp.route('/me', methods=['GET'])
@jwt_required()
def get_current_user():
    try:
        user_id = get_jwt_identity()
        # Convert string to int (JWT identity is stored as string)
        user_id = int(user_id) if user_id else None
        user = User.query.get(user_id)
        
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        return jsonify({'user': user.to_dict()}), 200
        
    except (ValueError, TypeError) as e:
        return jsonify({'error': 'Invalid user ID'}), 400
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    # JWT is stateless, so logout is handled client-side by removing the token
    # In production, you might want to implement a token blacklist
    return jsonify({'message': 'Logged out successfully'}), 200

