package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.delivery.DeliveryDetailsXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryHeadRecordXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryHeadXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryPageDetailsXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryPageHeadXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryRecordSectionXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryRequestXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliverySendToAddressXmlModel;
import jp.co.jun.edi.component.model.delivery.DeliveryTotalRecordXmlModel;
import jp.co.jun.edi.entity.extended.ExtendedDeliveryRequestPdfDetailEntity;
import jp.co.jun.edi.entity.extended.ExtendedDeliveryRequestPdfHeaderEntity;
import jp.co.jun.edi.entity.extended.ExtendedPdfTDeliveryDetailEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.extended.ExtendedDeliveryRequestDetailRepository;
import jp.co.jun.edi.repository.extended.ExtendedDeliveryRequestHeaderRepository;
import jp.co.jun.edi.repository.extended.ExtendedPdfTDeliveryDetailRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.BusinessUtils;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;
import jp.co.jun.edi.util.StringUtils;

/**
 * PDFファイルを作成するコンポーネント.
 */
@Component
public class DeliveryRequestCreateXmlComponent {
    private static final int SUBSTRING_CUT_6 = 6;
    private static final int SUBSTRING_CUT_61 = 61;
    private static final int SUBSTRING_CUT_101 = 101;

    private static final int RECORD_SIZE = 15;

    private static final List<List<String>> ARRAY_DIVISION = Arrays.asList(
            Arrays.asList("11", "東京１課"),
            Arrays.asList("12", "東京２課"),
            Arrays.asList("13", "東京３課"),
            Arrays.asList("14", "東京４課"),
            Arrays.asList("15", "東京５課"),
            Arrays.asList("16", "東京６課"),
            Arrays.asList("17", "東京７課"),
            Arrays.asList("", ""),
            Arrays.asList("21", "関西１課"),
            Arrays.asList("22", "中国１課"),
            Arrays.asList("", ""),
            Arrays.asList("18", "縫製検品"));

    @Autowired
    private ExtendedDeliveryRequestDetailRepository extendedDeliveryRequestDetailRepository;
    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ExtendedDeliveryRequestHeaderRepository extendedDeliveryRequestHeaderRepository;

    @Autowired
    private ExtendedPdfTDeliveryDetailRepository extendedPdfTDeliveryDetailRepository;

    /**
     * PDFファイルを作成する.
     * @param deliveryId 納品ID
     * @param xmlPath XMLファイルパス
     * @throws Exception IOException
     */
    public void createXml(final BigInteger deliveryId, final Path xmlPath) throws Exception {

        //PDF情報取得(ヘッダ)
        final ExtendedDeliveryRequestPdfHeaderEntity extendedDeliveryRequestPdfHeaderEntity = getListDeliveryRequestHeader(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ResultMessages.warning().add(
                                MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                        .message("t_delivery not found.")
                                        .value("delivery_id", deliveryId)
                                        .build())));

