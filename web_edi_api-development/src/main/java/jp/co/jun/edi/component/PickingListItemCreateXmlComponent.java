package jp.co.jun.edi.component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.pickingList.PickingListDetailXmlModel;
import jp.co.jun.edi.component.model.pickingList.PickingListHeadXmlModel;
import jp.co.jun.edi.component.model.pickingList.PickingListRecordColumnXmlModel;
import jp.co.jun.edi.component.model.pickingList.PickingListRecordXmlModel;
import jp.co.jun.edi.component.model.pickingList.PickingListSectionXmlModel;
import jp.co.jun.edi.component.model.pickingList.PickingListSkuSectionXmlModel;
import jp.co.jun.edi.component.model.pickingList.PickingListXmlModel;
import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedPickingListPDFDetailEntity;
import jp.co.jun.edi.entity.extended.ExtendedPickingListPDFHeaderEntity;
import jp.co.jun.edi.entity.extended.ExtendedTDeliveryStoreSkuPDFEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedPickingListPDFDetailRepository;
import jp.co.jun.edi.repository.extended.ExtendedPickingListPDFHeaderRepository;
import jp.co.jun.edi.repository.extended.ExtendedTDeliveryStoreSkuPDFRepository;
import jp.co.jun.edi.type.AllocationType;
import jp.co.jun.edi.type.BooleanType;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;
import jp.co.jun.edi.util.StringUtils;

/**
 * xmlを作成するコンポーネント.
 */
@Component
public class PickingListItemCreateXmlComponent {

    private static final String DATE_FORMAT = "yyyy/MM/dd";

    private static final String COMPLETED = "完納";

    /** 1ページに表示可能なサイズの最大値(縦). */
    private static final int SIZE_DISPLAY_ROW_MAX_SIZE = 20;

    /** 1ページに表示可能なサイズの最大値(横). */
    private static final int SIZE_DISPLAY_COL_MAX_SIZE = 10;

    private static final String STR_FREE = "フリー";
    private static final String STR_HOLD = "保留";

    private static final int NAME_LENGTH = 21;

    private static final int PRODUCT_NAME_MAX_LENGTH =60;
    private static final int SIRE_NAME_MAX_LENGTH =30;

    @Autowired
    private ExtendedPickingListPDFHeaderRepository headerRepository;

    @Autowired
    private ExtendedPickingListPDFDetailRepository detailRepository;

    @Autowired
    private MCodmstRepository codmstRepository;

    @Autowired
    private ExtendedTDeliveryStoreSkuPDFRepository extendedStoreSkuRepository;

    @Autowired
    private TDeliveryStoreSkuRepository storeSkuRepository;

    /**
     * XMLデータファイルを生成する.
     *
     * @param deliveryId 納品ID
     * @param deliveryCount 納品依頼回数
     * @param orderId 発注ID
     * @param xmlPath XMLファイルパス
     * @throws Exception 例外
     * @throws IOException 例外
     */
    public void createXml(
            final BigInteger deliveryId,
            final Integer deliveryCount,
            final BigInteger orderId, final Path xmlPath) throws Exception, IOException {

        final PickingListXmlModel model = new PickingListXmlModel();

        // ヘッダ情報を生成
        final List<PickingListHeadXmlModel> headModels = genaratedHeadXmlModel(deliveryId);

        // 各ページ情報を生成

        // ヘッダごとにデータを編集
        int currentPage = 1;
        final List<PickingListDetailXmlModel> detailModels = new ArrayList<PickingListDetailXmlModel>();
        for (PickingListHeadXmlModel headModel: headModels) {
            // ヘッダ情報を生成
            final List<PickingListRecordColumnXmlModel> headSkuModels
            = genaratedColumnHeadXmlModel(deliveryId, headModel.getDivisionCode());

            final List<PickingListDetailXmlModel> tmpDetailModels =
                    genaratedDetailXmlModel(deliveryId, headModel, headSkuModels);
            detailModels.addAll(tmpDetailModels);
        }
        int totalPage = detailModels.size();
        for (PickingListDetailXmlModel detailModel: detailModels) {
            // ページ番号ふり
            detailModel.setPageNumber(StringUtils.defaultString(currentPage));
            currentPage = currentPage + 1;
            detailModel.getPageHead().setPageNumberTotal(StringUtils.defaultString(totalPage));
        }

        model.setPageDetails(detailModels);

        // ModelをXMLデータに変換
        final StringWriter strModel = new StringWriter();
        JAXB.marshal(model, strModel);

        // XMLファイルを一時ディレクトリへ出力
        try (BufferedWriter writer = Files.newBufferedWriter(xmlPath)) {
            writer.append(strModel.toString());
        }
    }

