package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.AdrmstEntity;

/**
 * AdrmstRepository.
 */
@Repository
public interface AdrmstRepository extends JpaRepository<AdrmstEntity, BigInteger> {

    /**
     * 送信区分が「送信する」のアドレスマスタ情報を取得する.
	 * @param brkg ブランドコード
     * @return メールアドレスリスト
     */
    //PRD_0184 mod JFE start
    //@Query(value = "SELECT DISTINCT t.email FROM adrmst t"
    @Query(value = "SELECT DISTINCT t.email FROM m_adrmst t"
    //PRD_0184 mod JFE end
            + " WHERE t.brand01_60 = :brkg"
    		+ " AND t.ssnkbn = '1'"
            + " AND t.deleted_at is null", nativeQuery = true)
    List<String> findMailAddressByBrkg(
    		@Param("brkg") String brkg);

    /**
     * 送信区分が「送信する」のアドレスマスタ情報を取得する.
     * @return AdrmstEntity
     */
    //PRD_0184 mod JFE start
    //@Query(value = "SELECT * FROM adrmst t"
    @Query(value = "SELECT * FROM m_adrmst t"
    //PRD_0184 mod JFE end
            + " WHERE t.ssnkbn = '1'"
    		+ " AND t.deleted_at is null"
            + " ORDER BY t.email, t.brand01_60", nativeQuery = true)
    Page<AdrmstEntity> findInfo(Pageable pageable);
}
