package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MSireEntity;

/**
 * 仕入先マスタのRepository.
 */
@Repository
public interface MSireRepository extends JpaRepository<MSireEntity, BigInteger> {

    /**
     * 仕入先コードと工場コードをキーに取引先情報リストを検索する.
     * @param sireCode 仕入先コード
     * @param kojCode 工場コード
     * @return 取引先情報リスト
     */
    @Query(value = "SELECT"
            + "       k.id AS id"
            + "       , k.reckbn AS reckbn"
            + "       , k.sire AS sire_code"
            + "       , s.name AS sire_name"
            + "       , k.kojcd AS koj_code"
            + "       , k.name AS koj_name"
            + "       , k.sname AS skoj_name"
            + "       , s.dummy2 AS in_out"
            + "       , k.sirkbn AS sirkbn"
            + "       , CONCAT(s.sire, s.name) AS knkt_sire"
            + "       , k.yubin AS yubin"
            + "       , k.add1 AS add1"
            + "       , k.add2 AS add2"
            + "       , k.add3 AS add3"
            + "       , k.tel1 AS tel1"
            + "       , s.yugaikbn AS yugaikbn"
            + "       , s.yugaiymd AS yugaiymd"
            + "       , k.brand1 AS brand1"
            + "       , k.brand2 AS brand2"
            + "       , k.brand3 AS brand3"
            + "       , k.brand4 AS brand4"
            + "       , k.brand5 AS brand5"
            + "       , k.brand6 AS brand6"
            + "       , k.brand7 AS brand7"
            + "       , k.brand8 AS brand8"
            + "       , k.brand9 AS brand9"
            + "       , k.brand10 AS brand10"
            + "       , k.brand11 AS brand11"
            + "       , k.brand12 AS brand12"
            + "       , k.brand13 AS brand13"
            + "       , k.brand14 AS brand14"
            + "       , k.brand15 AS brand15"
            + "       , k.brand16 AS brand16"
            + "       , k.brand17 AS brand17"
            + "       , k.brand18 AS brand18"
            + "       , k.brand19 AS brand19"
            + "       , k.brand20 AS brand20"
            + "       , k.brand21 AS brand21"
            + "       , k.brand22 AS brand22"
            + "       , k.brand23 AS brand23"
            + "       , k.brand24 AS brand24"
            + "       , k.brand25 AS brand25"
            + "       , k.brand26 AS brand26"
            + "       , k.brand27 AS brand27"
            + "       , k.brand28 AS brand28"
            + "       , k.brand29 AS brand29"
            + "       , k.brand30 AS brand30"
            + "       , k.hkiji AS hkiji"
            + "       , k.hseihin AS hseihin"
            + "       , k.hnefuda AS hnefuda"
            + "       , k.hfuzoku AS hfuzoku"
            + "       , k.hsofkbn AS hsofkbn"
            + "       , TRIM(k.hemail1) AS hemail1"
            + "       , k.nsofkbn AS nsofkbn"
            + "       , TRIM(k.nemail1) AS nemail1"
            + "       , k.ysofkbn AS ysofkbn"
            + "       , TRIM(k.yemail1) AS yemail1"
            + "     FROM"
            + "       m_kojmst k"
            + "       LEFT OUTER JOIN m_sirmst s"
            + "         ON k.sire = s.sire"
            + "         AND s.deleted_at IS NULL"
            + "     WHERE"
            + "       k.sire = :sireCode"
            + "       AND k.kojcd = :kojCode"
            + "       AND k.deleted_at IS NULL", nativeQuery = true)
    Optional<MSireEntity> findBySireCodeAndKojCode(
            @Param("sireCode") String sireCode,
            @Param("kojCode") String kojCode);
}
