package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MItemPartsEntity;

/**
 * MItemPartsRepository.
 */
public interface MItemPartsRepository extends JpaRepository<MItemPartsEntity, BigInteger> {

    /**
     * アイテムコードから パーツ情報 を検索する.
     *
     * @param itemCode itemCode
     * @param pageable pageable
     * @return パーツ情報
     */
    @Query("SELECT t FROM MItemPartsEntity t WHERE t.itemCode = :itemCode AND t.deletedAt is null AND t.sortOrder <> -1")
    Page<MItemPartsEntity> findByPartsNotIncludeSortOrderMinus(
            @Param("itemCode") String itemCode, Pageable pageable);

    /**
     * アイテムコードから パーツ情報 を検索する.
     *
     * @param itemCode itemCode
     * @param pageable pageable
     * @return パーツ情報
     */
    @Query("SELECT t FROM MItemPartsEntity t WHERE t.itemCode = :itemCode AND t.deletedAt is null")
    Page<MItemPartsEntity> findByParts(
            @Param("itemCode") String itemCode, Pageable pageable);


    /**
     * @param idList idリスト
     * @return 取得件数
     */
    @Query("SELECT COUNT(*) FROM MItemPartsEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    int countByIdList(@Param("idList") List<BigInteger> idList);
}
