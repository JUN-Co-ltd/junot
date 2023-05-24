package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnLinkingCsvFileEntity;

/**
 * ExtendedTMakerReturnLinkingCsvFileEntityRepository.
 * メーカー返品指示バッチで使用する拡張仕入情報Repository.
 */
@Repository
public interface ExtendedTMakerReturnLinkingCsvFileRepository extends JpaRepository<ExtendedTMakerReturnLinkingCsvFileEntity, BigInteger> {

    /**
     * ※m_brand_shopは削除済でも取得します.
     * @param wmsLinkingFileId 倉庫連携ファイルID
     * @return メーカー返品指示バッチ用拡張メーカー返品情報リスト
     */
    @Query(value = "SELECT"
            + "        mr.* "
            //PRD_0109 #7811 mod JFE start
//            + "        , IFNULL(i.matl_cost, 0) + "              // 生地原価
//            + "          IFNULL(i.processing_cost, 0) + "        // 加工賃
//            + "          IFNULL(i.accessories_cost, 0) + "       // 附属品
//            + "          IFNULL(i.other_cost, 0) as unit_price " // その他原価、上記単価合計を単価とする
            + "        , IFNULL(o.unit_price,0)as unit_price"     // 返品時は、発注情報.単価とする
            //PRD_0109 #7811 mod JFE end
            + "        , o.non_conforming_product_unit_price"
            + "        , mb.proper_rate"
            // PRD_0089 add SIT start
            + "        , o.retail_price"
            + "        , o.mdf_maker_factory_code"
            // PRD_0089 add SIT end
            + "      FROM"
            + "        t_maker_return mr"
            + "        INNER JOIN t_order o "
            + "          ON o.id = mr.order_id"
            + "          AND o.deleted_at IS NULL "
            + "        INNER JOIN t_item i"
            + "          ON o.part_no_id = i.id "
            + "          AND i.deleted_at IS NULL "
            + "        INNER JOIN m_brand_shop mb "
            + "          ON mb.shop_cd = mr.shpcd"
            + "          AND mb.brand_cd = LEFT(mr.part_no, 2)"
            + "      WHERE"
            + "        mr.wms_linking_file_id = :wmsLinkingFileId "
            + "        AND mr.deleted_at IS NULL"
            + "      ORDER BY mr.sequence ASC ",
            nativeQuery = true)
    List<ExtendedTMakerReturnLinkingCsvFileEntity> findByWmsLinkingFileId(@Param("wmsLinkingFileId") BigInteger wmsLinkingFileId);
}
