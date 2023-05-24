package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MScreenStructureEntity;
import jp.co.jun.edi.type.MCodmstTblIdType;

/**
 * 画面構成マスタを検索するリポジトリ.
 */
public interface MScreenStructureRepository extends JpaRepository<MScreenStructureEntity, BigInteger>, JpaSpecificationExecutor<MScreenStructureEntity> {
    /**
     * 画面構成マスタ情報検索.
     * @param pageable ページ情報
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MScreenStructureEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MScreenStructureEntity> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 画面構成マスタ情報検索.
     * @param tblid マスタコード
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MScreenStructureEntity t"
            + " WHERE t.deletedAt IS NULL"
            + " AND t.tableId = :tblid ")
    Optional<MScreenStructureEntity> findByTblidAndDeletedAtIsNull(@Param("tblid") MCodmstTblIdType tblid);

}
