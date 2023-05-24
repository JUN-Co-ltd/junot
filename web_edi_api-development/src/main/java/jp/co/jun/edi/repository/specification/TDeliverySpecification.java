package jp.co.jun.edi.repository.specification;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.TDeliveryEntity;

/**
 * 納品情報動的検索条件設定クラス.
 */
@Component
public class TDeliverySpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * 発注Idで納品情報テーブルの絞り込みを行う。(完全一致).
     * @param orderId 発注Id
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryEntity> orderIdContains(final BigInteger orderId) {
        if (Objects.isNull(orderId)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("orderId"), orderId);
        };
    }

    /**
     * 発注Noで納品情報テーブルの絞り込みを行う。(完全一致).
     * @param orderNumber 発注No
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryEntity> orderNumberContains(final BigInteger orderNumber) {
        if (Objects.isNull(orderNumber)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("orderNumber"), orderNumber);
        };
    }


    /**
     * 納品Idで納品情報テーブルの絞り込みを行う。(完全一致).
     * @param deliveryId 納品Id
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryEntity> deliveryIdContains(final BigInteger deliveryId) {
        if (Objects.isNull(deliveryId)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("id"), deliveryId);
        };
    }

    /**
     * 品番で納品情報テーブルの絞り込みを行う。(部分一致).
     * @param partNo 品番
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryEntity> partNoContains(final String partNo) {
        if (StringUtils.isEmpty(partNo)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.like(root.get("partNo"), "%" + partNo + "%");
        };
    }

}
