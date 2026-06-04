import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const today = new Date().toISOString().slice(0, 10);

function readSession() {
  return {
    accessToken: localStorage.getItem("accessToken") || "",
    refreshToken: localStorage.getItem("refreshToken") || "",
    adminAccessToken: localStorage.getItem("adminAccessToken") || ""
  };
}

async function api(path, options = {}, token = "") {
  const headers = { ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(path, { ...options, headers });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    const message = data?.message || data?.error || `Request failed with ${response.status}`;
    throw new Error(message);
  }

  return data;
}

function App() {
  const [session, setSession] = useState(readSession);
  const [activeTab, setActiveTab] = useState("patient");
  const [notice, setNotice] = useState("Ready. Start with patient login or register.");
  const [profile, setProfile] = useState(null);
  const [prescriptions, setPrescriptions] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [reminders, setReminders] = useState([]);
  const [dietToday, setDietToday] = useState([]);
  const [dietReminders, setDietReminders] = useState([]);
  const [compliance, setCompliance] = useState(null);
  const [patients, setPatients] = useState([]);

  const isPatientLoggedIn = Boolean(session.accessToken);
  const isAdminLoggedIn = Boolean(session.adminAccessToken);

  function savePatientSession(data) {
    localStorage.setItem("accessToken", data.accessToken);
    localStorage.setItem("refreshToken", data.refreshToken);
    setSession(readSession());
  }

  function saveAdminSession(data) {
    localStorage.setItem("adminAccessToken", data.accessToken);
    setSession(readSession());
  }

  function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("adminAccessToken");
    setSession(readSession());
    setProfile(null);
    setNotice("Logged out locally.");
  }

  async function run(label, fn) {
    try {
      setNotice(`${label}...`);
      await fn();
      setNotice(`${label} done.`);
    } catch (error) {
      setNotice(error.message);
    }
  }

  async function loadPatientDashboard() {
    const [me, prescriptionPage, medicineSchedules, medicineReminders, meals, mealReminders, dietScore] =
      await Promise.all([
        api("/api/v1/patients/me", {}, session.accessToken),
        api("/api/v1/prescriptions?page=0&size=10", {}, session.accessToken),
        api("/api/v1/medicine-schedules", {}, session.accessToken),
        api("/api/v1/medicine-schedules/reminders", {}, session.accessToken),
        api("/api/v1/diet/today", {}, session.accessToken),
        api("/api/v1/diet/reminders", {}, session.accessToken),
        api("/api/v1/diet/compliance", {}, session.accessToken)
      ]);

    setProfile(me);
    setPrescriptions(prescriptionPage.content || []);
    setSchedules(medicineSchedules || []);
    setReminders(medicineReminders || []);
    setDietToday(meals || []);
    setDietReminders(mealReminders || []);
    setCompliance(dietScore);
  }

  async function loadAdminDashboard() {
    const patientPage = await api("/api/v1/admin/patients?page=0&size=20", {}, session.adminAccessToken);
    setPatients(patientPage.content || []);
  }

  useEffect(() => {
    if (isPatientLoggedIn) {
      run("Loading patient dashboard", loadPatientDashboard);
    }
  }, [session.accessToken]);

  useEffect(() => {
    if (isAdminLoggedIn) {
      run("Loading admin dashboard", loadAdminDashboard);
    }
  }, [session.adminAccessToken]);

  const nextReminder = useMemo(() => {
    return reminders.find((reminder) => reminder.status === "PENDING") || reminders[0];
  }, [reminders]);

  return (
    <main>
      <header className="hero">
        <div>
          <p className="eyebrow">Smart Healthcare Reminder</p>
          <h1>Medicine, diet, prescription OCR, and WhatsApp fallback in one demo.</h1>
          <p className="hero-copy">
            Built for the Tekravio Java assignment with a Spring Boot backend and a clean React dashboard.
          </p>
        </div>
        <div className="status-card">
          <span>Status</span>
          <strong>{notice}</strong>
          <button onClick={logout}>Clear login</button>
        </div>
      </header>

      <nav className="tabs">
        <button className={activeTab === "patient" ? "active" : ""} onClick={() => setActiveTab("patient")}>
          Patient
        </button>
        <button className={activeTab === "admin" ? "active" : ""} onClick={() => setActiveTab("admin")}>
          Admin
        </button>
      </nav>

      {activeTab === "patient" ? (
        <PatientArea
          session={session}
          savePatientSession={savePatientSession}
          run={run}
          loadPatientDashboard={loadPatientDashboard}
          isLoggedIn={isPatientLoggedIn}
          profile={profile}
          prescriptions={prescriptions}
          schedules={schedules}
          reminders={reminders}
          nextReminder={nextReminder}
          dietToday={dietToday}
          dietReminders={dietReminders}
          compliance={compliance}
        />
      ) : (
        <AdminArea
          session={session}
          saveAdminSession={saveAdminSession}
          run={run}
          loadAdminDashboard={loadAdminDashboard}
          isLoggedIn={isAdminLoggedIn}
          patients={patients}
        />
      )}
    </main>
  );
}

