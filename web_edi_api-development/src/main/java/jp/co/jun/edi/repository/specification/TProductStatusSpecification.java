package jp.co.jun.edi.repository.specification;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TProductionStatusEntity;

/**
 * 生産ステータス情報動的検索条件設定クラス.
 */
@Component
public class TProductStatusSpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<TOrderEntity>
     */
    public Specification<TProductionStatusEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * 発注Idで生産ステータス情報テーブルの絞り込みを行う。(完全一致).
     * @param orderId 発注ID
     * @return Specification<TOrderEntity>
     */
    public Specification<TProductionStatusEntity> orderIdContains(final BigInteger orderId) {
        if (Objects.isNull(orderId)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("orderId"), orderId);
        };
    }

 }
