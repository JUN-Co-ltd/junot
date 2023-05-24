package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFTapeWidthEntity;

/**
 * フクキタル用テープ巾マスタを検索するリポジトリ.
 */
public interface MFTapeWidthRepository extends JpaRepository<MFTapeWidthEntity, BigInteger>, JpaSpecificationExecutor<MFTapeWidthEntity>  {
    /**
     * フクキタル用テープ巾マスタ検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFTapeWidthEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFTapeWidthEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用テープ巾マスタ検索.
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFTapeWidthEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFTapeWidthEntity> findByAll(
            Pageable pageable);
}
