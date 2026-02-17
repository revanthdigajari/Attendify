from fastapi import FastAPI
from app.routers import health, auth

app = FastAPI(
    title="Attendify Backend",
    version="1.0.0"
)

app.include_router(health.router)
app.include_router(auth.router)

@app.get("/")
def root():
    return {"message": "Attendify Backend Running"}
