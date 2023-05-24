package jp.co.jun.edi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.FukukitaruComponent;
import jp.co.jun.edi.component.ScreenSettingFukukitaruComponent;
import jp.co.jun.edi.component.model.ScreenSettingFukukitaruComponentSearchConditionModel;
import jp.co.jun.edi.model.ScreenSettingFukukiatruModel;
import jp.co.jun.edi.model.ScreenSettingFukukitaruSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.FukukitaruMasterOrderType;

/**
 * フクキタル用品番画面構成情報を取得するService.
 */
@Service
public class ScreenSettingFukukitaruItemService
        extends
        GenericListService<ListServiceParameter<ScreenSettingFukukitaruSearchConditionModel>, ListServiceResponse<ScreenSettingFukukiatruModel>> {
    /** 品種の最大文字数. */
    private static final int PART_NO_KIND_MAX_LENGTH = 3;

    /** 発注種別（NULL）. */
    private static final FukukitaruMasterOrderType ORDER_TYPE = null;

    @Autowired
    private ScreenSettingFukukitaruComponent component;
    @Autowired
    private FukukitaruComponent fukukitaruComponent;

    @Override
    protected ListServiceResponse<ScreenSettingFukukiatruModel> execute(
            final ListServiceParameter<ScreenSettingFukukitaruSearchConditionModel> serviceParameter) {

        // バリデーションチェック
        if (!checkValidation(serviceParameter)) {
            // バリデーションエラーの場合は、空のリストを返す
            return ListServiceResponse.<ScreenSettingFukukiatruModel>builder().items(new ArrayList<ScreenSettingFukukiatruModel>()).build();
        }

        // コンポーネントパラメータ用のデータを生成する
        final ScreenSettingFukukitaruComponentSearchConditionModel paramModel = new ScreenSettingFukukitaruComponentSearchConditionModel();
        BeanUtils.copyProperties(serviceParameter.getSearchCondition(), paramModel);
        paramModel.setBrandCode(serviceParameter.getSearchCondition().getPartNoKind().substring(0, 2));
        paramModel.setItemCode(serviceParameter.getSearchCondition().getPartNoKind().substring(2));
        paramModel.setCompany(serviceParameter.getLoginUser().getCompany());
        paramModel.setOrderType(component.identifyOrderType(paramModel.getBrandCode(), paramModel.getItemCode(), ORDER_TYPE));

        // データを取得
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
    public boolean checkValidation(final ListServiceParameter<ScreenSettingFukukitaruSearchConditionModel> serviceParameter) {
        final ScreenSettingFukukitaruSearchConditionModel param = serviceParameter.getSearchCondition();
        final CustomLoginUser user = serviceParameter.getLoginUser();

        // パラメータがNULLの場合、バリデーションエラー
        if (Objects.isNull(param)) {
            return false;
        }
        // マスタ情報リストがNULL、または、空の場合、バリデーションエラー
        if (Objects.isNull(param.getListMasterType()) || param.getListMasterType().isEmpty()) {
            return false;
        }

        // パラメータがNULLの場合、バリデーションエラー
        if (Objects.isNull(param.getPartNoKind())) {
            return false;
        }

        // 品種が3文字出ない場合は、バリデーションエラー
        if (param.getPartNoKind().length() != PART_NO_KIND_MAX_LENGTH) {
            return false;
        }

        // フクキタルを利用不可の場合は、バリデーションエラー
        final String brandCode = param.getPartNoKind().substring(0, 2);
        if (!fukukitaruComponent.isMaterialOrderAvailable(user.getCompany(), brandCode)) {
            return false;
        }

        return true;
    }

}
