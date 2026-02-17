from fastapi import APIRouter, Depends
from app.core.security import get_current_user

router = APIRouter()

@router.get("/health")
def health_check():
    return {"status": "Backend is healthy"}


@router.get("/me")
def get_me(current_user: dict = Depends(get_current_user)):
    return {
        "message": "You are authenticated",
        "user": current_user
    }

from fastapi import APIRouter, Depends
from app.core.security import get_current_user, require_role

router = APIRouter()

@router.get("/health")
def health_check():
    return {"status": "Backend is healthy"}


@router.get("/me")
def get_me(current_user: dict = Depends(get_current_user)):
    return {
        "message": "You are authenticated",
        "user": current_user
    }

# 🔐 Student Only
@router.get("/student/test")
def student_test(current_user: dict = Depends(require_role("student"))):
    return {"message": "Student access granted", "user": current_user}


# 🔐 Teacher Only
@router.get("/teacher/test")
def teacher_test(current_user: dict = Depends(require_role("teacher"))):
    return {"message": "Teacher access granted", "user": current_user}


# 🔐 Admin Only
@router.get("/admin/test")
def admin_test(current_user: dict = Depends(require_role("admin"))):
    return {"message": "Admin access granted", "user": current_user}
