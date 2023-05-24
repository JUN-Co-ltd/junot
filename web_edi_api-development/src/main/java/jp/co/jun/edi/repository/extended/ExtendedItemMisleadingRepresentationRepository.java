package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedItemMisleadingRepresentationEntity;

/**
 *
 * ExtendedItemMisleadingRepresentationRepository.
 *
 */
@Repository
public interface ExtendedItemMisleadingRepresentationRepository extends JpaRepository<ExtendedItemMisleadingRepresentationEntity, BigInteger> {

    /**
     * 品番IDから 優良誤認用に品番情報+コード名称 を検索する.
     * メンテ区分＝"3"（削除）分も表示する.
     *
     * @param id 品番ID
     * @return 優良誤認用拡張品番情報を取得する
     */
    @Query(value = "SELECT"
            + " t.id"
            + ", t.part_no"
            + ", t.deployment_date"
            + ", t.deployment_week"
            + ", t.product_name"
            + ", t.year"
            + ", t.season_code"
            + ", os.supplier_code as mdf_maker_code"
            + ", ms1.name as mdf_maker_name"
            + ", ms1.yugaikbn as hazardous_substance_response_type"
            + ", ms1.yugaiymd as hazardous_substance_response_at"
            + ", t.coo_code"
            + ", mc4.item1 as coo_name"
            + ", t.retail_price"
            + ", t.processing_cost"
            + ", t.accessories_cost"
            + ", t.other_cost"
            + ", t.planner_code"
            + ", mc1.item2 as planner_name"
            + ", t.mdf_staff_code"
            + ", mc2.item2 as mdf_staff_name"
            + ", t.pataner_code"
            + ", mc3.item2 as pataner_name"
            + ", t.material_code"
            + ", mc5.item1 as material_name"
            + ", t.brand_code"
            + ", mc6.item1 as brand_name"
            + ", t.item_code"
            + ", mc9.item1 as item_name"
            + ", t.memo"
            + ", t.quality_composition_status"
            + ", t.quality_coo_status"
            + ", t.quality_harmful_status"
            + ", ("
            + "     SELECT o.order_number"
            + "       FROM t_order o"
            + "      WHERE o.part_no_id = t.id"
            + "        AND o.deleted_at IS NULL"
            + "   ORDER BY o.order_number DESC"
            + "      LIMIT 1"
            + "  ) as order_number"
            + ", ("
            + "     SELECT o.quantity"
            + "       FROM t_order o"
            + "      WHERE o.part_no_id = t.id"
            + "        AND o.deleted_at IS NULL"
            + "   ORDER BY o.order_number DESC"
            + "      LIMIT 1"
            + "  ) as quantity"
            + " FROM t_item t"
            + "   LEFT OUTER JOIN t_order_supplier os "
            + "          ON t.current_product_order_supplier_id = os.id "
            + "         AND os.deleted_at is null "
            + "   LEFT JOIN m_sirmst ms1 "
            + "          ON ms1.deleted_at is null "
            + "         AND os.supplier_code = ms1.sire "
            + "   LEFT JOIN m_codmst mc1 "
            + "          ON mc1.tblid = '22' "
            + "         AND mc1.deleted_at is null "
            + "         AND t.planner_code = mc1.code1 "
            + "   LEFT JOIN m_codmst mc2 "
            + "          ON mc2.tblid = '22' "
            + "         AND mc2.deleted_at is null "
            + "         AND t.mdf_staff_code = mc2.code1 "
            + "   LEFT JOIN m_codmst mc3 "
            + "          ON mc3.tblid = '22' "
            + "         AND mc3.deleted_at is null "
            + "         AND t.pataner_code = mc3.code1 "
            + "   LEFT JOIN m_codmst mc4 "
            + "          ON mc4.tblid = '05' "
            + "         AND mc4.deleted_at is null "
            + "         AND t.coo_code = mc4.code1 "
            + "   LEFT JOIN m_codmst mc5 "
            + "          ON mc5.tblid = 'B7'"
            + "         AND mc5.deleted_at is null "
            + "         AND t.material_code = mc5.code1 "
            + "   LEFT JOIN m_codmst mc6 "
            + "          ON mc6.tblid = '02' "
            + "         AND mc6.deleted_at is null "
            + "         AND t.brand_code = mc6.code1 "
            + "   LEFT JOIN m_codmst mc9 "
            + "          ON mc9.tblid = '03' "
            + "         AND mc9.deleted_at is null "
            + "         AND t.item_code = mc9.code2 "
            + "         AND t.brand_code = mc9.code1 "
            + " WHERE t.id = :id "
            + " AND t.regist_status = 1"
            + " AND t.deleted_at is null ", nativeQuery = true)
    Optional<ExtendedItemMisleadingRepresentationEntity> findById(
            @Param("id") BigInteger id);
}
