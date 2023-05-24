package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.DelischeDeliverySkuSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.VDelischeDeliverySkuModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DelischeDeliverySkuListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * デリスケ納品SKUAPI.
 */
@RestController
@RequestMapping("/api/v1/delischeDeliverySkus")
@Secured("ROLE_JUN")
public class DelischeDeliverySkuV1Api {
    @Autowired
    private DelischeDeliverySkuListService listService;

    /**
     * デリスケ納品Sku情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DelischeDeliverySkuSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<VDelischeDeliverySkuModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final DelischeDeliverySkuSearchConditionModel searchCondition) {
        final ListServiceResponse<VDelischeDeliverySkuModel> serviceResponse = listService
                .call(ListServiceParameter.<DelischeDeliverySkuSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<VDelischeDeliverySkuModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
