package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.AccountPurchaseConfirmLinkingCreateDatFileComponent;
import jp.co.jun.edi.component.ConvertComponent;
import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.S3Component;
import jp.co.jun.edi.component.model.AccountPurchaseConfirmFileLinkingDatModel;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.entity.TDeliverySkuEntity;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSupplierEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TWmsLinkingFileEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.repository.TDeliverySkuRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSupplierRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.repository.TWmsLinkingFileRepository;
import jp.co.jun.edi.type.BusinessType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.FileInfoStatusType;
import jp.co.jun.edi.type.IfCategoryType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.PsType;
import jp.co.jun.edi.type.PurchaseType;
import jp.co.jun.edi.type.WmsLinkingStatusType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;
import jp.co.jun.edi.util.StringUtils;

/**
 * 会計仕入確定ファイル作成スケジュールのコンポーネント.
 */
@Component
public class AccountPurchaseConfirmFileLinkingScheduleComponent {
    /** コンテンツタイプ. */
    private static final String CONTENT_TYPE = "application/octet-stream";

    /** 日付フォーマット：yyyyMMdd. */
    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyyMMdd";

    /** 日付フォーマット：yyMMdd. */
    private static final String DATE_FORMAT_YY_MM_DD = "yyMMdd";

    /** グルーピングキー連結文字：_. */
    private static final String UNDER_BAR = "_";

    // DATファイルデータにセットする定数
    /** 半角スペース. */
    private static final String HALF_WIDTH_SPACE = " ";
    /** ゼロ1桁. */
    private static final String STRING_ONE_ZERO = "0";
    /** ゼロ2桁. */
    private static final String STRING_TWO_ZERO = "00";
    /** ゼロ3桁. */
    private static final String STRING_THREE_ZERO = "000";
    /** ゼロ4桁. */
    private static final String STRING_FOUR_ZERO = "0000";
    // PRD_0160 add JFE start
    /** ゼロ5桁. */
    private static final String STRING_FIVE_ZERO = "00000";
    // PRD_0160 add JFE end
    /** ゼロ6桁. */
    private static final String STRING_SIX_ZERO = "000000";
    /** ゼロ7桁. */
    private static final String STRING_SEVEN_ZERO = "0000000";
    /** ゼロ8桁. */
    private static final String STRING_EIGHT_ZERO = "00000000";
    /** "1". */
    private static final String STRING_ONE = "1";
    /** +ゼロ3桁. */
    private static final String STRING_PLUS_THREE_ZERO = "+000";
    /** +ゼロ5桁. */
    private static final String STRING_PLUS_FIVE_ZERO = "+00000";

    // 文字切り捨て・拡張桁数
    /** 1桁. */
    private static final int DIGIT_1 = 1;
    /** 2桁. */
    private static final int DIGIT_2 = 2;
    /** 3桁. */
    private static final int DIGIT_3 = 3;
    /** 4桁. */
    private static final int DIGIT_4 = 4;
    /** 5桁. */
    private static final int DIGIT_5 = 5;
    /** 6桁. */
    private static final int DIGIT_6 = 6;
    /** 7桁. */
    private static final int DIGIT_7 = 7;
    /** 8桁. */
    private static final int DIGIT_8 = 8;
    /** 9桁. */
    private static final int DIGIT_9 = 9;
    /** 10桁. */
    private static final int DIGIT_10 = 10;
    /** 11桁. */
    private static final int DIGIT_11 = 11;
    /** 12桁. */
    private static final int DIGIT_12 = 12;
    /** 20桁. */
    private static final int DIGIT_20 = 20;
    /** 21桁. */
    private static final int DIGIT_21 = 21;

    @Autowired
    private AccountPurchaseConfirmLinkingCreateDatFileComponent createDatFileComponent;

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private S3Component s3Component;

    @Autowired
    private ConvertComponent convertComponent;

    @Autowired
    private TWmsLinkingFileRepository tWmsLinkingFileRepository;

    @Autowired
    private TPurchaseRepository tPurchaseRepository;

    @Autowired
    private TOrderRepository tOrderRepository;

    @Autowired
    private TItemRepository tItemRepository;

    @Autowired
    private TOrderSupplierRepository tOrderSupplierRepository;

    @Autowired
    private TDeliverySkuRepository tDeliverySkuRepository;

    @Autowired
    private MSirmstRepository mSirmstRepository;

    /**
     * 計上日の仕入情報毎に処理実行.
     * ・DATファイルの作成
     * ・S3へのアップロード
     * ・倉庫連携ファイル情報の登録
     * ・仕入情報の更新(会計連携ステータス)
     *
     * @param purchases 仕入情報リスト
     * @param recordAt 計上日
     * @param nitymdStr 日計日
     * @param userId システムユーザID
     * @throws Exception 例外
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void executeByRecordAtPurchases(
            final List<TPurchaseEntity> purchases,
            final Date recordAt,
            final String nitymdStr,
            final BigInteger userId) throws Exception {
        // 仕入情報リストを基に、DATファイル作成用Modelリストを作成
        final List<AccountPurchaseConfirmFileLinkingDatModel> datModels = generateInfo(purchases, nitymdStr);

        // DATファイル生成
        final File instructionFile = createInstructionFile(datModels, recordAt);

        // S3プレフィックスを取得する：shipment/OR
        final String s3Prefix = propertyComponent.getBatchProperty().getShipmentProperty()
                .getShipmentS3Prefix() + getBusinessType().getValue();

        // S3へアップロード
        final String s3Key = uploadFileToS3(instructionFile, s3Prefix);

        // 倉庫連携ファイル情報登録
        createWmsLinkingFile(instructionFile, s3Key, s3Prefix, userId);

        // 仕入情報の会計連携ステータスを更新(ファイル処理済)
        updateAccountLinkingStatus(FileInfoStatusType.FILE_COMPLETED, purchases, userId);
    }

    /**
     * 倉庫連携ファイル情報を登録する.
     *
     * @param instructionFile ファイル
     * @param s3Key S3キー
     * @param s3Prefix S3プレフィックス
     * @param userId ユーザID
     */
    private void createWmsLinkingFile(
            final File instructionFile,
            final String s3Key,
            final String s3Prefix,
            final BigInteger userId) {
        final TWmsLinkingFileEntity wmsLinkingFileEntity = new TWmsLinkingFileEntity();

        wmsLinkingFileEntity.setBusinessType(getBusinessType());
        wmsLinkingFileEntity.setManageNumber(STRING_SIX_ZERO);
        wmsLinkingFileEntity.setFileName(instructionFile.getName());
        wmsLinkingFileEntity.setS3Key(s3Key);
        wmsLinkingFileEntity.setS3Prefix(s3Prefix);
        wmsLinkingFileEntity.setFileCreatedAt(DateUtils.createNow());
        wmsLinkingFileEntity.setWmsLinkingStatus(WmsLinkingStatusType.FILE_CREATED);
        wmsLinkingFileEntity.setCreatedUserId(userId);
        wmsLinkingFileEntity.setUpdatedUserId(userId);

        tWmsLinkingFileRepository.save(wmsLinkingFileEntity);
    }