function PatientArea(props) {
  const {
    session,
    savePatientSession,
    run,
    loadPatientDashboard,
    isLoggedIn,
    profile,
    prescriptions,
    schedules,
    reminders,
    nextReminder,
    dietToday,
    dietReminders,
    compliance
  } = props;

  return (
    <section className="grid">
      <AuthCard saveSession={savePatientSession} run={run} />

      <Card title="Patient Profile">
        {profile ? (
          <div className="summary">
            <strong>{profile.fullName}</strong>
            <span>{profile.email}</span>
            <span>WhatsApp: {profile.whatsappNumber || "Not added"}</span>
          </div>
        ) : (
          <p>Login first, then your profile appears here.</p>
        )}
        <button disabled={!isLoggedIn} onClick={() => run("Refreshing patient dashboard", loadPatientDashboard)}>
          Refresh patient dashboard
        </button>
      </Card>

      <PrescriptionCard session={session} run={run} reload={loadPatientDashboard} disabled={!isLoggedIn} />
      <ScheduleCard session={session} run={run} reload={loadPatientDashboard} disabled={!isLoggedIn} />

      <Card title="Medicine Reminders">
        <p className="metric">{reminders.length}</p>
        <p>Total reminder logs loaded.</p>
        {nextReminder ? (
          <div className="button-row">
            <button onClick={() => run("Marking taken", async () => {
              await api(`/api/v1/medicine-schedules/reminders/${nextReminder.id}/taken`, { method: "PATCH" }, session.accessToken);
              await loadPatientDashboard();
            })}>Mark taken</button>
            <button onClick={() => run("Snoozing reminder", async () => {
              await api(`/api/v1/medicine-schedules/reminders/${nextReminder.id}/snooze`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ minutes: 10 })
              }, session.accessToken);
              await loadPatientDashboard();
            })}>Snooze 10 min</button>
          </div>
        ) : null}
      </Card>

      <ListCard title="Prescriptions" items={prescriptions} render={(item) => `${item.originalFileName} - ${item.status}`} />
      <ListCard title="Schedules" items={schedules} render={(item) => `${item.medicineName} - ${item.frequency}`} />
      <ListCard title="Today Diet" items={dietToday} render={(item) => `${item.mealType}: ${item.description}`} />

      <Card title="Diet Compliance">
        <p className="metric">{compliance?.compliancePercentage ?? 0}%</p>
        <p>Weekly diet compliance score.</p>
        <p>{dietReminders.length} diet reminder logs loaded.</p>
      </Card>
    </section>
  );
}

