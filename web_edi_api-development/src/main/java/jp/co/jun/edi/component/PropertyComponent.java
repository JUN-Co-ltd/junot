package jp.co.jun.edi.component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.component.model.BatchPropertyModel;
import jp.co.jun.edi.component.model.BulkRegistItemPropertyModel;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.MaterialOrderLinkingErrorMailForwardPropertyModel;
import jp.co.jun.edi.component.model.MaterialOrderLinkingPropertyModel;
import jp.co.jun.edi.component.model.ShipmentPropertyModel;
import jp.co.jun.edi.component.model.TagdatPropertyModel;
import jp.co.jun.edi.entity.MPropertyEntity;
import jp.co.jun.edi.repository.MPropertyRepository;
import jp.co.jun.edi.type.PropertyCategoryType;

/**
 * プロパティ関連のコンポーネント.
 */
@Component
public class PropertyComponent extends GenericComponent {
    @Autowired
    private MPropertyRepository mPropertyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /** シングルトン.共通プロパティ. */
    private CommonPropertyModel commonProperty;

    /** シングルトン.バッチプロパティ. */
    private BatchPropertyModel batchProperty;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void postConstruct() {
        getCommonProperty();
        getBatchProperty();
    }

    /**
     * バッチプロパティ情報を取得する.
     *
     * @return バッチプロパティ情報
     */
    public synchronized BatchPropertyModel getBatchProperty() {
        if (Objects.isNull(batchProperty)) {
            batchProperty = generateBatchProperty();
        }

        return batchProperty;
    }

    /**
     * バッチプロパティ情報を生成する.
     *
     * @return バッチプロパティ情報
     */
    private BatchPropertyModel generateBatchProperty() {
        final Map<String, String> propertyMap = getPropertyMap(PropertyCategoryType.BATCH);

        final BatchPropertyModel property = BatchPropertyModel.builder()
                .scheduleFopTemporaryFolder(getPropertyValue(propertyMap, "schedule.fop.temporary-folder"))
                .scheduleFopPathXconf(getPropertyValue(propertyMap, "schedule.fop.path.xconf"))
                .orderReceiveMaxProcesses(toInteger(getPropertyValue(propertyMap, "order-receive.max-processes")))
                .orderReceivePathXsl(getPropertyValue(propertyMap, "order-receive.path.xsl"))
                .orderReceiveSendMailSend(toBoolean(getPropertyValue(propertyMap, "order-receive.send-mail.send")))
                .deliveryRequestPathXsl(getPropertyValue(propertyMap, "delivery-request.path.xsl"))
                .deliveryRequestAddress(getPropertyValue(propertyMap, "delivery-request.address"))
                .deliveryRequestMaxProcesses(toInteger(getPropertyValue(propertyMap, "delivery-request.max-processes")))
                .deliveryRequestSendMailSend(toBoolean(getPropertyValue(propertyMap, "delivery-request.send-mail.send")))
                .deliveryOfficialRequestSendMailSend(toBoolean(getPropertyValue(propertyMap, "delivery-official-request.send-mail.send")))
                .orderApprovalPathXsl(getPropertyValue(propertyMap, "order-approval.path.xsl"))
                .orderApprovalMaxProcesses(toInteger(getPropertyValue(propertyMap, "order-approval.max-processes")))
                .orderApprovalSendMailSend(toBoolean(getPropertyValue(propertyMap, "order-approval.send-mail.send")))
                .orderApprovalOfficialSendMailSend(toBoolean(getPropertyValue(propertyMap, "order-approval-official.send-mail.send")))
                .materialOrderLinkingErrorMailForward(toObject(getPropertyValue(propertyMap, "material-order.linking-error-mail-forward"),
                                MaterialOrderLinkingErrorMailForwardPropertyModel.class))
                .materialOrderLinking(toObject(getPropertyValue(propertyMap, "material-order.linking"), MaterialOrderLinkingPropertyModel.class))
                .shipmentProperty(toObject(getPropertyValue(propertyMap, "shipment"), ShipmentPropertyModel.class))
                .returnItemMaxProcesses(toInteger(getPropertyValue(propertyMap, "return-item.max-processes")))
                .returnItemPathXsl(getPropertyValue(propertyMap, "return-item.path.xsl"))
                //PRD_0134 #10654 add JEF start
                .purchaseItemMaxProcesses(toInteger(getPropertyValue(propertyMap, "purchase-item.max-processes")))
                .purchaseItemPathXsl(getPropertyValue(propertyMap, "purchase-item.path.xsl"))
                //PRD_0134 #10654 add JEF end
                //PRD_0151 add JFE start
                .purchaseRecordPathXsl(getPropertyValue(propertyMap, "purchase-record.path.xsl"))
                //PRD_0151 add JFE end
                //PRD_0177 add JFE start
                .purchaseDigestionReturnPathXsl(getPropertyValue(propertyMap, "purchase-digestion-return.path.xsl"))
                //PRD_0177 add JFE end
                .directDeliveryPathXsl(getPropertyValue(propertyMap, "direct-delivery.path.xsl"))
                .pickingListPathXsl(getPropertyValue(propertyMap, "picking-list.path.xsl"))
                .accountPurchaseConfirmMaxProcesses(toInteger(getPropertyValue(propertyMap, "account-purchase-confirm.max-processes")))
                // PRD_0143 #10423 JFE add start
                .tagdatProperty(toObject(getPropertyValue(propertyMap, "tagdat.send-mail.send"), TagdatPropertyModel.class))
                // PRD_0143 #10423 JFE add end
                .build();

        return property;
    }

