package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TNewsEntity;

/**
 *
 * TNewsRepository.
 *
 */
@Repository
public interface TNewsRepository
                extends JpaRepository<TNewsEntity, BigInteger>, JpaSpecificationExecutor<TNewsEntity>  {
    /**
     * idで論理削除されていないお知らせ情報を取得する.
     * @param id id
     * @return お知らせ情報
     */
    Optional<TNewsEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

    /**
     * お知らせ情報を取得する.
     *
     * @param pageable ページ情報
     * @return コードマスタのリスト
     */
    @Query("SELECT t FROM TNewsEntity t"
            + " WHERE t.deletedAt IS NULL"
            + " ORDER BY t.openStartAt DESC, t.id DESC")
    Page<TNewsEntity> findByDeletedAtIsNullOrderByOpenStartAtDesc(
            Pageable pageable);
}
