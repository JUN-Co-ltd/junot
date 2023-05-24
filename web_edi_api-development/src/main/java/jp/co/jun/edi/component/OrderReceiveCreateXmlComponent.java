package jp.co.jun.edi.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.order.OrderDetailOfQualityDisplayXmlModel;
import jp.co.jun.edi.component.model.order.OrderDetailsXmlModel;
import jp.co.jun.edi.component.model.order.OrderHeadRecordXmlModel;
import jp.co.jun.edi.component.model.order.OrderHeadXmlModel;
import jp.co.jun.edi.component.model.order.OrderPageDetailsXmlModel;
import jp.co.jun.edi.component.model.order.OrderPageHeadXmlModel;
import jp.co.jun.edi.component.model.order.OrderPageQualityDisplayXmlModel;
import jp.co.jun.edi.component.model.order.OrderQualityLabelHeadXmlModel;
import jp.co.jun.edi.component.model.order.OrderQualityLabelXmlModel;
import jp.co.jun.edi.component.model.order.OrderReceiveXmlModel;
import jp.co.jun.edi.component.model.order.OrderRecordSectionXmlModel;
import jp.co.jun.edi.component.model.order.OrderTotalRecordXmlModel;
import jp.co.jun.edi.entity.extended.ExtendedTCompositionEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderPDFEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderSkuEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.extended.ExtendedTCompositionRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderPDFRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderSkuRepository;
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
public class OrderReceiveCreateXmlComponent {
    /** 共通のカラーコード：00. */
    private static final String COMMON_COLOR_CODE = "00";

    /** 1ページに表示可能なカラーの最大値. */
    private static final int COLOR_DISPLAY_MAX_SIZE = 10;

    /** 1ページに表示可能なサイズの最大値. */
    private static final int SIZE_DISPLAY_MAX_SIZE = 10;

    /** 品質表示部.最大表示個数. */
    private static final int ORDER_PAGE_QUALITY_DISPLAY_QUALITY_LABEL_MAX_SIZE = 12;

    /** 6文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_6 = 6;
    /** 13文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_13 = 13;
    /** 16文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_16 = 16;
    /** 101文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_101 = 101;
    /** 301文字目の文字切捨て. */
    private static final int SUBSTRING_CUT_301 = 301;

    /** 切り捨て桁数. */
    private static final int ROUNDING_DOWN_NUMBER = 4;
    /** 百分率. */
    private static final BigDecimal PERCENTAGE = BigDecimal.valueOf(100);

    @Autowired
    private ExtendedTOrderSkuRepository extendedTOrderSkuRepository;
    @Autowired
    private ExtendedTCompositionRepository extendedTCompositionRepository;
    @Autowired
    private ExtendedTOrderPDFRepository extendedTOrderPDFRepository;

