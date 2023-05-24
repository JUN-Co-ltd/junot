package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ScreenSettingDeliveryModel;
import jp.co.jun.edi.model.ScreenSettingDeliverySearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ScreenSettingDeliveryService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 納品依頼関連の画面構成情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/screenSettings")
public class DeliveryScreenSettingV1Api {
    @Autowired
    private ScreenSettingDeliveryService screenSettingDeliveryService;

    /**
     * 納品依頼画面の設定情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ScreenSettingDeliverySearchConditionModel} instance
     * @return {@link ScreenSettingDeliveryModel} instance
     */
    @PostMapping("/delivery")
    public GenericListMobel<ScreenSettingDeliveryModel> listScreenSettingsDelivery(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final ScreenSettingDeliverySearchConditionModel searchCondition) {

        final ListServiceResponse<ScreenSettingDeliveryModel> serviceResponse = screenSettingDeliveryService
                .call(ListServiceParameter.<ScreenSettingDeliverySearchConditionModel>builder()
                        .loginUser(loginUser)
                        .searchCondition(searchCondition)
                        .build());

        final GenericListMobel<ScreenSettingDeliveryModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

}
