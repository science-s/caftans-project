from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app import db
from app.models import Caftan, Category, User

bp = Blueprint('caftans', __name__)

@bp.route('', methods=['GET'])
def get_caftans():
    try:
        category_id = request.args.get('category_id', type=int)
        search = request.args.get('search', '')
        availability = request.args.get('availability', '')
        
        query = Caftan.query
        
        if category_id:
            query = query.filter_by(category_id=category_id)
        
        if search:
            query = query.filter(
                db.or_(
                    Caftan.name.ilike(f'%{search}%'),
                    Caftan.description.ilike(f'%{search}%')
                )
            )
        
        if availability:
            query = query.filter_by(availability_status=availability)
        
        caftans = query.all()
        
        return jsonify({
            'caftans': [caftan.to_dict() for caftan in caftans]
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@bp.route('/<int:caftan_id>', methods=['GET'])
def get_caftan(caftan_id):
    try:
        caftan = Caftan.query.get(caftan_id)
        if not caftan:
            return jsonify({'error': 'Caftan not found'}), 404
        
        return jsonify({'caftan': caftan.to_dict()}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@bp.route('', methods=['POST'])
@jwt_required()
def create_caftan():
    try:
        current_user_id = get_jwt_identity()
        current_user = User.query.get(current_user_id)
        
        if not current_user or current_user.role != 'admin':
            return jsonify({'error': 'Unauthorized'}), 403
        
        data = request.get_json()
        
        if not data or not data.get('name') or not data.get('category_id') or not data.get('price_per_day'):
            return jsonify({'error': 'Name, category_id, and price_per_day are required'}), 400
        
        # Verify category exists
        category = Category.query.get(data['category_id'])
        if not category:
            return jsonify({'error': 'Category not found'}), 404
        
        caftan = Caftan(
            name=data['name'],
            category_id=data['category_id'],
            description=data.get('description'),
            price_per_day=data['price_per_day'],
            availability_status=data.get('availability_status', 'available'),
            image_url=data.get('image_url')
        )
        
        db.session.add(caftan)
        db.session.commit()
        
        return jsonify({
            'message': 'Caftan created successfully',
            'caftan': caftan.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@bp.route('/<int:caftan_id>', methods=['PUT'])
@jwt_required()
def update_caftan(caftan_id):
    try:
        current_user_id = get_jwt_identity()
        current_user = User.query.get(current_user_id)
        
        if not current_user or current_user.role != 'admin':
            return jsonify({'error': 'Unauthorized'}), 403
        
        caftan = Caftan.query.get(caftan_id)
        if not caftan:
            return jsonify({'error': 'Caftan not found'}), 404
        
        data = request.get_json()
        
        if data.get('name'):
            caftan.name = data['name']
        if data.get('description') is not None:
            caftan.description = data['description']
        if data.get('category_id'):
            category = Category.query.get(data['category_id'])
            if not category:
                return jsonify({'error': 'Category not found'}), 404
            caftan.category_id = data['category_id']
        if data.get('price_per_day'):
            caftan.price_per_day = data['price_per_day']
        if data.get('availability_status'):
            caftan.availability_status = data['availability_status']
        if data.get('image_url') is not None:
            caftan.image_url = data['image_url']
        
        db.session.commit()
        
        return jsonify({
            'message': 'Caftan updated successfully',
            'caftan': caftan.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@bp.route('/<int:caftan_id>', methods=['DELETE'])
@jwt_required()
def delete_caftan(caftan_id):
    try:
        current_user_id = get_jwt_identity()
        current_user = User.query.get(current_user_id)
        
        if not current_user or current_user.role != 'admin':
            return jsonify({'error': 'Unauthorized'}), 403
        
        caftan = Caftan.query.get(caftan_id)
        if not caftan:
            return jsonify({'error': 'Caftan not found'}), 404
        
        db.session.delete(caftan)
        db.session.commit()
        
        return jsonify({'message': 'Caftan deleted successfully'}), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

