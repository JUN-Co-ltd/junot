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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.directDelivery.DirectDeliveryDetailXmlModel;
import jp.co.jun.edi.component.model.directDelivery.DirectDeliveryHeadXmlModel;
import jp.co.jun.edi.component.model.directDelivery.DirectDeliveryRecordXmlModel;
import jp.co.jun.edi.component.model.directDelivery.DirectDeliverySectionXmlModel;
import jp.co.jun.edi.component.model.directDelivery.DirectDeliveryTotalRecordXmlModel;
import jp.co.jun.edi.component.model.directDelivery.DirectDeliveryXmlModel;
import jp.co.jun.edi.entity.TDeliveryStoreSkuEntity;
import jp.co.jun.edi.entity.extended.ExtendedDirectDeliveryPDFDetailEntity;
import jp.co.jun.edi.entity.extended.ExtendedDirectDeliveryPDFHeaderEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TDeliveryStoreSkuRepository;
import jp.co.jun.edi.repository.extended.ExtendedDirectDeliveryPDFDetailRepository;
import jp.co.jun.edi.repository.extended.ExtendedDirectDeliveryPDFHeaderRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.NumberUtils;
/**
 * xmlを作成するコンポーネント.
 */
@Component
public class DirectDeliveryItemCreateXmlComponent {
    /** 1ページに表示可能なサイズの最大値. */
    private static final int SIZE_DISPLAY_MAX_SIZE = 15;

    private static final String TEL1 = "ＪＵＮ本社流通部　tel 03-3472-8083\r\n";
    private static final String TEL2 = "神戸ディスタ　　　tel 078-302-8426\r\n";
    private static final String TEL3 = "白河ディスタ　　　tel 0248-25-4211\r\n";
    private static final String TEL4 = "下館ディスタ　　　tel 0296-28-2585\r\n";
    private static final String SHIPPING_ORIGIN = "メーカー直送";

    @Autowired
    private ExtendedDirectDeliveryPDFHeaderRepository headerRepository;

