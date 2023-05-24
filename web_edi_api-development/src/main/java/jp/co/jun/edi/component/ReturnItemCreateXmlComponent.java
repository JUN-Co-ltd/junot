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

import jp.co.jun.edi.component.model.returnItem.RetunItemHeadRecordXmlModel;
import jp.co.jun.edi.component.model.returnItem.RetunItemHeadXmlModel;
import jp.co.jun.edi.component.model.returnItem.RetunItemSectionXmlModel;
import jp.co.jun.edi.component.model.returnItem.ReturnItemDetailXmlModel;
import jp.co.jun.edi.component.model.returnItem.ReturnItemDetailsXmlModel;
import jp.co.jun.edi.component.model.returnItem.ReturnItemHeadXmlModel;
import jp.co.jun.edi.component.model.returnItem.ReturnItemStockXmlModel;
import jp.co.jun.edi.component.model.returnItem.ReturnItemTotalRecordXmlModel;
import jp.co.jun.edi.component.model.returnItem.ReturnItemXmlModel;
import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnHeadPDFEntity;
import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnPDFEntity;
import jp.co.jun.edi.entity.extended.ExtendedTMakerReturnStockPDFEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.extended.ExtendedTMakerReturnHeadPDFRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMakerReturnPDFRepository;
import jp.co.jun.edi.repository.extended.ExtendedTMakerReturnStockPDFRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.StringUtils;

/**
 * xmlを作成するコンポーネント.
 */
@Component
public class ReturnItemCreateXmlComponent {
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
    private ExtendedTMakerReturnHeadPDFRepository extendedTMakerReturnHeadPDFRepository;

    @Autowired
    private ExtendedTMakerReturnPDFRepository extendedTMakerReturnPDFRepository;

    @Autowired
    private ExtendedTMakerReturnStockPDFRepository extendedTMakerReturnStockPDFRepository;

    /**
     * XMLデータファイルを生成する.
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @param xmlPath XMLファイルパス
     * @throws Exception 例外
     * @throws IOException 例外
     */
    // PRD_0073 mod SIT start
    //public void createXml(final String voucherNumber, final BigInteger orderId, final Path xmlPath) throws Exception, IOException {
    public void createXml(final String voucherNumber, final BigInteger orderId, final Date createdAt, final Path xmlPath) throws Exception, IOException {
    // PRD_0073 mod SIT start

        // PDF作成用の返品明細(HEAD用)を取得する
        // PRD_0073 mod SIT start
        //final ExtendedTMakerReturnHeadPDFEntity makerReturnHeadInfo = getHeadPdfInfo(voucherNumber, orderId).orElseThrow(() -> new ResourceNotFoundException(
        final ExtendedTMakerReturnHeadPDFEntity makerReturnHeadInfo = getHeadPdfInfo(voucherNumber, orderId, createdAt).orElseThrow(() -> new ResourceNotFoundException(
        // PRD_0073 mod SIT end
                ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                .message("t_maker_return not found.")
                                .value("voucher_number", voucherNumber)
                                .build())));

