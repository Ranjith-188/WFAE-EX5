# Hostel Out-Pass Workflow (Camunda 8 SaaS + Spring Boot)

A hostel out-pass process built with Camunda 8 SaaS and a Spring Boot app. Students request an out-pass through a REST call. Day-outs are auto-approved, overnight requests go to the Warden in Tasklist, and invalid dates are rejected.

---

## Process Model

<img width="1229" height="773" alt="image" src="https://github.com/user-attachments/assets/49b97022-ac38-4aae-ab61-e7d7707d15de" />


**Flow**
1. **Start Event** – Out-pass requested (`POST /outpass`).
2. **Check request** (service task, `check-outpass`) – calculates nights and checks the dates.
3. **Gateway** –
   - Invalid dates → rejected.
   - 0 nights (day out) → auto-approved.
   - 1 or more nights → **Warden approval**.
4. **Warden approval** (user task with form) – the warden approves or rejects and adds remarks.
5. **Issue out-pass** (service task, `issue-outpass`) – creates pass number `OP-<regNo>-<outDate>` and logs "SMS to parent".
6. **End Event** – `passStatus` = `ISSUED` or `REJECTED`.

---

## Camunda Cluster

<img width="1600" height="780" alt="image" src="https://github.com/user-attachments/assets/2a4c4a36-1412-4fe3-abe3-a04f937c9d2a" />


| Item | Value |
|---|---|
| Cluster name | leave-demo |
| Version | 8.8 |
| Status | Healthy |
| API client | outpass-app |

---

## Tasklist – Warden Approval

<img width="1248" height="466" alt="image" src="https://github.com/user-attachments/assets/cf698582-27b1-4346-9102-70efd8079bea" />


The warden opens the task, clicks **Assign to me**, ticks **Approve this out-pass**, adds remarks and completes the task.

---

## Key IDs

| Item | Value |
|---|---|
| Process ID | `hostel-outpass` |
| Task types | `check-outpass`, `issue-outpass` |
| Form ID | `warden-approval-form` |
| Form keys | `approved`, `wardenRemarks` |

## Variables

| Worker | Reads | Writes |
|---|---|---|
| check-outpass | regNo, outDate, returnDate | nights, valid, approved |
| issue-outpass | regNo, outDate, studentName, parentPhone, approved, wardenRemarks | passStatus, passNumber |

---

## How to Run

1. Put your Client ID, Client Secret, Cluster ID and Region in `src/main/resources/application.yaml`.
2. Run:
```
   mvn spring-boot:run
```
3. Test with curl:
```
   curl -X POST http://localhost:8080/outpass -H "Content-Type: application/json" -d '{"studentName":"Priya","regNo":"RA2311003010001","hostelBlock":"A","outDate":"2026-10-25","returnDate":"2026-10-27","parentPhone":"9876543210","purpose":"Home visit"}'
```
4. Open Tasklist, complete the warden task, and check the result in Operate.

## Test Cases

| Case | outDate | returnDate | Result |
|---|---|---|---|
| Day out | 2026-10-25 | 2026-10-25 | Auto-approved, ISSUED |
| Overnight | 2026-10-25 | 2026-10-27 | Warden approval, then ISSUED or REJECTED |
| Invalid | 2026-10-27 | 2026-10-25 | REJECTED |

## Tools Used
- Camunda 8 SaaS (Modeler, Tasklist, Operate)
- Spring Boot 3.5.6
- Java 17
- BPMN 2.0
- GitHub
