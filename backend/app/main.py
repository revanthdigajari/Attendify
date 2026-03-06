from fastapi import FastAPI
from app.routers import health, auth,admin,teacher,student

app = FastAPI(
    title="Attendify Backend",
    version="1.0.0"
)

app.include_router(health.router)
app.include_router(auth.router)
app.include_router(admin.router)
app.include_router(teacher.router)
app.include_router(student.router)  

@app.get("/")
def root():
    return {"message": "Attendify Backend Running"}
