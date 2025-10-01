package com.fi.taxscan.mappers;

import com.fi.taxscan.entity.PropertyTaxRecord;
import org.springframework.stereotype.Component;

@Component
public class DefaultTaxRecordMapper implements TaxRecordMapper {
    @Override
    public void mergeRecords(PropertyTaxRecord original, PropertyTaxRecord fetched) {
        original.setAddress(fetched.getAddress() != null ? fetched.getAddress() : original.getAddress());
        original.setPropertySiteAddress(fetched.getPropertySiteAddress() != null ? fetched.getPropertySiteAddress() : original.getPropertySiteAddress());
        original.setLegalDescription(fetched.getLegalDescription() != null ? fetched.getLegalDescription() : original.getLegalDescription());
        original.setTaxLevy2024(fetched.getTaxLevy2024() != null ? fetched.getTaxLevy2024() : original.getTaxLevy2024());
        original.setAmountDue2024(fetched.getAmountDue2024() != null ? fetched.getAmountDue2024() : original.getAmountDue2024());
        original.setHalfPaymentOption(fetched.getHalfPaymentOption() != null ? fetched.getHalfPaymentOption() : original.getHalfPaymentOption());
        original.setPriorYearsDue(fetched.getPriorYearsDue() != null ? fetched.getPriorYearsDue() : original.getPriorYearsDue());
        original.setTotalAmountDue(fetched.getTotalAmountDue() != null ? fetched.getTotalAmountDue() : original.getTotalAmountDue());
        original.setLastPaymentAmount(fetched.getLastPaymentAmount() != null ? fetched.getLastPaymentAmount() : original.getLastPaymentAmount());
        original.setLastPayer(fetched.getLastPayer() != null ? fetched.getLastPayer() : original.getLastPayer());
        original.setLastPaymentDate(fetched.getLastPaymentDate() != null ? fetched.getLastPaymentDate() : original.getLastPaymentDate());
        original.setActiveLawsuits(fetched.getActiveLawsuits() != null ? fetched.getActiveLawsuits() : original.getActiveLawsuits());
        original.setActiveJudgments(fetched.getActiveJudgments() != null ? fetched.getActiveJudgments() : original.getActiveJudgments());
        original.setPendingPayments(fetched.getPendingPayments() != null ? fetched.getPendingPayments() : original.getPendingPayments());
        original.setTotalMarketValue(fetched.getTotalMarketValue() != null ? fetched.getTotalMarketValue() : original.getTotalMarketValue());
        original.setLandValue(fetched.getLandValue() != null ? fetched.getLandValue() : original.getLandValue());
        original.setImprovementValue(fetched.getImprovementValue() != null ? fetched.getImprovementValue() : original.getImprovementValue());
        original.setCappedValue(fetched.getCappedValue() != null ? fetched.getCappedValue() : original.getCappedValue());
        original.setAgriculturalValue(fetched.getAgriculturalValue() != null ? fetched.getAgriculturalValue() : original.getAgriculturalValue());
        original.setExemptions(fetched.getExemptions() != null ? fetched.getExemptions() : original.getExemptions());
        original.setJurisdictions(fetched.getJurisdictions() != null ? fetched.getJurisdictions() : original.getJurisdictions());
        original.setDelinquentAfter(fetched.getDelinquentAfter() != null ? fetched.getDelinquentAfter() : original.getDelinquentAfter());
    }
}