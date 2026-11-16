package com.project.hospital.invoice.mapper;

import com.project.hospital.invoice.dto.InvoiceItemResponse;
import com.project.hospital.invoice.dto.InvoiceResponse;
import com.project.hospital.invoice.dto.InvoiceSummaryResponse;
import com.project.hospital.invoice.dto.PaymentResponse;
import com.project.hospital.invoice.entity.Invoice;
import com.project.hospital.invoice.entity.InvoiceItem;
import com.project.hospital.invoice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "visit.id", target = "visitId")
    @Mapping(source = "visit.visitDate", target = "visitDate")
    @Mapping(source = "visit.patient.id", target = "patientId")
    @Mapping(source = "visit.patient.fullName", target = "patientName")
    @Mapping(source = "visit.patient.medicalRecordNumber", target = "medicalRecordNumber")
    @Mapping(source = "visit.doctor.fullName", target = "doctorName")
    @Mapping(source = "visit.doctor.poli.name", target = "poliName")
    InvoiceResponse toResponse(Invoice invoice);

    @Mapping(source = "visit.id", target = "visitId")
    @Mapping(source = "visit.visitDate", target = "visitDate")
    @Mapping(source = "visit.patient.id", target = "patientId")
    @Mapping(source = "visit.patient.fullName", target = "patientName")
    @Mapping(source = "visit.patient.medicalRecordNumber", target = "medicalRecordNumber")
    @Mapping(source = "visit.doctor.fullName", target = "doctorName")
    @Mapping(source = "visit.doctor.poli.name", target = "poliName")
    InvoiceSummaryResponse toSummary(Invoice invoice);

    InvoiceItemResponse toItemResponse(InvoiceItem item);

    @Mapping(source = "invoice.id", target = "invoiceId")
    @Mapping(source = "receivedBy.email", target = "receivedBy")
    PaymentResponse toPaymentResponse(Payment payment);
}