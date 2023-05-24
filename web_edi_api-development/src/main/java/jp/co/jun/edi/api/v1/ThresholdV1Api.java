package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ThresholdModel;
import jp.co.jun.edi.model.ThresholdSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ThresholdListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * 閾値情報を取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/thresholds")
public class ThresholdV1Api {
    @Autowired
    private ThresholdListService listService;

    /**
     * 引数の情報から閾値情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ThresholdSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<ThresholdModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final ThresholdSearchConditionModel searchCondition) {
        final ListServiceResponse<ThresholdModel> serviceResponse = listService
                .call(ListServiceParameter.<ThresholdSearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<ThresholdModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
