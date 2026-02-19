package com.wallet.ledger.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Provisioning service (e.g. DSTv, Airtel Prepaid, Netflix)")
public class ProvisioningServiceResponse {

    private String id;
    private String type;
    private String name;
    private String code;
    private String subcategory;
    private String description;
    private String status;
    private Instant createdAt;

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String id; private String type; private String name; private String code;
        private String subcategory; private String description; private String status; private Instant createdAt;
        public Builder id(String id) { this.id = id; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder subcategory(String subcategory) { this.subcategory = subcategory; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ProvisioningServiceResponse build() { return new ProvisioningServiceResponse(id, type, name, code, subcategory, description, status, createdAt); }
    }
}
