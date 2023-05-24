package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFAppendicesTermEntity;

/**
 * フクキタル用付記用語マスタを検索するリポジトリ.
 */
public interface MFAppendicesTermRepository extends JpaRepository<MFAppendicesTermEntity, BigInteger>, JpaSpecificationExecutor<MFAppendicesTermEntity> {
    /**
     * フクキタル用付記用語情報検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFAppendicesTermEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFAppendicesTermEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用付記用語情報検索.
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFAppendicesTermEntity t"
            + " WHERE t.deletedAt IS NULL")
    Page<MFAppendicesTermEntity> findByDeletedAtIsNull(
            Pageable pageable);
}
