# Hostel Out-Pass Workflow (Camunda 8 SaaS + Spring Boot)

## Run
1. Fill the four values in `src/main/resources/application.yaml` (or set the CAMUNDA_* environment variables).
2. `mvn spring-boot:run` - the log should show the deployment of `hostel-outpass` and the two workers opening.

## Model values that must match the code
| In the model | Value | In the code |
|---|---|---|
| Process ID | hostel-outpass | `bpmnProcessId("hostel-outpass")` |
| Task type | check-outpass | `@JobWorker(type = "check-outpass")` |
| Task type | issue-outpass | `@JobWorker(type = "issue-outpass")` |
| Form ID | warden-approval-form | `warden-approval.form` in `@Deployment` |
| Form keys | approved, wardenRemarks | `@Variable` names in `issueOutpass` |

Gateway conditions: `=valid and nights = 0`, `=valid and nights >= 1`, `=not(valid)`

## Test cases (curl)
Auto-approved (day out):
```
curl -X POST http://localhost:8080/outpass -H "Content-Type: application/json" -d '{"studentName":"Priya","regNo":"RA2311003010001","hostelBlock":"A","outDate":"2026-10-25","returnDate":"2026-10-25","parentPhone":"9876543210","purpose":"Shopping"}'
```
Warden approval needed (2 nights) - then Tasklist -> Assign to me -> tick Approve -> Complete:
```
curl -X POST http://localhost:8080/outpass -H "Content-Type: application/json" -d '{"studentName":"Priya","regNo":"RA2311003010001","hostelBlock":"A","outDate":"2026-10-25","returnDate":"2026-10-27","parentPhone":"9876543210","purpose":"Home visit"}'
```
Rejected (return before out):
```
curl -X POST http://localhost:8080/outpass -H "Content-Type: application/json" -d '{"studentName":"Priya","regNo":"RA2311003010001","hostelBlock":"A","outDate":"2026-10-27","returnDate":"2026-10-25","parentPhone":"9876543210","purpose":"Home visit"}'
```

## Variables note
| Worker | Reads | Writes |
|---|---|---|
| check-outpass | regNo, outDate, returnDate | nights, valid, approved (true for a day out) |
| issue-outpass | regNo, outDate, studentName, parentPhone, approved, wardenRemarks | passStatus, passNumber |

User task *Warden approval* writes `approved` (checkbox) and `wardenRemarks`.
Final variable `passStatus` = ISSUED or REJECTED.
