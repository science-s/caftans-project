from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func
from app import db
from app.models import Caftan, Category, User
from app.utils.decorators import admin_required
import os
from werkzeug.utils import secure_filename
from flask import current_app

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
            search_lower = f'%{search.lower()}%'
            query = query.filter(
                db.or_(
                    func.lower(Caftan.name).like(search_lower),
                    func.lower(Caftan.description).like(search_lower)
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
@admin_required()
def create_caftan():
    try:
        # Handle multipart/form-data
        name = request.form.get('name')
        category_id = request.form.get('category_id')
        price_per_day = request.form.get('price_per_day')
        description = request.form.get('description')
        availability_status = request.form.get('availability_status', 'available')
        
        if not name or not category_id or not price_per_day:
            return jsonify({'error': 'Name, category_id, and price_per_day are required'}), 400
        
        # Verify category exists
        try:
            cat_id = int(category_id)
            category = Category.query.get(cat_id)
            if not category:
                return jsonify({'error': 'Category not found'}), 404
        except ValueError:
            return jsonify({'error': 'Invalid category_id format'}), 400
            
        try:
            price = float(price_per_day)
        except ValueError:
             return jsonify({'error': 'Invalid price_per_day format'}), 400

        image_url = None
        if 'image' in request.files:
            file = request.files['image']
            if file and file.filename != '':
                filename = secure_filename(file.filename)
                upload_folder = os.path.join(current_app.root_path,'uploads')
                os.makedirs(upload_folder, exist_ok=True)
                file.save(os.path.join(upload_folder, filename))
                image_url = f'/uploads/{filename}'
        
        caftan = Caftan(
            name=name,
            category_id=cat_id,
            description=description,
            price_per_day=price,
            availability_status=availability_status,
            image_url=image_url
        )
        
        db.session.add(caftan)
        db.session.commit()
        
        return jsonify({
            'message': 'Caftan created successfully',
            'caftan': caftan.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        import traceback
        traceback.print_exc()
        return jsonify({'error': str(e), 'trace': traceback.format_exc()}), 500

@bp.route('/<int:caftan_id>', methods=['PUT'])
@admin_required()
def update_caftan(caftan_id):
    try:
        caftan = Caftan.query.get(caftan_id)
        if not caftan:
            return jsonify({'error': 'Caftan not found'}), 404
        
        # Check if request is JSON or Multipart
        if request.is_json:
            data = request.get_json()
        else:
            data = request.form
        
        if data.get('name'):
            caftan.name = data['name']
        if data.get('description') is not None:
            caftan.description = data['description']
        if data.get('category_id'):
            # Explicit cast to int
            try:
                cat_id = int(data['category_id'])
                category = Category.query.get(cat_id)
                if not category:
                    return jsonify({'error': 'Category not found'}), 404
                caftan.category_id = cat_id
            except ValueError:
                return jsonify({'error': 'Invalid category_id format'}), 400
                
        if data.get('price_per_day'):
            try:
                caftan.price_per_day = float(data['price_per_day'])
            except ValueError:
                return jsonify({'error': 'Invalid price_per_day format'}), 400
                
        if data.get('availability_status'):
            caftan.availability_status = data['availability_status']
            
        # Handle Image Upload
        if 'image' in request.files:
            file = request.files['image']
            if file and file.filename != '':
                filename = secure_filename(file.filename)
                upload_folder = os.path.join(current_app.root_path, 'uploads')
                os.makedirs(upload_folder, exist_ok=True)
                file.save(os.path.join(upload_folder, filename))
                caftan.image_url = f'/uploads/{filename}'
        # If image_url string is passed (e.g. keeping existing or explicit url)
        elif data.get('image_url') is not None:
             caftan.image_url = data['image_url']
        
        db.session.commit()
        
        return jsonify({
            'message': 'Caftan updated successfully',
            'caftan': caftan.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
        import traceback
        traceback.print_exc() # Print to server log
        return jsonify({'error': str(e), 'trace': traceback.format_exc()}), 500

@bp.route('/<int:caftan_id>', methods=['DELETE'])
@admin_required()
def delete_caftan(caftan_id):
    try:
        caftan = Caftan.query.get(caftan_id)
        if not caftan:
            return jsonify({'error': 'Caftan not found'}), 404
        
        db.session.delete(caftan)
        db.session.commit()
        
        return jsonify({'message': 'Caftan deleted successfully'}), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

