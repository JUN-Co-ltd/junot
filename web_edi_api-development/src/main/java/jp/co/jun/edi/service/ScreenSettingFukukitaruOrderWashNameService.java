package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruComponent;
import jp.co.jun.edi.component.ScreenSettingFukukitaruComponent;
import jp.co.jun.edi.component.model.ScreenSettingFukukitaruComponentSearchConditionModel;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.model.ScreenSettingFukukiatruModel;
import jp.co.jun.edi.model.ScreenSettingFukukitaruSearchConditionModel;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;

/**
 * フクキタル用洗濯ネーム発注画面構成情報を取得するService.
 */
@Service
public class ScreenSettingFukukitaruOrderWashNameService
        extends
        GenericListService<ListServiceParameter<ScreenSettingFukukitaruSearchConditionModel>, ListServiceResponse<ScreenSettingFukukiatruModel>> {

    /** 発注種別(洗濯ネーム). */
    private static final FukukitaruMasterOrderType ORDER_TYPE = FukukitaruMasterOrderType.WASH_NAME;

    @Autowired
    private ScreenSettingFukukitaruComponent component;
    @Autowired
    private TItemRepository tItemRepository;
    @Autowired
    private FukukitaruComponent fukukitaruComponent;

    @Override
    protected ListServiceResponse<ScreenSettingFukukiatruModel> execute(
            final ListServiceParameter<ScreenSettingFukukitaruSearchConditionModel> serviceParameter) {

        // バリデーションチェック
        if (!checkValidation(serviceParameter.getSearchCondition())) {
            // バリデーションエラーの場合は、空のリストを返す
            return ListServiceResponse.<ScreenSettingFukukiatruModel>builder().items(new ArrayList<ScreenSettingFukukiatruModel>()).build();
        }

        // 品番情報
        final Optional<TItemEntity> optionalItem = tItemRepository.findByIdAndDeletedAtIsNull(serviceParameter.getSearchCondition().getPartNoId());
        if (!optionalItem.isPresent()) {
            // 品番情報がない場合は、空のリストを返す
            return ListServiceResponse.<ScreenSettingFukukiatruModel>builder().items(new ArrayList<ScreenSettingFukukiatruModel>()).build();
        }
        final TItemEntity tItemEntity = optionalItem.get();

        // コンポーネントパラメータ用のデータを生成する
        final ScreenSettingFukukitaruComponentSearchConditionModel paramModel = new ScreenSettingFukukitaruComponentSearchConditionModel();
        BeanUtils.copyProperties(serviceParameter.getSearchCondition(), paramModel);
        paramModel.setCompany(serviceParameter.getLoginUser().getCompany());
        paramModel.setBrandCode(tItemEntity.getBrandCode());
        paramModel.setItemCode(tItemEntity.getItemCode());
        paramModel.setPartNoKind(tItemEntity.getBrandCode() + tItemEntity.getItemCode()); // 品種（ブランドコード＋アイテム）
        paramModel.setOrderType(component.identifyOrderType(paramModel.getBrandCode(), paramModel.getItemCode(), ORDER_TYPE));


        // フクキタルを利用不可の場合は、空のリストを返す
        if (!fukukitaruComponent.isMaterialOrderAvailable(serviceParameter.getLoginUser().getCompany(), paramModel.getBrandCode())) {
            return ListServiceResponse.<ScreenSettingFukukiatruModel>builder().items(new ArrayList<ScreenSettingFukukiatruModel>()).build();
        }

        // フクキタル画面構成情報を取得
        final ScreenSettingFukukiatruModel returnModel = component.execute(paramModel);

        // 戻り値
        final List<ScreenSettingFukukiatruModel> items = new ArrayList<ScreenSettingFukukiatruModel>();
        items.add(returnModel);
        return ListServiceResponse.<ScreenSettingFukukiatruModel>builder().items(items).build();
    }


    /**
     * バリデーションチェック.
     * @param serviceParameter {@link ScreenSettingFukukitaruSearchConditionModel}
     * @return エラーがない場合は true が返され、そうでなければ false を返す
     */
    public boolean checkValidation(final ScreenSettingFukukitaruSearchConditionModel serviceParameter) {
        // パラメータがNULLの場合、バリデーションエラー
        if (Objects.isNull(serviceParameter)) {
            return false;
        }
        // マスタ情報リストがNULL、または、空の場合、バリデーションエラー
        if (Objects.isNull(serviceParameter.getListMasterType()) || serviceParameter.getListMasterType().isEmpty()) {
            return false;
        }

        // パラメータがNULLの場合、バリデーションエラー
        if (Objects.isNull(serviceParameter.getPartNoId())) {
            return false;
        }

        // パラメータがNULLの場合、バリデーションエラー
        if (Objects.isNull(serviceParameter.getOrderId())) {
            return false;
        }

        return true;
    }
}
