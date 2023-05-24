package jp.co.jun.edi.repository.specification;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * コードマスタ　組成動的検索条件設定クラス.
 */
@Component
public class JunpcCodmstCompositionSpecification {

    /**
     * 削除日時がNULLでない.
     *
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> notDeleteContains() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    /**
     * メンテ区分が1,2,空白のいずれか.
     *
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> mntflgContains() {
        final List<String> activeMntflgList = Arrays.asList(new String[]{"1", "2", ""});
        return (root, query, cb) -> root.get("mntflg").in(activeMntflgList);
    }

    /**
     * テーブルID条件を設定.
     *
     * @param tblIdType MCodmstTblIdType
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> tblidContains(final MCodmstTblIdType tblIdType) {
        return (root, query, cb) -> cb.equal(root.get("tblid"), tblIdType.getValue());
    }

    /**
     * 組成コードの絞り込み検索条件(IN検索).
     *
     * @param compositionCodeList 組成コード
     * @return Specification<MCodmstEntity>
     */
    public Specification<MCodmstEntity> compositionCodeInContains(final List<String> compositionCodeList) {
        if (CollectionUtils.isEmpty(compositionCodeList)) {
            return null;
        }

        return (root, query, cb) -> root.get("code1").in(compositionCodeList);
    }
}
