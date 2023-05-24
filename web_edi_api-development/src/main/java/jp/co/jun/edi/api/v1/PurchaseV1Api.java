package jp.co.jun.edi.api.v1;
import java.math.BigInteger;

import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.PurchaseConfirmListModel;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.model.PurchaseSearchConditionModel;
import jp.co.jun.edi.model.PurchaseSearchResultModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.PurchaseConfirmService;
import jp.co.jun.edi.service.PurchaseCreateService;
import jp.co.jun.edi.service.PurchaseGetService;
import jp.co.jun.edi.service.PurchaseSearchService;
import jp.co.jun.edi.service.PurchaseUpdateService;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * 仕入API.
 */
@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseV1Api {

    @Autowired
    private PurchaseGetService getService;

    @Autowired
    private PurchaseCreateService createService;

    @Autowired
    private PurchaseUpdateService updateService;

    @Autowired
    private PurchaseSearchService searchService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    @Autowired
    private PurchaseConfirmService confirmService;

    /**
     * 仕入情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param deliveryId 納品ID
     * @return {@link ItemModel} instance
     */
    @GetMapping("/{deliveryId}")
    public PurchaseModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("deliveryId") final BigInteger deliveryId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(deliveryId).build()).getItem();
    }

    /**
     * 仕入情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link PurchaseModel} instance
     * @return {@link GenericListMobel<PurchaseModel>} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    public PurchaseModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final PurchaseModel item) {
        return createService.call(CreateServiceParameter.<PurchaseModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 仕入情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link ItemModel} instance
     * @return {@link ItemModel} instance
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    public PurchaseModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final PurchaseModel item) {
        return updateService.call(UpdateServiceParameter.<PurchaseModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 仕入一覧を検索する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link PurchaseSearchConditionModel} instance
     * @return {@link GenericListModel} instance
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    public GenericListMobel<PurchaseSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final PurchaseSearchConditionModel searchCondition) {
        final PurchaseSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, PurchaseSearchConditionModel.class);
        final ListServiceResponse<PurchaseSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<PurchaseSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());
        final GenericListMobel<PurchaseSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));
        return response;
    }

    /**
     * 仕入情報のLG送信区分、LG送信対象グループIDを更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link PurchaseConfirmListModel} instance
     * @return {@link PurchaseConfirmListModel} instance
     */
    @PutMapping("/confirm")
    @PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    public ApprovalServiceResponse confirm(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(UpdateValidationGroup.class) final PurchaseConfirmListModel item) {

        return confirmService.call(ApprovalServiceParameter.<PurchaseConfirmListModel>builder().loginUser(loginUser).item(item).build());
    }
}
