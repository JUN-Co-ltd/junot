package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MUserEntity;

/**
 * ユーザマスタのRepository.
 */
public interface MUserRepository extends JpaRepository<MUserEntity, BigInteger> {

    /**
     * 会社コードで検索して取得件数を返す.
     * @param company 会社名
     * @return 件数
     */
    @Query("SELECT COUNT(*) FROM MUserEntity t"
            + " WHERE t.company = :company"
            + " AND t.enabled = true"
            + " AND t.systemManaged = false"
            + " AND t.deletedAt IS NULL")
    Integer countByCompany(@Param("company") String company);

    /**
     * idで論理削除されておらず、システム管理が無効のユーザエンティティを取得する.
     *
     * @param id id
     * @return ユーザエンティティ
     */
    @Query("SELECT t FROM MUserEntity t"
            + " WHERE t.id = :id"
            + " AND t.systemManaged = false"
            + " AND t.deletedAt IS NULL")
    Optional<MUserEntity> findByIdAndSystemManagedFalseDeletedAtIsNull(
            @Param("id") BigInteger id);

    /**
     * アカウント名と会社名で取得する.
     * @param accountName アカウント名
     * @param company 会社名
     * @return ユーザエンティティ
     */
    @Query("SELECT t FROM MUserEntity t"
            + " WHERE t.accountName = :accountName"
            + " AND t.company = :company"
            + " AND t.enabled = true"
            + " AND t.systemManaged = false"
            + " AND t.deletedAt IS NULL")
    Optional<MUserEntity> findByAccountNameAndCompany(
            @Param("accountName") String accountName,
            @Param("company") String company);
    /**
     * アカウント名と会社名で取得する.
     * @param accountName アカウント名
     * @param company 会社名
     * @return ユーザエンティティ
     */
    @Query("SELECT t FROM MUserEntity t"
            + " WHERE t.accountName = :accountName"
            + " AND t.company = :company"
            + " AND t.enabled = true"
            + " AND t.deletedAt IS NULL")
    Optional<MUserEntity> findByAccountNameAndCompanyIgnoreSystemManaged(
            @Param("accountName") String accountName,
            @Param("company") String company);

    /**
     * 製造担当コード、企画担当コード、会社コード
     * で メールアドレスのリスト を取得する.
     *
     * @param staffCodes 製造担当コード、企画担当コード、パタンナーコードのリスト
     * @param company 会社コード
     * @return メールアドレスリスト
     */
    @Query(value = "SELECT DISTINCT m.mail_address "
            + " FROM m_user m"
            + " WHERE m.account_name IN ( :staffCodes )"
            + " AND m.company = :company "
            + " AND m.enabled = true"
            + " AND m.system_managed = false"
            + " AND m.deleted_at is null ", nativeQuery = true)
    List<String> findMailAddressByAccountNameAndCompany(
            @Param("staffCodes") Set<String> staffCodes,
            @Param("company") String company);

    /**
     * 生産メーカー担当ID(ユーザーID)で メールアドレス を取得する.
     *
     * @param mdfMakerStaffId 生産メーカー担当ID
     * @return メールアドレスリスト
     */
    @Query(value = "SELECT m.mail_address "
            + " FROM m_user m"
            + " WHERE m.id = :mdfMakerStaffId"
            + " AND m.enabled = true"
            + " AND m.system_managed = false"
            + " AND m.deleted_at is null ", nativeQuery = true)
    String findMailAddressById(
            @Param("mdfMakerStaffId") BigInteger mdfMakerStaffId);

    /**
     * idで論理削除されておらず、有効なユーザエンティティを取得する.
     *
     * @param id id
     * @return ユーザエンティティ
     */
    @Query("SELECT t FROM MUserEntity t"
            + " WHERE t.id = :id"
            + " AND t.enabled = true"
            + " AND t.deletedAt IS NULL")
    Optional<MUserEntity> findByIdAndEnabledTrueAndDeletedAtIsNull(
            @Param("id") BigInteger id);

    /**
     * 所属会社に紐付くすべてのアカウントのメールアドレスを取得する.
     *
     * @param company メーカーコード
     * @return メールアドレスリスト
     */
    @Query(value = "SELECT m.mail_address "
            + " FROM m_user m"
            + " WHERE m.company = :company"
            + " AND m.enabled = true"
            + " AND m.system_managed = false"
            + " AND m.deleted_at IS NULL ", nativeQuery = true)
    List<String> findMailAddressByCompany(
            @Param("company") String company);
}
