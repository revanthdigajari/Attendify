from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from datetime import datetime
from . import models, database
from .routers import admin_router 
from .models import Base, Teacher, Section, Subject, AcademicMap, Student, AttendanceRecords, AttendanceFlag

# --- STEP 1: FORCE DATABASE REBUILD ---
print("🧹 ATTENTION: Wiping and rebuilding database...")
try:
    Base.metadata.drop_all(bind=database.engine)
    Base.metadata.create_all(bind=database.engine)
    print("✅ Database rebuilt successfully.")
except Exception as e:
    print(f"❌ Critical Reset Error: {e}")

app = FastAPI(
    title="Attendify Admin System",
    description="Full Backend for Student Onboarding and Attendance"
)

# 2. CORS MIDDLEWARE
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 3. SEED DATA FUNCTION
@app.on_event("startup")
def seed_college_data():
    db: Session = database.SessionLocal()
    try:
        # --- A. SECTIONS ---
        sections = ["A1", "B2"]
        for sec_name in sections:
            db.add(Section(section_name=sec_name, capacity=60, semester=1, current_enrollment=0))
        db.commit()

        # --- B. TEACHER ---
        db.add(Teacher(
            name="Prof. Harrison",
            department="CS",
            email="harrison@college.edu",
            phone="9876543210",
            is_active=True
        ))
        db.commit()

        # --- C. SUBJECT (Mandatory for Attendance) ---
        new_subject = Subject(subject_name="Mathematics", department="General", credits=4)
        db.add(new_subject)
        db.commit()

        # --- D. STUDENTS ---
        sec_a1 = db.query(Section).filter_by(section_name="A1").first()
        if sec_a1:
            db.add(Student(name="Steve Smith", roll_number="2334", section_id=sec_a1.id))
            db.add(Student(name="Albert Flores", roll_number="5542", section_id=sec_a1.id))
        db.commit()

        # --- E. ATTENDANCE (Now calling the logic inside the flow) ---
        student = db.query(Student).first()
        subject = db.query(Subject).first()

        if student and subject:
            db.add(AttendanceRecords(
                timestamp=datetime.now(),
                status="Present",
                student_id=student.id,
                subject_id=subject.id
            ))
            db.commit()
            print(f"✅ Created attendance for {student.name}")

        # --- F. ATTENDANCE FLAGS ---
        db.add_all([
            AttendanceFlag(student_id="STU101", student_name="Alice Smith", reason="QR Error", date="2023-10-27", status="PENDING"),
            AttendanceFlag(student_id="STU102", student_name="Bob Jones", reason="Battery died", date="2023-10-28", status="PENDING")
        ])
        db.commit()
        
        print(">>> ✅ Data seeded successfully.")
        
    except Exception as e:
        db.rollback()
        print(f"❌ Seed Error: {e}")
    finally:
        db.close()

app.include_router(admin_router.router)

@app.get("/")
async def root():
    return {"project": "Attendify", "status": "Online"}