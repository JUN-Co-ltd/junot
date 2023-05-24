package jp.co.jun.edi.repository.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import jp.co.jun.edi.entity.extended.ExtendedMTnpmstEntity;
import jp.co.jun.edi.model.DeliveryStoreInfoModel;
import jp.co.jun.edi.model.ScreenSettingDeliverySearchConditionModel;
import jp.co.jun.edi.repository.custom.ExtendedMTnpmstRepositoryCustom;
import jp.co.jun.edi.type.AllocationType;
import jp.co.jun.edi.type.BooleanType;

/**
 * ExtendedMTnpmstEntityRepository実装クラス.
 */
public class ExtendedMTnpmstRepositoryImpl implements ExtendedMTnpmstRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    private static final String JUMMST_SQL =
            "    FROM m_junmst AS mj"
            + " WHERE mj.brand = :brandCode"
            + "   AND mj.hka NOT IN ('19','29')"
            + "   AND mj.haikbn <> " + AllocationType.FREE.getValue()
            + "   AND mj.mntflg IN ('1','2','')"
            + "   AND mj.deleted_at IS NULL";

    /**
     * 配分順位マスタからブランドコードをキーに店舗コードの抽出し、
     * 店舗コードをキーに店舗マスタから取得する.
     * ただし配分順位マスタの配分課が19課・29課、配分区分がフリーの店舗は除く.
     * 配分順位マスタから店舗が削除されても納品得意先テーブルに登録済の店舗は店舗マスタから取得する.
     */
    @Override
    public Page<ExtendedMTnpmstEntity> findBySpec(final ScreenSettingDeliverySearchConditionModel searchCondition) {

        final List<String> deliveryStoreCodes = extractDeliveryStoreCodeList(searchCondition.getDeliveryStoreInfos());
        final StringBuilder sql = new StringBuilder();
        sql.append(" SELECT mt.*");
        sql.append("        ,(");
        sql.append("          SELECT hka");
        sql.append(JUMMST_SQL);
        sql.append("             AND shpcd = mt.shpcd");
        sql.append("         ) AS hka");
        sql.append("        ,(");
        sql.append("          SELECT hjun");
        sql.append(JUMMST_SQL);
        sql.append("             AND shpcd = mt.shpcd");
        sql.append("         ) AS hjun");
        sql.append("   FROM m_tnpmst AS mt");
        sql.append("  WHERE mt.shpcd IN");
        sql.append("      (");
        sql.append("       SELECT mj.shpcd");
        sql.append(JUMMST_SQL);
        sql.append("      )");
        if (!Objects.isNull(deliveryStoreCodes) && deliveryStoreCodes.size() > 0) {
            sql.append(" OR mt.shpcd IN (:deliveryStoreCodes)");
        }
        sql.append("    AND mt.deletedtype =" + BooleanType.FALSE.getValue());
        sql.append("    AND mt.deleted_at IS NULL");
        sql.append(" ORDER BY hka ASC");
        sql.append("          ,hjun ASC");

        final Query query = entityManager.createNativeQuery(sql.toString(), ExtendedMTnpmstEntity.class);
        query.setParameter("brandCode", searchCondition.getBrandCode());
        if (!Objects.isNull(deliveryStoreCodes) && deliveryStoreCodes.size() > 0) {
            query.setParameter("deliveryStoreCodes", deliveryStoreCodes);
        }

        @SuppressWarnings("unchecked")
        final List<ExtendedMTnpmstEntity> rslt = query.getResultList();

        final PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        return new PageImpl<>(rslt, pageRequest, rslt.size());
    }

    /**
     * 店舗情報リストから店舗コードリストを抽出する.
     * @param deliveryStoreInfos 店舗情報リスト
     * @return 店舗コードリスト
     */
    private List<String> extractDeliveryStoreCodeList(final List<DeliveryStoreInfoModel> deliveryStoreInfos) {
        if (deliveryStoreInfos == null) {
            return null;
        }

        return deliveryStoreInfos.stream()
                .map(store -> store.getStoreCode())
                .collect(Collectors.toList());
    }
}
