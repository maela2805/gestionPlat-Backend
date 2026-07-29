package GestionPlat.example.demo.modules.billing.repository;

import GestionPlat.example.demo.modules.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByTiersId(Long tiersId);

    boolean existsByPaymentNumber(String paymentNumber);
}