    /**
     * 仕入情報の会計連携ステータスを更新する.
     *
     * @param type 会計連携ステータス
     * @param purchases 仕入情報
     * @param updatedUserId 更新ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateAccountLinkingStatus(
            final FileInfoStatusType type,
            final List<TPurchaseEntity> purchases,
            final BigInteger updatedUserId) {
        final List<BigInteger> ids = purchases.stream().map(entity -> entity.getId()).collect(Collectors.toList());
        tPurchaseRepository.updateAccountLinkingStatusByIds(type, ids, updatedUserId);
    }

    /**
     * バッチ処理対象のファイル作成データを取得する.
     *
     * @param purchases 仕入情報リスト
     * @param nitymdStr 日計日
     * @return ファイル作成用Modelリスト
     */
    private List<AccountPurchaseConfirmFileLinkingDatModel> generateInfo(
            final List<TPurchaseEntity> purchases,
            final String nitymdStr) {
        // ファイル作成用Modelリスト作成
        final List<AccountPurchaseConfirmFileLinkingDatModel> datModels = purchases
                .stream()
                .map(purchase -> generateAccountPurchaseConfirmModel(purchase, nitymdStr))
                .collect(Collectors.toList());

        // 採番項目のセット
        generateNumberingItem(datModels);

        return datModels;
    }

    /**
     * 採番項目データセット.
     * ・データ固有 伝票No
     * ・データ固有 行No
     * ・管理情報 SEQ
     *
     * @param datModels ファイル作成用Modelリスト
     */
    private void generateNumberingItem(final List<AccountPurchaseConfirmFileLinkingDatModel> datModels) {
        // 採番順にソート
        sortForNumbering(datModels);

        // 採番用Map作成
        final Map<String, List<AccountPurchaseConfirmFileLinkingDatModel>> datMap = groupingForNumbering(datModels);

        // 採番項目セット：
        int voucherNum = 1;
        int manageSeq = 1;
        for (final Entry<String, List<AccountPurchaseConfirmFileLinkingDatModel>> entry : datMap.entrySet()) {
            int lineNum = 1;
            for (final AccountPurchaseConfirmFileLinkingDatModel dat : entry.getValue()) {
                // データ固有 伝票No
                dat.setDataSpecificVoucherNumber(StringUtils.toStringPadding0(voucherNum, DIGIT_6));
                // データ固有 行No
                dat.setDataSpecificLineNumber(StringUtils.toStringPadding0(lineNum, DIGIT_3));
                // 管理情報 SEQ
                dat.setManageSeq(StringUtils.toStringPadding0(manageSeq, DIGIT_5));

                manageSeq++;
                lineNum++;
            }
            voucherNum++;
        }
    }

