package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MMailTemplateEntity;
import jp.co.jun.edi.type.MMailCodeType;

/**
 * MMailTemplateRepository.
 */
public interface MMailTemplateRepository extends JpaRepository<MMailTemplateEntity, BigInteger> {

    /**
     * メール分類(mail_code)から メールテンプレート を検索する.
     * @param mailCode メール分類
     * @return メールテンプレートエンティティ
     */
    @Query("SELECT t FROM MMailTemplateEntity t"
            + " WHERE t.mailCode = :mail_code"
            + " AND t.deletedAt IS NULL")
    Optional<MMailTemplateEntity> findByMailCode(
            @Param("mail_code") MMailCodeType mailCode);
}
