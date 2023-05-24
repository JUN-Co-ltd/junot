package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TagdatEntity;

/**
 * TagdatRepository.
 */
@Repository
public interface TagdatRepository extends JpaRepository<TagdatEntity, BigInteger> {

	/**
     * 送信ステータスが未送信のTAGDAT情報を取得する.
     * @return TAGDAT情報
     */
    @Query("SELECT t FROM TagdatEntity t"
            + " WHERE t.sendStatus = 0"
            + " AND t.deletedAt is null")
    Page<TagdatEntity> findBySendStatus(Pageable pageable);

    /**
     * 送信ステータスが未送信のTAGDAT情報件数を取得する.
     * @return 取得件数
     * @param brkg ブランドコード
     */
    @Query("SELECT COUNT(*) FROM TagdatEntity t"
            + " WHERE t.brkg = :brkg"
            + " AND t.sendStatus = 0"
            + " AND t.deletedAt is null")
    int countByBrand(
    		@Param("brkg") String brkg);

    /**
     * TAGDAT未作成の品番情報を取得する.
     * @return idリスト
     */
    @Query("SELECT t.id FROM TagdatEntity t"
            + " WHERE t.sendStatus = 0"
            + " AND t.deletedAt is null")
    List<BigInteger> findId();

    /**
     * TAGDAT送信ステータスを更新する.
     * @param id TAGDATID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TagdatEntity t"
            + " SET t.sendStatus = :sendStatus"
            + "   , t.updatedUserId = :updatedUserId"
            + "   , t.updatedAt = now()"
            + " WHERE t.brkg = :brkg "
            + " AND t.sendStatus = 0"
            + " AND t.deletedAt is null")
    int updateSendStatus(
    		@Param("sendStatus") int sendStatus,
            @Param("brkg") String brkg,
            @Param("updatedUserId") BigInteger updatedUserId);
}
