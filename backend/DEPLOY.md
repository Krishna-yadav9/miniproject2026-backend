# Deploying the ApexStore backend on Render

## 1. Firebase (once)
1. Create a Firebase project at https://console.firebase.google.com and open **Build -> Firestore Database -> Create database** (choose the `(default)` database, production mode).
2. Open **Project settings -> Service accounts -> Generate new private key**. A `.json` file downloads. Keep it private; never commit it.
3. Note your **Project ID** (Project settings -> General).

## 2. Render (Web Service)
1. Push this `backend` folder to a GitHub repository (the `Dockerfile` must be in the repository root, or set Render's *Root Directory* to `backend`).
2. Render dashboard -> **New -> Web Service** -> select the repository.
3. Settings: **Runtime = Docker**, Instance type = Free (or higher), Health Check Path = `/api/health`.
   (Or use **New -> Blueprint**, which reads `render.yaml`.)
4. Add these **Environment Variables**:

| Key | Value |
|---|---|
| `FIREBASE_PROJECT_ID` | your Firebase project id |
| `FIREBASE_DATABASE_ID` | `(default)` |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | the full content of the downloaded JSON file |
| `JWT_SECRET` | a random string of at least 32 characters |
| `CORS_ALLOWED_ORIGINS` | your frontend URL (add after the frontend is deployed) |
| `ADMIN_EMAIL` | `admin@apexstore.com` (or your own) |
| `ADMIN_PASSWORD` | your own strong password |

5. Click **Create Web Service**. The first build takes several minutes.
6. Open `https://<your-backend>.onrender.com/api/health` -> should show `{"status":"UP"}`.
   Open `https://<your-backend>.onrender.com/api/products` -> should list the sample products.

## 3. After the frontend is deployed
Set `CORS_ALLOWED_ORIGINS` to the frontend URL (no trailing slash needed) and let Render redeploy.

## Notes
- Render's free instances sleep after inactivity; the first request after a pause can take about a minute.
- If the log shows `PERMISSION_DENIED`, the service-account JSON or project id is wrong.
- The server listens on the `PORT` that Render provides; locally it defaults to 8081.
