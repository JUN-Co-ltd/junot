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
import jp.co.jun.edi.model.ItemMisleadingRepresentationModel;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchConditionModel;
import jp.co.jun.edi.model.ItemMisleadingRepresentationSearchResultModel;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.maint.MaintNewsModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ItemMisleadingRepresentationGetService;
import jp.co.jun.edi.service.ItemMisleadingRepresentationSearchService;
import jp.co.jun.edi.service.ItemMisleadingRepresentationUpdateService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * 優良誤認検査承認一覧情報API.
 */
@RestController
@RequestMapping("/api/v1/itemMisleadingRepresentations")
public class ItemMisleadingRepresentationV1Api {

    @Autowired
    private ItemMisleadingRepresentationSearchService searchService;

    @Autowired
    private ItemMisleadingRepresentationGetService getService;

    @Autowired
    private ItemMisleadingRepresentationUpdateService updateService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    /**
     * 優良誤認情報を取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id ID
     * @return {@link MaintNewsModel} instance
     */
    @GetMapping("/{id}")
    public ItemMisleadingRepresentationModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id) {

        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(id).build()).getItem();
    }

    /**
     * 優良誤認情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param id 品番ID
     * @param item {@link OrderModel} instance
     * @return {@link OrderModel} instance
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_QA')")
    public ItemMisleadingRepresentationModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("id") final BigInteger id,
            @RequestBody @Validated(UpdateValidationGroup.class) final ItemMisleadingRepresentationModel item) {

        item.setId(id);

        return updateService.call(UpdateServiceParameter.<ItemMisleadingRepresentationModel>builder().loginUser(loginUser).item(item)
                .preItem(getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(id).build()).getItem())
                .build()).getItem();
    }

    /**
     * 優良誤認検査承認一覧情報を検索する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ItemMisleadingRepresentationSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<ItemMisleadingRepresentationSearchResultModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final ItemMisleadingRepresentationSearchConditionModel searchCondition) {

        final ItemMisleadingRepresentationSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, ItemMisleadingRepresentationSearchConditionModel.class);

        final ListServiceResponse<ItemMisleadingRepresentationSearchResultModel> serviceResponse = searchService
                .call(ListServiceParameter.<ItemMisleadingRepresentationSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<ItemMisleadingRepresentationSearchResultModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }
}