        // 納品詳細情報を取得（課コード、依頼No）
        final List<ExtendedPdfTDeliveryDetailEntity> listTDeliveryDetailEntity = getListTDeliveryDetailEntity(deliveryId);
        if (listTDeliveryDetailEntity.isEmpty()) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                    .message("t_delivery_detail not found.")
                                    .value("delivery_id", deliveryId)
                                    .build()));
        }

        // PDF情報取得(明細) （SKU別、課別の納品数集計データ）
        final List<ExtendedDeliveryRequestPdfDetailEntity> listSkuDivisionAggregatedData = getListDeliveryRequestDetail(deliveryId,
                extendedDeliveryRequestPdfHeaderEntity.getBrandCode(), extendedDeliveryRequestPdfHeaderEntity.getItemCode());

        createXml(xmlPath, extendedDeliveryRequestPdfHeaderEntity, listSkuDivisionAggregatedData, listTDeliveryDetailEntity);

    }

    /**
     * XMLを生成する.
     * @param xmlFile xmlファイル
     * @param extendedDeliveryRequestPdfHeaderEntity 納品依頼書PDFのヘッダEntity
     * @param listSkuDivisionAggregatedData SKU別課別集計データ
     * @param listTDeliveryDetailEntity 納品明細情報のEntityのリスト
     */
    private void createXml(final Path xmlFile,
            final ExtendedDeliveryRequestPdfHeaderEntity extendedDeliveryRequestPdfHeaderEntity,
            final List<ExtendedDeliveryRequestPdfDetailEntity> listSkuDivisionAggregatedData,
            final List<ExtendedPdfTDeliveryDetailEntity> listTDeliveryDetailEntity) {

        final DeliveryRequestXmlModel deliveryRequestXmlModel = new DeliveryRequestXmlModel();

        // 詳細情報
        deliveryRequestXmlModel.setPageDetails(generatedPageDetails(listSkuDivisionAggregatedData, listTDeliveryDetailEntity));

        // ヘッダ設定
        deliveryRequestXmlModel
                .setPageHead(generatedPageHead(extendedDeliveryRequestPdfHeaderEntity, listSkuDivisionAggregatedData, listTDeliveryDetailEntity.get(0)));

        // XMLファイルを出力
        JAXB.marshal(deliveryRequestXmlModel, xmlFile.toFile());

    }

    /**
     * ページ詳細 生成.
     * @param listSkuDivisionAggregatedData List<ExtendedDeliveryRequestPdfDetailEntity>
     * @param listTDeliveryDetailEntity List<ExtendedPdfTDeliveryDetailEntity>
     * @return ページ詳細
     */
    private List<DeliveryPageDetailsXmlModel> generatedPageDetails(
            final List<ExtendedDeliveryRequestPdfDetailEntity> listSkuDivisionAggregatedData,
            final List<ExtendedPdfTDeliveryDetailEntity> listTDeliveryDetailEntity) {

        // ページ明細＞ヘッダ 情報は常に同じのため、ループ前に生成する
        final DeliveryHeadXmlModel head = generatedHead(listTDeliveryDetailEntity);

        // ページ明細＞レコードセクション＞送付先 情報は常に同じのため、ループ前に生成する
        final DeliverySendToAddressXmlModel sendToAddress = generatedSendToAddress(listTDeliveryDetailEntity.get(0));

        // ページ明細＞レコードセクション＞合計レコード＞納品場所  情報は常に同じのため、ループ前に生成する
        final List<String> lisetDeliveryLocation = generatedDeliveryLocation(listTDeliveryDetailEntity);

        // ページごとにデータを分割
        final List<List<ExtendedDeliveryRequestPdfDetailEntity>> chunkListSkuDivisionAggregatedData = CollectionUtils
                .chunk(listSkuDivisionAggregatedData, RECORD_SIZE);

        // 最終ページ番号
        final int lastPageNumber = chunkListSkuDivisionAggregatedData.size();
        int pageNumber = 1; // ページNo.

        final List<DeliveryPageDetailsXmlModel> listDeliveryPageDetailsXmlModel = new ArrayList<DeliveryPageDetailsXmlModel>();

        List<Integer> summaryTotalBySection = new ArrayList<Integer>();

        // ページごとにループ
        for (List<ExtendedDeliveryRequestPdfDetailEntity> data : chunkListSkuDivisionAggregatedData) {
            final boolean isLastPage = pageNumber == lastPageNumber;

            // 課別集計
            final List<Integer> summarySection = summaryTotalBySection(data);
            if (summaryTotalBySection.isEmpty()) {
                summaryTotalBySection = summarySection;
            } else {
                summaryTotalBySection = generatedTotalList(summaryTotalBySection, summarySection);
            }

            final DeliveryPageDetailsXmlModel deliveryPageDetailsXmlModel = new DeliveryPageDetailsXmlModel();
            deliveryPageDetailsXmlModel.setHead(head); // ヘッダ
            deliveryPageDetailsXmlModel.setPageNumber(pageNumber); // ページNo
            deliveryPageDetailsXmlModel.setRecordSection(generatedRecordSection(data, sendToAddress,
                    lisetDeliveryLocation, summaryTotalBySection, isLastPage)); // レコードセクション

            // 改ページ
            pageNumber++;

            listDeliveryPageDetailsXmlModel.add(deliveryPageDetailsXmlModel);
        }
        return listDeliveryPageDetailsXmlModel;

    }

    /**
     * リストの同一Index同士を合計する.
     * @param list1 List<Integer>
     * @param list2 List<Integer>
     * @return 合計リスト
     */
    private List<Integer> generatedTotalList(final List<Integer> list1, final List<Integer> list2) {
        final List<Integer> addList = new ArrayList<Integer>();
        int i = 0;
        for (Integer data : list1) {
            if (data == null && list2.get(i) == null) {
                addList.add(null);
            } else if (data == null) {
                addList.add(list2.get(i));
            } else if (list2.get(i) == null) {
                addList.add(data);
            } else {
                addList.add(data + list2.get(i));
            }
            i++;
        }

        return addList;
    }

    /**
     * 納品場所 生成.
     * @param listTDeliveryDetailEntity List<ExtendedPdfTDeliveryDetailEntity>
     * @return 納品場所
     */
    private List<String> generatedDeliveryLocation(final List<ExtendedPdfTDeliveryDetailEntity> listTDeliveryDetailEntity) {
        return ARRAY_DIVISION.stream().map(division -> {

            return listTDeliveryDetailEntity
                    .stream()
                    .filter(entity -> division.get(0).equals(entity.getDivisionCode()))
                    .map(entity -> entity.getDeliveryLocation())
                    .findFirst().orElse("");

        }).collect(Collectors.toList());
    }

    /**
     * レコードセクション 生成.
     * @param listExtendedDeliveryRequestPdfDetailEntity List<ExtendedDeliveryRequestPdfDetailEntity>
     * @param sendToAddress 送付先
     * @param lisetDeliveryLocation 納品場所
     * @param summaryTotalBySection 課別集計
     * @param isLastPage 最終ページフラグ
     * @return レコードセクション
     */
    private DeliveryRecordSectionXmlModel generatedRecordSection(
            final List<ExtendedDeliveryRequestPdfDetailEntity> listExtendedDeliveryRequestPdfDetailEntity,
            final DeliverySendToAddressXmlModel sendToAddress,
            final List<String> lisetDeliveryLocation,
            final List<Integer> summaryTotalBySection,
            final boolean isLastPage) {
        final DeliveryRecordSectionXmlModel deliveryRecordSectionXmlModel = new DeliveryRecordSectionXmlModel();
        deliveryRecordSectionXmlModel.setDetails(generatedDetails(listExtendedDeliveryRequestPdfDetailEntity)); // 明細
        deliveryRecordSectionXmlModel.setSendToAddress(sendToAddress); // 送付先
        deliveryRecordSectionXmlModel.setTotalRecord(generatedTotalRecord(listExtendedDeliveryRequestPdfDetailEntity,
                lisetDeliveryLocation, summaryTotalBySection, isLastPage)); // 合計レコード
        return deliveryRecordSectionXmlModel;
    }

    /**
     * 合計レコード 生成.
     * @param listExtendedDeliveryRequestPdfDetailEntity List<ExtendedDeliveryRequestPdfDetailEntity>
     * @param lisetDeliveryLocation 納品場所
     * @param summaryTotalBySection 課別集計
     * @param isLastPage 最終ページフラグ
     * @return 合計レコード
     */
    private DeliveryTotalRecordXmlModel generatedTotalRecord(final List<ExtendedDeliveryRequestPdfDetailEntity> listExtendedDeliveryRequestPdfDetailEntity,
            final List<String> lisetDeliveryLocation,
            final List<Integer> summaryTotalBySection,
            final boolean isLastPage) {

        final DeliveryTotalRecordXmlModel deliveryTotalRecordXmlModel = new DeliveryTotalRecordXmlModel();
        deliveryTotalRecordXmlModel.setTotalBySection(generatedTotalBySection(summaryTotalBySection)); // 課別合計
        deliveryTotalRecordXmlModel.setOverallTotal(generatedOverallTotal(summaryTotalBySection)); // 全体合計
        deliveryTotalRecordXmlModel.setDeliveryLocation(lisetDeliveryLocation); // 納品場所
        if (isLastPage) {
            deliveryTotalRecordXmlModel.setWholeSubtotalChar(""); // 小計
            deliveryTotalRecordXmlModel.setWholeTotalAmountChar("合計"); // 合計
        } else {
            deliveryTotalRecordXmlModel.setWholeSubtotalChar("小計"); // 小計
            deliveryTotalRecordXmlModel.setWholeTotalAmountChar(""); // 合計
        }
        return deliveryTotalRecordXmlModel;
    }

    /**
     * 全体合計 生成.
     * @param summaryTotalBySection 課別集計
     * @return 全体合計
     */
    private String generatedOverallTotal(final List<Integer> summaryTotalBySection) {
        final int total = summaryTotalBySection.stream().mapToInt(summary -> Optional.ofNullable(summary).orElse(0)).sum();
        return String.valueOf(total);
    }

    /**
     * 課別合計 生成.
     * @param listExtendedDeliveryRequestPdfDetailEntity List<ExtendedDeliveryRequestPdfDetailEntity>
     * @return 課別合計
     */
    private List<Integer> summaryTotalBySection(final List<ExtendedDeliveryRequestPdfDetailEntity> listExtendedDeliveryRequestPdfDetailEntity) {
        int sumDivisionCode11 = 0;
        int sumDivisionCode12 = 0;
        int sumDivisionCode13 = 0;
        int sumDivisionCode14 = 0;
        int sumDivisionCode15 = 0;
        int sumDivisionCode16 = 0;
        int sumDivisionCode17 = 0;
        int sumDivisionCode21 = 0;
        int sumDivisionCode22 = 0;
        int sumDivisionCode18 = 0;

        for (ExtendedDeliveryRequestPdfDetailEntity entity : listExtendedDeliveryRequestPdfDetailEntity) {
            sumDivisionCode11 += entity.getDivisionCode11(); // 東京1課
            sumDivisionCode12 += entity.getDivisionCode12(); // 東京2課
            sumDivisionCode13 += entity.getDivisionCode13(); // 東京3課
            sumDivisionCode14 += entity.getDivisionCode14(); // 東京4課
            sumDivisionCode15 += entity.getDivisionCode15(); // 東京5課
            sumDivisionCode16 += entity.getDivisionCode16(); // 東京6課
            sumDivisionCode17 += entity.getDivisionCode17(); // 東京7課

            sumDivisionCode21 += entity.getDivisionCode21(); // 関西1課
            sumDivisionCode22 += entity.getDivisionCode22(); // 中国1課

            sumDivisionCode18 += entity.getDivisionCode18(); // 縫製検品
        }

        final List<Integer> listTotalBySection = new ArrayList<Integer>();
        listTotalBySection.add(sumDivisionCode11); // 東京1課
        listTotalBySection.add(sumDivisionCode12); // 東京2課
        listTotalBySection.add(sumDivisionCode13); // 東京3課
        listTotalBySection.add(sumDivisionCode14); // 東京4課
        listTotalBySection.add(sumDivisionCode15); // 東京5課
        listTotalBySection.add(sumDivisionCode16); // 東京6課
        listTotalBySection.add(sumDivisionCode17); // 東京7課
        listTotalBySection.add(null); // 空欄
        listTotalBySection.add(sumDivisionCode21); // 関西1課
        listTotalBySection.add(sumDivisionCode22); // 中国1課
        listTotalBySection.add(null); //空欄
        listTotalBySection.add(sumDivisionCode18); // 縫製検品

        return listTotalBySection;
    }

    /**
     * 課別合計 生成.
     * @param summaryTotalBySection 課別集計
     * @return 課別合計
     */
    private List<String> generatedTotalBySection(final List<Integer> summaryTotalBySection) {
        return summaryTotalBySection.stream().map(summary -> {
            if (summary == null) {
                return "";
            }
            return StringUtils.convertIntToStringZeroIsBlank(summary);
        }).collect(Collectors.toList());

    }

    /**
     * 送付先 生成.
     * @param extendedPdfTDeliveryDetailEntity ExtendedPdfTDeliveryDetailEntity
     * @return 送付先
     */
    private DeliverySendToAddressXmlModel generatedSendToAddress(final ExtendedPdfTDeliveryDetailEntity extendedPdfTDeliveryDetailEntity) {
        final DeliverySendToAddressXmlModel deliverySendToAddressXmlModel = new DeliverySendToAddressXmlModel();
        deliverySendToAddressXmlModel.setNFax(extendedPdfTDeliveryDetailEntity.getFax()); // FAXNo
        deliverySendToAddressXmlModel.setAddressLabel(extendedPdfTDeliveryDetailEntity.getCompanyName()); // 住所ラベル
        deliverySendToAddressXmlModel.setNPostLabel(extendedPdfTDeliveryDetailEntity.getPostalCode()); // 郵便番号
        deliverySendToAddressXmlModel.setNAddress(extendedPdfTDeliveryDetailEntity.getAddress()); // 住所
        deliverySendToAddressXmlModel.setNPhoneNumber(extendedPdfTDeliveryDetailEntity.getTel()); // TEL
        deliverySendToAddressXmlModel.setShootingCautionLabel(""); // 撮影用注意ラベル 空欄
        deliverySendToAddressXmlModel.setCargoAt(""); // 出荷日 空欄
        deliverySendToAddressXmlModel.setShippingCompany(""); // 運送会社 空欄
        deliverySendToAddressXmlModel.setInvoice(""); // 送り状No 空欄
        deliverySendToAddressXmlModel.setRemarks(""); // 備考 空欄

        return deliverySendToAddressXmlModel;
    }

    /**
     * 明細 生成.
     * @param listExtendedDeliveryRequestPdfDetailEntity List<ExtendedDeliveryRequestPdfDetailEntity>
     * @return 明細
     */
    private List<DeliveryDetailsXmlModel> generatedDetails(final List<ExtendedDeliveryRequestPdfDetailEntity> listExtendedDeliveryRequestPdfDetailEntity) {
        return listExtendedDeliveryRequestPdfDetailEntity.stream()
                .map(entity -> {
                    final DeliveryDetailsXmlModel deliveryDetailsXmlModel = new DeliveryDetailsXmlModel();
                    deliveryDetailsXmlModel.setSize(entity.getSize()); // サイズ
                    deliveryDetailsXmlModel.setColorCode(entity.getColorCode()); // カラーコード
                    deliveryDetailsXmlModel.setColorCodeName(StringUtils.substring(entity.getColorCodeName(), SUBSTRING_CUT_6)); // カラーコード名
                    deliveryDetailsXmlModel.setOrderDeliveryLot(generatedListOrderDeliveryLot(entity)); //納品依頼数
                    deliveryDetailsXmlModel.setColorSizeSubtotal(StringUtils.convertIntToStringZeroIsBlank(generatedColorSizeSubtotal(entity))); // カラー・サイズ小計
                    return deliveryDetailsXmlModel;
                }).collect(Collectors.toList());

    }

    /**
     * DeliveryHeadXmlModel 生成.
     * @param listTDeliveryDetailEntity TDeliveryDetailEntityリスト
     * @return DeliveryHeadXmlModel
     */
    private DeliveryHeadXmlModel generatedHead(final List<ExtendedPdfTDeliveryDetailEntity> listTDeliveryDetailEntity) {

        final List<DeliveryHeadRecordXmlModel> headRecord = ARRAY_DIVISION.stream().map(division -> {

            final DeliveryHeadRecordXmlModel deliveryHeadRecordXmlModel = new DeliveryHeadRecordXmlModel();
            deliveryHeadRecordXmlModel.setDivisionCode(division.get(0)); // 課コード
            deliveryHeadRecordXmlModel.setDivisionName(division.get(1)); // 課名称
            deliveryHeadRecordXmlModel.setRequestNumber(listTDeliveryDetailEntity
                    .stream()
                    .filter(entity -> division.get(0).equals(entity.getDivisionCode()))
                    .map(entity -> entity.getDeliveryRequestNumber())
                    .findFirst()
                    .orElse("")); // 依頼No.

            return deliveryHeadRecordXmlModel;
        }).collect(Collectors.toList());

        final DeliveryHeadXmlModel deliveryHeadXmlModel = new DeliveryHeadXmlModel();
        deliveryHeadXmlModel.setHeadRecord(headRecord);

        return deliveryHeadXmlModel;
    }

    /**
     * カラー・サイズ小計 生成.
     * @param entity ExtendedDeliveryRequestPdfDetailEntity
     * @return カラー・サイズ小計
     */
    private int generatedColorSizeSubtotal(final ExtendedDeliveryRequestPdfDetailEntity entity) {
        final int subtotal = entity.getDivisionCode11()
                + entity.getDivisionCode12()
                + entity.getDivisionCode13()
                + entity.getDivisionCode14()
                + entity.getDivisionCode15()
                + entity.getDivisionCode16()
                + entity.getDivisionCode17()
                + entity.getDivisionCode18()
                + entity.getDivisionCode21()
                + entity.getDivisionCode22();

        return subtotal;
    }

    /**
     * XMLページヘッダにDBヘッダの値を格納する処理.
     * @param entity 納品依頼書PDFのヘッダEntity
     * @param listSkuDivisionAggregatedData SKU別課別集計データ
     * @param tDeliveryDetailEntity 納品詳細情報
     * @return XMLページヘッダ
     */
    private DeliveryPageHeadXmlModel generatedPageHead(final ExtendedDeliveryRequestPdfHeaderEntity entity,
            final List<ExtendedDeliveryRequestPdfDetailEntity> listSkuDivisionAggregatedData,
            final ExtendedPdfTDeliveryDetailEntity tDeliveryDetailEntity) {

        final DeliveryPageHeadXmlModel xmlModel = new DeliveryPageHeadXmlModel();

        final BigDecimal unitPrice;

        // 単価算出。B級品単価の時は、B級品単価を使用する。
        if (entity.getNonConformingProductType().convertToValue()) {
            unitPrice = NumberUtils.defaultInt(entity.getNonConformingProductUnitPrice());
        } else {
            unitPrice = NumberUtils.defaultInt(entity.getUnitPrice());
        }
        xmlModel.setDeliveryAt(DateUtils.formatFromDate(tDeliveryDetailEntity.getCorrectionAt(), "yyyy年MM月dd日")); // 納品日付
        xmlModel.setDeliveryNumber(tDeliveryDetailEntity.getDeliveryNumber()); // 納品
        xmlModel.setOrderNumber(entity.getOrderNumber()); // 発注No
        xmlModel.setDeliveryCount(String.format("%2s", entity.getDeliveryCount()).replace(" ", "0")); // 回数（前ゼロ2桁）
        xmlModel.setMdfMakerCode(entity.getMdfMakerCode()); // 取引先コード
        xmlModel.setName(StringUtils.substring(entity.getName(), SUBSTRING_CUT_61)); // 取引先名称 (61文字以降切捨て)
        xmlModel.setPartNo(BusinessUtils.formatPartNo(entity.getPartNo())); // 品番
        xmlModel.setProductName(StringUtils.substring(entity.getProductName(), SUBSTRING_CUT_101)); // 品名（101文字以降切捨て）
        xmlModel.setRetailPrice(entity.getRetailPrice()); // 上代
        xmlModel.setDeliveryLot(generatedDeliveryLot(listSkuDivisionAggregatedData)); // 数量
        xmlModel.setUnitPrice(unitPrice); // 単価
        xmlModel.setPrice(generatedPrice(xmlModel.getDeliveryLot(), xmlModel.getUnitPrice())); // 金額
        xmlModel.setAddress(propertyComponent.getBatchProperty().getDeliveryRequestAddress()); // 住所
        xmlModel.setCompanyName(entity.getCompanyName()); // 会社名
        xmlModel.setBrandName(entity.getBrandName()); // ブランド名

        return xmlModel;
    }

    /**
     * 数量 生成.
     * @param listSkuDivisionAggregatedData SKU別課別集計データ
     * @return 数量
     */
    private BigDecimal generatedDeliveryLot(final List<ExtendedDeliveryRequestPdfDetailEntity> listSkuDivisionAggregatedData) {
        return BigDecimal.valueOf(summaryTotalBySection(listSkuDivisionAggregatedData)
                .stream()
                .mapToLong(data -> Optional.ofNullable(data).orElse(0))
                .sum());
    }

    /**
     * 金額 生成.
     * @param deliveryLot 数量
     * @param unitPrice 単価
     * @return 金ｓｓ
     */
    private BigDecimal generatedPrice(final BigDecimal deliveryLot, final BigDecimal unitPrice) {
        return deliveryLot.multiply(unitPrice).setScale(0, RoundingMode.DOWN);
    }

    /**
     * 納品依頼数、カラー小計を出力する処理.
     * @param entity 納品依頼書PDFの明細Entity
     * @return 納品依頼数
     */
    private List<String> generatedListOrderDeliveryLot(final ExtendedDeliveryRequestPdfDetailEntity entity) {
        final List<String> listOrderDeliveryLot = new ArrayList<>();
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode11())); // 東京1課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode12())); // 東京2課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode13())); // 東京3課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode14())); // 東京4課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode15())); // 東京5課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode16())); // 東京6課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode17())); // 東京7課
        listOrderDeliveryLot.add(""); // 空欄
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode21())); // 関西1課
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode22())); // 中国1課
        listOrderDeliveryLot.add(""); // 空欄
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(entity.getDivisionCode18())); // 縫製検品
        listOrderDeliveryLot.add(""); // 小計（左）空欄
        listOrderDeliveryLot.add(StringUtils.convertIntToStringZeroIsBlank(generatedColorSizeSubtotal(entity))); // 小計（右）色・サイズの小計

        return listOrderDeliveryLot;
    }

    /**
     * 処理前の納品依頼メール送信の明細情報 を取得する.
     * @param deliveryId 納品ID
     * @param brandCode ブランドコード
     * @param itemCode アイテムコード
     * @return 処理前の納品依頼メール送信の明細情報のリスト
     */
    private List<ExtendedDeliveryRequestPdfDetailEntity> getListDeliveryRequestDetail(final BigInteger deliveryId, final String brandCode,
            final String itemCode) {
        final String partNoKind = brandCode.concat(itemCode);
        return extendedDeliveryRequestDetailRepository.findByDeliveryId(deliveryId, partNoKind, PageRequest.of(0, Integer.MAX_VALUE)).getContent();

    }

    /**
     * 課コード、依頼Noのリスト取得 を取得する.
     * @param deliveryId 納品ID
     * @return 納品詳細情報のリスト
     */
    private List<ExtendedPdfTDeliveryDetailEntity> getListTDeliveryDetailEntity(final BigInteger deliveryId) {
        return extendedPdfTDeliveryDetailRepository.findByDeliveryId(deliveryId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * 処理前の納品依頼メール送信のヘッダー情報 を取得する.
     * @param deliveryId 納品ID
     * @return 処理前の納品依頼メール送信のヘッダー情報のリスト
     */
    private Optional<ExtendedDeliveryRequestPdfHeaderEntity> getListDeliveryRequestHeader(final BigInteger deliveryId) {
        return extendedDeliveryRequestHeaderRepository.findByDeliveryId(deliveryId);
    }

}
