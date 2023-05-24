package jp.co.jun.edi.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.co.jun.edi.type.FukukitaruMasterOrderType;
import lombok.Data;

/**
 * フクキタル関連の画面構成情報のModel.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScreenSettingFukukiatruModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 品番情報. */
    private ItemModel item;

    /** 発注情報. */
    private OrderModel order;

    /** フクキタル品番情報. */
    private FukukitaruItemModel fkItem;

    /** 納品宛先リスト. */
    private List<FukukitaruDestinationModel> listDeriveryAddress;

    /** 発注宛先リスト. */
    private List<FukukitaruDestinationModel> listSupplierAddress;

    /** 請求宛先リスト. */
    private List<FukukitaruDestinationModel> listBillingAddress;

    /** テープ巾リスト. */
    private List<FukukitaruMasterModel> listTapeWidth;

    /** テープ種類リスト. */
    private List<FukukitaruMasterModel> listTapeType;

    /** 洗濯ネーム付記用語リスト. */
    private List<FukukitaruMaterialAppendicesTermModel> listWashNameAppendicesTerm;

    /** アテンションタグ付記用語リスト. */
    private List<FukukitaruMaterialAppendicesTermModel> listAttentionTagAppendicesTerm;

    /** アテンションシールのシール種類リスト. */
    private List<FukukitaruMasterModel> listAttentionSealType;

    /** リサイクルマークリスト. */
    private List<FukukitaruMasterModel> listRecycle;

    /** 中国内版情報製品分類リスト. */
    private List<FukukitaruMasterModel> listCnProductCategory;

    /** 中国内版情報製品種別リスト. */
    private List<FukukitaruMasterModel> listCnProductType;

    /** アテンションタグリスト. */
    private List<FukukitaruMaterialAttentionTagModel> listAttentionTag;

    /** アテンションネームリスト. */
    private List<FukukitaruMaterialAttentionNameModel> listAttentionName;

    /** アテンション下札リスト. */
    private List<FukukitaruMasterModel> listAttentionBottomBill;

    /** 同封副資材リスト. */
    private List<FukukitaruMasterModel> listAuxiliaryMaterial;

    /** 下札類リスト. */
    private List<FukukitaruMasterModel> listBottomBill;

    /** 洗濯マークリスト. */
    private List<FukukitaruMasterModel> listWashPattern;

    /** 洗濯ネームリスト. */
    private List<FukukitaruMasterModel> listWashName;

    /** SKU. */
    private List<ScreenSettingFukukitaruSkuModel> listScreenSku;

    /** フクキタル用ダウンロードファイルリスト. */
    private List<MaterialFileInfoModel> listMaterialFile;

    /** 発注種別. */
    private FukukitaruMasterOrderType orderType;

    /** カテゴリコードリスト. */
    private List<FukukitaruMasterModel> listCategoryCode;

    /** Nergyメリット下札. */
    private List<FukukitaruMasterModel> listHangTagNergyMerit;

    /** 入力補助セット. */
    private List<FukukitaruInputAssistSetModel> listInputAssistSet;

    /**
     * サスティナブルマーク印字表示フラグ.
     * true：表示、false：非表示
     * */
    private boolean sustainableMarkDisplayFlg;

}
