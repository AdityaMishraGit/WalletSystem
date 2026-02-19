package com.wallet.ledger.domain.valueobject;

/**
 * Filter for listing service bundles by their fixed amount relative to a given amount.
 * Used by GET /provisioning-services/bundles?amount=&amp;amountFilter=
 */
public enum AmountFilter {

    /** Return bundles whose fixed_amount is greater than the given amount */
    GREATER_THAN,

    /** Return bundles whose fixed_amount is less than the given amount */
    LESS_THAN
}
