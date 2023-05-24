package jp.co.jun.edi.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.MThresholdEntity;

/**
 * 閾値マスタ動的検索条件設定クラス.
 */
@Component
public class MThresholdSpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<MThresholdEntity>
     */
    public Specification<MThresholdEntity> notDeleteContains() {
        return (root, query, cb) -> {

            return cb.isNull(root.get("deletedAt"));

        };
    }

    /**
     * ブランドコードで閾値の絞り込みを行う。(完全一致).
     * @param brandCode ブランドコード
     * @return Specification<MThresholdEntity>
     */
    public Specification<MThresholdEntity> brandCodeContains(final String brandCode) {

        if (StringUtils.isEmpty(brandCode)) {
            return (root, query, cb) -> {
                // charは空文字が入る可能性があるので、Nullまたはブランク値を条件に追加
                return cb.or(cb.isNull(root.get("brandCode")), cb.equal(root.get("brandCode"), ""));
            };

        }
        return (root, query, cb) -> {
            return cb.equal(root.get("brandCode"), brandCode);
        };
    }

    /**
     * アイテムコードで閾値の絞り込みを行う。(完全一致).
     * @param itemCode アイテムコード
     * @return Specification<MThresholdEntity>
     */
    public Specification<MThresholdEntity> itemCodeContains(final String itemCode) {
        if (StringUtils.isEmpty(itemCode)) {
            return (root, query, cb) -> {
             // charは空文字が入る可能性があるので、Nullまたはブランク値を条件に追加
                return cb.or(cb.isNull(root.get("itemCode")), cb.equal(root.get("itemCode"), ""));
            };
        }
        return (root, query, cb) -> {
            return cb.equal(root.get("itemCode"), itemCode);
        };
    }
 }
