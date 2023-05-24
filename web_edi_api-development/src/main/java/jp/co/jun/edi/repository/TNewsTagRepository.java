package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TNewsTagEntity;

/**
 *
 * TNewsTagRepository.
 *
 */
@Repository
public interface TNewsTagRepository
                extends JpaRepository<TNewsTagEntity, BigInteger>, JpaSpecificationExecutor<TNewsTagEntity>  {
    /**
     * お知らせIDから お知らせタグ情報 を取得する.
     *
     * @param newsId お知らせID
     * @param pageable pageable
     * @return お知らせタグ情報を取得する
     */
    @Query("SELECT t FROM TNewsTagEntity t" + " WHERE t.newsId = :newsId AND t.deletedAt is null")
    Page<TNewsTagEntity> findByNewsId(@Param("newsId") BigInteger newsId, Pageable pageable);

    /**
     * お知らせIDからお知らせタグ情報を論理削除する.
     *
     * @param newsId お知らせID
     * @param deletedAt 削除日時
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query(value = "UPDATE t_news_tag t"
            + " SET t.deleted_at = :deletedAt,"
            + " t.updated_user_id = :updatedUserId"
            + " WHERE t.news_id = :newsId"
            + " AND t.deleted_at IS NULL", nativeQuery = true)
    int updateDelete(
            @Param("newsId") BigInteger newsId,
            @Param("deletedAt") Date deletedAt,
            @Param("updatedUserId") BigInteger updatedUserId);
}
