package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ScreenSettingFukukiatruModel;
import jp.co.jun.edi.model.ScreenSettingFukukitaruSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ScreenSettingFukukitaruItemService;
import jp.co.jun.edi.service.ScreenSettingFukukitaruOrderHangTagService;
import jp.co.jun.edi.service.ScreenSettingFukukitaruOrderWashNameService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * フクキタル関連の画面構成情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/screenSettings")
public class FukukitaruScreenSettingV1Api {
    @Autowired
    private ScreenSettingFukukitaruOrderWashNameService screenSettingFukukitaruOrderWashNameService;

    @Autowired
    private ScreenSettingFukukitaruOrderHangTagService screenSettingFukukitaruOrderHangTagService;

    @Autowired
    private ScreenSettingFukukitaruItemService screenSettingFukukitaruItemService;


    /**
     * フクキタル洗濯ネーム発注画面の設定情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ScreenSettingFukukitaruSearchConditionModel} instance
     * @return {@link ScreenSettingFukukiatruModel} instance
     */
    @PostMapping("/fukukitaru/orders/washName")
    public GenericListMobel<ScreenSettingFukukiatruModel> listScreenSettingsFukukitaruOrderWashName(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final ScreenSettingFukukitaruSearchConditionModel searchCondition) {

        final ListServiceResponse<ScreenSettingFukukiatruModel> serviceResponse = screenSettingFukukitaruOrderWashNameService
                .call(ListServiceParameter.<ScreenSettingFukukitaruSearchConditionModel>builder()
                        .loginUser(loginUser)
                        .searchCondition(searchCondition)
                        .build());

        final GenericListMobel<ScreenSettingFukukiatruModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

    /**
     * フクキタル下札発注画面の設定情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ScreenSettingFukukitaruSearchConditionModel} instance
     * @return {@link ScreenSettingFukukiatruModel} instance
     */
    @PostMapping("/fukukitaru/orders/bottomBill")
    public GenericListMobel<ScreenSettingFukukiatruModel> listScreenSettingsFukukitaruOrderHangTag(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final ScreenSettingFukukitaruSearchConditionModel searchCondition) {

        final ListServiceResponse<ScreenSettingFukukiatruModel> serviceResponse = screenSettingFukukitaruOrderHangTagService
                .call(ListServiceParameter.<ScreenSettingFukukitaruSearchConditionModel>builder()
                        .loginUser(loginUser)
                        .searchCondition(searchCondition)
                        .build());

        final GenericListMobel<ScreenSettingFukukiatruModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

    /**
     * 商品情報/品番情報画面のフクキタル用の画面設定情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ScreenSettingFukukitaruSearchConditionModel} instance
     * @return {@link ScreenSettingFukukiatruModel} instance
     */
    @PostMapping("/fukukitaru/items")
    public GenericListMobel<ScreenSettingFukukiatruModel> listScreenSettingsFukukitaruItem(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final ScreenSettingFukukitaruSearchConditionModel searchCondition) {

        final ListServiceResponse<ScreenSettingFukukiatruModel> serviceResponse = screenSettingFukukitaruItemService
                .call(ListServiceParameter.<ScreenSettingFukukitaruSearchConditionModel>builder()
                        .loginUser(loginUser)
                        .searchCondition(searchCondition)
                        .build());

        final GenericListMobel<ScreenSettingFukukiatruModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
