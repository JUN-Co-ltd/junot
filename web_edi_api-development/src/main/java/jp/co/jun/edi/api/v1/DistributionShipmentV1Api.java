package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.DistributionShipmentConfirmListModel;
import jp.co.jun.edi.model.DistributionShipmentSearchConditionModel;
import jp.co.jun.edi.model.DistributionShipmentSearchResultModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DistributionShipmentConfirmService;
import jp.co.jun.edi.service.DistributionShipmentSearchListService;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * 配分出荷指示API.
 */
@RestController
@RequestMapping("/api/v1/distributionShipments")
@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
public class DistributionShipmentV1Api {

    @Autowired
    private DistributionShipmentSearchListService searchService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    @Autowired
    private DistributionShipmentConfirmService confirmService;

    /**
     * 納品情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DistributionShipmentSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping("/search")
    public GenericListMobel<DistributionShipmentSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final DistributionShipmentSearchConditionModel searchCondition) {
        final DistributionShipmentSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, DistributionShipmentSearchConditionModel.class);

        final ListServiceResponse<DistributionShipmentSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<DistributionShipmentSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<DistributionShipmentSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));
        return response;
    }

    /**
     * 納品明細情報の出荷指示済みフラグ、出荷指示日を更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link DistributionShipmentConfirmListModel} instance
     * @return {@link DistributionShipmentConfirmListModel} instance
     */
    @PutMapping("/confirm")
    public ApprovalServiceResponse confirm(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(UpdateValidationGroup.class) final DistributionShipmentConfirmListModel item) {
        return confirmService.call(
                ApprovalServiceParameter.<DistributionShipmentConfirmListModel>builder()
                .loginUser(loginUser).item(item).build());
    }

}
