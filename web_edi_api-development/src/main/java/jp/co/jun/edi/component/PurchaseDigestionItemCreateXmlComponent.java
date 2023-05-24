package jp.co.jun.edi.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

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
import jp.co.jun.edi.entity.TPurchaseDigestionItemPDFEntity;
import jp.co.jun.edi.entity.extended.ExtendedTPurchasePDFEntity;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

//PRD_0134 #10654 add JEF start
/**
 * xmlを作成するコンポーネント.
 */
@Slf4j
@Component
public class PurchaseDigestionItemCreateXmlComponent {
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

    /**
     * XMLデータファイルを生成する.
     * @param purchaseVoucherNumber 伝票番号
     * @param orderId 発注ID
     * @param xmlPath XMLファイルパス
     * @param yyMMdd
     * @return
     * @return
     * @throws Exception 例外
     * @throws IOException 例外
     */
    public  void createXml(final List<TPurchaseDigestionItemPDFEntity> purchaseDigestionEntity, final Path xmlPath, String yyMMdd) throws Exception, IOException {
        log.info(LogStringUtil.of("createXml")
                .message("Start processing of createXML.")
                .value("PartNo", purchaseDigestionEntity.get(0).getPartNo())
                .value("SupplierCode", purchaseDigestionEntity.get(0).getSupplierCode())
                .build());
    	final PurchaseItemXmlModel purchaseItemXmlModel = new PurchaseItemXmlModel();
        // PDF作成用の仕入明細(HEAD用)を設定する
        final PurchaseItemHeadXmlModel purchaseHeadInfoModel = generateHeadPdfInfo(purchaseDigestionEntity,yyMMdd);

        // PDF作成用の入荷情報を取得する
        final PurchaseItemStockXmlModel purchaseStockInfoModel = generateStockPdfInfo(purchaseDigestionEntity);

        // PDF作成用の仕入情報を取得する
        final List<PurchaseItemDetailXmlModel> purchaseItemDetailXmlModel = generateMakerPurchaseInfo(purchaseDigestionEntity);

        // ヘッダー情報
        purchaseItemXmlModel.setPageHead(purchaseHeadInfoModel);

        // ページ入荷情報部
        purchaseItemXmlModel.setPageStock(purchaseStockInfoModel);

        // 詳細情報
        purchaseItemXmlModel.setPageDetails(purchaseItemDetailXmlModel);


        // ModelをXMLデータに変換
        final StringWriter purchaseItemXmlWriter = new StringWriter();
        JAXB.marshal(purchaseItemXmlModel, purchaseItemXmlWriter);

        // XMLデータを生成
        final String purchaseItemXml = purchaseItemXmlWriter.toString();

        // XMLファイルを一時ディレクトリへ出力
        try (BufferedWriter writer = Files.newBufferedWriter(xmlPath)) {
            writer.append(purchaseItemXml);
        }
        log.info(LogStringUtil.of("createXml")
                .message("End processing of createXml.")
                .value("PartNo", purchaseDigestionEntity.get(0).getPartNo())
                .value("SupplierCode", purchaseDigestionEntity.get(0).getSupplierCode())
                .build());
    }

