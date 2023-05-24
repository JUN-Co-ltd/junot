package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.DelischeDeliveryRequestSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.VDelischeDeliveryRequestModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DelischeDeliveryRequestListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * デリスケ納品依頼API.
 */
@RestController
@RequestMapping("/api/v1/delischeDeliveryRequests")
@Secured("ROLE_JUN")
public class DelischeDeliveryRequestV1Api {
    @Autowired
    private DelischeDeliveryRequestListService listService;

    /**
     * デリスケ納品依頼情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DelischeDeliveryRequestSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<VDelischeDeliveryRequestModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final DelischeDeliveryRequestSearchConditionModel searchCondition) {
        final ListServiceResponse<VDelischeDeliveryRequestModel> serviceResponse = listService
                .call(ListServiceParameter.<DelischeDeliveryRequestSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<VDelischeDeliveryRequestModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
