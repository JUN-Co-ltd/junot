package jp.co.jun.edi.repository.specification;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.TOrderEntity;

/**
 * 発注情報動的検索条件設定クラス.
 */
@Component
public class TOrderSpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<TOrderEntity>
     */
    public Specification<TOrderEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * 発注Idで発注情報テーブルの絞り込みを行う。(完全一致).
     * @param orderId 発注ID
     * @return Specification<TOrderEntity>
     */
    public Specification<TOrderEntity> orderIdContains(final BigInteger orderId) {
        if (Objects.isNull(orderId)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("id"), orderId);
        };
    }

    /**
     * 発注Noで発注情報テーブルの絞り込みを行う。(完全一致).
     * @param orderNumber 発注No
     * @return Specification<TOrderEntity>
     */
    public Specification<TOrderEntity> orderNumberContains(final BigInteger orderNumber) {
        if (Objects.isNull(orderNumber)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("orderNumber"), orderNumber);
        };
    }

    /**
     * 品番で発注情報テーブルの絞り込みを行う。(部分一致).
     * @param partNo 品番
     * @return Specification<TOrderEntity>
     */
    public Specification<TOrderEntity> partNoContains(final String partNo) {
        if (StringUtils.isEmpty(partNo)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.like(root.get("partNo"), "%" + partNo + "%");
        };
    }

 }
