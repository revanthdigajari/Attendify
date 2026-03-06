from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import text
from pydantic import BaseModel
from app.database import get_db

router = APIRouter(prefix="/admin", tags=["admin"])

class AssignTeacherRequest(BaseModel):
    class_code: str
    subject_code: str
    teacher_uid: str


@router.post("/assign-teacher")
def assign_teacher(data: AssignTeacherRequest, db: Session = Depends(get_db)):

    class_row = db.execute(
        text("SELECT id FROM classes WHERE class_code=:code"),
        {"code": data.class_code}
    ).fetchone()

    if not class_row:
        raise HTTPException(status_code=404, detail="Class not found")

    subject_row = db.execute(
        text("SELECT id FROM subjects WHERE subject_code=:code"),
        {"code": data.subject_code}
    ).fetchone()

    if not subject_row:
        raise HTTPException(status_code=404, detail="Subject not found")

    teacher_row = db.execute(
        text("SELECT id FROM users WHERE uid=:uid AND role='teacher'"),
        {"uid": data.teacher_uid}
    ).fetchone()

    if not teacher_row:
        raise HTTPException(status_code=404, detail="Teacher not found")

    # check if already assigned
    existing = db.execute(
        text("""
        SELECT id FROM class_subject_teachers
        WHERE class_id=:class_id AND subject_id=:subject_id
        """),
        {
            "class_id": class_row.id,
            "subject_id": subject_row.id
        }
    ).fetchone()

    if existing:
        raise HTTPException(
            status_code=409,
            detail="Teacher already assigned for this subject and class"
        )

    db.execute(
        text("""
        INSERT INTO class_subject_teachers (class_id, subject_id, teacher_id)
        VALUES (:class_id, :subject_id, :teacher_id)
        """),
        {
            "class_id": class_row.id,
            "subject_id": subject_row.id,
            "teacher_id": teacher_row.id
        }
    )

    db.commit()

    return {"message": "Teacher assigned successfully"}