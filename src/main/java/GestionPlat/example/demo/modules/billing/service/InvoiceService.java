package GestionPlat.example.demo.modules.billing.service;

import GestionPlat.example.demo.modules.billing.dto.*;
import GestionPlat.example.demo.modules.billing.model.InvoiceStatus;
import GestionPlat.example.demo.modules.billing.model.InvoiceType;

import java.util.List;

public interface InvoiceService {

    List<InvoiceDTO> getAllInvoices(InvoiceType type, InvoiceStatus status);

    InvoiceDTO getInvoiceById(Long id);

    InvoiceDTO createInvoice(CreateInvoiceRequest request);

    InvoiceDTO createInvoiceFromSale(Long saleId);

    InvoiceDTO createInvoiceFromPurchaseOrder(Long purchaseOrderId);

    InvoiceDTO validateInvoice(Long id);

    InvoiceDTO cancelInvoice(Long id);

    InvoiceDTO updateInvoice(Long id, UpdateInvoiceRequest request);

    InvoiceDTO confirmDelivery(Long id);

    PaymentDTO addPayment(CreatePaymentRequest request);

    List<PaymentDTO> getPaymentsByInvoiceId(Long invoiceId);
}
