from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from pydantic import BaseModel
import random
import uuid
from datetime import datetime
from app.database import get_db

router = APIRouter(prefix="/teacher", tags=["teacher"])

@router.get("/classes")
def get_teacher_classes(teacher_uid: str, db: Session = Depends(get_db)):

    result = db.execute(
        text("""
        SELECT
            c.class_code,
            s.subject_code,
            s.subject_name
        FROM class_subject_teachers cst
        JOIN classes c ON cst.class_id = c.id
        JOIN subjects s ON cst.subject_id = s.id
        JOIN users u ON cst.teacher_id = u.id
        WHERE u.uid = :teacher_uid
        """),
        {"teacher_uid": teacher_uid}
    ).fetchall()

    response = []

    for row in result:
        response.append({
            "class_code": row.class_code,
            "subject_code": row.subject_code,
            "subject_name": row.subject_name
        })

    return response

class StartSessionRequest(BaseModel):
    class_code: str
    subject_code: str

@router.post("/start-session")
def start_session(data: StartSessionRequest, db: Session = Depends(get_db)):

    assignment = db.execute(
        text("""
        SELECT cst.id
        FROM class_subject_teachers cst
        JOIN classes c ON cst.class_id = c.id
        JOIN subjects s ON cst.subject_id = s.id
        WHERE c.class_code = :class_code
        AND s.subject_code = :subject_code
        """),
        {
            "class_code": data.class_code,
            "subject_code": data.subject_code
        }
    ).fetchone()

    if not assignment:
        return {"error": "Class subject assignment not found"}

    otp = str(random.randint(100000, 999999))

    qr_token = str(uuid.uuid4())

    session = db.execute(
        text("""
        INSERT INTO attendance_sessions
        (class_subject_teacher_id, session_start, otp, qr_code, is_active)
        VALUES (:assignment_id, :start_time, :otp, :qr, true)
        RETURNING id
        """),
        {
            "assignment_id": assignment.id,
            "start_time": datetime.utcnow(),
            "otp": otp,
            "qr": qr_token
        }
    ).fetchone()

    db.commit()

    return {
        "session_id": str(session.id),
        "otp": otp,
        "qr_code": qr_token
    }