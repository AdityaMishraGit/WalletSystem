package com.wallet.ledger.application.service;

import com.wallet.ledger.application.port.*;
import com.wallet.ledger.domain.entity.Account;
import com.wallet.ledger.domain.entity.ServiceBundle;
import com.wallet.ledger.domain.entity.ProvisioningService;
import com.wallet.ledger.domain.valueobject.AmountFilter;
import com.wallet.ledger.domain.valueobject.ProvisioningServiceType;
import com.wallet.ledger.domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProvisioningTransactionService {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningTransactionService.class);
    @Value("${ledger.system-accounts.fee}")
    private String feeAccountIdValue;

    private final FindAccountPort findAccountPort;
    private final FindWalletPort findWalletPort;
    private final FindProvisioningServicesPort findProvisioningServicesPort;
    private final FindServiceBundlesPort findServiceBundlesPort;
    private final SaveProvisioningServicePort saveProvisioningServicePort;
    private final SaveServiceBundlePort saveServiceBundlePort;
    private final LedgerPostingEngine ledgerPostingEngine;

    public ProvisioningService createService(String type, String name, String code, String subcategory, String description, String status) {
        String effectiveStatus = status != null && !status.isBlank() ? status : "ACTIVE";
        ProvisioningService service = ProvisioningService.builder()
                .id(null)
                .type(ProvisioningServiceType.valueOf(type))
                .name(name)
                .code(code)
                .subcategory(subcategory)
                .description(description)
                .status(effectiveStatus)
                .createdAt(null)
                .build();
        ProvisioningService created = saveProvisioningServicePort.create(service);
        log.info("Created provisioning service id={} code={}", created.getId(), created.getCode());
        return created;
    }

    public ProvisioningService updateService(UUID id, String type, String name, String code, String subcategory, String description, String status) {
        ProvisioningService service = ProvisioningService.builder()
                .id(id)
                .type(ProvisioningServiceType.valueOf(type))
                .name(name)
                .code(code)
                .subcategory(subcategory)
                .description(description)
                .status(status)
                .createdAt(null)
                .build();
        ProvisioningService updated = saveProvisioningServicePort.update(id, service);
        log.info("Updated provisioning service id={}", id);
        return updated;
    }

    public List<ProvisioningService> listServices(String serviceType, String subcategory) {
        return findProvisioningServicesPort.findAll(serviceType, subcategory);
    }

    public List<ServiceBundle> listBundles(String serviceType, BigDecimal amount, AmountFilter amountFilter,
                                          String subcategory, UUID serviceId) {
        return findServiceBundlesPort.findBundles(serviceType, amount, amountFilter, subcategory, serviceId);
    }

    public ServiceBundle createBundle(UUID provisioningServiceId, String name, String code, BigDecimal fixedAmount,
                                      String subcategory, Integer validityDays, String description, String status) {
        findProvisioningServicesPort.findById(provisioningServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Provisioning service not found: " + provisioningServiceId));
        String effectiveStatus = status != null && !status.isBlank() ? status : "ACTIVE";
        ServiceBundle bundle = ServiceBundle.builder()
                .id(null)
                .provisioningServiceId(provisioningServiceId)
                .name(name)
                .code(code)
                .amountMin(null)
                .amountMax(null)
                .fixedAmount(fixedAmount)
                .subcategory(subcategory)
                .validityDays(validityDays)
                .description(description)
                .status(effectiveStatus)
                .createdAt(null)
                .build();
        ServiceBundle created = saveServiceBundlePort.create(bundle);
        log.info("Created service bundle id={} code={} serviceId={}", created.getId(), created.getCode(), provisioningServiceId);
        return created;
    }

    public ServiceBundle updateBundle(UUID bundleId, UUID provisioningServiceId, String name, String code, BigDecimal fixedAmount,
                                      String subcategory, Integer validityDays, String description, String status) {
        findProvisioningServicesPort.findById(provisioningServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Provisioning service not found: " + provisioningServiceId));
        ServiceBundle bundle = ServiceBundle.builder()
                .id(bundleId)
                .provisioningServiceId(provisioningServiceId)
                .name(name)
                .code(code)
                .amountMin(null)
                .amountMax(null)
                .fixedAmount(fixedAmount)
                .subcategory(subcategory)
                .validityDays(validityDays)
                .description(description)
                .status(status)
                .createdAt(null)
                .build();
        ServiceBundle updated = saveServiceBundlePort.update(bundleId, bundle);
        log.info("Updated service bundle id={}", bundleId);
        return updated;
    }

    @Transactional
    public PostingResult executeProvisioning(String userId, UUID bundleId, String provisioningReference) {
        var wallet = findWalletPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: " + userId));
        ServiceBundle bundle = findServiceBundlesPort.findBundleById(bundleId)
                .orElseThrow(() -> new IllegalArgumentException("Service bundle not found: " + bundleId));
        if (!bundle.isActive()) {
            throw new IllegalArgumentException("Service bundle is not active: " + bundleId);
        }
        BigDecimal amount = bundle.getEffectiveAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bundle has no valid amount: " + bundleId);
        }
        Account userAccount = findAccountPort.findByWalletIdAndType(wallet.getWalletId(), AccountType.USER_WALLET_ACCOUNT)
                .orElseThrow(() -> new IllegalArgumentException("User wallet account not found for userId: " + userId));
        Account feeAccount = findAccountPort.findSystemAccountByType(AccountType.FEE_ACCOUNT)
                .orElseThrow(() -> new IllegalStateException("FEE account not found"));

        String referenceId = "provisioning-" + UUID.randomUUID();
        PostingCommand cmd = PostingCommand.builder()
                .transactionId(TransactionId.generate())
                .transactionType(TransactionType.PROVISIONING)
                .referenceId(referenceId)
                .legs(List.of(
                        PostingLeg.builder().accountId(userAccount.getAccountId()).direction(EntryDirection.DEBIT).amount(amount).build(),
                        PostingLeg.builder().accountId(feeAccount.getAccountId()).direction(EntryDirection.CREDIT).amount(amount).build()))
                .serviceBundleId(bundleId)
                .provisioningReference(provisioningReference)
                .build();
        PostingResult result = ledgerPostingEngine.post(cmd);
        log.info("Provisioning completed userId={} bundleId={} txnId={}", userId, bundleId, result.getTransaction().getTransactionId().value());
        return result;
    }
}
