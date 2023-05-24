package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TItemEntity;

/**
 * TItemRepository.
 */
@Repository
public interface TItemRepository extends JpaRepository<TItemEntity, BigInteger> {

    /**
     * @param partNo 品番
     * @return 品番情報
     */
    @Query("SELECT t FROM TItemEntity t"
            + " WHERE t.partNo = :part_no"
            + " AND t.deletedAt is null")
    Optional<TItemEntity> findByPartNo(
            @Param("part_no") String partNo);

    /**
     * 品番が存在するか確認する.
     * TODO ※年度は条件から外す。過去年度の品番を参照する際は、別途条件を考慮する。
     *
     * @param partNo 品番
     * @return 存在有無
     */
    @Query("SELECT COUNT(t.id) > 0 FROM TItemEntity t"
            + " WHERE t.partNo = :part_no"
            + " AND t.deletedAt is null")
    boolean existsByPartNo(
            @Param("part_no") String partNo);

    /**
     * idで論理削除されていない品番情報を取得する.
     * @param id id
     * @return 品番情報
     */
    Optional<TItemEntity> findByIdAndDeletedAtIsNull(@Param("id") BigInteger id);

    // PRD_0142 #10423 JFE add start
    /**
     * TAGDAT未作成の品番情報を取得する.
     * @return idリスト
     */
    @Query("SELECT t.id FROM TItemEntity t"
            + " WHERE t.tagdatCreatedFlg = '0'"
            + " AND t.deletedAt is null")
    List<BigInteger> findItem();

    /**
     * TAGDAT作成フラグとTAGDAT作成日を更新する.
     * @param id 品番ID
     * @param updatedUserId 更新ユーザID
     * @return 更新件数
     */
    @Modifying
    @Query("UPDATE TItemEntity t"
            + " SET t.tagdatCreatedFlg = '1'"
            + "   , t.tagdatCreatedAt = now()"
            + "   , t.updatedUserId = :updatedUserId"
            + "   , t.updatedAt = now()"
            + " WHERE t.id = :id ")
    int updateTagdatCreatedFlg(
            @Param("id") BigInteger id,
            @Param("updatedUserId") BigInteger updatedUserId);
    // PRD_0142 #10423 JFE add end
}
