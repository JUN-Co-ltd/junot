package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TShopStockEntity;

/**
 * TShopStockEntityRepository.
 */
@Repository
public interface TShopStockRepository extends JpaRepository<TShopStockEntity, BigInteger>, JpaSpecificationExecutor<TShopStockEntity> {

    /**
     * @param shopCode 店舗コード
     * @return 店別在庫情報リスト
     */
    @Query("SELECT t FROM TShopStockEntity t"
            + " WHERE t.shopCode = :shopCode"
            + " AND t.deletedAt is null")
    Optional<List<TShopStockEntity>> findByShopCode(
            @Param("shopCode") String shopCode);

    // PRD_0031 add SIT start
    /**
     * @param partNo 品番
     * @param shpcds 店舗コードリスト
     * @return 店別在庫情報リスト
     */
    @Query("SELECT t FROM TShopStockEntity t"
            + " WHERE t.shopCode IN (:shpcds)"
            + " AND t.partNo = :partNo"
            + " AND t.stockLot is not null"
            + " AND t.deletedAt is null")
    List<TShopStockEntity> findByPartNo(
            @Param("partNo") String partNo,
            @Param("shpcds") List<String> shpcds);
    // PRD_0031 add SIT end
}
