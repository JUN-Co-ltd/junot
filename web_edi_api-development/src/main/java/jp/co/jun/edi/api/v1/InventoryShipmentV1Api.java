package jp.co.jun.edi.api.v1;

import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.InventoryShipmentConfirmListModel;
import jp.co.jun.edi.model.InventoryShipmentSearchConditionModel;
import jp.co.jun.edi.model.InventoryShipmentSearchResultModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.InventoryShipmentConfirmService;
import jp.co.jun.edi.service.InventoryShipmentSearchListService;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * 在庫出荷指示API.
 */
@RestController
@RequestMapping("/api/v1/inventoryShipment")
@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
public class InventoryShipmentV1Api {
    @Autowired
    private InventoryShipmentSearchListService searchService;
    @Autowired
    private SearchConditionComponent searchConditionComponent;
    @Autowired
    private InventoryShipmentConfirmService confirmService;

    /**
     * 在庫出荷指示データをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link InventoryShipmentSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    public GenericListMobel<InventoryShipmentSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final InventoryShipmentSearchConditionModel searchCondition) {
        final InventoryShipmentSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, InventoryShipmentSearchConditionModel.class);
        final ListServiceResponse<InventoryShipmentSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<InventoryShipmentSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());
        final GenericListMobel<InventoryShipmentSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));
        return response;
    }

    /**
     * 在庫出荷指示情報のLg送信区分を更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link InventoryShipmentConfirmListModel} instance
     * @return {@link InventoryShipmentConfirmListModel} instance
     */
    @PutMapping("/confirm")
    public ApprovalServiceResponse confirm(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(UpdateValidationGroup.class) final InventoryShipmentConfirmListModel item) {
        return confirmService.call(
                ApprovalServiceParameter.<InventoryShipmentConfirmListModel>builder()
                .loginUser(loginUser).item(item).build());
    }
}