    /**
     *  PDF作成用ヘッダー情報取得.
     * @param yyMMdd
     *  @return PDF作成情報
     */
    private PurchaseItemHeadXmlModel generateHeadPdfInfo(final List<TPurchaseDigestionItemPDFEntity> purchaseDigestionEntity, String yyMMdd) {

    	final PurchaseItemHeadXmlModel purchaseItemPageHeadXmlModel = new PurchaseItemHeadXmlModel();

    	purchaseItemPageHeadXmlModel.setArrivalAt(yyMMdd);//入荷日
    	purchaseItemPageHeadXmlModel.setSlipKbn(purchaseDigestionEntity.get(0).getPurchaseVoucherType());//伝区
    	// PRD_0209 && TEAM_ALBUS-41 add start
//    	purchaseItemPageHeadXmlModel.setDivisionCode(purchaseDigestionEntity.get(0).getDivisionCode());//課コード
    	purchaseItemPageHeadXmlModel.setDivisionCode("");//課コード
    	// PRD_0209 && TEAM_ALBUS-41 add end
    	//PRD_0152 mod JFE start
//    	purchaseItemPageHeadXmlModel.setPurchaseVoucherNumber(purchaseDigestionEntity.get(0).getPurchaseVoucherNumber().toString());//伝票番号
    	purchaseItemPageHeadXmlModel.setPurchaseVoucherNumber(purchaseDigestionEntity.get(0).getPurchaseVoucherNumber());//伝票番号
    	//PRD_0152 mod JFE end
    	purchaseItemPageHeadXmlModel.setYubin(purchaseDigestionEntity.get(0).getYubin());//郵便番号
    	purchaseItemPageHeadXmlModel.setAddress1(purchaseDigestionEntity.get(0).getAdd1());//住所１
    	purchaseItemPageHeadXmlModel.setAddress2(purchaseDigestionEntity.get(0).getAdd2());//住所2
    	purchaseItemPageHeadXmlModel.setAddress3(purchaseDigestionEntity.get(0).getAdd3());//住所3
    	purchaseItemPageHeadXmlModel.setSire(purchaseDigestionEntity.get(0).getSupplierCode());//仕入先コード
    	purchaseItemPageHeadXmlModel.setSendToName(StringUtils.substring(purchaseDigestionEntity.get(0).getName(), SUBSTRING_CUT_21)); // 御取引先名（21文字以降は切り捨て）
    	purchaseItemPageHeadXmlModel.setCompanyName(purchaseDigestionEntity.get(0).getCompanyName());//会社名
    	purchaseItemPageHeadXmlModel.setBrandName(purchaseDigestionEntity.get(0).getBrand_name());//ブランド名
    	purchaseItemPageHeadXmlModel.setCompanyYubin("107-8384");//会社情報(郵便
    	purchaseItemPageHeadXmlModel.setCompanyAddress("東京都港区南青山２－２６－１　"); // 会社情報(住所)1
        purchaseItemPageHeadXmlModel.setCompanyTel("D-LIFEPLACE 南青山4F"); // 会社情報(住所)2
        purchaseItemPageHeadXmlModel.setPartNo(BusinessUtils.formatPartNo(purchaseDigestionEntity.get(0).getPartNo())); // 品番（前3桁と4桁目に"-"を設定）
        purchaseItemPageHeadXmlModel.setProductName(StringUtils.substring(purchaseDigestionEntity.get(0).getProductName(), SUBSTRING_CUT_101)); // 品名（101文字以降は切り捨て）

        return purchaseItemPageHeadXmlModel;
    }

    /**
     *  PDF作成用情報取得.
     *  @param purchaseVoucherNumber 伝票番号
     *  @param orderId 発注ID
     *  @return PDF作成情報
     */
	private PurchaseItemStockXmlModel generateStockPdfInfo(final List<TPurchaseDigestionItemPDFEntity> purchaseDigestionEntity) {
    	final PurchaseItemStockXmlModel purchaseItemPageStockXmlModel = new PurchaseItemStockXmlModel();

    	//数量と金額の計算。
		long sumCount = 0;
		for (int i = 0; i < purchaseDigestionEntity.size(); i++) {
			sumCount += purchaseDigestionEntity.get(i).getFixArrivalCount();
		}
		// PRD_0209 && TEAM_ALBUS-41 add start
		// 返品だったら数量をマイナスにする
		final String denk = purchaseDigestionEntity.get(0).getPurchaseVoucherType().substring(0,1);
        final String arrivalPlace = purchaseDigestionEntity.get(0).getArrivalPlace();
        if ((denk.equals("4")|| denk.equals("6")) && arrivalPlace.equals("19")) {
            sumCount = sumCount * -1;
        }
        // PRD_0209 && TEAM_ALBUS-41 add end
		BigDecimal Quantity = new BigDecimal(sumCount);
		BigDecimal price =  purchaseDigestionEntity.get(0).getUnitPrice().multiply(Quantity);

        purchaseItemPageStockXmlModel.setStockCode(""); // 入荷先コード
        purchaseItemPageStockXmlModel.setStockName(""); // 入荷先名
        purchaseItemPageStockXmlModel.setRetailPrice(purchaseDigestionEntity.get(0).getRetailPrice()); // 上代
        purchaseItemPageStockXmlModel.setRequestNumber(""); // 依頼No(空白固定)
        purchaseItemPageStockXmlModel.setOrderNumber(null); // 発注No
        purchaseItemPageStockXmlModel.setAllCompletionType(purchaseDigestionEntity.get(0).getAllCompletionType()); // 全済区分:9:未　0:済
        // 完納区分名
        if (purchaseDigestionEntity.get(0).getAllCompletionType() == 0) {
            purchaseItemPageStockXmlModel.setFullPaymentName(ALL_COMPLETION_TYPE_NAME_NORMAL);
        } else {
            purchaseItemPageStockXmlModel.setFullPaymentName(ALL_COMPLETION_TYPE_NAME_OTHER);
        }
        // PRD_0209 && TEAM_ALBUS-41 add start
//        purchaseItemPageStockXmlModel.setDivisionCode(purchaseDigestionEntity.get(0).getDivisionCode()); //  部門コード
        purchaseItemPageStockXmlModel.setDivisionCode(""); //  部門コード
        // PRD_0209 && TEAM_ALBUS-41 add end
        purchaseItemPageStockXmlModel.setBrandName(purchaseDigestionEntity.get(0).getBrand_name()); // ブランド名
        purchaseItemPageStockXmlModel.setItemName(purchaseDigestionEntity.get(0).getItemName()); // アイテム名
        purchaseItemPageStockXmlModel.setQuantity(Quantity); // 数量
        purchaseItemPageStockXmlModel.setUnitPrice(purchaseDigestionEntity.get(0).getUnitPrice()); // 単価
        purchaseItemPageStockXmlModel.setPrice(price); // 金額

        return purchaseItemPageStockXmlModel;
    }

