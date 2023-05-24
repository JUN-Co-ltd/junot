package jp.co.jun.edi.repository.specification;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MFDestinationEntity;

/**
 * ブランドコード別宛先検索条件設定クラス.
 */
@Component
public class MFDestinationSpecification {


    /**
     * 会社名の絞り込みを行う。(部分一致).
     * @param companyName 会社名
     * @return Specification<MFDestinationEntity>
     */
    public Specification<MFDestinationEntity> companyNameLikeContains(final String companyName) {
        if (StringUtils.isEmpty(companyName)) {
            return null;
        }
        return (root, query, cb) -> {
            return cb.like(root.get("companyName"), "%" + companyName + "%");
        };
    }

    /**
     * 宛先IDでの絞り込みを行う。(完全一致).
     * @param id 宛先ID
     * @return Specification<MFDestinationEntity>
     */
    public Specification<MFDestinationEntity> idInContains(final List<BigInteger> id) {
        if (Objects.isNull(id) || id.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            return root.get("id").in(id);
        };
    }

    /**
     * 削除日時がNULLでの絞り込みを行う。.
     * @return Specification<MFDestinationEntity>
     */
    public Specification<MFDestinationEntity> notDeleteContains() {
        return (root, query, cb) -> {
            return cb.isNull(root.get("deletedAt"));
        };
    }
}
