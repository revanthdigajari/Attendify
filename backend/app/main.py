from fastapi import FastAPI

app = FastAPI(title="Attendify API")

@app.get("/")
async def root():
    return {
        "status": "Running",
        "message": "Welcome to Attendify Backend",
        "version": "1.0.0"
    }

@app.get("/health")
async def health_check():
    return {"status": "healthy"}