package jp.co.jun.edi.component.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.MItemPartsEntity;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import lombok.Data;

/**
 * 品番関連のマスタデータ用のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MItemModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ブランドコード（変更前）. */
    private String preBrandCode;

    /** ブランドコード. */
    private String brandCode;

    /** アイテムコード（変更前）. */
    private String preItemCode;

    /** アイテムコード. */
    private String itemCode;

    /** 生産メーカーコード（変更前）. */
    private String preMdfMakerCode;

    /** 生産メーカーコード. */
    private String mdfMakerCode;

    /** 生産工場コード（変更前）. */
    private String preMdfMakerFactoryCode;

    /** 生産工場コード. */
    private String mdfMakerFactoryCode;

    /** 企画担当コード（変更前）. */
    private String prePlannerCode;

    /** 企画担当コード. */
    private String plannerCode;

    /** 製造担当コード（変更前）. */
    private String preMdfStaffCode;

    /** 製造担当コード. */
    private String mdfStaffCode;

    /** パターンナーコード（変更前）. */
    private String prePatanerCode;

    /** パターンナーコード. */
    private String patanerCode;

    /** アイテム. */
    private MCodmstEntity item;

    /** 色リスト. */
    private List<MCodmstEntity> colors = Collections.emptyList();

    /** サイズリスト. */
    private List<MSizmstEntity> sizes = Collections.emptyList();

    /** パーツリスト. */
    private List<MItemPartsEntity> itemParts = Collections.emptyList();

    /** パーツマップ. */
    private Map<String, MItemPartsEntity> itemPartMap = Collections.emptyMap();

    /** 組成リスト. */
    private List<MCodmstEntity> compositions = Collections.emptyList();

    /** 組成リスト. */
    private Map<String, MCodmstEntity> compositionMap = Collections.emptyMap();

    /** メーカー - 生産メーカー. */
    private MSirmstEntity mdfMaker;

    /** メーカー - 生産工場. */
    private MKojmstEntity mdfMakerFactory;

    /** 原産国リスト. */
    private List<MCodmstEntity> originCountries = Collections.emptyList();

    /** 担当 - 企画担当. */
    private MCodmstEntity planner;

    /** 担当 - 製造担当. */
    private MCodmstEntity mdfStaff;

    /** 担当 - パターンナー. */
    private MCodmstEntity pataner;

    /** 丸井 - 丸井品番リスト. */
    private List<MCodmstEntity> maruiItems = Collections.emptyList();

    /** 丸井 - Voi区分リスト. */
    private List<MCodmstEntity> voiSections = Collections.emptyList();

    /** 統計情報 - 素材リスト. */
    private List<MCodmstEntity> materials = Collections.emptyList();

    /** 統計情報 - ゾーンリスト. */
    private List<MCodmstEntity> zones = Collections.emptyList();

    /** 統計情報 - サブブランドリスト. */
    private List<MCodmstEntity> subBrands = Collections.emptyList();

    /** 統計情報 - テイストリスト. */
    private List<MCodmstEntity> tastes = Collections.emptyList();

    /** 統計情報 - タイプ1リスト. */
    private List<MCodmstEntity> type1s = Collections.emptyList();

    /** 統計情報 - タイプ2リスト. */
    private List<MCodmstEntity> type2s = Collections.emptyList();

    /** 統計情報 - タイプ3リスト. */
    private List<MCodmstEntity> type3s = Collections.emptyList();

    /** 統計情報 - 展開リスト. */
    private List<MCodmstEntity> outlets = Collections.emptyList();

    /**
     * マスタデータの検索キーを設定する.
     *
     * @param item {@link ItemModel} instance
     */
    public void setMasterDataSearchKey(
            final ItemModel item) {
        // ブランドコードを設定
        setBrandCode(item.getBrandCode());

        // アイテムコードを設定
        setItemCode(item.getItemCode());

        final OrderSupplierModel orderSupplier;

        if (CollectionUtils.isNotEmpty(item.getOrderSuppliers())) {
            // IDが未設定の先頭の1レコードを取得
            orderSupplier = item.getOrderSuppliers().stream()
                    .filter(os -> Objects.isNull(os.getId())).findFirst().orElse(null);
        } else {
            orderSupplier = null;
        }

        if (Objects.nonNull(orderSupplier)) {
            // 生産メーカーコードを設定
            setMdfMakerCode(orderSupplier.getSupplierCode());

            // 生産工場コードを設定
            setMdfMakerFactoryCode(orderSupplier.getSupplierFactoryCode());
        } else {
            // 生産メーカーコードを設定
            setMdfMakerCode(null);

            // 生産工場コードを設定
            setMdfMakerFactoryCode(null);
        }

        // 企画担当コードを設定
        setPlannerCode(item.getPlannerCode());

        // 製造担当コードを設定
        setMdfStaffCode(item.getMdfStaffCode());

        // パターンナーコードを設定
        setPatanerCode(item.getPatanerCode());
    }
}
