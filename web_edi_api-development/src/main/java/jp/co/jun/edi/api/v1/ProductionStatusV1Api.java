package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ProductionStatusHistoryModel;
import jp.co.jun.edi.model.ProductionStatusHistorySearchConditionModel;
import jp.co.jun.edi.model.ProductionStatusModel;
import jp.co.jun.edi.model.ProductionStatusSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ProductionStatusCreateService;
import jp.co.jun.edi.service.ProductionStatusHistoryListService;
import jp.co.jun.edi.service.ProductionStatusListService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;

/**
 * 生産ステータスAPI.
 */
@RestController
@RequestMapping("/api/v1/productionStatus")
public class ProductionStatusV1Api {
    @Autowired
    private ProductionStatusCreateService createService;

    @Autowired
    private ProductionStatusListService listService;

    @Autowired
    private ProductionStatusHistoryListService historyListService;


    /**
     * 生産ステータスを作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link ProductionStatusModel} instance
     * @return {@link ProductionStatusModel} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public ProductionStatusModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final ProductionStatusModel item) {
        return createService.call(CreateServiceParameter.<ProductionStatusModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 生産ステータスをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ProductionStatusSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<ProductionStatusModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final ProductionStatusSearchConditionModel searchCondition) {
        final ListServiceResponse<ProductionStatusModel> serviceResponse = listService
                .call(ListServiceParameter.<ProductionStatusSearchConditionModel>builder().loginUser(loginUser)
                .searchCondition(searchCondition).build());

        final GenericListMobel<ProductionStatusModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

    /**
     * 生産ステータス履歴を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ProductionStatusHistorySearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping(":history")
    public GenericListMobel<ProductionStatusHistoryModel> historyList(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final ProductionStatusHistorySearchConditionModel searchCondition) {
        final ListServiceResponse<ProductionStatusHistoryModel> serviceResponse = historyListService
                .call(ListServiceParameter.<ProductionStatusHistorySearchConditionModel>builder()
                        .loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<ProductionStatusHistoryModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