    /**
     * 採番用にグルーピング.
     * キー：
     * (1) 仕入データ作成日
     * (2) 仕入データ管理No
     * (3) 部門上2桁
     * (4) 入荷場所
     * (5) 発注No
     * (6) 仕入伝票No
     * (7) 製品仕入先
     * (8) 入荷日
     * (9) 伝票区分
     *
     * @param datModels ファイル作成用Modelリスト
     * @return グルーピング後のMap
     */
    private Map<String, List<AccountPurchaseConfirmFileLinkingDatModel>> groupingForNumbering(
            final List<AccountPurchaseConfirmFileLinkingDatModel> datModels) {
        // グルーピング
        return datModels.stream().collect(
                Collectors.groupingBy(dat -> generateDatGroupingKey(dat),
                        // ※順番変えない
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /**
     * グルーピング用キー作成.
     * キー：
     * (1) 仕入データ作成日
     * (2) 仕入データ管理No
     * (3) 部門上2桁
     * (4) 入荷場所
     * (5) 発注No
     * (6) 仕入伝票No
     * (7) 製品仕入先
     * (8) 入荷日
     * (9) 伝票区分
     * 上記を「_」で連結
     *
     * @param datModel ファイル作成用Model
     * @return 連結後の文字列
     */
    private String generateDatGroupingKey(final AccountPurchaseConfirmFileLinkingDatModel datModel) {
        final StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(datModel.getNumSqManageDate()))
        .append(UNDER_BAR)
        .append(datModel.getSqManageNumber())
        .append(UNDER_BAR)
        .append(datModel.getNumDeptCode())
        .append(UNDER_BAR)
        .append(datModel.getNumArrivalPlace())
        .append(UNDER_BAR)
        .append(datModel.getOrderNumber())
        .append(UNDER_BAR)
        .append(datModel.getPurchaseVoucherNumber())
        .append(UNDER_BAR)
        .append(datModel.getSupplierCode())
        .append(UNDER_BAR)
        .append(datModel.getArrivalAt())
        .append(UNDER_BAR)
        .append(datModel.getVoucherCategory());
        return sb.toString();
    }

    /**
     * 採番順にソート.
     * ※値にNULLが無いためNULLの考慮不要
     * (1) 仕入データ作成日,昇順
     * (2) 仕入データ管理No,昇順
     * (3) 部門上2桁       ,昇順
     * (4) 入荷場所        ,昇順
     * (5) 発注No          ,昇順
     * (6) 仕入伝票No      ,昇順
     * (7) 製品仕入先      ,昇順
     * (8) 入荷日          ,昇順
     * (9) 伝票区分        ,昇順
     *
     * @param datModels ファイル作成用Modelリスト
     */
    private void sortForNumbering(final List<AccountPurchaseConfirmFileLinkingDatModel> datModels) {
        Collections.sort(datModels,
                Comparator.comparing(AccountPurchaseConfirmFileLinkingDatModel::getNumSqManageDate)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getSqManageNumber)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getNumDeptCode)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getNumArrivalPlace)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getOrderNumber)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getPurchaseVoucherNumber)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getSupplierCode)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getArrivalAt)
                .thenComparing(AccountPurchaseConfirmFileLinkingDatModel::getVoucherCategory));
    }

    /**
     * DATファイル作成用Modelを作成する.
     *
     * @param purchase 仕入情報
     * @param nitymdStr 日計日
     * @return DATファイル作成用Model
     */
    private AccountPurchaseConfirmFileLinkingDatModel generateAccountPurchaseConfirmModel(
            final TPurchaseEntity purchase,
            final String nitymdStr) {
        final AccountPurchaseConfirmFileLinkingDatModel datModel = new AccountPurchaseConfirmFileLinkingDatModel();

        // 仕入情報を基にデータセット
        generateDataByPurchase(datModel, purchase);
        // 発注情報と品番情報を基にデータセット
        generateDataByOrderAndItem(datModel, purchase);
        // その他データセット(納品SKU、管理マスタなど)
        generateOtherData(datModel, purchase, nitymdStr);
        // 固定値データセット
        generateFixedData(datModel);
        // NULL項目の整形
        formatEmptyData(datModel);
        // 採番項目データセット
        generateNumberingData(datModel, purchase);

        return datModel;
    }

    /**
     * 仕入情報を基にデータセット.
     *
     * @param datModel DATファイル作成用Model
     * @param purchase 仕入情報
     */
    private void generateDataByPurchase(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TPurchaseEntity purchase) {
        // 入荷伝票No
//PRD_0197        datModel.setSqManageNumber(purchase.getSqManageNumber());
    	//PRD_0203 jfe mod start
        //datModel.setSqManageNumber(String.format("%6s", purchase.getSqManageNumber().replace(" ", "0")));
    	datModel.setSqManageNumber(paddingSpaceRight(purchase.getSqManageNumber(),DIGIT_6));
        //PRD_0203 jfe mod end

        // 入荷伝票行 ※仕入伝票行 > 3桁の場合、エラー
        checkOverLength("purchase_voucher_line", purchase.getPurchaseVoucherLine().toString(), DIGIT_3, purchase);
        datModel.setPurchaseVoucherLine(StringUtils.toStringPadding0(purchase.getPurchaseVoucherLine(), DIGIT_3));

        // 場所（至）
//PRD_0197        datModel.setArrivalPlaceTo(generateArrivalPlace(purchase));
        //PRD_0203 jfe mod start
//        datModel.setArrivalPlaceTo(String.format("%2s",generateArrivalPlace(purchase).replace(" ", "0")));
        datModel.setArrivalPlaceTo(paddingSpaceRight(generateArrivalPlace(purchase), DIGIT_2));
        //PRD_0203 jfe mod end
        // 場所（発生）
//PRD_0197        datModel.setArrivalPlaceOccur(datModel.getArrivalPlaceTo());
        //PRD_0203 jfe mod start
//        datModel.setArrivalPlaceOccur(String.format("%2s",datModel.getArrivalPlaceTo().replace(" ", "0")));
        datModel.setArrivalPlaceOccur(paddingSpaceRight(datModel.getArrivalPlaceTo(), DIGIT_2));
        //PRD_0203 jfe mod end
        // 配分コード 課
        // PRD_0160 del JFE start
        //datModel.setDivisionCode(purchase.getDivisionCode());
        // PRD_0160 del JFE end

        // 入荷日
        if (Objects.isNull(purchase.getArrivalAt())) {
            // 日付がNULLの場合は"00000000"固定
            datModel.setArrivalAt(STRING_EIGHT_ZERO);
        } else {
            // NULLでない場合はyyyyMMdd形式にフォーマット
            datModel.setArrivalAt(DateUtils.formatFromDate(purchase.getArrivalAt(), DATE_FORMAT_YYYY_MM_DD));
        }

        // 伝票日付(=入荷日)
        datModel.setVoucherAt(datModel.getArrivalAt());
        // 計上日
        datModel.setRecordAt(DateUtils.formatFromDate(purchase.getRecordAt(), DATE_FORMAT_YYYY_MM_DD));

        // 発注No
        if (Objects.isNull(purchase.getOrderNumber())) {
            // 発注Noがない場合は"000000"固定
            datModel.setOrderNumber(STRING_SIX_ZERO);
        } else {
            // 6桁まで前0パディング(0の場合、"000000"に変換するため)
            datModel.setOrderNumber(StringUtils.toStringPadding0(purchase.getOrderNumber(), DIGIT_6));
        }

        // 引取回数
        // PRD_0160 del JFE start
        //datModel.setPurchaseCount(generatePurchaseCount(purchase));
        // PRD_0160 del JFE end

        // 仕入伝票No
//PRD_0197        datModel.setPurchaseVoucherNumber(purchase.getPurchaseVoucherNumber());
        //PRD_0203 jfe mod start
//        datModel.setPurchaseVoucherNumber(String.format("%6s",purchase.getPurchaseVoucherNumber().replace(" ", "0")));
        datModel.setPurchaseVoucherNumber(paddingSpaceRight(purchase.getPurchaseVoucherNumber(), DIGIT_6));
        //PRD_0203 jfe mod end
        // 先方伝票No
        if (org.apache.commons.lang3.StringUtils.isEmpty(purchase.getMakerVoucherNumber())) {
            // 仕入相手伝票Noがない場合は"000000"固定
            datModel.setMakerVoucherNumber(STRING_SIX_ZERO);
        } else {
//PRD_0197            datModel.setMakerVoucherNumber(purchase.getMakerVoucherNumber());
        	//PRD_0203 jfe mod start
//            datModel.setMakerVoucherNumber(String.format("%6s",purchase.getMakerVoucherNumber().replace(" ", "0")));
            datModel.setMakerVoucherNumber(paddingSpaceRight(purchase.getMakerVoucherNumber(), DIGIT_6));
            //PRD_0203 jfe mod end
        }

        // 金額関連項目
        generatePriceItem(datModel, purchase);
        // カラー
        datModel.setColorCode(purchase.getColorCode());
        // 予備(5桁)
        // PRD_0160 del JFE start
        //datModel.setReserve5Digit(StringUtils.paddingSpaceRight(purchase.getSize(), DIGIT_5));
        // PRD_0160 del JFE end

        //PRD_0110 #7831 JFE mod start
        // 管理情報 日付
//        datModel.setManageDate(DateUtils.formatFromDate(purchase.getCreatedAt(), DATE_FORMAT_YY_MM_DD));
        datModel.setManageDate(DateUtils.formatFromDate(purchase.getRecordAt(), DATE_FORMAT_YY_MM_DD));
        //PRD_0110 #7831 JFE mod end

        // PRD_0160 mod JFE start
    	// 仕入区分 = 2,4,5 の場合
    	if (purchase.getPurchaseType() == PurchaseType.ADDITIONAL_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.RETURN_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.SHIPMENT_PURCHASE_LUMP) {
    		// 配分コード 課："00"固定
        	datModel.setDivisionCode(STRING_TWO_ZERO);

        	// 引取回数："01"固定
        	datModel.setPurchaseCount("01");
        	// 原反　反番号：仕入情報「入荷店舗」
        	//PRD_0193 #11702 JFE mod start
//        	if (Objects.isNull(purchase.getArrivalShop())) {
//        		datModel.setOriginalFabricClothNumber("      ");
//        	} else {
//        		datModel.setOriginalFabricClothNumber(purchase.getArrivalShop().substring(0, 6));
//        	}
        	if (Objects.isNull(purchase.getArrivalShop())) {
        		datModel.setOriginalFabricClothNumber("      ");
            } else if (purchase.getArrivalShop().length() >= 6){
        		datModel.setOriginalFabricClothNumber(purchase.getArrivalShop().substring(0, 6));
            }else {
                datModel.setOriginalFabricClothNumber("      ");
        	}
            //PRD_0193 #11702 JFE mod end
        	// 予備(5桁)：半角スペース5桁
        	datModel.setReserve5Digit("     ");
        } else {
        	// 配分コード 課
//PRD_0197        	datModel.setDivisionCode(purchase.getDivisionCode());
        	//PRD_0203 JFE mod start
//            datModel.setDivisionCode(String.format("%2s",purchase.getDivisionCode().replace(" ", "0")));
//        	String divisionCode = purchase.getDivisionCode();
//        	if(divisionCode == null) divisionCode = "";
//            if(divisionCode.equals("")) divisionCode = STRING_TWO_ZERO;
            datModel.setDivisionCode(paddingSpaceRight(purchase.getDivisionCode(),DIGIT_2));
            //PRD_0203 JFE mod end
        	// 引取回数
        	datModel.setPurchaseCount(generatePurchaseCount(purchase));
        	// 原反 反番号：半角スペース6桁
            datModel.setOriginalFabricClothNumber("      ");
        	// 予備(5桁)
        	datModel.setReserve5Digit(StringUtils.paddingSpaceRight(purchase.getSize(), DIGIT_5));
        }
    	// PRD_0160 mod JFE end
    }

    //PRD_0203 JFE add start
    /**
     * 空文字、nullデータの変換.
     * ・引数で指定した桁まで前0パディング
     * ・nullの場合は指定した桁数0を設定する
     *
     * @param 処理対象の文字列
     * @param 0の個数
     * @return datファイルに指定する文字列
     */
    private String paddingSpaceRight(final String str,final Integer len){
    	String fmt = "";
    	String AftStr = "";
    	String tmp = "";
    	fmt = "%" + len.toString() + "s";

    	if(str == null) AftStr = "";
    	else AftStr = str;

        if(AftStr.equals("")) {
//        	for(int i = 0 ;i < len;i++)	AftStr += "0";
            for(int i = 0 ;i < len;i++) {
                tmp = AftStr.concat("0");
                AftStr = tmp;
            }
        }else {
        	AftStr =  String.format(fmt,str.replace(" ", "0"));
        }
        return AftStr;
    }
    //PRD_0203 JFE add end

    /**
     * 「取引回数」の変換.
     * ・2桁まで前0パディング
     * ・nullの場合は"01"
     * ・3桁以上の場合エラー
     *
     * @param purchase 仕入情報
     * @return 取引回数
     */
    private String generatePurchaseCount(final TPurchaseEntity purchase) {
        final Integer purchaseCount = purchase.getPurchaseCount();
        if (Objects.isNull(purchaseCount)) {
            return "01";
        }
        // 引取回数 > 2桁の場合、エラー
        checkOverLength("purchase_count", purchaseCount.toString(), DIGIT_2, purchase);

        return StringUtils.toStringPadding0(purchaseCount, DIGIT_2);
    }

    /**
     * 金額関連項目のセット.
     * ・単価
     * ・金額
     * ・数量
     *
     * @param datModel DATファイル作成用Model
     * @param purchase 仕入情報
     */
    private void generatePriceItem(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TPurchaseEntity purchase) {
        //PRD_0200 JFE mod start
//        final BigDecimal unitPrice = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getPurchaseUnitPrice()));
//
//        // PRD_0160 mod JFE start
//        final BigDecimal fixArrivalCount;
//        final BigDecimal productPrice;
//        if (purchase.getPurchaseType() == PurchaseType.ADDITIONAL_PURCHASE_RED ||
//    			purchase.getPurchaseType() == PurchaseType.RETURN_PURCHASE_RED ||
//    			purchase.getPurchaseType() == PurchaseType.SHIPMENT_PURCHASE_LUMP) {
//        	fixArrivalCount = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getFixArrivalCount())).divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
//
//            productPrice = unitPrice.multiply(fixArrivalCount).setScale(0, RoundingMode.DOWN);
//        } else {
//        //final BigDecimal fixArrivalCount = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getFixArrivalCount()));
//        	fixArrivalCount = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getFixArrivalCount()));
//
//            productPrice = unitPrice.multiply(fixArrivalCount);
//        }
//        // PRD_0160 mod JFE end
//
//        // 数量 > 7桁の場合、桁数超過エラー
//        checkOverLength("fix_arrival_count", fixArrivalCount.toPlainString(), DIGIT_7, purchase);
//
//// Deleted Merge Process 2023/03/20 (Mon) by JFE
////        final BigDecimal productPrice = unitPrice.multiply(fixArrivalCount);
//// Deleted Merge Process 2023/03/20 (Mon) by JFE
//        // 金額 > 11桁の場合、桁数超過エラー
//        checkOverLength("product_price", productPrice.toPlainString(), DIGIT_11, purchase);
//
//        // 単価
//        datModel.setUnitPrice(StringUtils.toStringPadding0(unitPrice.intValue(), DIGIT_9));
//        // 金額(=単価×数量)
//        datModel.setProductPrice(StringUtils.toStringPaddingPlus0(productPrice, DIGIT_12));
//        // 数量
//        datModel.setFixArrivalCount(StringUtils.toStringPaddingPlus0(fixArrivalCount, DIGIT_8));
        final BigDecimal unitPrice = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getPurchaseUnitPrice()));

        // PRD_0160 mod JFE start
        final BigDecimal fixArrivalCount;
        final BigDecimal productPrice;
        final BigDecimal OriginalFabricMeter;//原反メータ数
        if (purchase.getPurchaseType() == PurchaseType.ADDITIONAL_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.SHIPMENT_PURCHASE_LUMP) {
            //生地の場合、数量は数量÷100を四捨五入した整数にする
            fixArrivalCount = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getFixArrivalCount())).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            //fixArrivalCount = fixArrivalCount.setScale(0, BigDecimal.ROUND_HALF_UP);//整数になるよう四捨五入
            productPrice = unitPrice.multiply(fixArrivalCount).setScale(0, RoundingMode.DOWN);

            // 原反メータ数は数量。
            OriginalFabricMeter = BigDecimal.valueOf(purchase.getFixArrivalCount());// 原反メータ数は数量。
            datModel.setOriginalFabricMeter(StringUtils.toStringPadding0(OriginalFabricMeter.intValue(), DIGIT_7)); //7文字になるまで0パディング

        } else {
        //final BigDecimal fixArrivalCount = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getFixArrivalCount()));
        	fixArrivalCount = BigDecimal.valueOf(NumberUtils.defaultInt(purchase.getFixArrivalCount()));

            productPrice = unitPrice.multiply(fixArrivalCount);

            datModel.setOriginalFabricMeter(STRING_SEVEN_ZERO);
        }
        // PRD_0160 mod JFE end

        // 数量 > 7桁の場合、桁数超過エラー
        checkOverLength("fix_arrival_count", fixArrivalCount.toPlainString(), DIGIT_7, purchase);

