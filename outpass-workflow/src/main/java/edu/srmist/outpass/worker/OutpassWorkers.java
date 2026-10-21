package edu.srmist.outpass.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OutpassWorkers {

    private static final Logger log = LoggerFactory.getLogger(OutpassWorkers.class);

    /**
     * Reads : regNo, outDate, returnDate
     * Writes: nights, valid, approved (true only for a day out)
     */
    @JobWorker(type = "check-outpass")
    public Map<String, Object> checkOutpass(@Variable String regNo,
                                            @Variable String outDate,
                                            @Variable String returnDate) {
        LocalDate out = LocalDate.parse(outDate);
        LocalDate back = LocalDate.parse(returnDate);

        long nights = ChronoUnit.DAYS.between(out, back);   // negative if return is before out
        boolean valid = !back.isBefore(out);
        boolean approved = valid && nights == 0;             // day out -> auto-approve

        log.info("Checked out-pass for {}: nights={}, valid={}, autoApproved={}",
                regNo, nights, valid, approved);

        return Map.of("nights", nights, "valid", valid, "approved", approved);
    }

    /**
     * Reads : regNo, outDate, studentName, parentPhone, approved, wardenRemarks
     * Writes: passStatus (ISSUED / REJECTED), passNumber (only when issued)
     */
    @JobWorker(type = "issue-outpass")
    public Map<String, Object> issueOutpass(@Variable String regNo,
                                            @Variable String outDate,
                                            @Variable String studentName,
                                            @Variable String parentPhone,
                                            @Variable Boolean approved,
                                            @Variable String wardenRemarks) {
        boolean issued = Boolean.TRUE.equals(approved);
        String passStatus = issued ? "ISSUED" : "REJECTED";

        Map<String, Object> result = new HashMap<>();
        result.put("passStatus", passStatus);

        if (issued) {
            String passNumber = "OP-" + regNo + "-" + outDate;
            result.put("passNumber", passNumber);
            log.info("SMS to parent {}: out-pass {} for {} is ISSUED. Warden remarks: {}",
                    parentPhone, passNumber, studentName, wardenRemarks);
        } else {
            log.info("SMS to parent {}: out-pass request of {} is REJECTED. Warden remarks: {}",
                    parentPhone, studentName, wardenRemarks);
        }
        return result;
    }
}
