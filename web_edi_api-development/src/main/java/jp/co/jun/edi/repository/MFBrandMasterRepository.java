package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MFBrandMasterEntity;
import jp.co.jun.edi.type.FukukitaruMasterDeliveryType;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import jp.co.jun.edi.type.FukukitaruMasterType;

/**
 * フクキタル用ブランド別マスタ情報を検索するリポジトリ.
 */
public interface MFBrandMasterRepository extends JpaRepository<MFBrandMasterEntity, BigInteger>, JpaSpecificationExecutor<MFBrandMasterEntity> {
    /**
     * ブランドコード、アイテムコード、デリバリ種別、発注種別、マスタ種別からブランド別マスタ情報を取得.
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @param deliveryType フクキタルデリバリ種別 {@link FukukitaruMasterDeliveryType}
     * @param orderType フクキタル発注種別 {@link FukukitaruMasterOrderType}
     * @param masterType フクキタルマスタ種別 {@link FukukitaruMasterType}
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFBrandMasterEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode"
            + " AND t.itemCode = :itemCode"
            + " AND t.deliveryType = :deliveryType"
            + " AND t.orderType = :orderType"
            + " AND t.masterType = :masterType")
    Optional<MFBrandMasterEntity> findByBrandAndItemCodeAndDeliveryAndOrderTypeAndMasterType(
            @Param("brandCode") String brandCode,
            @Param("itemCode") String itemCode,
            @Param("deliveryType") FukukitaruMasterDeliveryType deliveryType,
            @Param("orderType") FukukitaruMasterOrderType orderType,
            @Param("masterType") FukukitaruMasterType masterType);

    /**
     * ブランドコード、アイテムコード(NULL)、デリバリ種別、発注種別、マスタ種別からブランド別マスタ情報を取得.
     * @param brandCode ブランドコード
     * @param deliveryType フクキタルデリバリ種別 {@link FukukitaruMasterDeliveryType}
     * @param orderType フクキタル発注種別 {@link FukukitaruMasterOrderType}
     * @param masterType フクキタルマスタ種別 {@link FukukitaruMasterType}
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFBrandMasterEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode"
            + " AND t.itemCode is null"
            + " AND t.deliveryType = :deliveryType"
            + " AND t.orderType = :orderType"
            + " AND t.masterType = :masterType")
    Optional<MFBrandMasterEntity> findByBrandAndItemCodeNullAndDeliveryeAndOrderTypeAndMasterType(
            @Param("brandCode") String brandCode,
            @Param("deliveryType") FukukitaruMasterDeliveryType deliveryType,
            @Param("orderType") FukukitaruMasterOrderType orderType,
            @Param("masterType") FukukitaruMasterType masterType);

    /**
     * ブランドコード、アイテムコードからブランド別マスタ情報を取得.
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @param masterType フクキタルマスタ種別 {@link FukukitaruMasterType}
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFBrandMasterEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode"
            + " AND t.itemCode = :itemCode"
            + " AND t.masterType = :masterType")
    Optional<MFBrandMasterEntity> findByBrandAndItemCodeAndMasterType(
            @Param("brandCode") String brandCode,
            @Param("itemCode") String itemCode,
            @Param("masterType") FukukitaruMasterType masterType);

    /**
     * ブランドコード、アイテムコード(NULL)からブランド別マスタ情報を取得.
     * @param brandCode ブランドコード
     * @param masterType フクキタルマスタ種別 {@link FukukitaruMasterType}
     * @return フクキタル品番情報
     */
    @Query("SELECT t FROM MFBrandMasterEntity t"
            + " WHERE t.deletedAt is null"
            + " AND t.brandCode = :brandCode"
            + " AND t.itemCode is null"
            + " AND t.masterType = :masterType")
    Optional<MFBrandMasterEntity> findByBrandAndItemCodeNullAndMasterType(
            @Param("brandCode") String brandCode,
            @Param("masterType") FukukitaruMasterType masterType);

}
