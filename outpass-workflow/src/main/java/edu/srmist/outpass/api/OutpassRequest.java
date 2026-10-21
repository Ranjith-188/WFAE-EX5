package edu.srmist.outpass.api;

/** JSON body of POST /outpass. Dates use yyyy-MM-dd. Becomes the process variables. */
public record OutpassRequest(
        String studentName,
        String regNo,
        String hostelBlock,
        String outDate,
        String returnDate,
        String parentPhone,
        String purpose) {
}
