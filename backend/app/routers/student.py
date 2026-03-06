from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text

from app.database import get_db

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