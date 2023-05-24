package jp.co.jun.edi.api.v1;

import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.MakerReturnProductCompositeModel;
import jp.co.jun.edi.model.MakerReturnProductSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.MakerReturnProductSearchService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * メーカー返品商品情報API.
 */
@RestController
@RequestMapping("/api/v1/makerReturnProducts")
@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN') or hasRole('ROLE_EDI')")
public class MakerReturnProductV1Api {

    @Autowired
    private MakerReturnProductSearchService searchService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    /**
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link MakerReturnProductSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<MakerReturnProductCompositeModel> search(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(Default.class) final MakerReturnProductSearchConditionModel searchCondition) {
        final MakerReturnProductSearchConditionModel localSearchCondition = searchConditionComponent.
                getSearchCondition(searchCondition, MakerReturnProductSearchConditionModel.class);

        final ListServiceResponse<MakerReturnProductCompositeModel> serviceResponse = searchService
                .call(ListServiceParameter.<MakerReturnProductSearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<MakerReturnProductCompositeModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }
}
