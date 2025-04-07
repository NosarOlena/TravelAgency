package com.epam.finaltask.specification;

import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.Voucher;
import com.epam.finaltask.model.VoucherStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class VoucherSpecifications {

    public static Specification<Voucher> hasId(UUID id) {
        return (root, query, builder) -> id == null ? null : builder.equal(root.get("id"), id);
    }

    public static Specification<Voucher> hasTitle(String title) {
        return (root, query, builder) ->
                title == null ? null
                        : builder.like(builder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Voucher> hasDescription(String description) {
        return (root, query, builder) ->
                description == null ? null
                        : builder.like(builder.lower(root.get("description")), "%" + description.toLowerCase() + "%");
    }

    public static Specification<Voucher> hasTourType(TourType tourType) {
        return (root, query, builder) -> tourType == null ? null : builder.equal(root.get("tourType"), tourType);
    }

    public static Specification<Voucher> hasTransferType(TransferType transferType) {
        return (root, query, builder) -> transferType == null ? null : builder.equal(root.get("transferType"), transferType);
    }

    public static Specification<Voucher> hasHotelType(HotelType hotelType) {
        return (root, query, builder) -> hotelType == null ? null : builder.equal(root.get("hotelType"), hotelType);
    }

    public static Specification<Voucher> hasStatus(VoucherStatus status) {
        return (root, query, builder) -> status == null ? null : builder.equal(root.get("status"), status);
    }

    public static Specification<Voucher> hasHotStatus(Boolean hotStatus) {
        return (root, query, builder) ->
                hotStatus == null ? null : builder.equal(root.get("isHot"), hotStatus);
    }

    public static Specification<Voucher> hasPriceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, builder) -> {
            if (min != null && max != null) {
                return builder.between(root.get("price"), min, max);
            } else if (min != null) {
                return builder.greaterThanOrEqualTo(root.get("price"), min);
            } else if (max != null) {
                return builder.lessThanOrEqualTo(root.get("price"), max);
            } else {
                return null;
            }
        };
    }

    public static Specification<Voucher> hasArrivalDateAfter(LocalDate date) {
        return (root, query, builder) -> date == null ? null : builder.greaterThanOrEqualTo(root.get("arrivalDate"), date);
    }

    public static Specification<Voucher> hasEvictionDateBefore(LocalDate date) {
        return (root, query, builder) -> date == null ? null : builder.lessThanOrEqualTo(root.get("evictionDate"), date);
    }

}
