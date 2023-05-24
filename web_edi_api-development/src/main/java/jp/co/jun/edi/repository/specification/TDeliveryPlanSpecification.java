package jp.co.jun.edi.repository.specification;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TDeliveryPlanEntity;

/**
 * 納品依頼情報動的検索条件設定クラス.
 */
@Component
public class TDeliveryPlanSpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryPlanEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * 発注Idで納品依頼情報テーブルの絞り込みを行う。(完全一致).
     * @param orderId 発注Id
     * @return Specification<TDeliveryEntity>
     */
    public Specification<TDeliveryPlanEntity> orderIdContains(final BigInteger orderId) {
        if (Objects.isNull(orderId)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("orderId"), orderId);
        };
    }
}
