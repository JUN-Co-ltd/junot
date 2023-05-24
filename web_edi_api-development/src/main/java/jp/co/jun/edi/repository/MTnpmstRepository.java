package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MTnpmstEntity;

/**
 * 発注生産システムの店舗マスタのリポジトリ.
 */
@Repository
public interface MTnpmstRepository extends JpaRepository<MTnpmstEntity, BigInteger>, JpaSpecificationExecutor<MTnpmstEntity> {
    /**
     * 直送用店舗コードを取得.
     * 本社在庫とするため、本社の店舗コードを取得.
     * @return 店舗コード
     */
    @Query(value = "SELECT m.shpcd FROM m_tnpmst m"
            + " WHERE m.shpcd LIKE '5000%'"
            + " AND m.warekind = 1 "
            + " AND m.shopkind = 3 "
            + " AND m.deleted_at IS NULL", nativeQuery = true)
    String findShopCodeForDirectDelivery();
}
