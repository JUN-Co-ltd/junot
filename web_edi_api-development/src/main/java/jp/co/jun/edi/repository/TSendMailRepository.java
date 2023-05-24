package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TSendMailEntity;
import jp.co.jun.edi.type.SendMailType;

/**
 * 送信メール情報を検索するリポジトリ.
 */
@Repository
public interface TSendMailRepository extends JpaRepository<TSendMailEntity, BigInteger>, JpaSpecificationExecutor<TSendMailEntity> {
    /**
     * 送信状態からメール送信情報を取得.
     * @return メール送信情報 List<TSendMailEntity>
     */
    @Query(value = "SELECT t FROM TSendMailEntity t"
            + " WHERE t.sendStatus = 0"
            + " AND t.deletedAt IS NULL")
    List<TSendMailEntity> findBySendStatusUnsend();

    /**
     * 送信状態を更新する.
     * @return 更新件数
     * @param id 送信状態を変更するメールのID
     * @param sendStatus 変更後のメール送信状態
     */
    @Modifying
    @Query(value = "UPDATE TSendMailEntity t"
            + " SET t.sendStatus = :sendStatus"
            + " WHERE t.id = :id")
    int updateSendStatus(
            @Param("sendStatus") SendMailType sendStatus,
            @Param("id") BigInteger id);
}