    /**
     * PickingListHeadXmlModel生成.
     *
     * @param deliveryId 納品ID
     * @return ヘッダXMLモデル
     */
    private List<PickingListHeadXmlModel> genaratedHeadXmlModel(
            final BigInteger deliveryId) {
        final List<ExtendedPickingListPDFHeaderEntity> entities =
                headerRepository.findByDeliveryId(deliveryId);
        if (CollectionUtils.isEmpty(entities)) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                            .message("header_data not found.")
                            .value("delivery_id", deliveryId)
                            .build()));
        }
        final List<PickingListHeadXmlModel> headModels = new ArrayList<PickingListHeadXmlModel>();
        for (ExtendedPickingListPDFHeaderEntity entity: entities) {
            final PickingListHeadXmlModel headModel = new PickingListHeadXmlModel();
            BeanUtils.copyProperties(entity, headModel);

            headModel.setDate(DateUtils.formatFromDate(DateUtils.createNow(), DATE_FORMAT));
            headModel.setOrderNumber(entity.getOrderNumber().toString());
            headModel.setDeliveryCount(StringUtils.defaultString(entity.getDeliveryCount()));
            // 文字数制限
            if (entity.getProductName().length() > PRODUCT_NAME_MAX_LENGTH + 1) {
                headModel.setProductName(entity.getProductName().substring(0, PRODUCT_NAME_MAX_LENGTH));
            }

            String retailPrice = entity.getRetailPrice().toString();
            if (entity.isNonConformingProductType()) {
                // B級品の場合はB級品単価をセット
                retailPrice = entity.getNonConformingProductUnitPrice().toString();
            }
            headModel.setRetailPrice(retailPrice);

            headModel.setArrivalAt(DateUtils.formatFromDate(entity.getArrivalAt(), DATE_FORMAT));
            headModel.setCorrectionAt(DateUtils.formatFromDate(entity.getCorrectionAt(), DATE_FORMAT));

            if (entity.getAllocationCompletePaymentFlg() == BooleanType.TRUE) {
                // TRUEなら文字列設定
                headModel.setAllocationCompletePaymentFlg(COMPLETED);
            }

            headModel.setSupplierCode(entity.getSire());
            headModel.setSupplierName(entity.getName());
            // 文字数制限
            if (entity.getName().length() > SIRE_NAME_MAX_LENGTH + 1) {
              headModel.setSupplierName(entity.getName().substring(0, SIRE_NAME_MAX_LENGTH));
            }

            // ブランド名、課名を設定
            // ** ブランド名(テーブルコード(02) item1). */
            final MCodmstEntity codmstEntity =
                    codmstRepository.findByTblidAndCode1(MCodmstTblIdType.BRAND.getValue(),
                            entity.getBrandCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ResultMessages.warning().add(
                                    MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                    .message("brand_name not found.")
                                    .value("delivery_id", deliveryId)
                                    .build())));
            headModel.setBrandName(codmstEntity.getItem1());

            // ** 課名(テーブルコード(25) item2). */
            final MCodmstEntity codmstEntityDivision =
                    codmstRepository.findByTblidAndCode1(MCodmstTblIdType.ALLOCATION.getValue(),
                            entity.getBrandCode() + entity.getDivisionCode())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ResultMessages.warning().add(
                                    MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                                    .message("division_name not found.")
                                    .value("delivery_id", deliveryId)
                                    .build())));
            headModel.setDivisionName(codmstEntityDivision.getItem2());

            // ページ数情報, ロット合計情報はDetail時に設定
            headModels.add(headModel);
        }
        return headModels;
    }

    /**
     * PickingListHeadXmlModel生成.
     *
     * @param deliveryId 納品ID
     * @param divisionCode 課コード
     * @return ヘッダXMLモデル
     */
    private List<PickingListRecordColumnXmlModel> genaratedColumnHeadXmlModel(
            final BigInteger deliveryId,
            final String divisionCode) {
        final List<ExtendedTDeliveryStoreSkuPDFEntity> entities =
                extendedStoreSkuRepository.findByDeliveryIdAndDivisionCodeGroupByColorCodeAndSize(deliveryId, divisionCode)
                .orElseThrow(null);
        if (CollectionUtils.isEmpty(entities)) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createColumnHeadXml")
                            .message("column_header_data not found.")
                            .value("delivery_id", deliveryId)
                            .build()));
        }
        final List<PickingListRecordColumnXmlModel> recordModels = entities.stream()
                .map(entity -> createColumnModel(entity))
                .collect(Collectors.toList());
        return recordModels;
    }

    /**
     * レコードを生成.
     *
     * @param entity DB登録データ
     * @return 一覧データモデル
     */
    private PickingListRecordColumnXmlModel createColumnModel(
            final ExtendedTDeliveryStoreSkuPDFEntity entity
            ) {
        final PickingListRecordColumnXmlModel model = new PickingListRecordColumnXmlModel();
        BeanUtils.copyProperties(entity, model);

        // 不足項目の補完
        model.setColorCode(entity.getColorCode());
        model.setSize(entity.getSize());
        model.setDeliveryLot(StringUtils.defaultString(entity.getDeliveryLot()));
        model.setArrivalLot(StringUtils.defaultString(entity.getArrivalLot()));
        return model;
    }

    /**
     * DetailXmlModel生成.
     *
     * @param deliveryId 納品ID
     * @param headModel ヘッダモデル
     * @param headSkuModels カラムヘッダ(sku)
     * @return 更新後のページ(1~)
     */
    private List<PickingListDetailXmlModel>  genaratedDetailXmlModel(
            final BigInteger deliveryId,
            final PickingListHeadXmlModel headModel,
            final List<PickingListRecordColumnXmlModel> headSkuModels
            ) {
        final List<ExtendedPickingListPDFDetailEntity> entities =
                detailRepository.findByDeliveryIdAndDivisionCode(deliveryId, headModel.getDivisionCode());
        if (CollectionUtils.isEmpty(entities)) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                            .message("detail_data not found.")
                            .value("delivery_id", deliveryId)
                            .build()));
        }

        // 該当する得意先Sku情報をすべて取得
        final List<TDeliveryStoreSkuEntity> skuEntities =
                storeSkuRepository.findByDeliveryIdAndDivisionCode(deliveryId, headModel.getDivisionCode());
        if (CollectionUtils.isEmpty(skuEntities)) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("genaratedDetailXmlModel")
                            .message("delivery_store_sku_data not found.")
                            .value("delivery_id", deliveryId)
                            .build()));
        }

        final List<PickingListRecordXmlModel> recordModels = entities.stream()
                .map(entity -> createModel(deliveryId, entity, headSkuModels,
                        skuEntities
                        ))
                .collect(Collectors.toList());

        // ヘッダには課全体の合計数を設定
        Integer kaLotTotal = recordModels.stream().mapToInt(s -> NumberUtils.createInteger(s.getLotTotal())).sum();
        headModel.setLotTotal(kaLotTotal.toString());

        // ここからページ(と集計)を設定
        final int pageRowCnt = (recordModels.size() / SIZE_DISPLAY_ROW_MAX_SIZE) + 1;
        final int pageColCnt = (headSkuModels.size() / SIZE_DISPLAY_COL_MAX_SIZE) + 1;

        final List<PickingListDetailXmlModel> tmpDetailModels = new ArrayList<PickingListDetailXmlModel>();
        for (int pageRowIdx = 0; pageRowIdx < pageRowCnt; pageRowIdx++) {
            // ページに収まるrowの数を設定
            final Integer startRowIdx = (pageRowIdx) * SIZE_DISPLAY_ROW_MAX_SIZE;
            Integer endRowIdx = (pageRowIdx + 1) * SIZE_DISPLAY_ROW_MAX_SIZE;
            if (endRowIdx > recordModels.size()) {
                endRowIdx = recordModels.size();
            }
            List<PickingListRecordXmlModel> subModels =
                    recordModels.subList(
                            startRowIdx,
                            endRowIdx);

            for (int pageColIdx = 0; pageColIdx < pageColCnt; pageColIdx++) {
                // ページに収まるrowの数を設定
                final Integer startColIdx = (pageColIdx) * SIZE_DISPLAY_COL_MAX_SIZE;
                Integer endColIdx = (pageColIdx + 1) * SIZE_DISPLAY_COL_MAX_SIZE;
                if (endColIdx > headSkuModels.size()) {
                    endColIdx = headSkuModels.size();
                }
                final Integer tmpIdx = endColIdx;

                // skuをページ分け
                final List<PickingListRecordXmlModel> subRecordModels =
                        subModels.stream()
                        .map(s -> pageingSkuModels(s, startColIdx, tmpIdx))
                        .collect(Collectors.toList());

                // skuヘッダの再構成(再集計)
                List<PickingListRecordColumnXmlModel> subSkuModels =
                        headSkuModels.subList(
                                startColIdx,
                                endColIdx);
                subSkuModels = subSkuModels.stream()
                        .map(s -> pageingSkuTotal(s, subRecordModels))
                        .collect(Collectors.toList());

                // セクション登録
                PickingListSkuSectionXmlModel pageSkuSection = new PickingListSkuSectionXmlModel();
                pageSkuSection.setSkus(subSkuModels);
                PickingListSectionXmlModel pageRecordSection = new PickingListSectionXmlModel();
                pageRecordSection.setDetails(subRecordModels);

                // ページ合計の再設定
                PickingListRecordColumnXmlModel skuTotal = createTotalColumnHeadXmlModel(subSkuModels);
                pageSkuSection.setSkuTotal(skuTotal);

                // ページセクションの生成
                PickingListDetailXmlModel pageModel = new PickingListDetailXmlModel();
                pageModel.setPageHead(headModel);

                // ページ番号を設定(仮置き。外で振る)
                pageModel.setPageNumber("0");
                pageModel.setRecordSection(pageRecordSection);
                pageModel.setSkuSection(pageSkuSection);

                tmpDetailModels.add(pageModel);
            }
        }

        return tmpDetailModels;
    }

    /**
     * SKU情報のページ分割.
     *
     * @param srcModel 編集前のモデル
     * @param startIdx 開始位置
     * @param endIdx 終了位置
     * @return ページング済みレコード
     */
    private PickingListRecordXmlModel pageingSkuModels(
            final PickingListRecordXmlModel srcModel,
            final Integer startIdx,
            final Integer endIdx
            ) {
        if (endIdx <= SIZE_DISPLAY_COL_MAX_SIZE) {
            return srcModel;
        }

        final PickingListRecordXmlModel destModel = new PickingListRecordXmlModel();
        BeanUtils.copyProperties(srcModel, destModel);
        PickingListSkuSectionXmlModel skuSectionModel = new PickingListSkuSectionXmlModel();
        List<PickingListRecordColumnXmlModel> skus = srcModel.getSkuSection().getSkus().subList(startIdx, endIdx);

        skuSectionModel.setSkus(skus);

        destModel.setSkuSection(skuSectionModel);

        // 集計もやりなおし
        Integer lotTotal = skus.stream().mapToInt(s -> NumberUtils.createInteger(s.getDeliveryLot())).sum();
        destModel.setLotTotal(lotTotal.toString());

        return destModel;
    }

    /**
     * 納品数の縦一列の集計関数.
     *
     * @param srcModel 編集前のモデル
     * @param recordModels 計算対象を検索する対象モデル
     * @return 集計済みカラムデータ
     */
    private PickingListRecordColumnXmlModel pageingSkuTotal(
            final PickingListRecordColumnXmlModel srcModel,
            final List<PickingListRecordXmlModel> recordModels) {
        PickingListRecordColumnXmlModel destModel = new PickingListRecordColumnXmlModel();
        BeanUtils.copyProperties(srcModel, destModel);

        List<PickingListRecordColumnXmlModel> allSkus = new ArrayList<PickingListRecordColumnXmlModel>();
        for (PickingListRecordXmlModel recordModel: recordModels) {
            allSkus.addAll(recordModel.getSkuSection().getSkus());
        }

        // フィルタリング
        allSkus = allSkus.stream().filter(s -> StringUtils.equals(s.getColorCode(), srcModel.getColorCode())
                && StringUtils.equals(s.getSize(), srcModel.getSize())).collect(Collectors.toList());
        // 集計
        Integer lot = allSkus.stream().mapToInt(s -> NumberUtils.createInteger(s.getDeliveryLot())).sum();
        destModel.setSubTotalLot(lot.toString());

        return destModel;
    }

    /**
     * レコードを生成.
     *
     * @param entity DB登録データ
     * @param deliveryId 納品ID
     * @param headSkuModels sku数
     * @param skuEntities 行のskuに登録対象のエンティティ
     * @return 一覧データモデル
     */
    private PickingListRecordXmlModel createModel(
            final BigInteger deliveryId,
            final ExtendedPickingListPDFDetailEntity entity,
            final List<PickingListRecordColumnXmlModel> headSkuModels,
            final List<TDeliveryStoreSkuEntity> skuEntities
            ) {
        final List<TDeliveryStoreSkuEntity> currentSkuEntities = skuEntities
                .stream()
                .filter(sku -> sku.getDeliveryStoreId().longValue() == entity.getId().longValue())
                .collect(Collectors.toList());

        final PickingListRecordXmlModel model = new PickingListRecordXmlModel();
        BeanUtils.copyProperties(entity, model);

        // 不足項目の補完
        final List<PickingListRecordColumnXmlModel> skuModels = headSkuModels.stream()
                .map(s -> createSkuModel(s, currentSkuEntities))
                .collect(Collectors.toList());
        final PickingListSkuSectionXmlModel skuSection = new PickingListSkuSectionXmlModel();
        skuSection.setSkus(skuModels);

        model.setShopCode(entity.getShpcd());
        String name = entity.getName();
        if (name.length() > NAME_LENGTH) {
            // 21文字以上の場合は切り捨て
            name = entity.getName().substring(0, NAME_LENGTH - 1);
        }
        model.setShopName(name);
        model.setShippingAt(DateUtils.formatFromDate(entity.getAllocationCargoAt(), DATE_FORMAT));
        model.setSkuSection(skuSection);
        Integer lotTotal = currentSkuEntities.stream().mapToInt(s -> s.getDeliveryLot()).sum();
        model.setLotTotal(lotTotal.toString());

        if (entity.getAllocationType() == AllocationType.HOLD
                || entity.getAllocationType() == AllocationType.SECURE) {
            model.setAllocationType(STR_HOLD);

        } else if (entity.getAllocationType() == AllocationType.SEWING_FREE
                || entity.getAllocationType() == AllocationType.FREE) {
            model.setAllocationType(STR_FREE);
        }
        return model;
    }

    /**
     * ヘッダ情報からskuを補完して抽出.
     *
     * @param model 補完対象のモデル(ColorCode, Size)
     * @param skuEntities 検索対象のエンティティ
     * @return skuクロスデータ
     */
    private PickingListRecordColumnXmlModel createSkuModel(
            final PickingListRecordColumnXmlModel model,
            final List<TDeliveryStoreSkuEntity> skuEntities) {
        PickingListRecordColumnXmlModel destModel = new PickingListRecordColumnXmlModel();
        destModel.setColorCode(model.getColorCode());
        destModel.setSize(model.getSize());
        List<TDeliveryStoreSkuEntity> currentEntities =
                skuEntities
                .stream()
                .filter(sku -> StringUtils.equals(sku.getColorCode(), model.getColorCode())
                        && StringUtils.equals(sku.getSize(), model.getSize())
                        ).collect(Collectors.toList());
        if (currentEntities.size() > 0) {
            // 0以上なら設定
            Integer lot = currentEntities.get(0).getDeliveryLot();
            destModel.setDeliveryLot(StringUtils.defaultString(lot));
        }
        return destModel;
    }

    /**
     * 合計行ヘッダの作成.
     *
     * @param models ヘッダ一覧
     * @return 合計のモデル
     */
    private PickingListRecordColumnXmlModel createTotalColumnHeadXmlModel(
            final List<PickingListRecordColumnXmlModel> models
            ) {
        final PickingListRecordColumnXmlModel model = new PickingListRecordColumnXmlModel();
        Integer deliveryLot = models.stream().mapToInt(s -> NumberUtils.createInteger(s.getDeliveryLot())).sum();
        model.setDeliveryLot(deliveryLot.toString());
        Integer arrivalLot = models.stream().mapToInt(s -> NumberUtils.createInteger(s.getArrivalLot())).sum();
        model.setArrivalLot(arrivalLot.toString());
        Integer subTotalLot = models.stream().mapToInt(s -> NumberUtils.createInteger(s.getSubTotalLot())).sum();
        model.setSubTotalLot(subTotalLot.toString());
        return model;
    }
}