function AuthCard({ saveSession, run }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({
    email: "patient@example.com",
    password: "Patient@12345",
    fullName: "Demo Patient",
    age: 31,
    medicalHistory: "Diabetes, BP monitoring",
    whatsappNumber: "+919876543210"
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    await run(mode === "login" ? "Patient login" : "Patient register", async () => {
      const path = mode === "login" ? "/api/v1/auth/login" : "/api/v1/auth/register";
      const body = mode === "login" ? { email: form.email, password: form.password } : form;
      const data = await api(path, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });
      saveSession(data);
    });
  }

  return (
    <Card title="Patient Access">
      <div className="button-row">
        <button className={mode === "login" ? "active-light" : ""} onClick={() => setMode("login")}>Login</button>
        <button className={mode === "register" ? "active-light" : ""} onClick={() => setMode("register")}>Register</button>
      </div>
      <form onSubmit={submit}>
        <Input label="Email" value={form.email} onChange={(value) => update("email", value)} />
        <Input label="Password" type="password" value={form.password} onChange={(value) => update("password", value)} />
        {mode === "register" ? (
          <>
            <Input label="Full name" value={form.fullName} onChange={(value) => update("fullName", value)} />
            <Input label="Age" type="number" value={form.age} onChange={(value) => update("age", Number(value))} />
            <Input label="WhatsApp number" value={form.whatsappNumber} onChange={(value) => update("whatsappNumber", value)} />
            <Textarea label="Medical history" value={form.medicalHistory} onChange={(value) => update("medicalHistory", value)} />
          </>
        ) : null}
        <button type="submit">{mode === "login" ? "Login patient" : "Register patient"}</button>
      </form>
    </Card>
  );
}

function PrescriptionCard({ session, run, reload, disabled }) {
  const [file, setFile] = useState(null);

  async function upload(event) {
    event.preventDefault();
    await run("Uploading prescription", async () => {
      const data = new FormData();
      data.append("file", file);
      await api("/api/v1/prescriptions", { method: "POST", body: data }, session.accessToken);
      await reload();
    });
  }

  return (
    <Card title="Prescription Upload">
      <p>Upload JPG, PNG, or PDF. Backend sends it to S3 and Textract.</p>
      <form onSubmit={upload}>
        <input disabled={disabled} type="file" accept=".jpg,.jpeg,.png,.pdf" onChange={(event) => setFile(event.target.files[0])} />
        <button disabled={disabled || !file} type="submit">Upload prescription</button>
      </form>
    </Card>
  );
}

function ScheduleCard({ session, run, reload, disabled }) {
  const [form, setForm] = useState({
    medicineName: "Metformin",
    dosage: "500 mg",
    frequency: "TWICE_DAILY",
    recurrenceType: "DAILY",
    startDate: today,
    timeSlots: "09:00,21:00"
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    await run("Creating medicine schedule", async () => {
      await api("/api/v1/medicine-schedules", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          medicineId: null,
          medicineName: form.medicineName,
          dosage: form.dosage,
          frequency: form.frequency,
          recurrenceType: form.recurrenceType,
          customIntervalHours: null,
          startDate: form.startDate,
          endDate: null,
          timeSlots: form.timeSlots.split(",").map((slot) => slot.trim())
        })
      }, session.accessToken);
      await reload();
    });
  }

  return (
    <Card title="Create Medicine Schedule">
      <form onSubmit={submit}>
        <Input label="Medicine" value={form.medicineName} onChange={(value) => update("medicineName", value)} />
        <Input label="Dosage" value={form.dosage} onChange={(value) => update("dosage", value)} />
        <Input label="Frequency" value={form.frequency} onChange={(value) => update("frequency", value)} />
        <Input label="Start date" type="date" value={form.startDate} onChange={(value) => update("startDate", value)} />
        <Input label="Times" value={form.timeSlots} onChange={(value) => update("timeSlots", value)} />
        <button disabled={disabled} type="submit">Create schedule</button>
      </form>
    </Card>
  );
}

