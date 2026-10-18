package com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingIncentive;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingIncentiveRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CalculateMonthlyMerchantIncentiveHandler extends Command<CalculateMonthlyMerchantIncentiveCommand, CalculateMonthlyMerchantIncentiveResult> {

    private final MerchantShippingIncentiveRepository incentiveRepository;
    private final ShipmentRepository shipmentRepository;

    public CalculateMonthlyMerchantIncentiveHandler(
            MerchantShippingIncentiveRepository incentiveRepository,
            ShipmentRepository shipmentRepository
    ) {
        this.incentiveRepository = incentiveRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public CalculateMonthlyMerchantIncentiveResult execute(CalculateMonthlyMerchantIncentiveCommand input) {
        List<Shipment> shipments = shipmentRepository.findDeliveredOrdersForMonth(
                input.organizationId(),
                input.startOfMonth(),
                input.endOfMonth()
        );

        CurrencyCode currency = CurrencyCode.NGN;
        Money totalBaseline = Money.zero(currency);
        Money totalActual = Money.zero(currency);
        int eligibleCount = 0;

        for (Shipment shipment : shipments) {
            if (shipment.isIncentiveEligible() && shipment.getStatus() == ShipmentStatus.DELIVERED) {
                currency = shipment.getCustomerShippingFee().currency();
                if (shipment.getBaselineSingleCost() != null) {
                    totalBaseline = totalBaseline.add(shipment.getBaselineSingleCost());
                }
                if (shipment.getAllocatedActualCost() != null) {
                    totalActual = totalActual.add(shipment.getAllocatedActualCost());
                } else if (shipment.getBaselineSingleCost() != null) {
                    totalActual = totalActual.add(shipment.getBaselineSingleCost());
                }
                eligibleCount++;
            }
        }

        Long incentiveId = incentiveRepository.nextIdentity();
        ZonedDateTime now = ZonedDateTime.now();

        MerchantShippingIncentive incentive = MerchantShippingIncentive.calculate(
                incentiveId,
                input.organizationId(),
                input.billingYearMonth(),
                eligibleCount,
                totalBaseline,
                totalActual,
                now
        );

        incentive.markCredited(now, input.correlationId());
        incentiveRepository.save(incentive);

        return new CalculateMonthlyMerchantIncentiveResult(
                incentive.getId(),
                incentive.getOrganizationId(),
                incentive.getBillingYearMonth(),
                incentive.getEligibleShipmentCount(),
                incentive.getRebateAmount(),
                incentive.getStatus().name()
        );
    }
}
