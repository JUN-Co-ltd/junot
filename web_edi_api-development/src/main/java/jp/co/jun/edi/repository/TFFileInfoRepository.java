package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TFFileInfoEntity;

/**
 *
 * TFFileInfoRepository.
 *
 */
@Repository
public interface TFFileInfoRepository extends JpaRepository<TFFileInfoEntity, BigInteger> {

    /**
     * ブランドコードから、フクキタル用ファイル情報を取得する.
     * @param brandCode ブランドコード
     * @param pageable ページ情報
     * @return 発注ファイル情報
     */
    @Query("SELECT t FROM TFFileInfoEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode")
    Page<TFFileInfoEntity> findByBrandCode(
           @Param("brandCode") String brandCode, Pageable pageable);

}
