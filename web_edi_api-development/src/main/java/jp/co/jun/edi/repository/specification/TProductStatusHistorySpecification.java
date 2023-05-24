package jp.co.jun.edi.repository.specification;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TProductionStatusHistoryEntity;

/**
 * 生産ステータス履歴情報動的検索条件設定クラス.
 */
@Component
public class TProductStatusHistorySpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<TProductionStatusHistoryEntity>
     */
    public Specification<TProductionStatusHistoryEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * 発注Idで生産ステータス履歴情報テーブルの絞り込みを行う。(完全一致).
     * @param orderId 発注ID
     * @return Specification<TProductionStatusHistoryEntity>
     */
    public Specification<TProductionStatusHistoryEntity> orderIdContains(final BigInteger orderId) {
        if (Objects.isNull(orderId)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("orderId"), orderId);
        };
    }

 }