    /**
     * 共通プロパティ情報を取得する.
     *
     * @return 共通プロパティ情報
     */
    public synchronized CommonPropertyModel getCommonProperty() {
        if (Objects.isNull(commonProperty)) {
            commonProperty = generateCommonProperty();
        }

        return commonProperty;
    }

    /**
     * 共通プロパティ情報を生成する.
     *
     * @return 共通プロパティ情報
     */
    private CommonPropertyModel generateCommonProperty() {
        final Map<String, String> propertyMap = getPropertyMap(PropertyCategoryType.COMMON);

        final CommonPropertyModel property = CommonPropertyModel.builder()
                .junotUrl(getPropertyValue(propertyMap, "junot.url"))
                .junCompany(getPropertyValue(propertyMap, "jun.company"))
                .sendMailFrom(getPropertyValue(propertyMap, "send-mail.from"))
                .sendMailTo(getPropertyValue(propertyMap, "send-mail.to"))
                .sendMailCc(getPropertyValue(propertyMap, "send-mail.cc"))
                .sendMailBcc(getPropertyValue(propertyMap, "send-mail.bcc"))
                .sendMailLoginUserSend(toBoolean(getPropertyValue(propertyMap, "send-mail.login-user-send")))
                .sendMailSend(toBoolean(getPropertyValue(propertyMap, "send-mail.send")))
                .sendMailTemplateEmbeddedCharacterSubjectPrefix(getPropertyValue(propertyMap, "send-mail.template.embedded-character.subject-prefix"))
                .s3PrefixPdf(getPropertyValue(propertyMap, "s3.prefix.pdf"))
                .adminUserCompany(getPropertyValue(propertyMap, "admin-user.company"))
                .adminUserAccountName(getPropertyValue(propertyMap, "admin-user.account-name"))
                .sendMailSignature(getPropertyValue(propertyMap, "send-mail.signature"))
                .bulkRegistItem(toObject(getPropertyValue(propertyMap, "bulk-regist-item"), BulkRegistItemPropertyModel.class))
                .build();
        return property;
    }

    /**
     * 共通プロパティ情報マップを取得する.
     *
     * @param category カテゴリ
     * @return プロパティ情報マップ
     */
    private Map<String, String> getPropertyMap(
            final PropertyCategoryType category) {
        return mPropertyRepository.findByCategory(category)
                .stream()
                .collect(Collectors.toMap(MPropertyEntity::getItemName, MPropertyEntity::getItemValue));
    }

    /**
     * プロパティ情報マップから値を取得する.
     *
     * @param propertyMap プロパティ情報マップ
     * @param key キー
     * @return 値
     * @throws IllegalArgumentException キーが存在しない場合
     */
    private String getPropertyValue(
            final Map<String, String> propertyMap,
            final String key) {
        if (!propertyMap.containsKey(key)) {
            throw new IllegalArgumentException("The key does not exist in the m_property table. key:" + key);
        }

        return propertyMap.get(key);
    }

    /**
     * Boolean型に変換する.
     *
     * @param value 値
     * @return 変換後の値
     */
    private boolean toBoolean(
            final String value) {
        return Boolean.valueOf(value);
    }

    /**
     * Integer型に変換する.
     *
     * @param value 値
     * @return 変換後の値
     */
    private Integer toInteger(
            final String value) {
        return Integer.valueOf(value);
    }

    /**
     * Object型に変換する.
     *
     * @param <T> 変換後のクラス
     * @param value 値
     * @param valueType 変換後のクラス
     * @return 変換後の値
     */
    private <T> T toObject(final String value, final Class<T> valueType) {
        try {
            return objectMapper.readValue(value, valueType);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("JsonParseException. value:" + value);
        } catch (JsonMappingException e) {
            throw new IllegalArgumentException("JsonMappingException. value:" + value);
        } catch (IOException e) {
            throw new IllegalArgumentException("IOException. value:" + value);
        }
    }
}
