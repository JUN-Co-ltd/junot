package jp.co.jun.edi.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.purchaseItem.PurchaeItemHeadRecordXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaeItemHeadXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaeItemSectionXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaseItemDetailXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaseItemDetailsXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaseItemHeadXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaseItemStockXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaseItemTotalRecordXmlModel;
import jp.co.jun.edi.component.model.purchaseItem.PurchaseItemXmlModel;
import jp.co.jun.edi.entity.extended.ExtendedTPurchaseHeadPDFEntity;
import jp.co.jun.edi.entity.extended.ExtendedTPurchasePDFEntity;
import jp.co.jun.edi.entity.extended.ExtendedTPurchaseStockPDFEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.extended.ExtendedTPurchaseHeadPDFRepository;
import jp.co.jun.edi.repository.extended.ExtendedTPurchasePDFRepository;
import jp.co.jun.edi.repository.extended.ExtendedTPurchaseStockPDFRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.StringUtils;

//PRD_0134 #10654 add JEF start
/**
 * xmlを作成するコンポーネント.
 */
@Component
public class PurchaseItemCreateXmlComponent {
    /** 全済区分:完納. */
    private static final String ALL_COMPLETION_TYPE_NAME_NORMAL = "完納";

    /** 全済区分:未完納. */
    private static final String ALL_COMPLETION_TYPE_NAME_OTHER = "未完納";

    /** 6文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_6 = 6;

    /** 21文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_21 = 21;

    /** 101文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_101 = 101;

    /** 1ページに表示可能なカラーの最大値. */
    private static final int COLOR_DISPLAY_MAX_SIZE = 8;

    /** 1ページに表示可能なサイズの最大値. */
    private static final int SIZE_DISPLAY_MAX_SIZE = 15;

    @Autowired
    private ExtendedTPurchaseHeadPDFRepository extendedTPurchaseHeadPDFRepository;

    @Autowired
    private ExtendedTPurchasePDFRepository extendedTPurchasePDFRepository;

    @Autowired
    private ExtendedTPurchaseStockPDFRepository extendedTPurchaseStockPDFRepository;

    /**
     * XMLデータファイルを生成する.
     * @param purchaseVoucherNumber 伝票番号
     * @param orderId 発注ID
     * @param xmlPath XMLファイルパス
     * @return
     * @throws Exception 例外
     * @throws IOException 例外
     */
    public boolean createXml(final String purchaseVoucherNumber, final BigInteger orderId, final Date createdAt, final Path xmlPath) throws Exception, IOException {

        // PDF作成用の仕入明細(HEAD用)を取得する
        final ExtendedTPurchaseHeadPDFEntity purchaseHeadInfo = getHeadPdfInfo(purchaseVoucherNumber, orderId, createdAt).orElseThrow(() -> new ResourceNotFoundException(
                ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                .message("t_purchase not found.")
                                .value("purchase_voucher_number", purchaseVoucherNumber)
                                .build())));

