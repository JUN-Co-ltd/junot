package jp.co.jun.edi.repository.specification;

import java.util.Date;
import java.util.Objects;

import javax.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.TNewsEntity;

/**
 * お知らせ情報動的検索条件設定クラス.
 */
@Component
public class TNewsSpecification {

    /**
     * 削除日時がNULLでない.
     * @return Specification<TNewsEntity>
     */
    public Specification<TNewsEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }

    /**
     * 公開範囲で納品情報テーブルの絞り込みを行う(基準日>=公開日)。.
     * @param referenceDate 基準日
     * @return Specification<TNewsEntity>
     */
    public Specification<TNewsEntity> periodGreaterThanOpenStartAt(final Date referenceDate) {
        if (Objects.isNull(referenceDate)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.lessThanOrEqualTo(root.get("openStartAt"), referenceDate);
        };
    }

    /**
     * 公開範囲で納品情報テーブルの絞り込みを行う(公開終了日がNULLor基準日<=公開終了日)。.
     * @param referenceDate 基準日
     * @return Specification<TNewsEntity>
     */
    public Specification<TNewsEntity> openEndAtContains(final Date referenceDate) {
        if (Objects.isNull(referenceDate)) {
            return null;
        }
        return (root, query, cb) -> {
            // 公開終了日がNULLのSQL文
            Predicate p1 = cb.isNull(root.get("openEndAt"));
            // 基準日<=公開終了日のSQL文
            Predicate p2 = cb.greaterThanOrEqualTo(root.get("openEndAt"), referenceDate);
            return cb.or(p1, p2);
        };
    }
}
