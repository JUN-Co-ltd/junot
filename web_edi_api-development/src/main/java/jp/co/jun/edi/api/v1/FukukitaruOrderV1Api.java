package jp.co.jun.edi.api.v1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.validation.groups.Default;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.api.ValidateException;
import jp.co.jun.edi.model.FukukitaruOrderModel;
import jp.co.jun.edi.model.FukukitaruOrderSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.model.ValidateModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.FukukitaruOrderApproveService;
import jp.co.jun.edi.service.FukukitaruOrderConfirmService;
import jp.co.jun.edi.service.FukukitaruOrderCreateService;
import jp.co.jun.edi.service.FukukitaruOrderDeleteService;
import jp.co.jun.edi.service.FukukitaruOrderGetService;
import jp.co.jun.edi.service.FukukitaruOrderListService;
import jp.co.jun.edi.service.FukukitaruOrderUpdateService;
import jp.co.jun.edi.service.materialorder.MaterialOrderValidateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * フクキタル発注API.
 */
@RestController
@RequestMapping("/api/v1/fukukitaru/orders")
public class FukukitaruOrderV1Api {
    @Autowired
    private MaterialOrderValidateService materialOrderValidateService;

    @Autowired
    private FukukitaruOrderCreateService createService;

    @Autowired
    private FukukitaruOrderGetService getService;

    @Autowired
    private FukukitaruOrderUpdateService updateService;

    @Autowired
    private FukukitaruOrderConfirmService confirmService;

    @Autowired
    private FukukitaruOrderApproveService approveService;

    @Autowired
    private FukukitaruOrderDeleteService deleteService;

    @Autowired
    private FukukitaruOrderListService listService;

    /**
     * フクキタル発注情報を作成します.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param item
     *            {@link FukukitaruOrderModel} instance
     * @return {@link FukukitaruOrderModel} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public FukukitaruOrderModel create(@AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final FukukitaruOrderModel item) {
        // バリデーショングループリストの作成
        final List<Object> validationGroups = new ArrayList<>();
        validationGroups.add(CreateValidationGroup.class);
        validationGroups.add(Default.class);

        // バリデーションサービス呼び出し(戻り値：ValidateModel)
        final ValidateModel validate = materialOrderValidateService.call(ValidateServiceParameter.<FukukitaruOrderModel>builder()
                .loginUser(loginUser).item(item).validationGroups(validationGroups).build()).getItem();

        // エラーがある場合は例外(ValidateException)を投げる
        if (CollectionUtils.isNotEmpty(validate.getErrors())) {
            throw new ValidateException(validate);
        }

        return createService.call(CreateServiceParameter.<FukukitaruOrderModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * フクキタル発注情報を取得します.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param fOrderId
     *            フクキタル発注ID
     * @return {@link FukukitaruOrderModel} instance
     */
    @GetMapping("/{fOrderId}")
    public FukukitaruOrderModel get(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("fOrderId") final BigInteger fOrderId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(fOrderId).build()).getItem();
    }

    /**
     * フクキタル発注情報を更新します.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param fOrderId
     *            フクキタル発注ID
     * @param item
     *            {@link FukukitaruOrderModel} instance
     * @return {@link FukukitaruOrderModel} instance
     */
    @PutMapping("/{fOrderId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public FukukitaruOrderModel update(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("fOrderId") final BigInteger fOrderId,
            @RequestBody @Validated(UpdateValidationGroup.class) final FukukitaruOrderModel item) {
        item.setId(fOrderId);

        // バリデーショングループリストの作成
        final List<Object> validationGroups = new ArrayList<>();
        validationGroups.add(CreateValidationGroup.class);
        validationGroups.add(Default.class);

        // バリデーションサービス呼び出し(戻り値：ValidateModel)
        final ValidateModel validate = materialOrderValidateService.call(ValidateServiceParameter.<FukukitaruOrderModel>builder()
                .loginUser(loginUser).item(item).validationGroups(validationGroups).build()).getItem();

        // エラーがある場合は例外(ValidateException)を投げる
        if (CollectionUtils.isNotEmpty(validate.getErrors())) {
            throw new ValidateException(validate);
        }

        return updateService.call(UpdateServiceParameter.<FukukitaruOrderModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * フクキタル発注情報を確定します.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param fOrderId
     *            フクキタル発注ID
     * @param item
     *            {@link FukukitaruOrderModel} instance
     * @return {@link FukukitaruOrderModel} instance
     */
    @PutMapping("/{fOrderId}/confirm")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public FukukitaruOrderModel confirm(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("fOrderId") final BigInteger fOrderId,
            @RequestBody @Validated(UpdateValidationGroup.class) final FukukitaruOrderModel item) {
        item.setId(fOrderId);

        // バリデーショングループリストの作成
        final List<Object> validationGroups = new ArrayList<>();
        validationGroups.add(CreateValidationGroup.class);
        validationGroups.add(Default.class);

        // バリデーションサービス呼び出し(戻り値：ValidateModel)
        final ValidateModel validate = materialOrderValidateService.call(ValidateServiceParameter.<FukukitaruOrderModel>builder()
                .loginUser(loginUser).item(item).validationGroups(validationGroups).build()).getItem();

        // エラーがある場合は例外(ValidateException)を投げる
        if (CollectionUtils.isNotEmpty(validate.getErrors())) {
            throw new ValidateException(validate);
        }

        return confirmService.call(UpdateServiceParameter.<FukukitaruOrderModel>builder().loginUser(loginUser).item(item).build()).getItem();

    }

    /**
     * フクキタル発注情報を承認します.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param fOrderId
     *            フクキタル発注ID
     * @param item
     *            {@link FukukitaruOrderModel} instance
     * @return {@link FukukitaruOrderModel} instance
     */
    @PutMapping("/{fOrderId}/approve")
    @PreAuthorize("hasRole('ROLE_EDI')")
    public FukukitaruOrderModel approve(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("fOrderId") final BigInteger fOrderId,
            @RequestBody @Validated(UpdateValidationGroup.class) final FukukitaruOrderModel item) {
        item.setId(fOrderId);
        // 戻り値としてModelを返す必要があるため、ApproveServiceParameterではなくUpdateServiceParameterを使用
        return approveService.call(UpdateServiceParameter.<FukukitaruOrderModel>builder().loginUser(loginUser).item(item).build()).getItem();

    }

    /**
     * TODO [未使用] フクキタル発注情報を削除します.     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param fOrderId
     *            発注ID
     */
    //@DeleteMapping("/{fOrderId}")
    public void delete(@AuthenticationPrincipal final CustomLoginUser loginUser, @PathVariable("fOrderId") final BigInteger fOrderId) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(fOrderId).build());

        return;
    }

    /**
     * フクキタル発注情報をリストで取得します.
     *
     * @param loginUser
     *            {@link CustomLoginUser} instance
     * @param searchCondition
     *            {@link ItemSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<FukukitaruOrderModel> list(@AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute final FukukitaruOrderSearchConditionModel searchCondition) {

        final ListServiceResponse<FukukitaruOrderModel> serviceResponse = listService
                .call(ListServiceParameter.<FukukitaruOrderSearchConditionModel>builder().loginUser(loginUser).searchCondition(searchCondition).build());

        final GenericListMobel<FukukitaruOrderModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

}
