package jp.co.jun.edi.repository.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jp.co.jun.edi.entity.MTnpmstEntity;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.ShopKindType;

/**
 * 店舗マスタ動的検索条件設定クラス.
 */
@Component
public class JunpcTnpmstSpecification {

    /**
     * 削除済区分がFALSEであること.
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> notDeleteContains() {
        return (root, query, cb) -> cb.equal(root.get("deletedtype"), BooleanType.FALSE);
    }

    /**
     * 店舗コードの絞り込み検索条件(前方一致).
     *
     * @param shpcdAhead 店舗コード前方文字
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> shpcdAheadMatchContains(final String shpcdAhead) {
        if (StringUtils.isEmpty(shpcdAhead)) {
            return null;
        }
        return (root, query, cb) -> cb.like(root.get("shpcd"), shpcdAhead + "%");
    }

    /**
     * 店舗コードの絞り込み検索条件(完全一致).
     *
     * @param shpcd 店舗コード
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> shpcdMatchContains(final String shpcd) {
        if (StringUtils.isEmpty(shpcd)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("shpcd"), shpcd);
    }

    /**
     * 店舗名の絞り込み検索条件(部分一致).
     *
     * @param name 店舗名
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> nameMatchContains(final String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
    }

    /**
     * 電話番号の絞り込み検索条件(完全一致).
     *
     * @param telban 電話番号
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> telbanMatchContains(final String telban) {
        if (StringUtils.isEmpty(telban)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("telban"), telban);
    }

    /**
     * 組織コードの絞り込み検索条件(IN検索).
     *
     * @param brands ブランドコードリスト
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> groupcdInContains(final List<String> brands) {
        if (brands == null) {
            return null;
        }
        return (root, query, cb) -> root.get("groupcd").in(brands);
    }

    /**
     * 店舗区分の絞り込み検索条件(完全一致).
     *
     * @param shopkind 店舗区分
     * @return Specification<MTnpmstEntity>
     */
    public Specification<MTnpmstEntity> shopkindMatchContains(final ShopKindType shopkind) {
        if (StringUtils.isEmpty(shopkind)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("shopkind"), shopkind);
    }

}