    @Autowired
    private ExtendedDirectDeliveryPDFDetailRepository detailRepository;

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
            final BigInteger orderId,
            final Path xmlPath) throws Exception, IOException {

        final DirectDeliveryXmlModel model = new DirectDeliveryXmlModel();

        // ヘッダ情報を生成
        final List<DirectDeliveryHeadXmlModel> headModels = genaratedHeadXmlModel(deliveryId);

        // 各ページ情報を生成

        // ヘッダごとにデータを編集
        final List<DirectDeliveryDetailXmlModel> detailModels = new ArrayList<DirectDeliveryDetailXmlModel>();
        for (DirectDeliveryHeadXmlModel headModel: headModels) {
            final List<DirectDeliveryDetailXmlModel> tmpDetailModels = new ArrayList<DirectDeliveryDetailXmlModel>();
            genaratedDetailXmlModel(deliveryId, headModel, tmpDetailModels);
            detailModels.addAll(tmpDetailModels);
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
     * HeadXmlModel生成.
     *
     * @param deliveryId 納品ID
     * @return ヘッダXMLモデル
     */
    private List<DirectDeliveryHeadXmlModel> genaratedHeadXmlModel(
            final BigInteger deliveryId) {
        // HEAD情報を検索する
        final List<ExtendedDirectDeliveryPDFHeaderEntity> entities =
                headerRepository.findByDeliveryColumns(deliveryId);
        if (CollectionUtils.isEmpty(entities)) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                            .message("header_data not found.")
                            .value("delivery_id", deliveryId)
                            .build()));
        }

        final List<DirectDeliveryHeadXmlModel> headModels = new ArrayList<DirectDeliveryHeadXmlModel>();

        for (ExtendedDirectDeliveryPDFHeaderEntity entity: entities) {
            final DirectDeliveryHeadXmlModel headModel = new DirectDeliveryHeadXmlModel();
            BeanUtils.copyProperties(entity, headModel);
            headModel.setDate(DateUtils.formatFromDate(DateUtils.createNow(), "yy.MM.dd"));

            StringBuilder builder = new StringBuilder();
            builder.append(TEL1);
            builder.append(TEL2);
            builder.append(TEL3);
            builder.append(TEL4);

            final PageRequest pageRequest = PageRequest.of(0, 1);
            final TDeliveryStoreSkuEntity storeSkuEntity = storeSkuRepository.findByDeliveryStoreId(entity.getId(), pageRequest).getContent().get(0);
            headModel.setVoucherNumber(storeSkuEntity.getShipmentVoucherNumber());
            headModel.setCompanyTel(builder.toString());
            headModel.setDivisionCode("");
            headModel.setShippingOrigin(SHIPPING_ORIGIN);
            headModels.add(headModel);
        }
        return headModels;
    }

    /**
     * DetailXmlModel生成.
     *
     * @param deliveryId 納品ID
     * @param headModel ヘッダモデル
     * @param detailModels 詳細モデル
     */
    private void genaratedDetailXmlModel(
            final BigInteger deliveryId,
            final DirectDeliveryHeadXmlModel headModel,
            final List<DirectDeliveryDetailXmlModel> detailModels
            ) {
        final List<ExtendedDirectDeliveryPDFDetailEntity> entities =
                detailRepository.findByDeliveryColumns(deliveryId, headModel.getShopCode());
        if (CollectionUtils.isEmpty(entities)) {
            throw new ResourceNotFoundException(
                    ResultMessages.warning().add(
                            MessageCodeType.CODE_002, LogStringUtil.of("createXml")
                            .message("detail_data not found.")
                            .value("delivery_id", deliveryId)
                            .value("shop_code", headModel.getShopCode())
                            .build()));
        }

        final List<DirectDeliveryRecordXmlModel> recordModels = entities.stream()
                .map(entity -> createModel(entity))
                .collect(Collectors.toList());

        final Integer maxCnt = (recordModels.size() / SIZE_DISPLAY_MAX_SIZE) + 1;
        for (int i = 0; i < maxCnt; i++) {
            final DirectDeliverySectionXmlModel sectionModel = new DirectDeliverySectionXmlModel();

            final Integer startRowIdx = i * SIZE_DISPLAY_MAX_SIZE;
            Integer endRowIdx = (i + 1) * SIZE_DISPLAY_MAX_SIZE;
            if (endRowIdx > recordModels.size()) {
                endRowIdx = recordModels.size();
            }

            // 1ページに登録するレコードを設定する
            List<DirectDeliveryRecordXmlModel> subRecordModels = recordModels.subList(startRowIdx, endRowIdx);
            Integer lotTotal = subRecordModels.stream().mapToInt(s -> NumberUtils.createInteger(s.getDeliveryLot())).sum();
            sectionModel.setDetails(subRecordModels);
            final DirectDeliveryDetailXmlModel model = new DirectDeliveryDetailXmlModel();
            model.setRecordSection(sectionModel);

            final DirectDeliveryTotalRecordXmlModel totalModel = new DirectDeliveryTotalRecordXmlModel();
            totalModel.setLotTotal(lotTotal.toString());
            model.setTotalRecord(totalModel);

            // ページ数分、ヘッダには同じヘッダを設定
            model.setPageHead(headModel);

            detailModels.add(model);
        }
    }

    /**
     * レコードを生成.
     *
     * @param entity DB登録データ
     * @return 一覧データモデル
     */
    private DirectDeliveryRecordXmlModel createModel(
            final ExtendedDirectDeliveryPDFDetailEntity entity
            ) {
        final DirectDeliveryRecordXmlModel model = new DirectDeliveryRecordXmlModel();
        BeanUtils.copyProperties(entity, model);

        // 不足項目の補完
        model.setPartNoColorSize(entity.getPartNo() + "-" + entity.getColorCode() + entity.getSize());
        model.setRetailPrice(entity.getRetailPrice().toString());
        model.setDeliveryLot(entity.getDeliveryLot().toString());
        model.setRetailPriceSubTotal(entity.getRetailPriceSubTotal().toString());

        return model;
    }
}
