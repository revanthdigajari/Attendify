from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.core.security import (
    verify_password,
    hash_password,
    create_access_token
)

router = APIRouter(prefix="/auth", tags=["Authentication"])

# Temporary fake user database (for testing)
fake_users_db = {
    "student@gcet.edu.in": {
        "email": "student@gcet.edu.in",
        "hashed_password": hash_password("student123"),
        "role": "student"
    },
    "teacher@gcet.edu.in": {
        "email": "teacher@gcet.edu.in",
        "hashed_password": hash_password("teacher123"),
        "role": "teacher"
    },
    "admin@gcet.edu.in": {
        "email": "admin@gcet.edu.in",
        "hashed_password": hash_password("admin123"),
        "role": "admin"
    }
}

class LoginRequest(BaseModel):
    email: str
    password: str

@router.post("/login")
def login(data: LoginRequest):
    user = fake_users_db.get(data.email)

    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")

    if not verify_password(data.password, user["hashed_password"]):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    token = create_access_token({
        "sub": user["email"],
        "role": user["role"]
    })

    return {
        "access_token": token,
        "token_type": "bearer",
        "role": user["role"]
    }
  