function AdminArea({ session, saveAdminSession, run, loadAdminDashboard, isLoggedIn, patients }) {
  return (
    <section className="grid">
      <AdminLoginCard saveSession={saveAdminSession} run={run} />
      <Card title="Admin Patients">
        <button disabled={!isLoggedIn} onClick={() => run("Refreshing admin dashboard", loadAdminDashboard)}>
          Refresh admin dashboard
        </button>
        <ul className="plain-list">
          {patients.map((patient) => (
            <li key={patient.id}>
              <strong>#{patient.id}</strong> {patient.fullName} - {patient.email}
            </li>
          ))}
        </ul>
      </Card>
      <DietPlanCard session={session} run={run} reload={loadAdminDashboard} disabled={!isLoggedIn} patients={patients} />
    </section>
  );
}

function AdminLoginCard({ saveSession, run }) {
  const [email, setEmail] = useState("admin@example.com");
  const [password, setPassword] = useState("change-me-strong-password");

  async function submit(event) {
    event.preventDefault();
    await run("Admin login", async () => {
      const data = await api("/api/v1/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
      });
      saveSession(data);
    });
  }

  return (
    <Card title="Admin Access">
      <form onSubmit={submit}>
        <Input label="Admin email" value={email} onChange={setEmail} />
        <Input label="Admin password" type="password" value={password} onChange={setPassword} />
        <button type="submit">Login admin</button>
      </form>
    </Card>
  );
}

function DietPlanCard({ session, run, reload, disabled, patients }) {
  const [form, setForm] = useState({
    patientId: "",
    mealType: "BREAKFAST",
    description: "Oats, boiled egg, and sugar-free tea",
    scheduledTime: "08:30",
    dietaryRestrictions: "Low sugar",
    calories: 450,
    startDate: today
  });

  useEffect(() => {
    if (!form.patientId && patients[0]?.id) {
      setForm((current) => ({ ...current, patientId: patients[0].id }));
    }
  }, [patients, form.patientId]);

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    await run("Creating diet plan", async () => {
      await api("/api/v1/admin/diet-plans", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...form,
          patientId: Number(form.patientId),
          calories: Number(form.calories),
          endDate: null
        })
      }, session.adminAccessToken);
      await reload();
    });
  }

  return (
    <Card title="Create Diet Plan">
      <form onSubmit={submit}>
        <Input label="Patient ID" value={form.patientId} onChange={(value) => update("patientId", value)} />
        <Input label="Meal type" value={form.mealType} onChange={(value) => update("mealType", value)} />
        <Textarea label="Description" value={form.description} onChange={(value) => update("description", value)} />
        <Input label="Scheduled time" type="time" value={form.scheduledTime} onChange={(value) => update("scheduledTime", value)} />
        <Input label="Restrictions" value={form.dietaryRestrictions} onChange={(value) => update("dietaryRestrictions", value)} />
        <Input label="Calories" type="number" value={form.calories} onChange={(value) => update("calories", value)} />
        <Input label="Start date" type="date" value={form.startDate} onChange={(value) => update("startDate", value)} />
        <button disabled={disabled || !form.patientId} type="submit">Create diet plan</button>
      </form>
    </Card>
  );
}

function Card({ title, children }) {
  return (
    <article className="card">
      <h2>{title}</h2>
      {children}
    </article>
  );
}

function ListCard({ title, items, render }) {
  return (
    <Card title={title}>
      {items.length ? (
        <ul className="plain-list">
          {items.map((item) => (
            <li key={item.id}>{render(item)}</li>
          ))}
        </ul>
      ) : (
        <p>No records yet.</p>
      )}
    </Card>
  );
}

function Input({ label, type = "text", value, onChange }) {
  return (
    <label>
      <span>{label}</span>
      <input type={type} value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function Textarea({ label, value, onChange }) {
  return (
    <label>
      <span>{label}</span>
      <textarea value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

createRoot(document.getElementById("root")).render(<App />);
