package com.wallet.ledger.adapter.web;

import com.wallet.ledger.adapter.web.dto.*;
import com.wallet.ledger.application.service.ProvisioningTransactionService;
import com.wallet.ledger.domain.entity.ProvisioningService;
import com.wallet.ledger.domain.valueobject.AmountFilter;
import com.wallet.ledger.domain.entity.ServiceBundle;
import com.wallet.ledger.domain.entity.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Provisioning", description = "Create/update provisioning services and bundles; list services and bundles; execute provisioning transaction")
public class ProvisioningController {

    private static final Logger log = LoggerFactory.getLogger(ProvisioningController.class);
    private final ProvisioningTransactionService provisioningTransactionService;

    @PostMapping(value = "/provisioning-services", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create provisioning service", description = "Create a new provisioning service (e.g. DSTv, Airtel). Code must be unique.")
    @ApiResponse(responseCode = "201", description = "Provisioning service created", content = @Content(schema = @Schema(implementation = ProvisioningServiceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or duplicate code")
    public ResponseEntity<ProvisioningServiceResponse> createService(@Valid @RequestBody CreateProvisioningServiceRequest request) {
        log.info("POST /provisioning-services code={} name={}", request.getCode(), request.getName());
        ProvisioningService created = provisioningTransactionService.createService(
                request.getType(), request.getName(), request.getCode(),
                request.getSubcategory(), request.getDescription(), request.getStatus());
        ProvisioningServiceResponse body = toServiceResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PutMapping(value = "/provisioning-services/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update provisioning service", description = "Update an existing provisioning service by id.")
    @ApiResponse(responseCode = "200", description = "Provisioning service updated", content = @Content(schema = @Schema(implementation = ProvisioningServiceResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or service not found")
    public ResponseEntity<ProvisioningServiceResponse> updateService(
            @Parameter(description = "Provisioning service ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateProvisioningServiceRequest request) {
        log.info("PUT /provisioning-services/{} code={}", id, request.getCode());
        ProvisioningService updated = provisioningTransactionService.updateService(
                id, request.getType(), request.getName(), request.getCode(),
                request.getSubcategory(), request.getDescription(), request.getStatus());
        ProvisioningServiceResponse body = toServiceResponse(updated);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @GetMapping(value = "/provisioning-services", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List provisioning services", description = "Get provisioning services with optional filters: serviceType, subcategory")
    @ApiResponse(responseCode = "200", description = "List of provisioning services", content = @Content(schema = @Schema(implementation = ProvisioningServiceResponse.class)))
    public ResponseEntity<List<ProvisioningServiceResponse>> listServices(
            @Parameter(description = "Service type: TV_RECHARGE, PREPAID_RECHARGE, POSTPAID_RECHARGE, SUBSCRIPTION, DATA_BUNDLE, UTILITY_BILL")
            @RequestParam(required = false) String serviceType,
            @Parameter(description = "Subcategory filter")
            @RequestParam(required = false) String subcategory) {
        log.info("GET /provisioning-services serviceType={} subcategory={}", serviceType, subcategory);
        List<ProvisioningService> list = provisioningTransactionService.listServices(serviceType, subcategory);
        List<ProvisioningServiceResponse> body = list.stream().map(this::toServiceResponse).toList();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @GetMapping(value = "/provisioning-services/bundles", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List service bundles", description = "Get bundles (each has a fixed amount). Optional filters: serviceType, amount + amountFilter (GREATER_THAN or LESS_THAN), subcategory, serviceId")
    @ApiResponse(responseCode = "200", description = "List of service bundles", content = @Content(schema = @Schema(implementation = ServiceBundleResponse.class)))
    public ResponseEntity<List<ServiceBundleResponse>> listBundles(
            @Parameter(description = "Service type filter")
            @RequestParam(required = false) String serviceType,
            @Parameter(description = "Amount threshold: used with amountFilter to return bundles with fixed_amount > or < this value")
            @RequestParam(required = false) BigDecimal amount,
            @Parameter(description = "GREATER_THAN = bundles with fixed_amount > amount; LESS_THAN = bundles with fixed_amount < amount. Ignored if amount is not provided.")
            @RequestParam(required = false) AmountFilter amountFilter,
            @Parameter(description = "Subcategory filter")
            @RequestParam(required = false) String subcategory,
            @Parameter(description = "Provisioning service ID filter")
            @RequestParam(required = false) UUID serviceId) {
        log.info("GET /provisioning-services/bundles serviceType={} amount={} amountFilter={} subcategory={} serviceId={}",
                serviceType, amount, amountFilter, subcategory, serviceId);
        List<ServiceBundle> list = provisioningTransactionService.listBundles(serviceType, amount, amountFilter, subcategory, serviceId);
        List<ServiceBundleResponse> body = list.stream().map(this::toBundleResponse).toList();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(value = "/provisioning-services/bundles", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create service bundle", description = "Add a new bundle to a provisioning service. Specify provisioningServiceId to attach the bundle to that service.")
    @ApiResponse(responseCode = "201", description = "Service bundle created", content = @Content(schema = @Schema(implementation = ServiceBundleResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or provisioning service not found")
    public ResponseEntity<ServiceBundleResponse> createBundle(@Valid @RequestBody CreateServiceBundleRequest request) {
        log.info("POST /provisioning-services/bundles code={} serviceId={}", request.getCode(), request.getProvisioningServiceId());
        ServiceBundle created = provisioningTransactionService.createBundle(
                UUID.fromString(request.getProvisioningServiceId()), request.getName(), request.getCode(),
                request.getFixedAmount(), request.getSubcategory(), request.getValidityDays(),
                request.getDescription(), request.getStatus());
        ServiceBundleResponse body = toBundleResponse(created);
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PutMapping(value = "/provisioning-services/bundles/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update service bundle", description = "Update an existing service bundle by id.")
    @ApiResponse(responseCode = "200", description = "Service bundle updated", content = @Content(schema = @Schema(implementation = ServiceBundleResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request or bundle/service not found")
    public ResponseEntity<ServiceBundleResponse> updateBundle(
            @Parameter(description = "Service bundle ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceBundleRequest request) {
        log.info("PUT /provisioning-services/bundles/{} code={}", id, request.getCode());
        ServiceBundle updated = provisioningTransactionService.updateBundle(
                id, UUID.fromString(request.getProvisioningServiceId()), request.getName(), request.getCode(),
                request.getFixedAmount(), request.getSubcategory(), request.getValidityDays(),
                request.getDescription(), request.getStatus());
        ServiceBundleResponse body = toBundleResponse(updated);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(value = "/provisioning/execute", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Execute provisioning", description = "Debit user wallet and credit FEE account for the selected service bundle. Uses userId (unique per wallet).")
    @ApiResponse(responseCode = "200", description = "Provisioning transaction completed", content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    public ResponseEntity<TransactionResponse> execute(@Valid @RequestBody ProvisioningExecuteRequest request) {
        log.info("POST /provisioning/execute userId={} bundleId={}", request.getUserId(), request.getBundleId());
        var result = provisioningTransactionService.executeProvisioning(
                request.getUserId(),
                UUID.fromString(request.getBundleId()),
                request.getProvisioningReference());
        TransactionResponse body = toTransactionResponse(result.getTransaction());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private ProvisioningServiceResponse toServiceResponse(ProvisioningService s) {
        return ProvisioningServiceResponse.builder()
                .id(s.getId().toString())
                .type(s.getType().name())
                .name(s.getName())
                .code(s.getCode())
                .subcategory(s.getSubcategory())
                .description(s.getDescription())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private ServiceBundleResponse toBundleResponse(ServiceBundle b) {
        return ServiceBundleResponse.builder()
                .id(b.getId().toString())
                .provisioningServiceId(b.getProvisioningServiceId().toString())
                .name(b.getName())
                .code(b.getCode())
                .amountMin(b.getAmountMin())
                .amountMax(b.getAmountMax())
                .fixedAmount(b.getFixedAmount())
                .subcategory(b.getSubcategory())
                .validityDays(b.getValidityDays())
                .description(b.getDescription())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .effectiveAmount(b.getEffectiveAmount())
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction t) {
        return TransactionResponse.builder()
                .transactionId(t.getTransactionId().value().toString())
                .transactionType(t.getTransactionType().name())
                .status(t.getStatus().name())
                .referenceId(t.getReferenceId())
                .createdAt(t.getCreatedAt())
                .serviceBundleId(t.getServiceBundleId() != null ? t.getServiceBundleId().toString() : null)
                .provisioningReference(t.getProvisioningReference())
                .build();
    }
}
