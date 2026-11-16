package com.project.hospital.invoice.service;

import com.project.hospital.common.exception.BusinessRuleViolationException;
import com.project.hospital.common.exception.ResourceNotFoundException;
import com.project.hospital.invoice.dto.PaymentRequest;
import com.project.hospital.invoice.entity.Invoice;
import com.project.hospital.invoice.entity.InvoiceStatus;
import com.project.hospital.invoice.entity.Payment;
import com.project.hospital.invoice.entity.PaymentMethod;
import com.project.hospital.invoice.mapper.InvoiceMapper;
import com.project.hospital.invoice.repository.InvoiceRepository;
import com.project.hospital.invoice.repository.PaymentRepository;
import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String STAFF_EMAIL = "staff@hospital.local";

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Invoice invoice;
    private User staff;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setTotalAmount(new BigDecimal("200000.00"));
        invoice.setPaidAmount(new BigDecimal("0.00"));
        invoice.setStatus(InvoiceStatus.UNPAID);

        staff = new User();
        staff.setEmail(STAFF_EMAIL);
    }

    private void givenInvoiceAndStaff() {
        when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(invoice));
        when(userRepository.findByEmail(STAFF_EMAIL)).thenReturn(Optional.of(staff));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void pay_partialAmount_marksInvoicePartiallyPaid() {
        givenInvoiceAndStaff();

        paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("50000"), PaymentMethod.CASH));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("50000");
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo("150000");

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment saved = captor.getValue();

        assertThat(saved.getAmount()).isEqualByComparingTo("50000");
        assertThat(saved.getMethod()).isEqualTo(PaymentMethod.CASH);
        assertThat(saved.getReceivedBy()).isSameAs(staff);
        assertThat(saved.getInvoice()).isSameAs(invoice);
    }

    @Test
    void pay_fullAmount_marksInvoicePaid() {
        givenInvoiceAndStaff();

        paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("200000"), PaymentMethod.TRANSFER));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getRemainingAmount()).isEqualByComparingTo("0");
    }

    @Test
    void pay_twoInstallments_endsAsPaid() {
        givenInvoiceAndStaff();

        paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("50000"), PaymentMethod.CASH));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);

        paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("150000"), PaymentMethod.CARD));

        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("200000");
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void pay_amountExceedsRemaining_throwsBusinessRule() {
        when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("200000.01"), PaymentMethod.CASH)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Payment amount exceeds the remaining amount");

        assertThat(invoice.getPaidAmount()).isEqualByComparingTo("0");
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void pay_invoiceAlreadyPaid_throwsBusinessRule() {
        invoice.setPaidAmount(new BigDecimal("200000.00"));
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("1000"), PaymentMethod.CASH)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Invoice is already fully paid");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void pay_invoiceNotFound_throwsNotFound() {
        when(invoiceRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.pay(STAFF_EMAIL, 1L,
                new PaymentRequest(new BigDecimal("1000"), PaymentMethod.CASH)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invoice not found");
    }
}