package jp.co.jun.edi.component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.entity.TDeliverySearchResultEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.DeliverySearchResultModel;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * 配分一覧関連のコンポーネント.
 */
@Component
public class DeliverySearchListComponent extends GenericComponent {

    @Autowired
    private OrderComponent orderComponent;

    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".repository";

    @Value("${" + PROPERTY_NAME_PREFIX + ".impl.tdelivery-search-list-impl.keyword-conditions-limit-size}")
    private int keywordConditionsLimitSize;

    /**
     * 配分一覧リストを取得する.
     *
     * @param pageTDeliverySearchResults 配分一覧(納品依頼)検索結果
     * @return 配分一覧(納品依頼)検索結果Modelリスト
     */
    public List<DeliverySearchResultModel> listDeliverySearchResult(final Page<TDeliverySearchResultEntity> pageTDeliverySearchResults) {
        final List<DeliverySearchResultModel> deliverySearchResultModelList = new ArrayList<>();

        for (final TDeliverySearchResultEntity deliverySearchResultEntity : pageTDeliverySearchResults) {
            final DeliverySearchResultModel deliverySearchResultModel = new DeliverySearchResultModel();
            BeanUtils.copyProperties(deliverySearchResultEntity, deliverySearchResultModel);

            // 完納フラグをセット(完納：true、未完納：false)
            deliverySearchResultModel.setOrderCompleteFlg(
                    orderComponent.isCompleteOrder(deliverySearchResultEntity.getProductCompleteOrder(), deliverySearchResultEntity.getAllCompletionType())
                    );

            deliverySearchResultModelList.add(deliverySearchResultModel); // レスポンスに返却する
        }

        return deliverySearchResultModelList;
    }

    /**
     * 完全一致、複数検索用条件分割処理.
     * 入力値は、全半角スペースで分割する.
     * 分割した結果上限値を超える場合はエラーをthrowする.
     * @param conditions 分割前のテキスト
     * @return スペースで分割した検索条件
     *
     */
    public List<String> splitPerfectMatchConditions(final String conditions) {
        final List<String> conditionsList = jp.co.jun.edi.util.StringUtils.splitWhitespace(conditions);

        if (isConditionsOverLimit(conditionsList)) {
            // 項目上限値エラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_003));
        }
        return conditionsList;

    }

    /**
     * 部分一致、複数検索用条件分割処理.
     * 入力値は、全半角スペースで分割し、前後に%をつける.
     * 分割した結果上限値を超える場合はエラーをthrowする.
     * @param conditions 分割前のテキスト
     * @return スペースで分割した検索条件
     *
     */
    public List<String> splitPartialMatchConditions(final String conditions) {
        final List<String> conditionsList = jp.co.jun.edi.util.StringUtils.splitWhitespaceGeneratePartialMatchList(conditions);

        if (isConditionsOverLimit(conditionsList)) {
            // 項目上限値エラー
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_003));
        }
        return conditionsList;
    }

    /**
     * 検索パラーメータが上限値以上設定されていないかチェックする.
     * @param conditions 条件のリスト
     * @return true：上限値を超えている
     *          falase：条件を超えていない
     */
    private boolean isConditionsOverLimit(final List<String> conditions) {

        if (conditions != null && conditions.size() > keywordConditionsLimitSize) {
            return true;
        }
        return false;
    }

}