        // PDF作成用の入荷情報を取得する
        final ExtendedTPurchaseStockPDFEntity purchaseStockInfo = getStockPdfInfo(purchaseVoucherNumber, orderId).orElseThrow(() -> new ResourceNotFoundException(
                ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                .message("t_purchase not found.")
                                .value("purchase_voucher_number", purchaseVoucherNumber)
                                .build())));

        // PDF作成用の仕入情報を取得する
        final List<ExtendedTPurchasePDFEntity> purchaseList = getMakerPurchaseInfo(purchaseVoucherNumber, orderId).getContent();
        if (purchaseList.isEmpty()) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                    .message("t_purchase not found.")
                                    .value("voucher_number", purchaseVoucherNumber)
                                    .build()));
        }

        // XMLデータを生成
        final String purchaseItemXml = genaratedXmlData(purchaseHeadInfo, purchaseStockInfo, purchaseList);

        // XMLファイルを一時ディレクトリへ出力
        try (BufferedWriter writer = Files.newBufferedWriter(xmlPath)) {
            writer.append(purchaseItemXml);
        }

        String purchaseType = purchaseHeadInfo.getPurchaseType();
        String arrivalPlace = purchaseHeadInfo.getArrivalPlace();
        if(((purchaseType.equals("9")) && (arrivalPlace.equals("19"))) || ((purchaseType.equals("3")) && (arrivalPlace.equals("19"))) ) {
        	return false;
        }else {
        	return true;
        }


    }

    /**
     *  PDF作成用情報取得.
     *  @param voucherNumber 伝票番号
     *  @param orderId 発注ID
     *  @param createdAt 返金日
     *  @return PDF作成情報
     */
    private Optional<ExtendedTPurchaseHeadPDFEntity> getHeadPdfInfo(final String purchaseVoucherNumber, final BigInteger orderId, final Date createdAt) {
        // PRD_0179 #10654 mod JEF start
        //return extendedTPurchaseHeadPDFRepository.getPurchaseHeadInfo(purchaseVoucherNumber, orderId);
        return extendedTPurchaseHeadPDFRepository.getPurchaseHeadInfo(purchaseVoucherNumber, orderId, createdAt);
        // PRD_0179 #10654 mod JEF end
    }

    /**
     *  PDF作成用情報取得.
     *  @param purchaseVoucherNumber 伝票番号
     *  @param orderId 発注ID
     *  @return PDF作成情報
     */
    private Optional<ExtendedTPurchaseStockPDFEntity> getStockPdfInfo(final String purchaseVoucherNumber, final BigInteger orderId) {
        return extendedTPurchaseStockPDFRepository.getPurchaseStockInfo(purchaseVoucherNumber, orderId);
    }

    /**
     *  仕入情報を取得.
     *  @param vouNum 伝票番号
     *  @param orderId 発注ID
     *  @return 仕入情報
     */
    private Page<ExtendedTPurchasePDFEntity> getMakerPurchaseInfo(final String purchaseVoucherNumber, final BigInteger orderId) {
        return extendedTPurchasePDFRepository.getPurchaseInfo(purchaseVoucherNumber, orderId, PageRequest.of(0, Integer.MAX_VALUE));
    }

    /**
     * XMLデータ生成.
     * @param purchaseHeadInfo PDF作成用の仕入明細(HEAD用)
     * @param purchaseStockInfo PDF作成用の仕入明細(STOCK用)
     * @param purchaseList 仕入情報
     * @return XMLデータ
     */
    private String genaratedXmlData(final ExtendedTPurchaseHeadPDFEntity purchaseHeadInfo,
            final ExtendedTPurchaseStockPDFEntity purchaseStockInfo,
            final List<ExtendedTPurchasePDFEntity> purchaseList) {

        final PurchaseItemXmlModel purchaseItemXmlModel = new PurchaseItemXmlModel();

        // 仕入情報をカラーコード、サイズごとにグルーピングする
        final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchase = purchaseList.stream()
                .collect(Collectors.groupingBy(o -> o.getColorCode(), Collectors.toMap(o -> o.getSize(), o -> o)));

        // サイズリストを取得する
        final List<String> listSize = getUniqueSizeList(purchaseList);

        // 詳細情報
        purchaseItemXmlModel.setPageDetails(genaratedPageDatails(mapColorCodePurchase, listSize));

        // ヘッダー情報
        purchaseItemXmlModel.setPageHead(genaratedpurchaseItemPageHeadXmlModel(purchaseHeadInfo));

        // ページ入荷情報部
        purchaseItemXmlModel.setPageStock(genaratedpurchaseItemPageStackXmlModel(purchaseStockInfo));

        // ModelをXMLデータに変換
        final StringWriter purchaseItemXml = new StringWriter();
        JAXB.marshal(purchaseItemXmlModel, purchaseItemXml);


        return purchaseItemXml.toString();

    }

    /**
     * OrderPageDetailsXmlModel生成.
     * @param mapColorCodePurchase カラーコード、サイズごとにグルーピングした仕入情報
     * @param listSize サイズ一覧
     * @return OrderPageDetailsXmlModelリスト
     */
    private List<PurchaseItemDetailXmlModel> genaratedPageDatails(final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchase,
            final List<String> listSize) {

        // ページごとのカラーリストを生成
        final List<List<String>> colorByPageList = CollectionUtils.chunk(
                mapColorCodePurchase.keySet().stream()
                        // カラーコードでソートする
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toList()),
                COLOR_DISPLAY_MAX_SIZE);

        // ページごとのサイズリストを生成
        final List<List<String>> sizeByPageList = CollectionUtils.chunk(listSize, SIZE_DISPLAY_MAX_SIZE);

        final List<PurchaseItemDetailXmlModel> purchaseItemDetailsXmlModelList = new ArrayList<>();

        // ページ番号
        int pageNumber = 1;

        for (final List<String> colorList : colorByPageList) {
            for (final List<String> sizeList : sizeByPageList) {
                final PurchaseItemDetailXmlModel purchaseItemDetailsXmlModel = new PurchaseItemDetailXmlModel();
                purchaseItemDetailsXmlModel.setHead(genaratedPurchaeItemHeadXmlModel(sizeList));
                purchaseItemDetailsXmlModel.setRecordSection(genaratedPurchaseItemSectionXmlModel(
                        mapColorCodePurchase,
                        colorList,
                        sizeList));
                purchaseItemDetailsXmlModel.setPageNumber(pageNumber);
                pageNumber++;

                purchaseItemDetailsXmlModelList.add(purchaseItemDetailsXmlModel);
            }
        }

        return purchaseItemDetailsXmlModelList;
    }

    /**
     * ソート順でソートした重複しないサイズコードリストを取得.
     * @param purchaseList 仕入情報情報
     * @return サイズコードリスト
     */
    private List<String> getUniqueSizeList(final List<ExtendedTPurchasePDFEntity> purchaseList) {
        return purchaseList.stream().sorted(
                // ソート順の昇順でソートする。サイズマスタには必ずソート順はあるが、存在しない場合も考慮。
                Comparator.comparing(ExtendedTPurchasePDFEntity::getSortOrder, Comparator.nullsFirst(Comparator.naturalOrder()))
                        // ソート順がnullの場合は、サイズの昇順でソートする
                        .thenComparing(Comparator.comparing(ExtendedTPurchasePDFEntity::getSize, Comparator.nullsFirst(Comparator.naturalOrder()))))
                .map(makerPurchase -> makerPurchase.getSize())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * PurchaeItemHeadXmlModel生成.
     * @param listSize サイズリスト
     * @return PurchaeItemHeadXmlModel
     */
    private PurchaeItemHeadXmlModel genaratedPurchaeItemHeadXmlModel(final List<String> listSize) {
        final PurchaeItemHeadXmlModel purchaeItemHeadXmlModel = new PurchaeItemHeadXmlModel();
        purchaeItemHeadXmlModel.setHeadRecord(genaratedPurchaeItemHeadRecordXmlModel(listSize));
        // 全てのページには合計を表示する
        purchaeItemHeadXmlModel.setSubtotal("");
        purchaeItemHeadXmlModel.setTotalAmount("合計");
        return purchaeItemHeadXmlModel;
    }

    /**
     * PurchaeItemHeadRecordXmlModel生成.
     * @param listSize サイズリスト
     * @return PurchaeItemHeadRecordXmlModel
     */
    private PurchaeItemHeadRecordXmlModel genaratedPurchaeItemHeadRecordXmlModel(final List<String> listSize) {
        final PurchaeItemHeadRecordXmlModel purchaeItemHeadRecordXmlModel = new PurchaeItemHeadRecordXmlModel();
        purchaeItemHeadRecordXmlModel.setSize(listSize);
        return purchaeItemHeadRecordXmlModel;
    }

    /**
     * PurchaeItemSectionXmlModel生成.
     * @param mapColorCodePurchaseMaker カラーコード、サイズごとにグルーピングしたメーカ仕入情報
     * @param colorList カラーリスト
     * @param sizeList サイズリスト
     * @return PurchaeItemSectionXmlModel
     */
    private PurchaeItemSectionXmlModel genaratedPurchaseItemSectionXmlModel(
            final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchaseMaker,
            final List<String> colorList,
            final List<String> sizeList) {
        final PurchaeItemSectionXmlModel purchaseItemSectionXmlModel = new PurchaeItemSectionXmlModel();
        purchaseItemSectionXmlModel.setDetails(genaratedPurchaseItemDetailsXmlModel(
                mapColorCodePurchaseMaker,
                colorList,
                sizeList));

        purchaseItemSectionXmlModel.setTotalRecord(genaratedPurchaseItemTotalRecordXmlModel(
                mapColorCodePurchaseMaker,
                sizeList));

        return purchaseItemSectionXmlModel;
    }

    /**
     * PurchaseItemDetailsXmlModel生成.
     * @param mapColorCodePurchaseMaker カラーコード、サイズごとにグルーピングしたメーカ仕入情報
     * @param colorList カラーリスト
     * @param sizeList サイズリスト
     * @return PurchaseItemDetailsXmlModelリスト
     */
    private List<PurchaseItemDetailsXmlModel> genaratedPurchaseItemDetailsXmlModel(
            final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchaseMaker,
            final List<String> colorList,
            final List<String> sizeList) {
        return colorList.stream().map(color -> {

            // カラーコードが一致する発注SKU情報を取得
            final Map<String, ExtendedTPurchasePDFEntity> mapSizePurchaseItem = mapColorCodePurchaseMaker.get(color);

            // 先頭の1件を取得（必ず取得可能）
            final ExtendedTPurchasePDFEntity sizePurchaseItem = mapSizePurchaseItem.values().stream().findFirst().get();

            final PurchaseItemDetailsXmlModel purchaseItemDetailsXmlModel = new PurchaseItemDetailsXmlModel();

            // カラーコードを設定
            purchaseItemDetailsXmlModel.setColorCode(sizePurchaseItem.getColorCode());

            // カラー名称を設定（6桁以降は削除）
            purchaseItemDetailsXmlModel.setColorName(StringUtils.substring(sizePurchaseItem.getColorCodeName(), SUBSTRING_CUT_6));

            // カラー別の予定数の合計を設定（全てのページには合計を表示する）
            purchaseItemDetailsXmlModel.setPlansSubtotal(StringUtils.convertIntToStringZeroIsBlank(
                    mapSizePurchaseItem.values().stream().mapToInt((PurchaseItem) -> PurchaseItem.getPlansNumber()).sum()));

            // カラー別の確定数の合計を設定（全てのページには合計を表示する）
            purchaseItemDetailsXmlModel.setConfirmSubtotal(StringUtils.convertIntToStringZeroIsBlank(
                    mapSizePurchaseItem.values().stream().mapToInt((PurchaseItem) -> PurchaseItem.getConfirmNumber()).sum()));

            // カラー別の予定数=カラー別の確定数の場合に、カラー別の予定数は0を設定する
            if(purchaseItemDetailsXmlModel.getPlansSubtotal().equals(purchaseItemDetailsXmlModel.getConfirmSubtotal()))purchaseItemDetailsXmlModel.setPlansSubtotal("");

            // カラー別サイズ別の予定数を設定
            purchaseItemDetailsXmlModel.setPlansNumber(
                    sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(getPlans(mapSizePurchaseItem.get(size))))
                            .collect(Collectors.toList()));

            // カラー別サイズ別の確定数を設定
            purchaseItemDetailsXmlModel.setConfirmNumber(
                    sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(getConfirmNumber(mapSizePurchaseItem.get(size))))
                            .collect(Collectors.toList()));

            return purchaseItemDetailsXmlModel;
        }).collect(Collectors.toList());
    }

	/**
     * 予定数を取得する.仕入情報がnull或いは予定数⁼確定数の場合0を返却する.
     * @param extendedTPurchasePDFEntity 仕入情報
     * @return 予定数
     */
    private int getPlans(
            final ExtendedTPurchasePDFEntity extendedTPurchasePDFEntity) {
        if (extendedTPurchasePDFEntity == null || extendedTPurchasePDFEntity.getPlansNumber() == extendedTPurchasePDFEntity.getConfirmNumber()) {
            return 0;
        }

        return extendedTPurchasePDFEntity.getPlansNumber();
	}

	/**
     * 予定数を取得する.仕入情報がnullの場合は、0を返却する.
     * @param extendedTPurchasePDFEntity 仕入情報
     * @return 予定数
     */
    private int getPlansNumber(
            final ExtendedTPurchasePDFEntity extendedTPurchasePDFEntity) {
        if (extendedTPurchasePDFEntity == null) {
            return 0;
        }

        return extendedTPurchasePDFEntity.getPlansNumber();
    }

    /**
     * 確定数を取得する.仕入情報がnullの場合は、0を返却する.
     * @param extendedTPurchasePDFEntity 仕入情報
     * @return 確定数
     */
    private int getConfirmNumber(
            final ExtendedTPurchasePDFEntity extendedTPurchasePDFEntity) {
        if (extendedTPurchasePDFEntity == null) {
            return 0;
        }

        return extendedTPurchasePDFEntity.getConfirmNumber();
    }

    /**
     * PurchaseItemTotalRecordXmlModel生成.
     * @param mapColorCodePurchaseItem カラーコード、サイズごとにグルーピングした発注SKU情報
     * @param sizeList サイズリスト
     * @return PurchaseItemTotalRecordXmlModel
     */
    private PurchaseItemTotalRecordXmlModel genaratedPurchaseItemTotalRecordXmlModel(
            final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchaseItem,
            final List<String> sizeList) {
        final PurchaseItemTotalRecordXmlModel purchaseItemTotalRecordXmlModel = new PurchaseItemTotalRecordXmlModel();

        // 全てのページには合計を表示する
        purchaseItemTotalRecordXmlModel.setWholeSubtotalChar("");
        purchaseItemTotalRecordXmlModel.setWholeTotalAmountChar("合計");

        // 全ての予定数の合計
        purchaseItemTotalRecordXmlModel.setWholePlansSubtotalNum(StringUtils.convertIntToStringZeroIsBlank(
                mapColorCodePurchaseItem.values().stream()
                        .mapToInt(mapSizePurchaseItem -> mapSizePurchaseItem.values().stream().mapToInt(purchaseItem -> purchaseItem.getPlansNumber()).sum()).sum()));

        // 全ての確定数の合計
        purchaseItemTotalRecordXmlModel.setWholeConfirmSubtotalNum(StringUtils.convertIntToStringZeroIsBlank(
                mapColorCodePurchaseItem.values().stream()
                        .mapToInt(mapSizePurchaseItem -> mapSizePurchaseItem.values().stream().mapToInt(purchaseItem -> purchaseItem.getConfirmNumber()).sum()).sum()));

        //全ての予定数の合計=全ての確定数の合計の場合に、全ての予定数の合計値は0を設定する
        if(purchaseItemTotalRecordXmlModel.getWholePlansSubtotalNum().equals(purchaseItemTotalRecordXmlModel.getWholeConfirmSubtotalNum()))purchaseItemTotalRecordXmlModel.setWholePlansSubtotalNum("");

        // サイズ別の予定数の合計
        purchaseItemTotalRecordXmlModel.setSizePlansSubtotal(
                sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(
                        mapColorCodePurchaseItem.values().stream().mapToInt((mapSizePurchaseItem) -> getPlansNumber(mapSizePurchaseItem.get(size))).sum()))
                        .collect(Collectors.toList()));

        // サイズ別の確定数の合計
        purchaseItemTotalRecordXmlModel.setSizeConfirmSubtotal(
                sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(
                        mapColorCodePurchaseItem.values().stream().mapToInt((mapSizePurchaseItem) -> getConfirmNumber(mapSizePurchaseItem.get(size))).sum()))
                        .collect(Collectors.toList()));

        //サイズ別の予定数の合計=サイズ別の確定数の合計の場合に、サイズ別の予定数の合計値は0を設定する
        for(int i = 0 ; i < purchaseItemTotalRecordXmlModel.getSizeConfirmSubtotal().size() ; i++) {
        			if(purchaseItemTotalRecordXmlModel.getSizeConfirmSubtotal().get(i).
        			equals(purchaseItemTotalRecordXmlModel.getSizePlansSubtotal().get(i))) {
        				purchaseItemTotalRecordXmlModel.getSizePlansSubtotal().set(i, "");
        			}
        }

        return purchaseItemTotalRecordXmlModel;
    }

    /**
     * PurchaseItemHeadXmlModel生成.
     * @param purchaseHeadInfo 仕入情報
     * @return PurchaseItemHeadXmlModel
     */
    private PurchaseItemHeadXmlModel genaratedpurchaseItemPageHeadXmlModel(final ExtendedTPurchaseHeadPDFEntity purchaseHeadInfo) {
        final PurchaseItemHeadXmlModel purchaseItemPageHeadXmlModel = new PurchaseItemHeadXmlModel();
        purchaseItemPageHeadXmlModel.setArrivalAt((DateUtils.formatFromDate(purchaseHeadInfo.getArrivalAt(), "yyyy年MM月dd日"))); // 入荷日
        //PRD_0199 del JFE start
        //purchaseItemPageHeadXmlModel.setSlipKbn("41"); // 伝票区分
        //PRD_0199 del JFE end
        purchaseItemPageHeadXmlModel.setDivisionCode(purchaseHeadInfo.getDivisionCode()); // 課コード
        purchaseItemPageHeadXmlModel.setPurchaseVoucherNumber(purchaseHeadInfo.getPurchaseVoucherNumber()); // 伝票番号
        purchaseItemPageHeadXmlModel.setYubin(purchaseHeadInfo.getYubin()); // 郵便番号
        purchaseItemPageHeadXmlModel.setAddress1(purchaseHeadInfo.getAddress1()); // 住所1
        purchaseItemPageHeadXmlModel.setAddress2(purchaseHeadInfo.getAddreess2()); // 住所2
        purchaseItemPageHeadXmlModel.setAddress3(purchaseHeadInfo.getAddreess3()); // 住所3
        purchaseItemPageHeadXmlModel.setSire(purchaseHeadInfo.getSire()); // 御取引先コード
        purchaseItemPageHeadXmlModel.setSendToName(StringUtils.substring(purchaseHeadInfo.getSendToName(), SUBSTRING_CUT_21)); // 御取引先名（21文字以降は切り捨て）
        purchaseItemPageHeadXmlModel.setCompanyName(purchaseHeadInfo.getCompanyName()); // 会社名
        purchaseItemPageHeadXmlModel.setBrandName(purchaseHeadInfo.getBrandName()); // ブランド名
        purchaseItemPageHeadXmlModel.setCompanyYubin("107-8384"); // 会社情報(郵便番号)
        purchaseItemPageHeadXmlModel.setCompanyAddress("東京都港区南青山２－２６－１　"); // 会社情報(住所)1
        purchaseItemPageHeadXmlModel.setCompanyTel("D-LIFEPLACE 南青山4F"); // 会社情報(住所)2
        purchaseItemPageHeadXmlModel.setPartNo(BusinessUtils.formatPartNo(purchaseHeadInfo.getPartNo())); // 品番（前3桁と4桁目に"-"を設定）
        purchaseItemPageHeadXmlModel.setProductName(StringUtils.substring(purchaseHeadInfo.getProductName(), SUBSTRING_CUT_101)); // 品名（101文字以降は切り捨て）
        //PRD_0199 add JFE start
        //伝区の算出
        String pType =  purchaseHeadInfo.getPurchaseType(); //仕入区分
        String sirkbn = purchaseHeadInfo.getSirkbn();//仕入先区分
        String hmk = purchaseHeadInfo.getExpense_item();//費目
        Integer orderId = purchaseHeadInfo.getOrder_id();//発注No
        String logCode = purchaseHeadInfo.getArrivalPlace();//入荷場所
        String pCount = purchaseHeadInfo.getPurchase_count();//引取回数
        String denk1 = "";
        String denk2 = "";

        if(pType == null) pType = "";
        if(logCode == null) logCode = "";
        if(orderId == null || orderId == 0) hmk = "01"; //発注情報が存在しない場合は費目を01に設定する

    	//PRD_0202 mod JFE start
//        if((pType.equals("3")||pType.equals("9")) && (logCode.equals("18")||logCode.equals("19"))) {
//            purchaseItemPageHeadXmlModel.setSlipKbn(pCount);
//            return purchaseItemPageHeadXmlModel;
//        }else if(pType.equals("2")) {
//            purchaseItemPageHeadXmlModel.setSlipKbn("32");
//            return purchaseItemPageHeadXmlModel;
//        }else if(pType.equals("4")) {
//            purchaseItemPageHeadXmlModel.setSlipKbn("33");
//            return purchaseItemPageHeadXmlModel;
//        }else if(pType.equals("5")) {
//            purchaseItemPageHeadXmlModel.setSlipKbn("");
//            return purchaseItemPageHeadXmlModel;
//        }
        if(pType.equals("2")||pType.equals("5")||pType.equals("9")) {
            purchaseItemPageHeadXmlModel.setSlipKbn(pCount);
            return purchaseItemPageHeadXmlModel;
        }else if(pType.equals("4")) {
            purchaseItemPageHeadXmlModel.setSlipKbn("33");
            return purchaseItemPageHeadXmlModel;
        }else if(pType.equals("3")&& (logCode.equals("18")||logCode.equals("19"))) {
            purchaseItemPageHeadXmlModel.setSlipKbn(pCount);
            return purchaseItemPageHeadXmlModel;
        }
    	//PRD_0202 mod JFE end

        if(sirkbn != null) { //仕入先マスタの仕入先区分が見つかった場合
            //伝区の上桁を設定
            if (sirkbn.equals("00")) {
                if(pType.equals("3")) {//返品(3)の場合
                    denk1 = "6";//振替仕入返品
                }else {
                    denk1 = "5";//振替仕入
                }
            }else {
                if(pType.equals("3")) {//返品(3)の場合
                    denk1 = "4";//仕入返品
                }else {
                    denk1 = "3";//仕入
                }
            }
            //伝区の下桁を設定
            if (hmk.equals("01") || hmk.equals("04")|| hmk.equals("24") || hmk.equals("05")) {
                denk2 = hmk.substring(hmk.length() -1);
            }else if( hmk.equals("20")|| hmk.equals("30")) {
                denk2 = hmk.substring(0,1);
            }
        }
        purchaseItemPageHeadXmlModel.setSlipKbn(denk1 + denk2);
        //PRD_0199 add JFE end
        return purchaseItemPageHeadXmlModel;
    }

    /**
     * PurchaseItemStockXmlModel生成.
     * @param purchaseStockInfo 仕入情報
     * @return PurchaseItemStockXmlModel
     */
    private PurchaseItemStockXmlModel genaratedpurchaseItemPageStackXmlModel(final ExtendedTPurchaseStockPDFEntity purchaseStockInfo) {
        final PurchaseItemStockXmlModel purchaseItemPageStockXmlModel = new PurchaseItemStockXmlModel();
        purchaseItemPageStockXmlModel.setStockCode(purchaseStockInfo.getStockCode()); // 入荷先コード
        purchaseItemPageStockXmlModel.setStockName(purchaseStockInfo.getStockName()); // 入荷先名
        purchaseItemPageStockXmlModel.setRetailPrice(purchaseStockInfo.getRetailPrice()); // 上代
        purchaseItemPageStockXmlModel.setRequestNumber(""); // 依頼No(空白固定)
        purchaseItemPageStockXmlModel.setOrderNumber(purchaseStockInfo.getOrderNumber()); // 発注No
        //PRD_0186 jfe mod start
//        purchaseItemPageStockXmlModel.setAllCompletionType(purchaseStockInfo.getAllCompletionType()); // 全済区分:9:未　0:済
//        // 完納区分名
//        if (purchaseStockInfo.getAllCompletionType() == 0) {
        if (Objects.isNull(purchaseStockInfo.getAllCompletionType())) {
            purchaseItemPageStockXmlModel.setAllCompletionType(0);
        }else {
        purchaseItemPageStockXmlModel.setAllCompletionType(purchaseStockInfo.getAllCompletionType()); // 全済区分:9:未　0:済
        }
        // 完納区分名
        if (purchaseItemPageStockXmlModel.getAllCompletionType() == 0) {
        //PRD_0186 jfe mod end
            purchaseItemPageStockXmlModel.setFullPaymentName(ALL_COMPLETION_TYPE_NAME_NORMAL);
        } else {
            purchaseItemPageStockXmlModel.setFullPaymentName(ALL_COMPLETION_TYPE_NAME_OTHER);
        }
        purchaseItemPageStockXmlModel.setDivisionCode(purchaseStockInfo.getDivisionCode()); //  部門コード
        purchaseItemPageStockXmlModel.setBrandName(purchaseStockInfo.getBrandName()); // ブランド名
        purchaseItemPageStockXmlModel.setItemName(purchaseStockInfo.getItemName()); // アイテム名
        purchaseItemPageStockXmlModel.setQuantity(purchaseStockInfo.getQuantity()); // 数量
        // 単価判定
        BigDecimal unitPrice = Objects.nonNull(purchaseStockInfo.getUnitPrice())?purchaseStockInfo.getUnitPrice():purchaseStockInfo.getPurchaseUnitPrice();
        if (Objects.nonNull(purchaseStockInfo.getNonConformingProductUnitPrice())
                && purchaseStockInfo.getNonConformingProductUnitPrice().signum() > 0) {
            unitPrice = purchaseStockInfo.getNonConformingProductUnitPrice(); // B級品単価を設定
        }
        final BigDecimal price = purchaseStockInfo.getQuantity().multiply(unitPrice);
        purchaseItemPageStockXmlModel.setUnitPrice(unitPrice); // 単価
        purchaseItemPageStockXmlModel.setPrice(price); // 金額

        return purchaseItemPageStockXmlModel;
    }
}
//PRD_0134 #10654 add JEF end