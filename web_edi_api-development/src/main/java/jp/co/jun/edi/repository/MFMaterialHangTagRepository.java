package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFMaterialHangTagEntity;
import jp.co.jun.edi.type.FukukitaruMasterMaterialType;

/**
 * フクキタル用資材情報下札マスタを検索するリポジトリ.
 */
public interface MFMaterialHangTagRepository
        extends JpaRepository<MFMaterialHangTagEntity, BigInteger>, JpaSpecificationExecutor<MFMaterialHangTagEntity> {
    /**
     * フクキタル用資材情報マスタ検索.
     *
     * @param idList IDリスト
     * @param pageable {@link Pageable} instance
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFMaterialHangTagEntity t"
            + " WHERE t.id IN :idList"
            + " AND t.deletedAt IS NULL")
    Page<MFMaterialHangTagEntity> findByIds(
            @Param("idList") List<BigInteger> idList, Pageable pageable);

    /**
     * フクキタル用資材情報マスタ検索.
     * @param materialType マテリアル種別
     * @param pageable ページ情報
     * @return マスタ情報のリスト
     */
    @Query("SELECT t FROM MFMaterialHangTagEntity t"
            + " WHERE t.deletedAt IS NULL"
            + " AND t.materialType = :materialType")
    Page<MFMaterialHangTagEntity> findByMaterialTypeAll(
            @Param("materialType") FukukitaruMasterMaterialType materialType,
            Pageable pageable);
}
