from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app import db
from app.models import Reservation, Caftan, User
from datetime import datetime

bp = Blueprint('reservations', __name__)

@bp.route('', methods=['GET'])
@jwt_required()
def get_reservations():
    try:
        user_id = get_jwt_identity()
        if not user_id:
            # more explicit and correct HTTP status for auth problems
            return jsonify({'error': 'Missing or invalid access token'}), 401
        print("user_id: ", user_id)
        current_user = User.query.get(user_id)
        
        # Users can only see their own reservations, admins can see all
        if current_user and current_user.role == 'admin':
            reservations = Reservation.query.all()
        else:
            reservations = Reservation.query.filter_by(user_id=user_id).all()
        print(reservations)
        return jsonify({
            'reservations': [reservation.to_dict() for reservation in reservations]
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@bp.route('/<int:reservation_id>', methods=['GET'])
@jwt_required()
def get_reservation(reservation_id):
    try:
        user_id = get_jwt_identity()
        current_user = User.query.get(user_id)
        
        reservation = Reservation.query.get(reservation_id)
        if not reservation:
            return jsonify({'error': 'Reservation not found'}), 404
        
        # Users can only see their own reservations, admins can see all
        if current_user.role != 'admin' and reservation.user_id != user_id:
            return jsonify({'error': 'Unauthorized'}), 403
        
        return jsonify({'reservation': reservation.to_dict()}), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@bp.route('', methods=['POST'])
@jwt_required()
def create_reservation():
    try:
        user_id = get_jwt_identity()
        data = request.get_json()
        
        if not data or not data.get('caftan_id') or not data.get('start_date') or not data.get('end_date'):
            return jsonify({'error': 'caftan_id, start_date, and end_date are required'}), 400
        
        # Verify caftan exists and is available
        caftan = Caftan.query.get(data['caftan_id'])
        if not caftan:
            return jsonify({'error': 'Caftan not found'}), 404
        
        if caftan.availability_status != 'available':
            return jsonify({'error': 'Caftan is not available'}), 400
        
        # Parse dates
        try:
            start_date = datetime.strptime(data['start_date'], '%Y-%m-%d').date()
            end_date = datetime.strptime(data['end_date'], '%Y-%m-%d').date()
        except ValueError:
            return jsonify({'error': 'Invalid date format. Use YYYY-MM-DD'}), 400
        
        if start_date >= end_date:
            return jsonify({'error': 'End date must be after start date'}), 400
        
        if start_date < datetime.now().date():
            return jsonify({'error': 'Start date cannot be in the past'}), 400
        
        reservation = Reservation(
            user_id=user_id,
            caftan_id=data['caftan_id'],
            start_date=start_date,
            end_date=end_date,
            status='pending',
            notes=data.get('notes')
        )
        
        db.session.add(reservation)
        db.session.commit()
        
        return jsonify({
            'message': 'Reservation created successfully',
            'reservation': reservation.to_dict()
        }), 201
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@bp.route('/<int:reservation_id>', methods=['PUT'])
@jwt_required()
def update_reservation(reservation_id):
    try:
        user_id = get_jwt_identity()
        current_user = User.query.get(user_id)
        
        reservation = Reservation.query.get(reservation_id)
        if not reservation:
            return jsonify({'error': 'Reservation not found'}), 404
        
        # Users can only update their own reservations, admins can update any
        if current_user.role != 'admin' and reservation.user_id != user_id:
            return jsonify({'error': 'Unauthorized'}), 403
        
        data = request.get_json()
        
        # Users can only cancel, admins can change status
        if current_user.role == 'admin':
            if data.get('status'):
                reservation.status = data['status']
        else:
            # Regular users can only cancel
            if data.get('status') == 'cancelled':
                reservation.status = 'cancelled'
            elif data.get('status'):
                return jsonify({'error': 'You can only cancel your reservations'}), 403
        
        if data.get('start_date'):
            try:
                reservation.start_date = datetime.strptime(data['start_date'], '%Y-%m-%d').date()
            except ValueError:
                return jsonify({'error': 'Invalid date format. Use YYYY-MM-DD'}), 400
        
        if data.get('end_date'):
            try:
                reservation.end_date = datetime.strptime(data['end_date'], '%Y-%m-%d').date()
            except ValueError:
                return jsonify({'error': 'Invalid date format. Use YYYY-MM-DD'}), 400
        
        if data.get('notes') is not None:
            reservation.notes = data['notes']
        
        db.session.commit()
        
        return jsonify({
            'message': 'Reservation updated successfully',
            'reservation': reservation.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

@bp.route('/<int:reservation_id>/status', methods=['PATCH'])
@jwt_required()
def update_reservation_status(reservation_id):
    try:
        user_id = get_jwt_identity()
        current_user = User.query.get(user_id)
        
        if not current_user or current_user.role != 'admin':
            return jsonify({'error': 'Unauthorized'}), 403
        
        reservation = Reservation.query.get(reservation_id)
        if not reservation:
            return jsonify({'error': 'Reservation not found'}), 404
        
        data = request.get_json()
        if not data or not data.get('status'):
            return jsonify({'error': 'status is required'}), 400
        
        reservation.status = data['status']
        db.session.commit()
        
        return jsonify({
            'message': 'Reservation status updated successfully',
            'reservation': reservation.to_dict()
        }), 200
        
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500

