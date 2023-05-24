package jp.co.jun.edi.repository.extended;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.extended.ExtendedTOrderSupplierEntity;

/**
 *
 * ExtendedTOrderSupplierRepository.
 *
 */
@Repository
public interface ExtendedTOrderSupplierRepository extends JpaRepository<ExtendedTOrderSupplierEntity, BigInteger> {

    /**
     * 品番IDから発注メーカー情報+コード名称 を検索する.
     *
     * @param partNoId 品番ID
     * @param orderCategoryType 発注分類区分
     * @param pageable pageable
     * @return 拡張発注メーカー情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "    ,ms1.name as supplier_name"
            + "    ,mk1.name as supplier_factory_name"
            + " FROM t_order_supplier t"
            + "   LEFT JOIN m_sirmst ms1 "
            + "          ON ms1.mntflg != '3' "
            + "         AND ms1.deleted_at is null "
            + "         AND t.supplier_code = ms1.sire "
            + "   LEFT JOIN m_kojmst mk1 "
            + "          ON mk1.mntflg != '3' "
            + "         AND mk1.deleted_at is null "
            + "         AND t.supplier_code = mk1.sire "
            + "         AND t.supplier_factory_code = mk1.kojcd "
            + "         AND TRIM(t.supplier_factory_code) <> ''"
            + " WHERE t.part_no_id = :partNoId "
            + " AND t.order_category_type = :orderCategoryType "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Page<ExtendedTOrderSupplierEntity> findByPartNoId(
            @Param("partNoId") BigInteger partNoId,
            @Param("orderCategoryType") String orderCategoryType,
            Pageable pageable);

    /**
     * IDから発注メーカー情報+コード名称 を検索する.
     *
     * @param id 発注メーカー情報ID
     * @return 拡張発注メーカー情報を取得する
     */
    @Query(value = "SELECT t.* "
            + "    ,ms1.name as supplier_name"
            + "    ,mk1.name as supplier_factory_name"
            + " FROM t_order_supplier t"
            + "   LEFT JOIN m_sirmst ms1 "
            + "          ON ms1.mntflg != '3' "
            + "         AND ms1.deleted_at is null "
            + "         AND t.supplier_code = ms1.sire "
            + "   LEFT JOIN m_kojmst mk1 "
            + "          ON mk1.mntflg != '3' "
            + "         AND mk1.deleted_at is null "
            + "         AND t.supplier_code = mk1.sire "
            + "         AND t.supplier_factory_code = mk1.kojcd "
            + "         AND TRIM(t.supplier_factory_code) <> ''"
            + " WHERE t.id = :id "
            + " AND t.deleted_at is null ", nativeQuery = true)
    Optional<ExtendedTOrderSupplierEntity> findById(
            @Param("id") BigInteger id);

}