    /**
     *  仕入情報を取得.
     *  @param vouNum 伝票番号
     *  @param orderId 発注ID
     *  @return 仕入情報
     */
    private List<PurchaseItemDetailXmlModel> generateMakerPurchaseInfo(final List<TPurchaseDigestionItemPDFEntity> purchaseDigestionEntity) {
    	//モデルに反映させる前にいったん、Entityに移す。(サイズごとにグルーピングするため)
    	final List<ExtendedTPurchasePDFEntity> purchaseList = new ArrayList<ExtendedTPurchasePDFEntity>();

    	for (int i = 0; i < purchaseDigestionEntity.size(); i++) {
    		ExtendedTPurchasePDFEntity purchaseEntity = new ExtendedTPurchasePDFEntity();
    		purchaseEntity.setPurchaseVoucherNumber(purchaseDigestionEntity.get(i).getPurchaseVoucherNumber());
    		purchaseEntity.setPurchaseVoucherLine(purchaseDigestionEntity.get(i).getPurchaseVoucherLine());
    		purchaseEntity.setPurchaseVoucherNumber(purchaseDigestionEntity.get(i).getPurchaseVoucherNumber());
    		purchaseEntity.setSize(purchaseDigestionEntity.get(i).getSize());
    		purchaseEntity.setColorCode(purchaseDigestionEntity.get(i).getColorCode());
    		purchaseEntity.setColorCodeName(purchaseDigestionEntity.get(i).getColorCodeName());
    		purchaseEntity.setPlansNumber(purchaseDigestionEntity.get(i).getArrivalCount());
    		purchaseEntity.setConfirmNumber(purchaseDigestionEntity.get(i).getFixArrivalCount());
    		purchaseEntity.setSortOrder(purchaseDigestionEntity.get(i).getJun());
    		purchaseList.add(purchaseEntity);
		}

    	//エンティティが作成できたら整形してモデルへ。
        // 仕入情報をカラーコード、サイズごとにグルーピングする
    	// PRD_0209 && TEAM_ALBUS-41 add start 確定数、予定数を集計する
//        final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchase = purchaseList.stream()
//                .collect(Collectors.groupingBy(o -> o.getColorCode(), Collectors.toMap(o -> o.getSize(), o -> o,(oldVal,newVal) -> newVal)));
    	final Map<String, Map<String, ExtendedTPurchasePDFEntity>> mapColorCodePurchase = purchaseList.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getColorCode(),
                        Collectors.toMap(
                                o -> o.getSize(),
                                o -> o,
                                (oldVal,newVal) -> {
                                    newVal.setPlansNumber(oldVal.getPlansNumber() + newVal.getPlansNumber());
                                    newVal.setConfirmNumber(oldVal.getConfirmNumber() + newVal.getConfirmNumber());
                                    return newVal;
                                }
                        )
                ));
    	// PRD_0209 && TEAM_ALBUS-41 add end

        // サイズリストを取得する
        final List<String> listSize = getUniqueSizeList(purchaseList);

     // 詳細情報
        return genaratedPageDatails(mapColorCodePurchase, listSize);
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
}
//PRD_0134 #10654 add JEF end