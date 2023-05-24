package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MPropertyEntity;
import jp.co.jun.edi.type.PropertyCategoryType;

/**
 * プロパティマスタリポジトリ.
 */
public interface MPropertyRepository extends JpaRepository<MPropertyEntity, BigInteger>, JpaSpecificationExecutor<MPropertyEntity> {
    /**
     * プロパティマスタ検索.
     *
     * @param category カテゴリ
     * @return プロパティマスタ情報のリスト
     */
    @Query("SELECT t FROM MPropertyEntity t"
            + " WHERE t.category = :category"
            + " AND t.deletedAt IS NULL")
    List<MPropertyEntity> findByCategory(@Param("category") PropertyCategoryType category);
}