// Deleted Merge Process 2023/03/20 (Mon) by JFE
//        final BigDecimal productPrice = unitPrice.multiply(fixArrivalCount);
// Deleted Merge Process 2023/03/20 (Mon) by JFE
        // 金額 > 11桁の場合、桁数超過エラー
        checkOverLength("product_price", productPrice.toPlainString(), DIGIT_11, purchase);

        // 単価
        datModel.setUnitPrice(StringUtils.toStringPadding0(unitPrice.intValue(), DIGIT_9));
        // 金額(=単価×数量)
        datModel.setProductPrice(StringUtils.toStringPaddingPlus0(productPrice, DIGIT_12));
        // 数量
        datModel.setFixArrivalCount(StringUtils.toStringPaddingPlus0(fixArrivalCount, DIGIT_8));
        //PRD_0200 JFE mod end
    }

    /**
     * 「入荷場所」の変換.
     * ・入荷場所が2桁の場合、それをそのまま設定
     * ・入荷場所が1桁の場合、以下ルールに従って変換：
     *   - 1桁コードが1の場合は末尾が1、1以外の場合は末尾が3
     *
     * @param purchase 仕入情報
     * @return 入荷場所
     */
    private String generateArrivalPlace(final TPurchaseEntity purchase) {
        final String arrivalPlace = purchase.getArrivalPlace();
        // 1桁の場合：1の場合は末尾が1、1以外の場合は末尾が3
        if (arrivalPlace.length() == 1) {
            if (STRING_ONE.equals(arrivalPlace)) {
                return arrivalPlace + "1";
            } else {
                return arrivalPlace + "3";
            }
        }
        // 2桁の場合、そのまま設定
        return arrivalPlace;
    }

    /**
     * 「伝区」の変換.
     *
     * @param datModel DATファイル作成用Model
     * @param purchase 仕入情報
     * @return 伝区
     */
    public String generateVoucherCategory(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TPurchaseEntity purchase) {
        // 仕入先コードを基に仕入先マスタを取得。取得できない場合はエラー
        final MSirmstEntity sire = mSirmstRepository.findBySire(datModel.getSupplierCode())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                MessageCodeType.CODE_002, LogStringUtil.of("generateInfo")
                .message("sire data not found.")
                .value("purchaseId", purchase.getId())
                .build())));

        // 上1桁設定
        final String firstDigit = generateFirstDigitOfVoucherCategory(sire, purchase);
        // 下1桁設定
        final String lastDigit = generateLastDigitOfVoucherCategory(datModel);

        return firstDigit + lastDigit;
    }

    /**
     * 伝区の上1桁を返す.
     * ・仕入マスタ.仕入先区分＝00(社内振替)の場合
     *   - 仕入情報.仕入区分＝3(仕入返品)：6
     *   - 仕入情報.仕入区分≠3(仕入返品)：5
     * ・仕入マスタ.仕入先区分≠00(社内振替)の場合
     *   - 仕入情報.仕入区分＝3(仕入返品)：4
     *   - 仕入情報.仕入区分≠3(仕入返品)：3
     *
     * @param sire 仕入先マスタ情報
     * @param purchase 仕入情報
     * @return 伝区の上1桁
     */
    private String generateFirstDigitOfVoucherCategory(
            final MSirmstEntity sire,
            final TPurchaseEntity purchase) {
        if (STRING_TWO_ZERO.equals(sire.getSirkbn())) {
            if (PurchaseType.RETURN_PURCHASE.equals(purchase.getPurchaseType())) {
                return "6";
            } else {
                return "5";
            }
        } else {
            if (PurchaseType.RETURN_PURCHASE.equals(purchase.getPurchaseType())) {
                return "4";
            } else {
                return "3";
            }
        }
    }

    /**
     * 伝区の下1桁を返す.
     * ・費目＝00(発注情報.費目なし)：1
     * ・費目≠00：費目の下1桁
     *   例) 費目＝01：1
     *       費目＝04：4
     *       費目＝24：4
     *
     * @param datModel DATファイル作成用Model
     * @return 伝区の下1桁
     */
    private String generateLastDigitOfVoucherCategory(final AccountPurchaseConfirmFileLinkingDatModel datModel) {
        if (ExpenseItemType.NO_EXPENSE.getValue().equals(datModel.getExpenseItem())) {
            return "1";
        } else {
            return org.apache.commons.lang3.StringUtils.right(datModel.getExpenseItem(), DIGIT_1);
        }
    }

    /**
     * 発注情報と品番情報を基にデータセット.
     *
     * @param datModel DATファイル作成用Model
     * @param purchase 仕入情報
     */
    private void generateDataByOrderAndItem(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TPurchaseEntity purchase) {
        // 発注情報取得。取得できない場合はnull
        final TOrderEntity order = tOrderRepository.findByOrderId(purchase.getOrderId()).orElse(null);
        // 品番情報取得。取得できない場合はnull
        final TItemEntity item = tItemRepository.findByPartNo(purchase.getPartNo()).orElse(null);

        // 品番情報がない場合、エラー
        if (Objects.isNull(item)) {
            // 発注情報がある場合のエラーメッセージ
            String msg = "item data not found.";
            if (Objects.isNull(order)) {
                // 発注情報がない場合のエラーメッセージ
                msg = "order and item data not found.";
            }
            throw new ResourceNotFoundException(ResultMessages.warning().add(
                     MessageCodeType.CODE_002, LogStringUtil.of("generateInfo")
                     .message(msg)
                     .value("purchaseId", purchase.getId())
                     .build()));
        }

        // 品番情報を基にデータセット：
        // 部門
        datModel.setDeptCode(item.getDeptCode());
        // 仕入区分２
        datModel.setPurchaseType2(PsType.convertToValue(item.getPsType()));
        // 品番
        datModel.setPartNo(item.getPartNo());
        // 品名(カナ)
        datModel.setProductNameKana(generateProductNameKana(item));

        // 年度 ※NULLの場合は"0000"固定
        if (Objects.isNull(item.getYear())) {
            datModel.setYear(STRING_FOUR_ZERO);
        } else {
            datModel.setYear(item.getYear().toString());
        }

        // シーズン ※NULLの場合は半角スペース1桁セット
        if (Objects.isNull(item.getSeasonCode())) {
            datModel.setSeasonCode(HALF_WIDTH_SPACE);
        } else {
            datModel.setSeasonCode(item.getSeasonCode());
        }

        // 会社 コード
        datModel.setCompanyCode(item.getBrandSortCode());

        final boolean isOrderExist = !Objects.isNull(order);

        // 発注情報を基にデータセット：
        // PRD_0160 del JFE start
        // 仕入先コード
        //datModel.setSupplierCode(generateSupplierCode(order, item, isOrderExist));
        // 費目
        //datModel.setExpenseItem(generateExpenseItem(order, isOrderExist));
        // PRD_0160 del JFE end
        // 上代
        datModel.setRetailPrice(generateRetailPrice(order, item, isOrderExist));
        // 製造原価 関連項目
        generateManufacturingCost(datModel, order, item, purchase, isOrderExist);
        // PRD_0160 del JFE start
        // 会社 ＩＦ区分
        //datModel.setIfType(generateIfType(order, isOrderExist));
        // PRD_0160 del JFE end

        // PRD_0160 mod JFE start
        // 仕入区分 = 2,4,5 の場合
    	if (purchase.getPurchaseType() == PurchaseType.ADDITIONAL_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.RETURN_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.SHIPMENT_PURCHASE_LUMP) {
    		// 仕入先コード：仕入情報「仕入先」
        	if (Objects.isNull(purchase.getSupplierCode())) {
        		datModel.setSupplierCode(STRING_FIVE_ZERO);
        	} else {
//PRD_0197        		datModel.setSupplierCode(purchase.getSupplierCode());
        		//PRD_0203 mod jfe start
//        	    datModel.setSupplierCode(String.format("%5s",purchase.getSupplierCode().replace(" ", "0")));
        	    datModel.setSupplierCode(paddingSpaceRight(purchase.getSupplierCode(),DIGIT_5));
        	    //PRD_0203 mod jfe end
        	}
        	// 費目：仕入情報「課コード」
        	if (Objects.isNull(purchase.getDivisionCode())) {
        		datModel.setExpenseItem(STRING_TWO_ZERO);
        	} else {
//PRD_0197        		datModel.setExpenseItem(purchase.getDivisionCode());
        		//PRD_0203 mod jfe start
//        	    datModel.setExpenseItem(String.format("%2s",purchase.getDivisionCode().replace(" ", "0")));
        		datModel.setExpenseItem(paddingSpaceRight(purchase.getDivisionCode(),DIGIT_2));
        	    //PRD_0203 mod jfe end
        	}
    		// 会社 ＩＦ区分："5"固定
    		datModel.setIfType("5");
    	} else {
    		 // 仕入先コード
            datModel.setSupplierCode(generateSupplierCode(order, item, isOrderExist));
            // 費目
            datModel.setExpenseItem(generateExpenseItem(order, isOrderExist));
        // 会社 ＩＦ区分
        datModel.setIfType(generateIfType(order, isOrderExist));
    }
        // PRD_0160 mod JFE end
    }

    /**
     * 製造原価関連項目のセット.
     * 発注情報がある場合は発注情報の値をセット
     * 発注情報がない場合は品番情報の値をセット
     * ※nullの場合は0に変換
     * ・製造原価 生地
     * ・製造原価 工賃
     * ・製造原価 附属品
     * ・製造原価 その他
     * ・製造原価 合計(=生地原価+加工賃+付属品+その他原価)
     *
     * @param datModel DATファイル作成用Model
     * @param order 発注情報
     * @param item 品番情報
     * @param purchase 仕入情報
     * @param isOrderExist 発注情報存在フラグ
     */
    private void generateManufacturingCost(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TOrderEntity order,
            final TItemEntity item,
            final TPurchaseEntity purchase,
            final boolean isOrderExist) {
        BigDecimal matlCost;
        BigDecimal processingCost;
        BigDecimal accessoriesCost;
        BigDecimal otherCost;
        if (isOrderExist) {
            matlCost = NumberUtils.defaultInt(order.getMatlCost()).setScale(0, RoundingMode.DOWN); // 小数第1位で切り捨て
            processingCost = NumberUtils.defaultInt(order.getProcessingCost());
            accessoriesCost = NumberUtils.defaultInt(order.getAttachedCost());
            otherCost = NumberUtils.defaultInt(order.getOtherCost());
        } else {
            matlCost = NumberUtils.defaultInt(item.getMatlCost());
            processingCost = NumberUtils.defaultInt(item.getProcessingCost());
            accessoriesCost = NumberUtils.defaultInt(item.getAccessoriesCost());
            otherCost = NumberUtils.defaultInt(item.getOtherCost());
        }

        final BigDecimal manufacturingCostSum = matlCost.add(processingCost).add(accessoriesCost).add(otherCost);

        // 製造原価 合計 > 9桁の場合、エラー
        checkOverLength("manufacturing_cost_sum", manufacturingCostSum.toPlainString(), DIGIT_9, purchase);

        // 製造原価 生地
        datModel.setMatlCost(StringUtils.toStringPaddingPlus0(matlCost, DIGIT_10));
        // 製造原価 工賃
        datModel.setProcessingCost(StringUtils.toStringPaddingPlus0(processingCost, DIGIT_10));
        // 製造原価 附属品
        datModel.setAccessoriesCost(StringUtils.toStringPaddingPlus0(accessoriesCost, DIGIT_10));
        // 製造原価 その他
        datModel.setOtherCost(StringUtils.toStringPaddingPlus0(otherCost, DIGIT_10));
        // 製造原価 合計
        datModel.setManufacturingCostSum(StringUtils.toStringPaddingPlus0(manufacturingCostSum, DIGIT_10));
    }

    /**
     * 「仕入先コード」の変換.
     * ・発注情報あり：
     *   - 発注情報.生産メーカー
     * ・発注情報なし：
     *   - 品番情報.発注先メーカーID(最新製品)に紐づく発注先メーカー情報のメーカーコード
     *
     * @param order 発注情報
     * @param item 品番情報
     * @param isOrderExist 発注情報存在フラグ
     * @return 仕入先コード
     */
    private String generateSupplierCode(
            final TOrderEntity order,
            final TItemEntity item,
            final boolean isOrderExist) {
        if (isOrderExist) {
            return order.getMdfMakerCode();

        } else {
            // 品番情報.発注先メーカーID(最新製品)に紐づく発注先メーカー情報取得。取得できない場合はエラー
            final TOrderSupplierEntity orderSupplier = tOrderSupplierRepository.findById(
                    item.getCurrentProductOrderSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("generateInfo")
                            .message("order_supplier data not found.")
                            .value("partNoId", item.getId())
                            .build())));
            return orderSupplier.getSupplierCode();
        }
    }

    /**
     * 「費目」の変換.
     * ・発注情報あり：発注情報.費目
     * ・発注情報なし：00(費目なし)
     *
     * @param order 発注情報
     * @param isOrderExist 発注情報存在フラグ
     * @return 費目
     */
    private String generateExpenseItem(final TOrderEntity order, final boolean isOrderExist) {
        if (isOrderExist) {
            return ExpenseItemType.convertToValue(order.getExpenseItem());
        } else {
            return ExpenseItemType.NO_EXPENSE.getValue();
        }
    }

    /**
     * 「上代」の変換.
     * ・発注情報あり：
     *   - 発注情報.費目=01：発注情報.上代
     *   - 発注情報.費目=01以外：000000000
     * ・発注情報なし：
     *   - 品番情報.上代
     *
     * @param order 発注情報
     * @param item 品番情報
     * @param isOrderExist 発注情報存在フラグ
     * @return 上代
     */
    private String generateRetailPrice(
            final TOrderEntity order,
            final TItemEntity item,
            final boolean isOrderExist) {
        if (isOrderExist) {
            if (ExpenseItemType.PRODUCT_ORDER.equals(order.getExpenseItem())) {
                return StringUtils.toStringPadding0(order.getRetailPrice().intValue(), DIGIT_9);
            } else {
                return StringUtils.toStringPadding0(BigInteger.ZERO, DIGIT_9);
            }

        } else {
            return StringUtils.toStringPadding0(item.getRetailPrice().intValue(), DIGIT_9);
        }
    }

    /**
     * 「会社 ＩＦ区分」の変換.
     * ・発注情報.費目 = 01 or 発注情報無し
     *     1:通常
     * ・発注情報.費目 <> 01
     *     5:その他仕入
     *
     * @param order 発注情報
     * @param isOrderExist 発注情報存在フラグ
     * @return 会社 ＩＦ区分
     */
    private String generateIfType(final TOrderEntity order, final boolean isOrderExist) {
        if (!isOrderExist || ExpenseItemType.PRODUCT_ORDER.equals(order.getExpenseItem())) {
            return IfCategoryType.NORMAL.getValue();
        } else {
            return IfCategoryType.OTHER_PURCHASE.getValue();
        }
    }

    /**
     * 「品名(カナ)」の変換.
     * ※NULLの場合は20桁まで半角スペース埋め
     * ・全角カタカナを半角カタカナに変換する
     * ※半角カタカナ変換後、全角文字が含まれている場合はエラー
     * ・文字数 <= 20の場合、20桁まで末尾に半角スペース追加
     *   文字数 > 20の場合、21桁以降切り捨て
     *
     * @param item 品番情報
     * @return 品名(カナ)
     */
    private String generateProductNameKana(final TItemEntity item) {
        if (Objects.isNull(item.getProductNameKana())) {
            return StringUtils.paddingSpaceRight(HALF_WIDTH_SPACE, DIGIT_20);
        }

        // 全角カタカナを半角カタカナに変換
        final String halfKana = convertComponent.convertToHalfKana(item.getProductNameKana());
        // 全角チェック
        if (StringUtils.isContainFullWidth(halfKana)) {
            throw new BusinessException(ResultMessages.warning().add(
                    MessageCodeType.SYSTEM_ERROR, LogStringUtil.of("generateInfo")
                    .message("product_name_kana contains full width after convert .")
                    .value("partNoId", item.getId())
                    .build()));
        }

        // 文字数チェック
        if (halfKana.length() > DIGIT_20) {
            return StringUtils.substring(halfKana, DIGIT_21);
        } else {
            return StringUtils.paddingSpaceRight(halfKana, DIGIT_20);
        }
    }

    /**
     * その他データセット.
     * ・納品依頼No
     * ・伝区
     * ・データ固有 日付
     * ・データ固有 区分
     *
     * @param datModel DATファイル作成用Model
     * @param purchase 仕入情報
     * @param nitymdStr 日計日
     */
    private void generateOtherData(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TPurchaseEntity purchase,
            final String nitymdStr) {
    	// PRD_0160 mod JFE start
    	// 仕入区分 = 2,4,5 の場合
    	if (purchase.getPurchaseType() == PurchaseType.ADDITIONAL_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.RETURN_PURCHASE_RED ||
    			purchase.getPurchaseType() == PurchaseType.SHIPMENT_PURCHASE_LUMP) {
    		// 納品依頼No：仕入情報「仕入伝票No」
        	if (Objects.isNull(purchase.getPurchaseVoucherNumber())) {
        		datModel.setDeliveryRequestNumber(STRING_SIX_ZERO);
        	} else {
//PRD_0197        		datModel.setDeliveryRequestNumber(purchase.getPurchaseVoucherNumber());
        		//PRD_0203 jfe mod start
//        	    datModel.setDeliveryRequestNumber(String.format("%6s",purchase.getPurchaseVoucherNumber().replace(" ", "0")));
        		datModel.setDeliveryRequestNumber(paddingSpaceRight(purchase.getPurchaseVoucherNumber(),DIGIT_6));
        	    //PRD_0203 jfe mod start
        	}
        	// 伝区：仕入情報「引取回数」
        	if (Objects.isNull(purchase.getPurchaseCount())) {
        		datModel.setVoucherCategory(STRING_TWO_ZERO);
        	} else {
        		datModel.setVoucherCategory(StringUtils.toStringPadding0(purchase.getPurchaseCount(), DIGIT_2));
        	}
    	} else {
        // 納品依頼No
        datModel.setDeliveryRequestNumber(generateDeliveryRequestNumber(purchase));
        // 伝区 ※datModelの費目が設定されてからセット
        datModel.setVoucherCategory(generateVoucherCategory(datModel, purchase));
    	}
    	// PRD_0160 mod JFE end
        // データ固有 日付
        datModel.setDataSpecificDate(nitymdStr);
        // データ固有 区分 ※datModelの場所（発生）が設定されてからセット
        datModel.setDataSpecificType(generateDataSpecificType(datModel));
    }

    /**
     * 「納品依頼No」の変換.
     * ・納品IDがある場合
     *     納品SKU情報.納品依頼No
     * ・納品IDがない場合
     *     "000000"固定
     *
     * @param purchase 仕入情報
     * @return 納品依頼No
     */
    private String generateDeliveryRequestNumber(final TPurchaseEntity purchase) {
        if (Objects.isNull(purchase.getDeliveryId())) {
            return STRING_SIX_ZERO;
        }

        // 納品SKU情報取得。取得できない場合はエラー
        final TDeliverySkuEntity deliverySku = tDeliverySkuRepository.findByPurchaseInfo(
                purchase.getDeliveryId(),
                purchase.getPurchaseCount(),
                purchase.getDivisionCode(),
                purchase.getColorCode(),
                purchase.getSize())
                .orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("generateInfo")
                        .message("deliverySku data not found.")
                        .value("purchaseId", purchase.getId())
                        .build())));
        return deliverySku.getDeliveryRequestNumber();
    }

    /**
     * 「データ固有 区分」の変換.
     * ・場所（発生）= 01～29
     *     1:本部
     * ・場所（発生）= 30～99
     *     2:ディスタ
     *
     * @param datModel DATファイル作成用Model
     * @return データ固有 区分
     */
    private String generateDataSpecificType(final AccountPurchaseConfirmFileLinkingDatModel datModel) {
        final Range<String> rng = Range.between("01", "29");
        if (rng.contains(datModel.getArrivalPlaceOccur())) {
            return "1";
        } else {
            return "2";
        }
    }

    /**
     * 固定値データセット.
     *
     * @param datModel DATファイル作成用Model
     */
    private void generateFixedData(final AccountPurchaseConfirmFileLinkingDatModel datModel) {
        // データ種別：SR
        datModel.setDataType(BusinessType.PURCHASE_INSTRUCTION.getValue());
        // 場所（自）
        datModel.setArrivalPlaceFrom(STRING_TWO_ZERO);
        // 配分コード グループ
        datModel.setAllocationCodeGroup(STRING_ONE_ZERO);
        // 配分コード 順位
        datModel.setAllocationCodeRank(STRING_TWO_ZERO);
        // 入出荷区分
        datModel.setReceiptAndShipmentType(STRING_ONE);
        // 伝種
        datModel.setVoucherVariety(STRING_TWO_ZERO);
        // 訂正区分
        datModel.setCorrectType(STRING_ONE_ZERO);
        // 製品区分
        datModel.setProductType(STRING_ONE_ZERO);
        // 共通区分１
        datModel.setCommonType1(STRING_ONE_ZERO);
        // 共通区分２
        datModel.setCommonType2(STRING_ONE_ZERO);
        // 完納区分
        datModel.setCompleteType(STRING_ONE_ZERO);
        // セット品区分
        datModel.setSetProductType(STRING_ONE_ZERO);
        // Ｂ級品区分
        datModel.setNonConformingProductType(STRING_ONE_ZERO);
        // 仕入区分１
        datModel.setPurchaseType1(STRING_ONE_ZERO);
        // 区分１
        datModel.setType1(STRING_ONE_ZERO);
        // 区分2
        datModel.setType2(STRING_ONE_ZERO);
        // 送り状No
        datModel.setInvoiceNumber(STRING_EIGHT_ZERO);
        // 運送会社コード
        datModel.setShippingCompanyCode(STRING_THREE_ZERO);
        // 個数口
        datModel.setPiece(STRING_PLUS_THREE_ZERO);
        // ハンガー数
        datModel.setHangerAmount(STRING_PLUS_THREE_ZERO);
        // サイズ区分
        datModel.setSizeType(STRING_TWO_ZERO);
        // サイズ別数１
        datModel.setSize1(STRING_PLUS_FIVE_ZERO);
        // サイズ別数２
        datModel.setSize2(STRING_PLUS_FIVE_ZERO);
        // サイズ別数３
        datModel.setSize3(STRING_PLUS_FIVE_ZERO);
        // サイズ別数４
        datModel.setSize4(STRING_PLUS_FIVE_ZERO);
        // サイズ別数５
        datModel.setSize5(STRING_PLUS_FIVE_ZERO);
        // サイズ別数６
        datModel.setSize6(STRING_PLUS_FIVE_ZERO);
        // サイズ別数７
        datModel.setSize7(STRING_PLUS_FIVE_ZERO);
        // サイズ別数８
        datModel.setSize8(STRING_PLUS_FIVE_ZERO);
        // PRD_0160 del JFE start
        // 原反 反番号：半角スペース6桁
        //datModel.setOriginalFabricClothNumber("      ");
        // PRD_0160 del JFE end
        // 原反 反数
        datModel.setOriginalFabricClothCount(STRING_THREE_ZERO);
        //PRD_0200 JFE del start
        // 原反 メーター数
//        datModel.setOriginalFabricMeter(STRING_SEVEN_ZERO);
        //PRD_0200 JFE del start
        // ファイル識別
        datModel.setFileIdentification(STRING_ONE);
        // 予備(13桁)：半角スペース13桁
        datModel.setReserve13Digit("             ");
        // 管理情報 メンテ区分
        datModel.setManageMntflg(STRING_ONE);
        // 管理情報 担当者
        datModel.setManageStaff(STRING_SIX_ZERO);
        // 管理情報 WSNo：半角スペース5桁
        datModel.setManageWsNumber("     ");
        // 管理情報 時間
        datModel.setManageAt(STRING_SIX_ZERO);
        // 管理情報 プログラム名：半角スペース5桁
        datModel.setManageProgramName("     ");
        // 管理情報 媒体：P
        datModel.setManageMedium("P");
    }

    /**
     * NULL項目の整形.
     *・文字列：NULLの場合は指定桁数まで半角スペース埋め
     *・数値：NULLの場合は指定桁数まで0埋め
     *
     * @param datModel DATファイル作成用Model
     */
    private void formatEmptyData(final AccountPurchaseConfirmFileLinkingDatModel datModel) {
        // 入荷伝票No.
        final String sqManageNumber = StringUtils.paddingZeroIfEmpty(datModel.getSqManageNumber(), DIGIT_6);
//PRD_0197        datModel.setSqManageNumber(sqManageNumber);
        //PRD_0203 jfe mod start
//        datModel.setSqManageNumber(String.format("%6s",sqManageNumber).replace(" ", "0"));
        datModel.setSqManageNumber(paddingSpaceRight(sqManageNumber,DIGIT_6));
        //PRD_0203 jfe mod end

        // 部門
        final String deptCode = StringUtils.paddingZeroIfEmpty(datModel.getDeptCode(), DIGIT_4);
//PRD_0197        datModel.setDeptCode(deptCode);
        //PRD_0203 jfe mod start
//        datModel.setDeptCode(String.format("%4s",deptCode).replace(" ", "0"));
        datModel.setDeptCode(paddingSpaceRight(deptCode,DIGIT_4));
        //PRD_0203 jfe mod end

        // 配分コード 課
        final String divisionCode = StringUtils.paddingZeroIfEmpty(datModel.getDivisionCode(), DIGIT_2);
//PRD_0197        datModel.setDivisionCode(divisionCode);
      	//PRD_0203 jfe mod start
//        datModel.setDivisionCode(String.format("%2s",divisionCode).replace(" ", "0"));
        datModel.setDivisionCode(paddingSpaceRight(divisionCode,DIGIT_2));
        //PRD_0203 jfe mod end

        // 仕入区分２ ※NULLの場合は半角スペース1桁をセット
        if (Objects.isNull(datModel.getPurchaseType2())) {
            datModel.setPurchaseType2(HALF_WIDTH_SPACE);
        }

        // 費目
        final String expenseItem = StringUtils.paddingZeroIfEmpty(datModel.getExpenseItem(), DIGIT_2);
//PRD_0197        datModel.setExpenseItem(expenseItem);
        //PRD_0203 jfe mod start
//        datModel.setExpenseItem(String.format("%2s",expenseItem).replace(" ", "0"));;
        datModel.setExpenseItem(paddingSpaceRight(expenseItem,DIGIT_2));
        //PRD_0203 jfe mod end
        // 仕入伝票No.
        final String purchaseVoucherNumber = StringUtils.paddingZeroIfEmpty(datModel.getPurchaseVoucherNumber(), DIGIT_6);
//PRD_0197        datModel.setPurchaseVoucherNumber(purchaseVoucherNumber);
        //PRD_0203 jfe mod start
//        datModel.setPurchaseVoucherNumber(String.format("%6s",purchaseVoucherNumber).replace(" ", "0"));
        datModel.setPurchaseVoucherNumber(paddingSpaceRight(purchaseVoucherNumber,DIGIT_6));
        //PRD_0203 jfe mod end

        // カラー
        //PRD_0197 JFE mod start
//        final String color = StringUtils.paddingZeroIfEmpty(datModel.getColorCode(), DIGIT_2);
        String color = datModel.getColorCode();
        if(color == null) color = "";
        if(color.equals("")) color = STRING_TWO_ZERO;
        //PRD_0197 JFE mod end
        datModel.setColorCode(color);

        // 会社コード
        final String company = StringUtils.paddingZeroIfEmpty(datModel.getCompanyCode(), DIGIT_2);
//PRD_0197        datModel.setCompanyCode(company);
        //PRD_0203 jfe mod start
//        datModel.setCompanyCode(String.format("%2s",company).replace(" ", "0"));
        datModel.setCompanyCode(paddingSpaceRight(company,DIGIT_2));
        //PRD_0203 jfe mod end
    }

    /**
     * 採番用項目データセット.
     * ※全データ変換・設定後にセット
     *
     * @param datModel DATファイル作成用Model
     * @param purchase 仕入情報
     */
    private void generateNumberingData(
            final AccountPurchaseConfirmFileLinkingDatModel datModel,
            final TPurchaseEntity purchase) {
        // 採番用_仕入データ作成日
        // ※NULLの場合は"00000000"固定
        if (Objects.isNull(purchase.getSqManageDate())) {
            datModel.setNumSqManageDate(STRING_EIGHT_ZERO);
        } else {
            datModel.setNumSqManageDate(DateUtils.formatFromDate(purchase.getSqManageDate(), DATE_FORMAT_YYYY_MM_DD));
        }

        // 採番用_部門：前2桁
//PRD_0197        datModel.setNumDeptCode(StringUtils.substring(datModel.getDeptCode(), DIGIT_3));
        //PRD_0203 jfe mod start
//        datModel.setNumDeptCode(StringUtils.substring(String.format("%3s",datModel.getDeptCode().replace(" ", "0")), DIGIT_3));
        datModel.setNumDeptCode(StringUtils.substring(paddingSpaceRight(datModel.getDeptCode(),DIGIT_3), DIGIT_3));
        //PRD_0203 jfe mod end
        // 採番用_入荷場所
        datModel.setNumArrivalPlace(purchase.getArrivalPlace());
    }

    /**
     * 桁数超過チェック.
     * 指定桁数を超過した場合はエラー
     *
     * @param item 項目名
     * @param value チェック対象文字列
     * @param length 指定桁数
     * @param purchase 仕入情報
     */
    private void checkOverLength(
            final String item,
            final String value,
            final int length,
            final TPurchaseEntity purchase) {
        if (value.length() > length) {
            // 指定桁数以上の場合、桁数超過エラー
            throw new BusinessException(ResultMessages.warning().add(
                    MessageCodeType.SYSTEM_ERROR, LogStringUtil.of("generateInfo")
                    .message(item + " length over.")
                    .value("purchaseId", purchase.getId())
                    .build()));
        }
    }

    /**
     * 業務区分を返す.
     *
     * @return 業務区分
     */
    private BusinessType getBusinessType() {
        return BusinessType.ACCOUNT_PURCHASE_CONFIRM;
    }

    /**
     * DATファイルを作成する.
     *
     * @param datModels DATファイル作成用Modelリスト
     * @param recordAt 計上日
     * @return ファイル書込情報
     * @throws Exception 例外
     */
    private File createInstructionFile(
            final List<AccountPurchaseConfirmFileLinkingDatModel> datModels,
            final Date recordAt) throws Exception {
        return createDatFileComponent.createDatFile(datModels, recordAt);
    }

    /**
     * ファイルをS3へアップロード.
     *
     * @param instructionFile ファイル
     * @param s3Prefix S3プレフィックス
     * @return S3キー
     * @throws Exception 例外
     */
    private String uploadFileToS3(
            final File instructionFile,
            final String s3Prefix) throws Exception {
        final String s3Key = s3Component.upload(instructionFile, s3Prefix, CONTENT_TYPE);
        return s3Key;
    }
}
