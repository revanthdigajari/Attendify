from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from app.database import get_db
from pydantic import BaseModel

class MarkAttendanceRequest(BaseModel):
    student_uid: str
    session_id: str
    otp: str

router = APIRouter(prefix="/student", tags=["student"])

@router.get("/active-session")
def get_active_session(student_uid: str, db: Session = Depends(get_db)):

    result = db.execute(
        text("""
        SELECT
            a.id as session_id,
            s.subject_code,
            c.class_code
        FROM attendance_sessions a
        JOIN class_subject_teachers cst
            ON a.class_subject_teacher_id = cst.id
        JOIN classes c
            ON cst.class_id = c.id
        JOIN subjects s
            ON cst.subject_id = s.id
        JOIN users u
            ON u.class_id = c.id
        WHERE u.uid = :student_uid
        AND a.is_active = true
        """),
        {"student_uid": student_uid}
    ).fetchone()

    if not result:
        return {"message": "No active session"}

    return {
        "session_id": str(result.session_id),
        "subject_code": result.subject_code,
        "class_code": result.class_code
    }

@router.post("/mark-attendance")
def mark_attendance(data: MarkAttendanceRequest, db: Session = Depends(get_db)):

    # check session
    session = db.execute(
        text("""
        SELECT id, otp, is_active
        FROM attendance_sessions
        WHERE id = :session_id
        """),
        {"session_id": data.session_id}
    ).fetchone()

    if not session:
        return {"error": "Session not found"}

    if not session.is_active:
        return {"error": "Session closed"}

    if session.otp != data.otp:
        return {"error": "Invalid OTP"}

    # get student
    student = db.execute(
        text("""
        SELECT id, class_id
        FROM users
        WHERE uid = :uid
        AND role = 'student'
        """),
        {"uid": data.student_uid}
    ).fetchone()

    if not student:
        return {"error": "Student not found"}

    # prevent duplicate attendance
    existing = db.execute(
        text("""
        SELECT id
        FROM attendance
        WHERE student_id = :student_id
        AND session_id = :session_id
        """),
        {
            "student_id": student.id,
            "session_id": data.session_id
        }
    ).fetchone()

    if existing:
        return {"error": "Attendance already marked"}

    # insert attendance
    db.execute(
        text("""
        INSERT INTO attendance (student_id, session_id, status)
        VALUES (:student_id, :session_id, 'present')
        """),
        {
            "student_id": student.id,
            "session_id": data.session_id
        }
    )

    db.commit()

    return {
        "status": "attendance marked successfully"
    }