    /**
     * XMLデータファイルを生成する.
     * @param orderId 発注ID
     * @param xmlPath XMLファイルパス
     * @throws Exception 例外
     * @throws IOException 例外
     */
    public void createXml(final BigInteger orderId, final Path xmlPath) throws Exception, IOException {
        // PDF作成用の発注情報を取得する
        final ExtendedTOrderPDFEntity order = getOrderPdfInfo(orderId).orElseThrow(() -> new ResourceNotFoundException(
                ResultMessages.warning().add(
                        MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                .message("t_order not found.")
                                .value("id", orderId)
                                .build())));

        // 発注SKU情報を取得する
        final List<ExtendedTOrderSkuEntity> skuInfo = getOrderSkuInfo(orderId, order.getBrandCode() + order.getItemCode()).getContent();
        if (skuInfo.isEmpty()) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                    .message("t_order_sku not found.")
                                    .value("orderId", orderId)
                                    .value("partNoKind", order.getBrandCode() + order.getItemCode())
                                    .build()));
        }

        // 組成情報を取得する(組成がないケースもあるため、組成なしではエラーとしない)
        final List<ExtendedTCompositionEntity> qualityInfo = getQualityInfo(order.getPartNoId()).getContent();

        // XMLデータを生成
        final String orderReceiveXml = genaratedXmlData(skuInfo, qualityInfo, order);

        // XMLファイルを一時ディレクトリへ出力
        try (BufferedWriter writer = Files.newBufferedWriter(xmlPath)) {
            writer.append(orderReceiveXml);
        }
    }

    /**
     *  発注SKU情報を取得.
     *  @param orderId 発注ID
     *  @param partNoKind 品種
     *  @return 発注SKU情報
     */
    private Page<ExtendedTOrderSkuEntity> getOrderSkuInfo(final BigInteger orderId, final String partNoKind) {
        return extendedTOrderSkuRepository.findByOrderId(orderId, partNoKind, PageRequest.of(0, Integer.MAX_VALUE));
    }

    /**
     *  品質情報を取得.
     *  @param partNoId 品番ID
     *  @return 品質情報
     */
    private Page<ExtendedTCompositionEntity> getQualityInfo(final BigInteger partNoId) {
        return extendedTCompositionRepository.findByPartNoId(partNoId,
                PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Order.asc("color_code"), Order.asc("serial_number"))));
    }

    /**
     *  PDF作成用情報取得.
     *  @param orderId 発注ID
     *  @return PDF作成情報
     */
    private Optional<ExtendedTOrderPDFEntity> getOrderPdfInfo(final BigInteger orderId) {
        return extendedTOrderPDFRepository.findByOrderId(orderId);
    }

    /**
     * XMLデータ生成.
     * @param orderSkuList 発注SKU情報
     * @param compositionList 組成情報
     * @param order 発注情報
     * @return XMLデータ
     */
    private String genaratedXmlData(final List<ExtendedTOrderSkuEntity> orderSkuList, final List<ExtendedTCompositionEntity> compositionList,
            final ExtendedTOrderPDFEntity order) {
        // 発注SKU情報をカラーコード、サイズごとにグルーピングする
        final Map<String, Map<String, ExtendedTOrderSkuEntity>> mapColorCodeOrderSku = orderSkuList.stream()
                .collect(Collectors.groupingBy(o -> o.getColorCode(), Collectors.toMap(o -> o.getSize(), o -> o)));

        // 組成情報をカラーコードごとにグルーピングする
        final Map<String, List<ExtendedTCompositionEntity>> mapColorCodeComposition = compositionList.stream()
                .collect(Collectors.groupingBy(ExtendedTCompositionEntity::getColorCode));

        // サイズリストを取得する
        final List<String> listSize = getUniqueSizeList(orderSkuList);

        final OrderReceiveXmlModel orderReceiveXmlModel = new OrderReceiveXmlModel();

        // 詳細情報
        orderReceiveXmlModel.setPageDetails(genaratedPageDatails(mapColorCodeOrderSku, listSize));

        // 品質情報の開始ページ番号
        int nextPageNumber = orderReceiveXmlModel.getPageDetails().size() + 1;

        // 品質情報
        orderReceiveXmlModel.setPageQualityDisplay(genaratedOrderPageQualityDisplay(nextPageNumber, mapColorCodeComposition));

        // ページ総数
        final int totalPageNumber = orderReceiveXmlModel.getPageDetails().size() + orderReceiveXmlModel.getPageQualityDisplay().size();

        // ヘッダー情報
        orderReceiveXmlModel.setPageHead(genaratedOrderPageHeadXmlModel(order, totalPageNumber, compositionList));

        // ModelをXMLデータに変換
        final StringWriter orderReceiveXml = new StringWriter();
        JAXB.marshal(orderReceiveXmlModel, orderReceiveXml);

        return orderReceiveXml.toString();
    }

    /**
     * OrderPageQualityDisplayXmlModel生成.
     * @param pageNumber 品質情報の開始ページ番号
     * @param mapColorCodeComposition 組成情報
     * @return OrderPageQualityDisplayXmlModelリスト
     */
    private List<OrderPageQualityDisplayXmlModel> genaratedOrderPageQualityDisplay(final Integer pageNumber,
            final Map<String, List<ExtendedTCompositionEntity>> mapColorCodeComposition) {

        // カラーコード（共通）を除く、カラーコード昇順の組成XMLモデルを取得する
        final List<OrderQualityLabelXmlModel> listQualityLabel = genaratedOrderQualityLabelXmlModel(mapColorCodeComposition);

        // 1ページに表示可能な最大数ごとにページをわける
        final List<List<OrderQualityLabelXmlModel>> pageOrderQualityLabelXmlModel = CollectionUtils.chunk(listQualityLabel,
                ORDER_PAGE_QUALITY_DISPLAY_QUALITY_LABEL_MAX_SIZE);

        // 品質表示情報の格納
        final List<OrderPageQualityDisplayXmlModel> list = pageOrderQualityLabelXmlModel.stream().map(entity -> {
            final OrderPageQualityDisplayXmlModel orderPageQualityDisplayXmlModel = new OrderPageQualityDisplayXmlModel();
            orderPageQualityDisplayXmlModel.setQualityLabel(entity);
            return orderPageQualityDisplayXmlModel;
        }).collect(Collectors.toList());

        // ページ番号の格納
        int nextPageNumber = pageNumber.intValue();
        for (OrderPageQualityDisplayXmlModel model : list) {
            model.setPageNumber(nextPageNumber);
            nextPageNumber++;
        }

        return list;
    }

    /**
     * OrderQualityLabelXmlModel生成.
     * @param mapColorCodeComposition カラーコードごとにグルーピングされた組成情報
     * @return OrderQualityLabelXmlModelリスト
     */
    private List<OrderQualityLabelXmlModel> genaratedOrderQualityLabelXmlModel(final Map<String, List<ExtendedTCompositionEntity>> mapColorCodeComposition) {
        // カラーコード（共通）を除く、カラーコード昇順の組成情報をXMLモデルに変換
        return mapColorCodeComposition.entrySet()
                .stream()
                .filter(value -> !value.getKey().equals(COMMON_COLOR_CODE))
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(value -> {
                    final OrderQualityLabelXmlModel orderQualityLabelXmlModel = new OrderQualityLabelXmlModel();
                    orderQualityLabelXmlModel.setColorCode(value.getKey());
                    orderQualityLabelXmlModel.setDetailOfQualityDisplay(genaratedDetailOfQualityDisplay(value.getValue()));

                    return orderQualityLabelXmlModel;

                })
                .collect(Collectors.toList());
    }

    /**
     * OrderDetailOfQualityDisplayXmlModel生成.
     * @param compositionList 組成情報
     * @return OrderDetailOfQualityDisplayXmlModelリスト
     */
    private List<OrderDetailOfQualityDisplayXmlModel> genaratedDetailOfQualityDisplay(
            final List<ExtendedTCompositionEntity> compositionList) {
        return compositionList.stream()
                .map(composition -> {
                    final OrderDetailOfQualityDisplayXmlModel orderDetailOfQualityDisplayXmlModel = new OrderDetailOfQualityDisplayXmlModel();
                    orderDetailOfQualityDisplayXmlModel
                            .setQualityDisplayParts(substringSuffixThreePointReader(composition.getPartsName(), SUBSTRING_CUT_13));
                    orderDetailOfQualityDisplayXmlModel
                            .setQualityDisplayComposition(substringSuffixThreePointReader(composition.getCompositionName(), SUBSTRING_CUT_13));
                    orderDetailOfQualityDisplayXmlModel.setQualityDisplayRate(composition.getPercent());
                    return orderDetailOfQualityDisplayXmlModel;
                }).collect(Collectors.toList());
    }

    /**
     * OrderPageDetailsXmlModel生成.
     * @param mapColorCodeOrderSku カラーコード、サイズごとにグルーピングした発注SKU情報
     * @param listSize サイズ一覧
     * @return OrderPageDetailsXmlModelリスト
     */
    private List<OrderPageDetailsXmlModel> genaratedPageDatails(final Map<String, Map<String, ExtendedTOrderSkuEntity>> mapColorCodeOrderSku,
            final List<String> listSize) {
        // ページごとのカラーリストを生成
        final List<List<String>> colorByPageList = CollectionUtils.chunk(
                mapColorCodeOrderSku.keySet().stream()
                        // カラーコードでソートする
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toList()),
                COLOR_DISPLAY_MAX_SIZE);

        // ページごとのサイズリストを生成
        final List<List<String>> sizeByPageList = CollectionUtils.chunk(listSize, SIZE_DISPLAY_MAX_SIZE);

        final List<OrderPageDetailsXmlModel> orderPageDetailsXmlModelList = new ArrayList<>();

        // ページ番号
        int pageNumber = 1;

        for (final List<String> colorList : colorByPageList) {
            for (final List<String> sizeList : sizeByPageList) {
                final OrderPageDetailsXmlModel orderPageDetailsXmlModel = new OrderPageDetailsXmlModel();
                orderPageDetailsXmlModel.setHead(genaratedOrderHeadXmlModel(sizeList));
                orderPageDetailsXmlModel.setRecordSection(genaratedOrderRecordSectionXmlModel(
                        mapColorCodeOrderSku,
                        colorList,
                        sizeList));
                orderPageDetailsXmlModel.setPageNumber(pageNumber);
                pageNumber++;

                orderPageDetailsXmlModelList.add(orderPageDetailsXmlModel);
            }
        }

        return orderPageDetailsXmlModelList;
    }

    /**
     * OrderRecordSectionXmlModel生成.
     * @param mapColorCodeOrderSku カラーコード、サイズごとにグルーピングした発注SKU情報
     * @param colorList カラーリスト
     * @param sizeList サイズリスト
     * @return OrderRecordSectionXmlModel
     */
    private OrderRecordSectionXmlModel genaratedOrderRecordSectionXmlModel(
            final Map<String, Map<String, ExtendedTOrderSkuEntity>> mapColorCodeOrderSku,
            final List<String> colorList,
            final List<String> sizeList) {
        final OrderRecordSectionXmlModel orderRecordSectionXmlModel = new OrderRecordSectionXmlModel();
        orderRecordSectionXmlModel.setDetails(genaratedOrderDetailsXmlModel(
                mapColorCodeOrderSku,
                colorList,
                sizeList));
        orderRecordSectionXmlModel.setTotalRecord(genaratedOrderTotalRecordXmlModel(
                mapColorCodeOrderSku,
                sizeList));

        return orderRecordSectionXmlModel;
    }

    /**
     * OrderDetailsXmlModel生成.
     * @param mapColorCodeOrderSku カラーコード、サイズごとにグルーピングした発注SKU情報
     * @param colorList カラーリスト
     * @param sizeList サイズリスト
     * @return OrderDetailsXmlModelリスト
     */
    private List<OrderDetailsXmlModel> genaratedOrderDetailsXmlModel(
            final Map<String, Map<String, ExtendedTOrderSkuEntity>> mapColorCodeOrderSku,
            final List<String> colorList,
            final List<String> sizeList) {
        return colorList.stream().map(color -> {
            // カラーコードが一致する発注SKU情報を取得
            final Map<String, ExtendedTOrderSkuEntity> mapSizeOrderSku = mapColorCodeOrderSku.get(color);

            // 先頭の1件を取得（必ず取得可能）
            final ExtendedTOrderSkuEntity sizeOrderSku = mapSizeOrderSku.values().stream().findFirst().get();

            final OrderDetailsXmlModel orderDetailsXmlModel = new OrderDetailsXmlModel();

            // カラーコードを設定
            orderDetailsXmlModel.setColorCode(sizeOrderSku.getColorCode());
            // カラー名称を設定（6桁以降は削除）
            orderDetailsXmlModel.setColorName(StringUtils.substring(sizeOrderSku.getColorName(), SUBSTRING_CUT_6));
            // 反数を設定
            orderDetailsXmlModel.setClothCount("");
            // メーター数を設定
            orderDetailsXmlModel.setQuantityDividedMeter("");
            // カラー別の製品発注数の合計を設定（全てのページには合計を表示する）
            orderDetailsXmlModel.setTotalAmount(StringUtils.convertIntToStringZeroIsBlank(
                    mapSizeOrderSku.values().stream().mapToInt((orderSku) -> orderSku.getProductOrderLot()).sum()));
            // カラー別サイズ別の数量を設定
            orderDetailsXmlModel.setSizeQuantity(
                    sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(getProductOrderLot(mapSizeOrderSku.get(size))))
                            .collect(Collectors.toList()));

            return orderDetailsXmlModel;
        }).collect(Collectors.toList());
    }

    /**
     * OrderTotalRecordXmlModel生成.
     * @param mapColorCodeOrderSku カラーコード、サイズごとにグルーピングした発注SKU情報
     * @param sizeList サイズリスト
     * @return OrderTotalRecordXmlModel
     */
    private OrderTotalRecordXmlModel genaratedOrderTotalRecordXmlModel(
            final Map<String, Map<String, ExtendedTOrderSkuEntity>> mapColorCodeOrderSku,
            final List<String> sizeList) {
        final OrderTotalRecordXmlModel orderTotalRecordXmlModel = new OrderTotalRecordXmlModel();

        // 全てのページには合計を表示する
        orderTotalRecordXmlModel.setWholeSubtotalChar("");
        orderTotalRecordXmlModel.setWholeTotalAmountChar("合計");

        // 全ての製品発注数の合計
        orderTotalRecordXmlModel.setWholeSubtotalNum(StringUtils.convertIntToStringZeroIsBlank(
                mapColorCodeOrderSku.values().stream()
                        .mapToInt(mapSizeOrderSku -> mapSizeOrderSku.values().stream().mapToInt(orderSku -> orderSku.getProductOrderLot()).sum()).sum()));

        // サイズ別の製品発注数の合計
        orderTotalRecordXmlModel.setSizeSubtotal(
                sizeList.stream().map(size -> StringUtils.convertIntToStringZeroIsBlank(
                        mapColorCodeOrderSku.values().stream().mapToInt((mapSizeOrderSku) -> getProductOrderLot(mapSizeOrderSku.get(size))).sum()))
                        .collect(Collectors.toList()));

        return orderTotalRecordXmlModel;
    }

    /**
     * 製品発注数を取得する.発注SKU情報がnullの場合は、0を返却する.
     * @param extendedTOrderSkuEntity 発注SKU情報
     * @return 製品発注数
     */
    private int getProductOrderLot(
            final ExtendedTOrderSkuEntity extendedTOrderSkuEntity) {
        if (extendedTOrderSkuEntity == null) {
            return 0;
        }

        return extendedTOrderSkuEntity.getProductOrderLot();
    }

    /**
     * OrderHeadXmlModel生成.
     * @param listSize サイズリスト
     * @return OrderHeadXmlModel
     */
    private OrderHeadXmlModel genaratedOrderHeadXmlModel(final List<String> listSize) {
        final OrderHeadXmlModel orderHeadXmlModel = new OrderHeadXmlModel();
        orderHeadXmlModel.setHeadRecord(genaratedOrderHeadRecordXmlModel(listSize));
        // 全てのページには合計を表示する
        orderHeadXmlModel.setSubtotal("");
        orderHeadXmlModel.setTotalAmount("合計");
        return orderHeadXmlModel;
    }

    /**
     * OrderHeadRecordXmlModel生成.
     * @param listSize サイズリスト
     * @return OrderHeadRecordXmlModel
     */
    private OrderHeadRecordXmlModel genaratedOrderHeadRecordXmlModel(final List<String> listSize) {
        final OrderHeadRecordXmlModel orderHeadRecordXmlModel = new OrderHeadRecordXmlModel();
        orderHeadRecordXmlModel.setSize(listSize);
        return orderHeadRecordXmlModel;
    }

    /**
     * ソート順でソートした重複しないサイズコードリストを取得.
     * @param orderSkuList 発注SKU情報
     * @return サイズコードリスト
     */
    private List<String> getUniqueSizeList(final List<ExtendedTOrderSkuEntity> orderSkuList) {
        return orderSkuList.stream().sorted(
                // ソート順の昇順でソートする。サイズマスタには必ずソート順はあるが、存在しない場合も考慮。
                Comparator.comparing(ExtendedTOrderSkuEntity::getSortOrder, Comparator.nullsFirst(Comparator.naturalOrder()))
                        // ソート順がnullの場合は、サイズの昇順でソートする
                        .thenComparing(Comparator.comparing(ExtendedTOrderSkuEntity::getSize, Comparator.nullsFirst(Comparator.naturalOrder()))))
                .map(orderSku -> orderSku.getSize())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * OrderPageHeadXmlModel生成.
     * @param order 発注情報
     * @param totalPageNumber 全体ページNo
     * @param compositionList 組成情報
     * @return OrderPageHeadXmlModel
     */
    private OrderPageHeadXmlModel genaratedOrderPageHeadXmlModel(final ExtendedTOrderPDFEntity order, final int totalPageNumber,
            final List<ExtendedTCompositionEntity> compositionList) {
        final OrderPageHeadXmlModel orderPageHeadXmlModel = new OrderPageHeadXmlModel();
        orderPageHeadXmlModel.setYubin(order.getYubin()); // 郵便番号
        orderPageHeadXmlModel.setAddress1(order.getAddress1()); // 住所1
        orderPageHeadXmlModel.setAddress2(order.getAddreess2()); // 住所2
        orderPageHeadXmlModel.setAddress3(order.getAddreess3()); // 住所3
        orderPageHeadXmlModel.setSendToName(order.getSendToName()); // 送付先名
        orderPageHeadXmlModel.setSire(order.getSire()); // 仕入先コード
        orderPageHeadXmlModel.setHphone(order.getHphone()); // 送付先電話番号
        orderPageHeadXmlModel.setTotalPageNumber(totalPageNumber); // 全体ページNo
        orderPageHeadXmlModel.setOrderNumber(order.getOrderNumber()); //発注No
        orderPageHeadXmlModel.setExpenseItemCode(String.format("%2s", order.getExpenseItemCode()).replace(" ", "0")); //費目コード（前ゼロ2桁）
        orderPageHeadXmlModel.setExpenseItemName(order.getExpenseItemName()); // 費目名称
        orderPageHeadXmlModel.setProductOrderDate((DateUtils.formatFromDate(order.getProductOrderDate(), "yy/MM/dd"))); //製品発注日（yy/MM/dd）
        // 注文番号
        // 費目コード
        orderPageHeadXmlModel.setDivisionCode(order.getDivisionCode()); // 部門コード
        orderPageHeadXmlModel.setDivisionName(order.getDivisionName()); // 事業部名
        orderPageHeadXmlModel.setItemName(order.getItemName()); // アイテム名
        orderPageHeadXmlModel.setCountryOfOrigin(StringUtils.substring(order.getCountryOfOrigin(), SUBSTRING_CUT_16)); // 原産国（16文字以降は切り捨て）
        orderPageHeadXmlModel.setQuantity(order.getQuantity()); // 数量（3桁毎にカンマ区切り）
        orderPageHeadXmlModel.setUnitPrice(order.getUnitPrice()); // 単価（3桁毎にカンマ区切り）
        orderPageHeadXmlModel.setPrice(order.getQuantity().multiply(order.getUnitPrice())); // 金額（3桁毎にカンマ区切り）※XSLでフォーマット
        orderPageHeadXmlModel.setProductDeliveryDate((DateUtils.formatFromDate(order.getProductDeliveryDate(), "yy/MM/dd"))); // 製造納期（yy/MM/dd）
        orderPageHeadXmlModel.setPartNumber(BusinessUtils.formatPartNo(order.getPartNo())); // 製造番号（前3桁と4桁目に"-"を設定）
        orderPageHeadXmlModel.setYear(order.getYear()); // 年度
        orderPageHeadXmlModel.setSeason(order.getSeason()); // 季節
        orderPageHeadXmlModel.setRetailPrice(order.getRetailPrice()); // 上代（3桁毎にカンマ区切り）※XSLでフォーマット
        orderPageHeadXmlModel.setProductName(StringUtils.substring(order.getProductName(), SUBSTRING_CUT_101)); // 品名（101文字以降は切り捨て）
        orderPageHeadXmlModel.setTextureOrderYubin(""); // 生地発注先 郵便番号（空欄）
        orderPageHeadXmlModel.setTextureOrderAddress1(""); // 生地発注先 住所1（空欄）
        orderPageHeadXmlModel.setTextureOrderAddress2(""); // 生地発注先 住所2（空欄）
        orderPageHeadXmlModel.setTextureOrderAddress3(""); // 生地発注先 住所3（空欄）
        orderPageHeadXmlModel.setTextureOrderCode(""); // 生地発注先 仕入先コード（空欄）
        orderPageHeadXmlModel.setTextureOrderName(""); // 生地発注先 発注先名称（空欄）
        orderPageHeadXmlModel.setMdfStaff(StringUtils.substring(order.getMdfStaff(), SUBSTRING_CUT_6)); // 製造担当
        orderPageHeadXmlModel.setPlanningStaff(StringUtils.substring(order.getPlanningStaff(), SUBSTRING_CUT_6)); //企画担当
        orderPageHeadXmlModel.setPataner(StringUtils.substring(order.getPataner(), SUBSTRING_CUT_6)); // パタンナー
        orderPageHeadXmlModel.setRelationNumber(order.getRelationNumber()); // 関連番号
        orderPageHeadXmlModel.setTextureNumber(""); // 生地番号（空欄）
        orderPageHeadXmlModel.setClothNumber(""); // 反番号（空欄）
        orderPageHeadXmlModel.setTextureName(""); // 品名（空欄）
        orderPageHeadXmlModel.setMatlDeliveryDate(""); // 生地納期（空欄）
        orderPageHeadXmlModel.setTotalClothCount(""); // 反数（空欄）
        orderPageHeadXmlModel.setLengthActual(""); // 用尺 目付（空欄）
        orderPageHeadXmlModel.setMaterialCost(""); // 原価欄 生地（空欄）
        orderPageHeadXmlModel.setProcessingCost(""); // 工賃（空欄）
        orderPageHeadXmlModel.setAtacchedCost(order.getAttachedCost()); // 附属品（3桁毎にカンマ区切り）※XSLでフォーマット
        orderPageHeadXmlModel.setOtherCost(order.getOtherCost()); // その他（3桁毎にカンマ区切り）※XSLでフォーマット
        orderPageHeadXmlModel.setProductCost(order.getProductCost()); //製造原価（3桁毎にカンマ区切り）※XSLでフォーマット
        // 原価率：(製品原価/上代)×100（小数点第3位以下切り捨て）
        orderPageHeadXmlModel.setCostRate(
                (order.getProductCost().divide(order.getRetailPrice(), ROUNDING_DOWN_NUMBER, RoundingMode.DOWN))
                        .multiply(PERCENTAGE));
        orderPageHeadXmlModel.setStandard(""); // 規格
        orderPageHeadXmlModel.setQualityLabelHead(genarateOrderQualityLabelHeadXmlModel(compositionList)); // 品質表示詳細
        orderPageHeadXmlModel.setApplication(StringUtils.substring(order.getApplication(), SUBSTRING_CUT_301)); // 適用
        orderPageHeadXmlModel.setAttentionLabel(""); // 但し書きラベル
        orderPageHeadXmlModel.setCompanyName(order.getCompanyName()); // 会社名

        return orderPageHeadXmlModel;
    }

    /**
     * カラーコード（00:共通）の組成情報のみ抽出し、OrderQualityLabelHeadXmlModelを生成.
     * @param compositionList 組成情報
     * @return OrderQualityLabelHeadXmlModelリスト
     */
    private List<OrderQualityLabelHeadXmlModel> genarateOrderQualityLabelHeadXmlModel(final List<ExtendedTCompositionEntity> compositionList) {
        return compositionList.stream()
                .filter(composition -> composition.getColorCode().equals(COMMON_COLOR_CODE))
                .map(composition -> {
                    final OrderQualityLabelHeadXmlModel orderQualityLabelHeadXmlModel = new OrderQualityLabelHeadXmlModel();
                    orderQualityLabelHeadXmlModel
                            .setQualityDisplayParts(substringSuffixThreePointReader(composition.getPartsName(), SUBSTRING_CUT_13));
                    orderQualityLabelHeadXmlModel
                            .setQualityDisplayComposition(substringSuffixThreePointReader(composition.getCompositionName(), SUBSTRING_CUT_13));
                    orderQualityLabelHeadXmlModel.setQualityDisplayRate(composition.getPercent());

                    return orderQualityLabelHeadXmlModel;
                }).collect(Collectors.toList());
    }

    /**
     * textサイズ ＞＝ length の場合、length目を切り捨て、接尾語に三点リーダーをつける.
     * textサイズ ＜   length の場合、textを返す
     * <pre>
     * substringSuffixThreePointReader("12345678901234",13) returns "123456789012…"
     * substringSuffixThreePointReader("1234567890123",13) returns "123456789012…"
     * substringSuffixThreePointReader("123456789012",13) returns "123456789012"
     * substringSuffixThreePointReader("12345",13) returns "12345"
     * substringSuffixThreePointReader(null,13) returns ""
     * substringSuffixThreePointReader("",13) returns ""
     *
     * </pre>
     * @param text 文字列
     * @param length 文字数
     * @return 文字列
     */
    private String substringSuffixThreePointReader(final String text, final int length) {
        if (text == null) {
            return "";
        }
        if (text.length() >= length) {
            // length文字以上の場合、length文字目を三点リーダーに変換した文字
            return StringUtils.substring(text, length).concat("…");
        }
        return text;
    }
}
