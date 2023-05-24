package jp.co.jun.edi.api.v1;
import java.math.BigInteger;

import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.component.model.MakerReturnKeyModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.MakerReturnInstructionListModel;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.model.MakerReturnSearchResultModel;
import jp.co.jun.edi.model.PurchaseModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.MakerReturnConfirmService;
import jp.co.jun.edi.service.MakerReturnCreateService;
import jp.co.jun.edi.service.MakerReturnDeleteService;
import jp.co.jun.edi.service.MakerReturnGetService;
import jp.co.jun.edi.service.MakerReturnSearchService;
import jp.co.jun.edi.service.MakerReturnUpdateService;
import jp.co.jun.edi.service.parameter.ApprovalServiceParameter;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ApprovalServiceResponse;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * メーカー返品API.
 */
@RestController
@RequestMapping("/api/v1/makerReturns")
@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_EDI')")
public class MakerReturnV1Api {

    @Autowired
    private MakerReturnGetService getService;

    @Autowired
    private MakerReturnCreateService createService;

    @Autowired
    private MakerReturnUpdateService updateService;

    @Autowired
    private MakerReturnDeleteService deleteService;

    @Autowired
    private MakerReturnSearchService searchService;

    @Autowired
    private MakerReturnConfirmService confirmService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    /**
     * メーカー返品情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     * @return {@link ItemModel} instance
     */
    @GetMapping("/{voucherNumber}")
    public MakerReturnModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("voucherNumber") final String voucherNumber,
            @RequestParam final BigInteger orderId) {
        final MakerReturnKeyModel key = new MakerReturnKeyModel(voucherNumber, orderId);
        return getService.call(GetServiceParameter.<MakerReturnKeyModel>builder().loginUser(loginUser).id(key).build()).getItem();
    }

    /**
     * メーカー返品情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link PurchaseModel} instance
     * @return {@link GenericListMobel<PurchaseModel>} instance
     */
    @PostMapping
    public MakerReturnModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final MakerReturnModel item) {
        return createService.call(CreateServiceParameter.<MakerReturnModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * メーカー返品情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link ItemModel} instance
     * @return {@link ItemModel} instance
     */
    @PutMapping
    public MakerReturnModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final MakerReturnModel item) {
        return updateService.call(UpdateServiceParameter.<MakerReturnModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * メーカー返品情報を削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param voucherNumber 伝票番号
     * @param orderId 発注ID
     */
    @DeleteMapping("/{voucherNumber}")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("voucherNumber") final String voucherNumber,
            @RequestParam final BigInteger orderId) {

        final MakerReturnKeyModel key = new MakerReturnKeyModel(voucherNumber, orderId);
        deleteService.call(DeleteServiceParameter.<MakerReturnKeyModel>builder().loginUser(loginUser).id(key).build());
        return;
    }

    /**
     * メーカー返品一覧情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link MakerReturnSearchResultModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<MakerReturnModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final MakerReturnSearchResultModel searchCondition) {

        final MakerReturnSearchResultModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, MakerReturnSearchResultModel.class);
        final ListServiceResponse<MakerReturnModel> serviceResponse = searchService
                .call(ListServiceParameter.<MakerReturnSearchResultModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());
        final GenericListMobel<MakerReturnModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));
        return response;
    }

    /**
     * メーカ返品情報のLG送信区分、LG送信対象グループIDを更新する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link MakerReturnInstructionListModel} instance
     * @return {@link MakerReturnInstructionListModel} instance
     */
    @PutMapping("/confirm")
    public ApprovalServiceResponse confirm(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(UpdateValidationGroup.class) final MakerReturnInstructionListModel item) {

        return confirmService.call(ApprovalServiceParameter.<MakerReturnInstructionListModel>builder().loginUser(loginUser).item(item).build());
    }
}
