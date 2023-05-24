package jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.groups.Default;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import jp.co.jun.edi.constants.RegexpConstants;
import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.RegistStatusType;
import jp.co.jun.edi.type.SubSeasonCodeType;
import jp.co.jun.edi.util.BooleanUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.StringUtils;
import jp.co.jun.edi.util.xlsx.XlsxSheetReader;
import jp.co.jun.edi.validation.constraints.CorrectDate;
import jp.co.jun.edi.validation.group.ConfirmValidationGroup;
import lombok.Data;

/**
 * 品番・商品一括登録用の品番・商品のシート情報.
 */
@Data
public class ItemSheet implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_CODE_PREFIX = "{jp.co.jun.edi.util.xlsx.bulkRegistItem.sheet.ItemSheet";

    /** ブランドコード開始位置. */
    private static final int BRAND_CODE_START_INDEX = 0;

    /** ブランドコード終了位置. */
    private static final int BRAND_CODE_END_INDEX = 2;

    /** アイテムコード開始位置. */
    private static final int ITEM_CODE_START_INDEX = 2;

    /** アイテムコード終了位置. */
    private static final int ITEM_CODE_END_INDEX = 3;

    /** シート名. */
    private static final String SHEET_NAME = "商品_品番取込用";

    /** 開始行番号. */
    private static final int START_ROW_INDEX = 5;

    /** 行番号. */
    private int rowIndex;

    /** 「品番」の列番号. */
    private static final int PART_NO_COLNUM_INDEX = 0;

    /** 「品番」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".partNo.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".partNo.Pattern}", regexp = RegexpConstants.PART_NO, groups = { Default.class })
    private String partNo;

    /** 「季番」の列番号. */
    private static final int SEASON_CODE_COLNUM_INDEX = 1;

    /** 「季番」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".seasonCode.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".seasonCode.Pattern}", regexp = RegexpConstants.SEASON_CODE, groups = { Default.class })
    private String seasonCode;

    /** 「年度」の列番号. */
    private static final int YEAR_COLNUM_INDEX = 2;

    /** 「年度」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".year.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".year.Pattern}", regexp = RegexpConstants.YAER, groups = { Default.class })
    private String year;

    /** 「品名」の列番号. */
    private static final int PRODUCT_NAME_COLNUM_INDEX = 3;

    /** 「品名」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".productName.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".productName.Pattern}", regexp = RegexpConstants.PRODUCT_NAME, groups = { Default.class })
    private String productName;

    /** 「半角(全角カナ/半角英数字)」（品名カナ）の列番号. */
    private static final int PRODUCT_NAME_KANA_COLNUM_INDEX = 4;

    /** 「半角(全角カナ/半角英数字)」（品名カナ）. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".productNameKana.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".productNameKana.Pattern}", regexp = RegexpConstants.PRODUCT_NAME_KANA, groups = { Default.class })
    private String productNameKana;

    /** 「仮発注日」の列番号. */
    private static final int PROVI_ORDER_DATE_COLNUM_INDEX = 5;

    /** 「仮発注日」. */
    @CorrectDate(message = MESSAGE_CODE_PREFIX + ".proviOrderDate.CorrectDate}", groups = { Default.class })
    private String proviOrderDate;

    /** 「納品日」の列番号. */
    private static final int PREFERRED_DELIVERY_DATE_COLNUM_INDEX = 6;

    /** 「納品日」.DB上は「希望納品日」のため注意すること. */
    @CorrectDate(message = MESSAGE_CODE_PREFIX + ".preferredDeliveryDate.CorrectDate}", groups = { Default.class })
    private String preferredDeliveryDate;

    /** 「投入日」の列番号. */
    private static final int DEPLOYMENT_DATE_COLNUM_INDEX = 7;

    /** 「投入日」. */
    @CorrectDate(message = MESSAGE_CODE_PREFIX + ".deploymentDate.CorrectDate}", groups = { Default.class })
    private String deploymentDate;

    /** 「投入週」の列番号. */
    private static final int DEPLOYMENT_WEEK_COLNUM_INDEX = 8;

    /** 「投入週」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".deploymentWeek.Pattern}", regexp = RegexpConstants.YAER_AND_WEEK, groups = { Default.class })
    private String deploymentWeek;

    /** 「P終了日」の列番号. */
    private static final int PEND_DATE_COLNUM_INDEX = 9;

    /** 「P終了日」. */
    @CorrectDate(message = MESSAGE_CODE_PREFIX + ".pendDate.CorrectDate}", groups = { Default.class })
    private String pendDate;

    /** 「P終了週」の列番号. */
    private static final int PEND_WEEK_COLNUM_INDEX = 10;

    /** 「P終了週」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".pendWeek.Pattern}", regexp = RegexpConstants.YAER_AND_WEEK, groups = { Default.class })
    private String pendWeek;

    /** 「上代(税無)」の列番号. */
    private static final int RETAIL_PRICE_COLNUM_INDEX = 11;

    /** 「上代(税無)」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".retailPrice.NotBlank}", groups = { ConfirmValidationGroup.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".retailPrice.Pattern}", regexp = RegexpConstants.PRICE, groups = { Default.class })
    private String retailPrice;

    /** 「原価（その他）」の列番号. */
    private static final int OTHER_COST_COLNUM_INDEX = 12;

    /** 「原価（その他）」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".otherCost.NotBlank}", groups = { ConfirmValidationGroup.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".otherCost.Pattern}", regexp = RegexpConstants.PRICE, groups = { Default.class })
    private String otherCost;

    /** 「生産メーカー」の列番号. */
    private static final int MDF_MAKER_CODE_COLNUM_INDEX = 13;

    /** 「生産メーカー」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".mdfMakerCode.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".mdfMakerCode.Pattern}", regexp = RegexpConstants.MAKER_CODE, groups = { Default.class })
    private String mdfMakerCode;

    /** 「生産工場」の列番号. */
    private static final int MDF_MAKER_FACTORY_CODE_COLNUM_INDEX = 14;

    /** 「生産工場」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".mdfMakerFactoryCode.Pattern}", regexp = RegexpConstants.FACTORY_CODE, groups = { Default.class })
    private String mdfMakerFactoryCode;

    /** 「委託先工場」の列番号. */
    private static final int CONSIGNMENT_FACTORY_COLNUM_INDEX = 15;

    /** 「委託先工場」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".consignmentFactory.Pattern}", regexp = RegexpConstants.CONSIGNMENT_FACTORY, groups = { Default.class })
    private String consignmentFactory;

    /** 「原産国」の列番号. */
    private static final int COO_CODE_COLNUM_INDEX = 16;

    /** 「原産国」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".cooCode.NotBlank}", groups = { ConfirmValidationGroup.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".cooCode.Pattern}", regexp = RegexpConstants.COO_CODE, groups = { Default.class })
    private String cooCode;

    /** 「企画担当」の列番号. */
    private static final int PLANNER_CODE_COLNUM_INDEX = 17;

    /** 「企画担当」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".plannerCode.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".plannerCode.Pattern}", regexp = RegexpConstants.STAFF_CODE, groups = { Default.class })
    private String plannerCode;

    /** 「製造担当」の列番号. */
    private static final int MDF_STAFF_CODE_COLNUM_INDEX = 18;

    /** 「製造担当」. */
    @NotBlank(message = MESSAGE_CODE_PREFIX + ".mdfStaffCode.NotBlank}", groups = { Default.class })
    @Pattern(message = MESSAGE_CODE_PREFIX + ".mdfStaffCode.Pattern}", regexp = RegexpConstants.STAFF_CODE, groups = { Default.class })
    private String mdfStaffCode;

    /** 「パターンナー」の列番号. */
    private static final int PATANER_CODE_COLNUM_INDEX = 19;

    /** 「パターンナー」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".patanerCode.Pattern}", regexp = RegexpConstants.STAFF_CODE, groups = { Default.class })
    private String patanerCode;

    /** 「パターンNo」の列番号. */
    private static final int PATTERN_NO_COLNUM_INDEX = 20;

    /** 「パターンNo」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".patternNo.Pattern}", regexp = RegexpConstants.PATTERN_NO, groups = { Default.class })
    private String patternNo;

    /** 「丸井品番」の列番号. */
    private static final int MARUI_GARMENT_NO_COLNUM_INDEX = 21;

    /** 「丸井品番」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".maruiGarmentNo.Pattern}", regexp = RegexpConstants.MARUI_GARMENT_NO, groups = { Default.class })
    private String maruiGarmentNo;

    /** 「Voi区分」の列番号. */
    private static final int VOI_CODE_COLNUM_INDEX = 22;

    /** 「Voi区分」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".voiCode.Pattern}", regexp = RegexpConstants.VOI_CODE, groups = { Default.class })
    private String voiCode;

    /** 「素材」の列番号. */
    private static final int MATERIAL_CODE_COLNUM_INDEX = 23;

    /** 「素材」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".materialCode.Pattern}", regexp = RegexpConstants.MATERIAL_CODE, groups = { Default.class })
    private String materialCode;

    /** 「ゾーン」の列番号. */
    private static final int ZONE_CODE_COLNUM_INDEX = 24;

    /** 「ゾーン」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".zoneCode.Pattern}", regexp = RegexpConstants.ZONE_CODE, groups = { Default.class })
    private String zoneCode;

    /** 「サブブランド」の列番号. */
    private static final int SUB_BRAND_CODE_COLNUM_INDEX = 25;

    /** 「サブブランド」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".subBrandCode.Pattern}", regexp = RegexpConstants.SUB_BRAND_CODE, groups = { Default.class })
    private String subBrandCode;

    /** 「テイスト」の列番号. */
    private static final int TASTE_CODE_COLNUM_INDEX = 26;

    /** 「テイスト」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".tasteCode.Pattern}", regexp = RegexpConstants.TASTE_CODE, groups = { Default.class })
    private String tasteCode;

    /** 「タイプ1」の列番号. */
    private static final int TYPE1_CODE_COLNUM_INDEX = 27;

    /** 「タイプ1」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".type1Code.Pattern}", regexp = RegexpConstants.TYPE1_CODE, groups = { Default.class })
    private String type1Code;

    /** 「タイプ2」の列番号. */
    private static final int TYPE2_CODE_COLNUM_INDEX = 28;

    /** 「タイプ2」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".type2Code.Pattern}", regexp = RegexpConstants.TYPE2_CODE, groups = { Default.class })
    private String type2Code;

    /** 「タイプ3」の列番号. */
    private static final int TYPE3_CODE_COLNUM_INDEX = 29;

    /** 「タイプ3」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".type3Code.Pattern}", regexp = RegexpConstants.TYPE3_CODE, groups = { Default.class })
    private String type3Code;

    /** 「展開」の列番号. */
    private static final int OUTLET_CODE_COLNUM_INDEX = 30;

    /** 「展開」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".outletCode.Pattern}", regexp = RegexpConstants.OUTLET_CODE, groups = { Default.class })
    private String outletCode;

    /** 「福袋」の列番号. */
    private static final int GRAB_BAG_COLNUM_INDEX = 31;

    /** 「福袋」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".grabBag.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String grabBag;

    /** 「在庫管理」の列番号. */
    private static final int INVENTORY_MANAGEMENT_TYPE_COLNUM_INDEX = 32;

    /** 「在庫管理」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".inventoryManagementType.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String inventoryManagementType;

    /** 「評価減」の列番号. */
    private static final int DEVALUATION_TYPE_COLNUM_INDEX = 33;

    /** 「評価減」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".devaluationType.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String devaluationType;

    /** 「消化委託」の列番号. */
    private static final int DIGESTION_COMMISSION_TYPE_COLNUM_INDEX = 34;

    /** 「消化委託」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".digestionCommissionType.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String digestionCommissionType;

    /** 「軽減税率対象」の列番号. */
    private static final int REDUCED_TAX_RATE_FLG_COLNUM_INDEX = 35;

    /** 「軽減税率対象」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".reducedTaxRateFlg.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String reducedTaxRateFlg;

    /** 「メーカー品番」の列番号. */
    private static final int MAKER_GARMENT_NO_COLNUM_INDEX = 36;

    /** 「メーカー品番」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".makerGarmentNo.Pattern}", regexp = RegexpConstants.MAKER_GARMENT_NO, groups = { Default.class })
    private String makerGarmentNo;

    /** 「取引メモ」の列番号. */
    private static final int MEMO_COLNUM_INDEX = 37;

    /** 「取引メモ」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".memo.Pattern}", regexp = RegexpConstants.MEMO, groups = { Default.class })
    private String memo;

    /** 「管理メモ」の列番号. */
    private static final int ITEM_MASSAGE_COLNUM_INDEX = 38;

    /** 「管理メモ」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".itemMassage.Pattern}", regexp = RegexpConstants.ITEM_MASSAGE, groups = { Default.class })
    private String itemMassage;

    /** 「管理メモ（店舗ヘ表示）」の列番号. */
    private static final int ITEM_MASSAGE_DISPLAY_COLNUM_INDEX = 39;

    /** 「管理メモ（店舗へ表示）」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".itemMassageDisplay.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String itemMassageDisplay;

    /** 「サンプル」の列番号. */
    private static final int SAMPLE_COLNUM_INDEX = 40;

    /** 「サンプル」. */
    @Pattern(message = MESSAGE_CODE_PREFIX + ".sample.Pattern}", regexp = RegexpConstants.BOOLEAN, groups = { Default.class })
    private String sample;

	// #PRD_0121 8761 add JFE start
	/** 「生地原価」の列番号. */
	private static final int MATL_COST_COLNUM_INDEX = 41;

	/** 「生地原価」. */
	@Pattern(message = MESSAGE_CODE_PREFIX + ".matlCost.Pattern}", regexp = RegexpConstants.PRICE, groups = {
			Default.class })
	private String matlCost;

	/** 「加工原価」の列番号. */
	private static final int PROCESSING_COST_COLNUM_INDEX = 42;

	/** 「加工原価」. */
	@Pattern(message = MESSAGE_CODE_PREFIX + ".processingCost.Pattern}", regexp = RegexpConstants.PRICE, groups = {
			Default.class })
	private String processingCost;

	/** 「付属原価」の列番号. */
	private static final int ACCESSORIES_COST_COLNUM_INDEX = 43;

	/** 「付属原価」. */
	@Pattern(message = MESSAGE_CODE_PREFIX + ".accessoriesCost.Pattern}", regexp = RegexpConstants.PRICE, groups = {
			Default.class })
	private String accessoriesCost;
	// #PRD_0121 8761 add JFE end

    /**
     * 「品番取込用」シートのリーダーを返却する.
     *
     * @param workbook {@link Workbook} instance.
     * @return レコードのリスト.
     */
    public static XlsxSheetReader<String, ItemSheet> getReader(
            final Workbook workbook) {
        return new XlsxSheetReader<String, ItemSheet>(workbook) {
            @Override
            protected String getKey(final Row row) {
                return getFormatStringValue(row.getCell(PART_NO_COLNUM_INDEX));
            }

            @Override
            protected ItemSheet getRow(final Row row) {
                final ItemSheet sheet = new ItemSheet();

                sheet.setRowIndex(row.getRowNum());
                sheet.setPartNo(getFormatStringValue(row.getCell(PART_NO_COLNUM_INDEX)));
                sheet.setSeasonCode(getFormatStringValue(row.getCell(SEASON_CODE_COLNUM_INDEX)));
                sheet.setYear(getFormatStringValue(row.getCell(YEAR_COLNUM_INDEX)));
                sheet.setProductName(getFormatStringValue(row.getCell(PRODUCT_NAME_COLNUM_INDEX)));
                sheet.setProductNameKana(getFormatStringValue(row.getCell(PRODUCT_NAME_KANA_COLNUM_INDEX)));
                sheet.setProviOrderDate(getFormatStringValue(row.getCell(PROVI_ORDER_DATE_COLNUM_INDEX)));
                sheet.setPreferredDeliveryDate(getFormatStringValue(row.getCell(PREFERRED_DELIVERY_DATE_COLNUM_INDEX)));
                sheet.setDeploymentDate(getFormatStringValue(row.getCell(DEPLOYMENT_DATE_COLNUM_INDEX)));
                sheet.setDeploymentWeek(getFormatStringValue(row.getCell(DEPLOYMENT_WEEK_COLNUM_INDEX)));
                sheet.setPendDate(getFormatStringValue(row.getCell(PEND_DATE_COLNUM_INDEX)));
                sheet.setPendWeek(getFormatStringValue(row.getCell(PEND_WEEK_COLNUM_INDEX)));
                sheet.setRetailPrice(getFormatStringValue(row.getCell(RETAIL_PRICE_COLNUM_INDEX)));
                sheet.setOtherCost(getFormatStringValue(row.getCell(OTHER_COST_COLNUM_INDEX)));
                sheet.setMdfMakerCode(getFormatStringValue(row.getCell(MDF_MAKER_CODE_COLNUM_INDEX)));
                sheet.setMdfMakerFactoryCode(getFormatStringValue(row.getCell(MDF_MAKER_FACTORY_CODE_COLNUM_INDEX)));
                sheet.setConsignmentFactory(getFormatStringValue(row.getCell(CONSIGNMENT_FACTORY_COLNUM_INDEX)));
                sheet.setCooCode(getFormatStringValue(row.getCell(COO_CODE_COLNUM_INDEX)));
                sheet.setPlannerCode(getFormatStringValue(row.getCell(PLANNER_CODE_COLNUM_INDEX)));
                sheet.setMdfStaffCode(getFormatStringValue(row.getCell(MDF_STAFF_CODE_COLNUM_INDEX)));
                sheet.setPatanerCode(getFormatStringValue(row.getCell(PATANER_CODE_COLNUM_INDEX)));
                sheet.setPatternNo(getFormatStringValue(row.getCell(PATTERN_NO_COLNUM_INDEX)));
                sheet.setMaruiGarmentNo(getFormatStringValue(row.getCell(MARUI_GARMENT_NO_COLNUM_INDEX)));
                sheet.setVoiCode(getFormatStringValue(row.getCell(VOI_CODE_COLNUM_INDEX)));
                sheet.setMaterialCode(getFormatStringValue(row.getCell(MATERIAL_CODE_COLNUM_INDEX)));
                sheet.setZoneCode(getFormatStringValue(row.getCell(ZONE_CODE_COLNUM_INDEX)));
                sheet.setSubBrandCode(getFormatStringValue(row.getCell(SUB_BRAND_CODE_COLNUM_INDEX)));
                sheet.setTasteCode(getFormatStringValue(row.getCell(TASTE_CODE_COLNUM_INDEX)));
                sheet.setType1Code(getFormatStringValue(row.getCell(TYPE1_CODE_COLNUM_INDEX)));
                sheet.setType2Code(getFormatStringValue(row.getCell(TYPE2_CODE_COLNUM_INDEX)));
                sheet.setType3Code(getFormatStringValue(row.getCell(TYPE3_CODE_COLNUM_INDEX)));
                sheet.setOutletCode(getFormatStringValue(row.getCell(OUTLET_CODE_COLNUM_INDEX)));
                sheet.setGrabBag(getFormatStringValue(row.getCell(GRAB_BAG_COLNUM_INDEX)));
                sheet.setInventoryManagementType(getFormatStringValue(row.getCell(INVENTORY_MANAGEMENT_TYPE_COLNUM_INDEX)));
                sheet.setDevaluationType(getFormatStringValue(row.getCell(DEVALUATION_TYPE_COLNUM_INDEX)));
                sheet.setDigestionCommissionType(getFormatStringValue(row.getCell(DIGESTION_COMMISSION_TYPE_COLNUM_INDEX)));
                sheet.setReducedTaxRateFlg(getFormatStringValue(row.getCell(REDUCED_TAX_RATE_FLG_COLNUM_INDEX)));
                sheet.setMakerGarmentNo(getFormatStringValue(row.getCell(MAKER_GARMENT_NO_COLNUM_INDEX)));
                sheet.setMemo(getFormatStringValue(row.getCell(MEMO_COLNUM_INDEX)));
                sheet.setItemMassage(getFormatStringValue(row.getCell(ITEM_MASSAGE_COLNUM_INDEX)));
                sheet.setItemMassageDisplay(getFormatStringValue(row.getCell(ITEM_MASSAGE_DISPLAY_COLNUM_INDEX)));
                sheet.setSample(getFormatStringValue(row.getCell(SAMPLE_COLNUM_INDEX)));
				// #PRD_0121 8761 add JFE start
				sheet.setMatlCost(getFormatStringValue(row.getCell(MATL_COST_COLNUM_INDEX)));
				sheet.setProcessingCost(getFormatStringValue(row.getCell(PROCESSING_COST_COLNUM_INDEX)));
				sheet.setAccessoriesCost(getFormatStringValue(row.getCell(ACCESSORIES_COST_COLNUM_INDEX)));
				// #PRD_0121 8761 add JFE end

                return sheet;
            }
        }.sheetName(SHEET_NAME).startRowIndex(START_ROW_INDEX);
    }

    /**
     * {@link ItemModel} に変換する.
     *
     * @param row {@link ItemSheet} instance.
     * @param registStatus {@link RegistStatusType}
     * @return {@link ItemModel} instance.
     */
    public static ItemModel toItem(
            final ItemSheet row,
            final RegistStatusType registStatus) {
        final ItemModel model = new ItemModel();

        model.setPartNo(row.getPartNo());
        model.setBrandCode(getBrandCode(row.getPartNo()));
        model.setItemCode(getItemCode(row.getPartNo()));

        // シーズンコードからtypeを取得
        SubSeasonCodeType.findByScreenSeasonCode(row.getSeasonCode()).ifPresent(v -> {
            model.setSeasonCode(v.getDbSeasonCode());
            model.setSubSeasonCode(v.getValue());
        });

        model.setYear(Integer.parseInt(row.getYear()));
        model.setProductName(row.getProductName());
        model.setProductNameKana(row.getProductNameKana());

        model.setProviOrderDate(DateUtils.stringYMDToDateOrNull(row.getProviOrderDate()));
        model.setPreferredDeliveryDate(DateUtils.stringYMDToDateOrNull(row.getPreferredDeliveryDate()));

        toDeploymentDateAndWeek(row, model);
        toPendDateAndWeek(row, model);

        model.setRetailPrice(NumberUtils.createBigDecimal(StringUtils.priceToNumber(row.getRetailPrice())));
        model.setOtherCost(NumberUtils.createBigDecimal(StringUtils.priceToNumber(row.getOtherCost())));

        // 発注先メーカー情報のリストに変換
        model.setOrderSuppliers(toOrderSuppliers(row));

        model.setCooCode(row.getCooCode());
        model.setPlannerCode(row.getPlannerCode());
        model.setMdfStaffCode(row.getMdfStaffCode());
        model.setPatanerCode(row.getPatanerCode());
        model.setPatternNo(row.getPatternNo());
        model.setMaruiGarmentNo(row.getMaruiGarmentNo());
        model.setVoiCode(row.getVoiCode());
        model.setMaterialCode(row.getMaterialCode());
        model.setZoneCode(row.getZoneCode());
        model.setSubBrandCode(row.getSubBrandCode());
        model.setTasteCode(row.getTasteCode());
        model.setType1Code(row.getType1Code());
        model.setType2Code(row.getType2Code());
        model.setType3Code(row.getType3Code());
        model.setOutletCode(row.getOutletCode());
        model.setGrabBag(BooleanUtils.toBoolean(
                org.apache.commons.lang3.StringUtils.defaultIfEmpty(row.getGrabBag(), "0")));
        model.setInventoryManagementType(BooleanUtils.toBoolean(
                org.apache.commons.lang3.StringUtils.defaultIfEmpty(row.getInventoryManagementType(), "1")));
        model.setDevaluationType(BooleanUtils.toBoolean(
                org.apache.commons.lang3.StringUtils.defaultIfEmpty(row.getDevaluationType(), "1")));
        model.setDigestionCommissionType(BooleanUtils.toBoolean(
                org.apache.commons.lang3.StringUtils.defaultIfEmpty(row.getDigestionCommissionType(), "0")));
        model.setReducedTaxRateFlg(BooleanUtils.toBoolean(
                org.apache.commons.lang3.StringUtils.defaultIfEmpty(row.getReducedTaxRateFlg(), "0")));
        model.setMakerGarmentNo(row.getMakerGarmentNo());
        model.setMemo(row.getMemo());
        model.setItemMassage(row.getItemMassage());
        model.setItemMassageDisplay(BooleanUtils.toBoolean(row.getItemMassageDisplay()));
        model.setRegistStatus(registStatus.getValue());
		// #PRD_0121 8761 add JFE start
		model.setMatlCost(row.getMatlCost() == null ? new BigDecimal(0)
				: NumberUtils.createBigDecimal(StringUtils.priceToNumber(row.getMatlCost())));
		model.setProcessingCost(row.getProcessingCost() == null ? new BigDecimal(0)
				: NumberUtils.createBigDecimal(StringUtils.priceToNumber(row.getProcessingCost())));
		model.setAccessoriesCost(row.getAccessoriesCost() == null ? new BigDecimal(0)
				: NumberUtils.createBigDecimal(StringUtils.priceToNumber(row.getAccessoriesCost())));
		// #PRD_0121 8761 add JFE end

        if (RegistStatusType.PART == registStatus) {
            // 品番として登録する場合、連携対象とする
            model.setLinkingStatus(LinkingStatusType.TARGET);
        }

        model.setSample(BooleanUtils.toBoolean(row.getSample()));

        // フクキタル関連の項目
        final FukukitaruItemModel fkItem = new FukukitaruItemModel();

        fkItem.setNergyBillCode1("");
        fkItem.setNergyBillCode2("");
        fkItem.setNergyBillCode3("");
        fkItem.setNergyBillCode4("");
        fkItem.setNergyBillCode5("");
        fkItem.setNergyBillCode6("");
        fkItem.setPrintAppendicesTerm(BooleanType.FALSE);
        fkItem.setPrintCoo(BooleanType.FALSE);
        fkItem.setPrintParts(BooleanType.FALSE);
        fkItem.setPrintQrcode(BooleanType.FALSE);
        fkItem.setPrintSize(BooleanType.FALSE);
        fkItem.setPrintWashPattern(BooleanType.FALSE);
        fkItem.setReefurPrivateBrandCode("");
        fkItem.setSaturdaysPrivateNyPartNo("");

        model.setFkItem(fkItem);

        return model;
    }

    /**
     * 品番からブランドコードを取得する.
     *
     * @param partNo 品番.
     * @return ブランドコード.
     */
    public static String getBrandCode(
            final String partNo) {
        return org.apache.commons.lang3.StringUtils.substring(partNo, BRAND_CODE_START_INDEX, BRAND_CODE_END_INDEX);
    }

    /**
     * 品番からアイテムコードを取得する.
     *
     * @param partNo 品番.
     * @return アイテムコード.
     */
    public static String getItemCode(
            final String partNo) {
        return org.apache.commons.lang3.StringUtils.substring(partNo, ITEM_CODE_START_INDEX, ITEM_CODE_END_INDEX);
    }

    /**
     * 日付と週番号を変換する.
     *
     * @param row {@link ItemSheet} instance.
     * @param model {@link ItemModel} instance.
     */
    private static void toDeploymentDateAndWeek(
            final ItemSheet row,
            final ItemModel model) {
        toDateAndWeek(
                row.getDeploymentDate(),
                row.getDeploymentWeek()).ifPresent(dw -> {
                    model.setDeploymentDate(dw.getDate());
                    model.setDeploymentWeek(dw.getWeek());
                });
    }

    /**
     * 日付と週番号を変換する.
     *
     * @param row {@link ItemSheet} instance.
     * @param model {@link ItemModel} instance.
     */
    private static void toPendDateAndWeek(
            final ItemSheet row,
            final ItemModel model) {
        toDateAndWeek(
                row.getPendDate(),
                row.getPendWeek()).ifPresent(dw -> {
                    model.setPendDate(dw.getDate());
                    model.setPendWeek(dw.getWeek());
                });
    }

    /**
     * 日付と週番号の文字列の型を変換する.
     *
     * @param strDate 日付.
     * @param strWeek 週番号.
     * @return 型変換後の日付と週番号
     */
    private static Optional<DateAndWeek> toDateAndWeek(
            final String strDate,
            final String strWeek) {
        final boolean isDateEmpty = org.apache.commons.lang3.StringUtils.isEmpty(strDate);
        final boolean isWeekEmpty = org.apache.commons.lang3.StringUtils.isEmpty(strWeek);

        if (isDateEmpty && isWeekEmpty) {
            return Optional.empty();
        }

        final DateAndWeek dw = new DateAndWeek();

        if (!isDateEmpty) {
            dw.setDate(DateUtils.stringYMDToDateOrNull(strDate));
        } else {
            dw.setDate(DateUtils.stringYYYYWWToDate(strWeek));
        }

        if (!isWeekEmpty) {
            dw.setWeek(Integer.parseInt(org.apache.commons.lang3.StringUtils.right(strWeek, 2)));
        } else {
            dw.setWeek(DateUtils.calcWeek(dw.getDate()));
        }

        return Optional.of(dw);
    }

    /**
     * 日付と週番号.
     */
    @Data
    private static class DateAndWeek {
        private Date date;
        private int week;
    }

    /**
     * 発注先メーカー情報のリストに変換する.
     *
     * @param row {@link ItemSheet} instance.
     * @return {@link List<OrderSupplierModel>} instance.
     */
    public static List<OrderSupplierModel> toOrderSuppliers(
            final ItemSheet row) {
        final List<OrderSupplierModel> orderSuppliers = new ArrayList<>();

        final OrderSupplierModel orderSupplier = new OrderSupplierModel();

        orderSupplier.setSupplierCode(row.getMdfMakerCode());
        orderSupplier.setOrderCategoryType(OrderCategoryType.PRODUCT);
        orderSupplier.setSupplierFactoryCode(row.getMdfMakerFactoryCode());
        orderSupplier.setConsignmentFactory(row.getConsignmentFactory());

        orderSuppliers.add(orderSupplier);

        return orderSuppliers;
    }
}
