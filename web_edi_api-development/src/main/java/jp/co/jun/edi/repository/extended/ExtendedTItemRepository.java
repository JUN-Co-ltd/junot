package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;

/**
 *
 * ExtendedTItemRepository.
 *
 */
@Repository
public interface ExtendedTItemRepository extends JpaRepository<ExtendedTItemEntity, BigInteger> {

    /**
     * 品番IDから 品番情報+コード名称 を検索する.
     *
     * @param id 品番ID
     * @return 拡張品番情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "   ,ms1.name as matl_maker_name"
            + "   ,ms2.name as mdf_maker_name"
            + "   ,mc1.item2 as planner_name"
            + "   ,mc2.item2 as mdf_staff_name"
            + "   ,mc3.item2 as pataner_name"
            + "   ,mc4.item1 as coo_name"
            + "   ,mc5.item1 as outlet_name"
            + " FROM t_item t"
            + "   LEFT OUTER JOIN t_order_supplier os_matl "
            + "          ON t.current_matl_order_supplier_id = os_matl.id"
            + "         AND os_matl.deleted_at IS NULL "
            + "   LEFT JOIN m_sirmst ms1 "
         //   + "          ON ms1.sirkbn = '30' "
            + "          ON ms1.mntflg != '3' "
            + "         AND ms1.deleted_at is null "
            + "         AND os_matl.supplier_code = ms1.sire "
            + "   LEFT OUTER JOIN t_order_supplier os_product "
            + "          ON t.current_matl_order_supplier_id = os_product.id"
            + "         AND os_product.deleted_at IS NULL "
            + "   LEFT JOIN m_sirmst ms2 "
         //   + "          ON ms2.sirkbn = '10' "
            + "          ON ms2.mntflg != '3' "
            + "         AND ms2.deleted_at is null "
            + "         AND os_product.supplier_code = ms2.sire "
            + "   LEFT JOIN m_codmst mc1 "
            + "          ON mc1.tblid = '22' "
            + "         AND mc1.mntflg != '3' "
            + "         AND mc1.deleted_at is null "
            + "         AND t.planner_code = mc1.code1 "
            + "   LEFT JOIN m_codmst mc2 "
            + "          ON mc2.tblid = '22' "
            + "         AND mc2.mntflg != '3' "
            + "         AND mc2.deleted_at is null "
            + "         AND t.mdf_staff_code = mc2.code1 "
            + "   LEFT JOIN m_codmst mc3 "
            + "          ON mc3.tblid = '22' "
            + "         AND mc3.mntflg != '3' "
            + "         AND mc3.deleted_at is null "
            + "         AND t.pataner_code = mc3.code1 "
            + "   LEFT JOIN m_codmst mc4 "
            + "          ON mc4.tblid = '05' "
            + "         AND mc4.mntflg != '3' "
            + "         AND mc4.deleted_at is null "
            + "         AND t.coo_code = mc4.code1 "
            + "   LEFT JOIN m_codmst mc5 "
            + "          ON mc5.tblid = '83' "
            + "         AND mc5.mntflg != '3' "
            + "         AND mc5.deleted_at is null "
            + "         AND t.outlet_code = mc5.code1 "
            + " WHERE t.id = :id "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Optional<ExtendedTItemEntity> findById(
            @Param("id") BigInteger id);
}
