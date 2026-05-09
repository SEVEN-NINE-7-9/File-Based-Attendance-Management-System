# 📚 File-Based Attendance Management System
**Week 7 Java Project — File Handling & Persistent Storage**

---

## 🎯 Project Overview

A complete attendance management system with two modes:
- **Console Mode** — Terminal-based UI (original Week 7 assignment)
- **Web Mode** — Beautiful browser interface linked to the Java backend

---

## 🗂️ Project Structure

```
AttendanceSystem/
├── src/
│   ├── AttendanceSystem.java   ← Console-based system (original)
│   └── AttendanceServer.java   ← HTTP server (for web UI)
├── web/
│   └── index.html              ← Web frontend
├── data/
│   └── attendance.txt          ← Persistent file storage (auto-created)
├── bin/                        ← Compiled .class files (auto-created)
├── run.bat                     ← Windows launcher
├── run.sh                      ← Linux/Mac launcher
└── README.md
```

---

## 🚀 How to Run

### Prerequisites
- Java JDK 11 or higher installed
- Check: `java -version` and `javac -version` in terminal

### Windows
```
Double-click run.bat
```
or in Command Prompt:
```
run.bat
```

### Linux / Mac
```bash
chmod +x run.sh
./run.sh
```

### Manual Compilation
```bash
mkdir bin
javac -d bin src/AttendanceSystem.java src/AttendanceServer.java
```

**Console mode:**
```bash
java -cp bin AttendanceSystem
```

**Web mode:**
```bash
java -cp bin AttendanceServer
# Then open http://localhost:8080 in your browser
```

---

## ✨ Features

### Console Mode (AttendanceSystem.java)
| # | Feature |
|---|---------|
| 1 | Mark Attendance (Name, Roll, Subject, Status) |
| 2 | View All Records (formatted table) |
| 3 | Search Student by name or roll number |
| 4 | Attendance Report (counts + percentage) |
| 5 | Delete Record by roll number |
| 6 | Exit |

### Web Mode (AttendanceServer.java + index.html)
- **Mark Attendance** — Beautiful form with Present/Absent/Late selector
- **View Records** — Searchable table with live refresh
- **Report Page** — Visual ring chart, subject breakdown, student-wise stats
- **Delete** — Remove all records for a student
- **Real-time stats** — Total, Present, Absent, Late counters

---

## 💾 File Handling (Core Concept)

The system uses `attendance.txt` for persistent storage. Format:
```
ROLL|NAME|SUBJECT|STATUS|DATE
CS101|John Doe|Data Structures|Present|01-01-2025 10:30
CS102|Jane Smith|Java Programming|Absent|01-01-2025 10:31
```

**Java classes used:**
- `FileWriter` + `BufferedWriter` → Writing records
- `FileReader` + `BufferedReader` → Reading records
- `File` → File existence checks

---

## 📖 Learning Outcomes (Week 7)

✅ File handling (read/write/append)  
✅ Persistent data storage  
✅ BufferedReader / BufferedWriter  
✅ Real-world college system design  
✅ HTTP server with Java (bonus)  
✅ REST API design (bonus)  
✅ HTML/CSS/JS frontend integration (bonus)  

---

## 🛑 Troubleshooting

| Issue | Fix |
|-------|-----|
| `javac not found` | Install Java JDK and add to PATH |
| Port 8080 in use | Change `PORT = 8080` to another port in AttendanceServer.java |
| Web page can't connect | Make sure server is running (`java -cp bin AttendanceServer`) |
| File not found error | The `data/` folder is auto-created on first run |