        // PDF作成用の入荷情報を取得する
        final ExtendedTMakerReturnStockPDFEntity makerReturnStockInfo = getStockPdfInfo(voucherNumber, orderId).orElseThrow(() -> new ResourceNotFoundException(
                ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                .message("t_maker_return not found.")
                                .value("voucher_number", voucherNumber)
                                .build())));

        // PDF作成用のメーカー返品情報を取得する
        final List<ExtendedTMakerReturnPDFEntity> makerReturnList = getMakerReturnInfo(voucherNumber, orderId).getContent();
        if (makerReturnList.isEmpty()) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                    .message("t_maker_return not found.")
                                    .value("voucher_number", voucherNumber)
                                    .build()));
        }

        // XMLデータを生成
        final String returnItemXml = genaratedXmlData(makerReturnHeadInfo, makerReturnStockInfo, makerReturnList);

        // XMLファイルを一時ディレクトリへ出力
        try (BufferedWriter writer = Files.newBufferedWriter(xmlPath)) {
            writer.append(returnItemXml);
        }
    }

    /**
     *  PDF作成用情報取得.
     *  @param voucherNumber 伝票番号
     *  @param orderId 発注ID
     *  @param createdAt 返金日
     *  @return PDF作成情報
     */
    // PRD_0073 mod SIT start
    //private Optional<ExtendedTMakerReturnHeadPDFEntity> getHeadPdfInfo(final String voucherNumber, final BigInteger orderId) {
    //return extendedTMakerReturnHeadPDFRepository.getMakerReturnHeadInfo(voucherNumber, orderId);
    private Optional<ExtendedTMakerReturnHeadPDFEntity> getHeadPdfInfo(final String voucherNumber, final BigInteger orderId, final Date createdAt) {
        return extendedTMakerReturnHeadPDFRepository.getMakerReturnHeadInfo(voucherNumber, orderId, createdAt);
    // PRD_0073 mod SIT end
    }

    /**
     *  PDF作成用情報取得.
     *  @param voucherNumber 伝票番号
     *  @param orderId 発注ID
     *  @return PDF作成情報
     */
    private Optional<ExtendedTMakerReturnStockPDFEntity> getStockPdfInfo(final String voucherNumber, final BigInteger orderId) {
        return extendedTMakerReturnStockPDFRepository.getMakerReturnStockInfo(voucherNumber, orderId);
    }

    /**
     *  メーカー返品情報を取得.
     *  @param vouNum 伝票番号
     *  @param orderId 発注ID
     *  @return メーカー返品情報
     */
    private Page<ExtendedTMakerReturnPDFEntity> getMakerReturnInfo(final String vouNum, final BigInteger orderId) {
        return extendedTMakerReturnPDFRepository.getMakerReturnInfo(vouNum, orderId, PageRequest.of(0, Integer.MAX_VALUE));
    }

    /**
     * XMLデータ生成.
     * @param makerReturnHeadInfo PDF作成用の返品明細(HEAD用)
     * @param makerReturnStockInfo PDF作成用の返品明細(STOCK用)
     * @param makerReturnList メーカー返品情報
     * @return XMLデータ
     */
    private String genaratedXmlData(final ExtendedTMakerReturnHeadPDFEntity makerReturnHeadInfo,
            final ExtendedTMakerReturnStockPDFEntity makerReturnStockInfo,
            final List<ExtendedTMakerReturnPDFEntity> makerReturnList) {

        final ReturnItemXmlModel returnItemXmlModel = new ReturnItemXmlModel();

        // メーカー返品情報をカラーコード、サイズごとにグルーピングする
        final Map<String, Map<String, ExtendedTMakerReturnPDFEntity>> mapColorCodeReturnMaker = makerReturnList.stream()
                .collect(Collectors.groupingBy(o -> o.getColorCode(), Collectors.toMap(o -> o.getSize(), o -> o)));

        // サイズリストを取得する
        final List<String> listSize = getUniqueSizeList(makerReturnList);

        // 詳細情報
        returnItemXmlModel.setPageDetails(genaratedPageDatails(mapColorCodeReturnMaker, listSize));

        // ヘッダー情報
        returnItemXmlModel.setPageHead(genaratedReturnItemPageHeadXmlModel(makerReturnHeadInfo));

        // ページ入荷情報部
        returnItemXmlModel.setPageStock(genaratedReturnItemPageStackXmlModel(makerReturnStockInfo));

        // ModelをXMLデータに変換
        final StringWriter returnItemXml = new StringWriter();
        JAXB.marshal(returnItemXmlModel, returnItemXml);


        return returnItemXml.toString();

    }

    /**
     * OrderPageDetailsXmlModel生成.
     * @param mapColorCodeReturnMaker カラーコード、サイズごとにグルーピングしたメーカー返品情報
     * @param listSize サイズ一覧
     * @return OrderPageDetailsXmlModelリスト
     */
    private List<ReturnItemDetailXmlModel> genaratedPageDatails(final Map<String, Map<String, ExtendedTMakerReturnPDFEntity>> mapColorCodeReturnMaker,
            final List<String> listSize) {

        // ページごとのカラーリストを生成
        final List<List<String>> colorByPageList = CollectionUtils.chunk(
                mapColorCodeReturnMaker.keySet().stream()
                        // カラーコードでソートする
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toList()),
                COLOR_DISPLAY_MAX_SIZE);

        // ページごとのサイズリストを生成
        final List<List<String>> sizeByPageList = CollectionUtils.chunk(listSize, SIZE_DISPLAY_MAX_SIZE);

        final List<ReturnItemDetailXmlModel> returnItemDetailsXmlModelList = new ArrayList<>();

        // ページ番号
        int pageNumber = 1;

        for (final List<String> colorList : colorByPageList) {
            for (final List<String> sizeList : sizeByPageList) {
                final ReturnItemDetailXmlModel returnItemDetailsXmlModel = new ReturnItemDetailXmlModel();
                returnItemDetailsXmlModel.setHead(genaratedRetunItemHeadXmlModel(sizeList));
                returnItemDetailsXmlModel.setRecordSection(genaratedReturnItemSectionXmlModel(
                        mapColorCodeReturnMaker,
                        colorList,
                        sizeList));
                returnItemDetailsXmlModel.setPageNumber(pageNumber);
                pageNumber++;

                returnItemDetailsXmlModelList.add(returnItemDetailsXmlModel);
            }
        }

        return returnItemDetailsXmlModelList;
    }

    /**
     * ソート順でソートした重複しないサイズコードリストを取得.
     * @param makerReturnList メーカ返品情報情報
     * @return サイズコードリスト
     */
    private List<String> getUniqueSizeList(final List<ExtendedTMakerReturnPDFEntity> makerReturnList) {
        return makerReturnList.stream().sorted(
                // ソート順の昇順でソートする。サイズマスタには必ずソート順はあるが、存在しない場合も考慮。
                Comparator.comparing(ExtendedTMakerReturnPDFEntity::getSortOrder, Comparator.nullsFirst(Comparator.naturalOrder()))
                        // ソート順がnullの場合は、サイズの昇順でソートする
                        .thenComparing(Comparator.comparing(ExtendedTMakerReturnPDFEntity::getSize, Comparator.nullsFirst(Comparator.naturalOrder()))))
                .map(makerReturn -> makerReturn.getSize())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * RetunItemHeadXmlModel生成.
     * @param listSize サイズリスト
     * @return RetunItemHeadXmlModel
     */
    private RetunItemHeadXmlModel genaratedRetunItemHeadXmlModel(final List<String> listSize) {
        final RetunItemHeadXmlModel returnItemHeadXmlModel = new RetunItemHeadXmlModel();
        returnItemHeadXmlModel.setHeadRecord(genaratedReturnItemHeadRecordXmlModel(listSize));
        // 全てのページには合計を表示する
        returnItemHeadXmlModel.setSubtotal("");
        returnItemHeadXmlModel.setTotalAmount("合計");
        return returnItemHeadXmlModel;
    }

    /**
     * RetunItemHeadRecordXmlModel生成.
     * @param listSize サイズリスト
     * @return RetunItemHeadRecordXmlModel
     */
    private RetunItemHeadRecordXmlModel genaratedReturnItemHeadRecordXmlModel(final List<String> listSize) {
        final RetunItemHeadRecordXmlModel returnItemHeadRecordXmlModel = new RetunItemHeadRecordXmlModel();
        returnItemHeadRecordXmlModel.setSize(listSize);
        return returnItemHeadRecordXmlModel;
    }

    /**
     * RetunItemSectionXmlModel生成.
     * @param mapColorCodeReturnMaker カラーコード、サイズごとにグルーピングしたメーカ返品情報
     * @param colorList カラーリスト
     * @param sizeList サイズリスト
     * @return RetunItemSectionXmlModel
     */
    private RetunItemSectionXmlModel genaratedReturnItemSectionXmlModel(
            final Map<String, Map<String, ExtendedTMakerReturnPDFEntity>> mapColorCodeReturnMaker,
            final List<String> colorList,
            final List<String> sizeList) {
        final RetunItemSectionXmlModel returnItemSectionXmlModel = new RetunItemSectionXmlModel();
        returnItemSectionXmlModel.setDetails(genaratedReturnItemDetailsXmlModel(
                mapColorCodeReturnMaker,
                colorList,
                sizeList));

        returnItemSectionXmlModel.setTotalRecord(genaratedReturnItemTotalRecordXmlModel(
                mapColorCodeReturnMaker,
                sizeList));

        return returnItemSectionXmlModel;
    }

    /**
     * ReturnItemDetailsXmlModel生成.
     * @param mapColorCodeReturnMaker カラーコード、サイズごとにグルーピングしたメーカ返品情報
     * @param colorList カラーリスト
     * @param sizeList サイズリスト
     * @return ReturnItemDetailsXmlModelリスト
     */
    private List<ReturnItemDetailsXmlModel> genaratedReturnItemDetailsXmlModel(
            final Map<String, Map<String, ExtendedTMakerReturnPDFEntity>> mapColorCodeReturnMaker,
            final List<String> colorList,
            final List<String> sizeList) {
        return colorList.stream().map(color -> {

            // カラーコードが一致する発注SKU情報を取得
            final Map<String, ExtendedTMakerReturnPDFEntity> mapSizeReturnItem = mapColorCodeReturnMaker.get(color);

            // 先頭の1件を取得（必ず取得可能）
            final ExtendedTMakerReturnPDFEntity sizeReturnItem = mapSizeReturnItem.values().stream().findFirst().get();

            final ReturnItemDetailsXmlModel returnItemDetailsXmlModel = new ReturnItemDetailsXmlModel();

            // カラーコードを設定
            returnItemDetailsXmlModel.setColorCode(sizeReturnItem.getColorCode());

            // カラー名称を設定（6桁以降は削除）
            returnItemDetailsXmlModel.setColorName(StringUtils.substring(sizeReturnItem.getColorCodeName(), SUBSTRING_CUT_6));

            // カラー別の予定数の合計を設定（全てのページには合計を表示する）
            returnItemDetailsXmlModel.setPlansSubtotal(StringUtils.convertIntToStringZeroIsBlank(
                    mapSizeReturnItem.values().stream().mapToInt((returnItem) -> returnItem.getPlansNumber()).sum()));

            // カラー別の確定数の合計を設定（全てのページには合計を表示する）
            returnItemDetailsXmlModel.setConfirmSubtotal(StringUtils.convertIntToStringZeroIsBlank(
                    mapSizeReturnItem.values().stream().mapToInt((returnItem) -> returnItem.getConfirmNumber()).sum()));

            // カラー別サイズ別の予定数を設定
            returnItemDetailsXmlModel.setPlansNumber(
                    sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(getPlansNumber(mapSizeReturnItem.get(size))))
                            .collect(Collectors.toList()));

            // カラー別サイズ別の確定数を設定
            returnItemDetailsXmlModel.setConfirmNumber(
                    sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(getConfirmNumber(mapSizeReturnItem.get(size))))
                            .collect(Collectors.toList()));

            return returnItemDetailsXmlModel;
        }).collect(Collectors.toList());
    }

    /**
     * 予定数を取得する.メーカ返品情報がnullの場合は、0を返却する.
     * @param extendedTMakerReturnPDFEntity メーカ返品情報
     * @return 予定数
     */
    private int getPlansNumber(
            final ExtendedTMakerReturnPDFEntity extendedTMakerReturnPDFEntity) {
        if (extendedTMakerReturnPDFEntity == null) {
            return 0;
        }

        return extendedTMakerReturnPDFEntity.getPlansNumber();
    }

    /**
     * 確定数を取得する.メーカ返品情報がnullの場合は、0を返却する.
     * @param extendedTMakerReturnPDFEntity メーカ返品情報
     * @return 確定数
     */
    private int getConfirmNumber(
            final ExtendedTMakerReturnPDFEntity extendedTMakerReturnPDFEntity) {
        if (extendedTMakerReturnPDFEntity == null) {
            return 0;
        }

        return extendedTMakerReturnPDFEntity.getConfirmNumber();
    }

    /**
     * ReturnItemTotalRecordXmlModel生成.
     * @param mapColorCodeReturnItem カラーコード、サイズごとにグルーピングした発注SKU情報
     * @param sizeList サイズリスト
     * @return ReturnItemTotalRecordXmlModel
     */
    private ReturnItemTotalRecordXmlModel genaratedReturnItemTotalRecordXmlModel(
            final Map<String, Map<String, ExtendedTMakerReturnPDFEntity>> mapColorCodeReturnItem,
            final List<String> sizeList) {
        final ReturnItemTotalRecordXmlModel returnItemTotalRecordXmlModel = new ReturnItemTotalRecordXmlModel();

        // 全てのページには合計を表示する
        returnItemTotalRecordXmlModel.setWholeSubtotalChar("");
        returnItemTotalRecordXmlModel.setWholeTotalAmountChar("合計");

        // 全ての予定数の合計
        returnItemTotalRecordXmlModel.setWholePlansSubtotalNum(StringUtils.convertIntToStringZeroIsBlank(
                mapColorCodeReturnItem.values().stream()
                        .mapToInt(mapSizeReturnItem -> mapSizeReturnItem.values().stream().mapToInt(returnItem -> returnItem.getPlansNumber()).sum()).sum()));

        // 全ての確定数の合計
        returnItemTotalRecordXmlModel.setWholeConfirmSubtotalNum(StringUtils.convertIntToStringZeroIsBlank(
                mapColorCodeReturnItem.values().stream()
                        .mapToInt(mapSizeReturnItem -> mapSizeReturnItem.values().stream().mapToInt(returnItem -> returnItem.getConfirmNumber()).sum()).sum()));

        // サイズ別の予定数の合計
        returnItemTotalRecordXmlModel.setSizePlansSubtotal(
                sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(
                        mapColorCodeReturnItem.values().stream().mapToInt((mapSizeReturnItem) -> getPlansNumber(mapSizeReturnItem.get(size))).sum()))
                        .collect(Collectors.toList()));

        // サイズ別の予定数の合計
        returnItemTotalRecordXmlModel.setSizePlansSubtotal(
                sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(
                        mapColorCodeReturnItem.values().stream().mapToInt((mapSizeReturnItem) -> getPlansNumber(mapSizeReturnItem.get(size))).sum()))
                        .collect(Collectors.toList()));

        // サイズ別の確定数の合計
        returnItemTotalRecordXmlModel.setSizeConfirmSubtotal(
                sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(
                        mapColorCodeReturnItem.values().stream().mapToInt((mapSizeReturnItem) -> getConfirmNumber(mapSizeReturnItem.get(size))).sum()))
                        .collect(Collectors.toList()));

        return returnItemTotalRecordXmlModel;
    }

    /**
     * ReturnItemHeadXmlModel生成.
     * @param makerReturnHeadInfo 返品情報
     * @return ReturnItemHeadXmlModel
     */
    private ReturnItemHeadXmlModel genaratedReturnItemPageHeadXmlModel(final ExtendedTMakerReturnHeadPDFEntity makerReturnHeadInfo) {
        final ReturnItemHeadXmlModel returnItemPageHeadXmlModel = new ReturnItemHeadXmlModel();
        returnItemPageHeadXmlModel.setReturnDate((DateUtils.formatFromDate(makerReturnHeadInfo.getReturnDate(), "yyyy年MM月dd日"))); // 返品日
        returnItemPageHeadXmlModel.setSlipKbn("41"); // 伝票区分
        returnItemPageHeadXmlModel.setDivisionCode(makerReturnHeadInfo.getDivisionCode()); // 課コード
        returnItemPageHeadXmlModel.setVoucherNumber(makerReturnHeadInfo.getVoucherNumber()); // 伝票番号
        returnItemPageHeadXmlModel.setYubin(makerReturnHeadInfo.getYubin()); // 郵便番号
        returnItemPageHeadXmlModel.setAddress1(makerReturnHeadInfo.getAddress1()); // 住所1
        returnItemPageHeadXmlModel.setAddress2(makerReturnHeadInfo.getAddreess2()); // 住所2
        returnItemPageHeadXmlModel.setAddress3(makerReturnHeadInfo.getAddreess3()); // 住所3
        returnItemPageHeadXmlModel.setSire(makerReturnHeadInfo.getSire()); // 御取引先コード
        returnItemPageHeadXmlModel.setSendToName(StringUtils.substring(makerReturnHeadInfo.getSendToName(), SUBSTRING_CUT_21)); // 御取引先名（21文字以降は切り捨て）
        returnItemPageHeadXmlModel.setCompanyName(makerReturnHeadInfo.getCompanyName()); // 会社名
        returnItemPageHeadXmlModel.setBrandName(makerReturnHeadInfo.getBrandName()); // ブランド名
        returnItemPageHeadXmlModel.setCompanyYubin("107-8384"); // 会社情報(郵便番号)
        // PRD_0132 #10060 mod JFE start
		//returnItemPageHeadXmlModel.setCompanyAddress("東京都港区南青山２－２－３"); // 会社情報(住所)
		//returnItemPageHeadXmlModel.setCompanyTel("03(6890)8888"); // 会社情報(電話番号)
        returnItemPageHeadXmlModel.setCompanyAddress("東京都港区南青山２－２６－１　"); // 会社情報(住所)
        returnItemPageHeadXmlModel.setCompanyTel("D-LIFEPLACE 南青山4F"); // 会社情報(電話番号)
        // PRD_0132 #10060 mod JFE end
        returnItemPageHeadXmlModel.setPartNo(BusinessUtils.formatPartNo(makerReturnHeadInfo.getPartNo())); // 品番（前3桁と4桁目に"-"を設定）
        returnItemPageHeadXmlModel.setProductName(StringUtils.substring(makerReturnHeadInfo.getProductName(), SUBSTRING_CUT_101)); // 品名（101文字以降は切り捨て）

        return returnItemPageHeadXmlModel;
    }

    /**
     * ReturnItemStockXmlModel生成.
     * @param makerReturnStockInfo 返品情報
     * @return ReturnItemStockXmlModel
     */
    private ReturnItemStockXmlModel genaratedReturnItemPageStackXmlModel(final ExtendedTMakerReturnStockPDFEntity makerReturnStockInfo) {
        final ReturnItemStockXmlModel returnItemPageStockXmlModel = new ReturnItemStockXmlModel();
        returnItemPageStockXmlModel.setStockCode(makerReturnStockInfo.getStockCode()); // 入荷先コード
        returnItemPageStockXmlModel.setStockName(makerReturnStockInfo.getStockName()); // 入荷先名
        returnItemPageStockXmlModel.setRetailPrice(makerReturnStockInfo.getRetailPrice()); // 上代
        returnItemPageStockXmlModel.setRequestNumber(""); // 依頼No(空白固定)
        returnItemPageStockXmlModel.setOrderNumber(makerReturnStockInfo.getOrderNumber()); // 発注No
        returnItemPageStockXmlModel.setAllCompletionType(makerReturnStockInfo.getAllCompletionType()); // 全済区分:9:未　0:済
        // 完納区分名
        if (makerReturnStockInfo.getAllCompletionType() == 0) {
            returnItemPageStockXmlModel.setFullPaymentName(ALL_COMPLETION_TYPE_NAME_NORMAL);
        } else {
            returnItemPageStockXmlModel.setFullPaymentName(ALL_COMPLETION_TYPE_NAME_OTHER);
        }
        returnItemPageStockXmlModel.setDivisionCode(makerReturnStockInfo.getDivisionCode()); //  部門コード
        returnItemPageStockXmlModel.setBrandName(makerReturnStockInfo.getBrandName()); // ブランド名
        returnItemPageStockXmlModel.setItemName(makerReturnStockInfo.getItemName()); // アイテム名
        returnItemPageStockXmlModel.setQuantity(makerReturnStockInfo.getQuantity()); // 数量
        // 単価判定
        BigDecimal unitPrice = makerReturnStockInfo.getUnitPrice();
        if (Objects.nonNull(makerReturnStockInfo.getNonConformingProductUnitPrice())
                && makerReturnStockInfo.getNonConformingProductUnitPrice().signum() > 0) {
            unitPrice = makerReturnStockInfo.getNonConformingProductUnitPrice(); // B級品単価を設定
        }
        final BigDecimal price = makerReturnStockInfo.getQuantity().multiply(unitPrice);
        returnItemPageStockXmlModel.setUnitPrice(unitPrice); // 単価
        returnItemPageStockXmlModel.setPrice(price); // 金額

        return returnItemPageStockXmlModel;
    }
}
