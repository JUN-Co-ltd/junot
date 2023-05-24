package jp.co.jun.edi.api.v1;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import jp.co.jun.edi.model.FukukitaruItemModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.FukukitaruItemCreateService;
import jp.co.jun.edi.service.FukukitaruItemDeleteService;
import jp.co.jun.edi.service.FukukitaruItemGetService;
import jp.co.jun.edi.service.FukukitaruItemUpdateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;

/**
 * TODO [未使用] フクキタル品番API.
 */
//@RestController
//@RequestMapping("/api/v1/fukukitaru/items")
public class FukukitaruItemV1Api {
    @Autowired
    private FukukitaruItemCreateService createService;

    @Autowired
    private FukukitaruItemGetService getService;

    @Autowired
    private FukukitaruItemUpdateService updateService;

    @Autowired
    private FukukitaruItemDeleteService deleteService;


    /**
     * フクキタル品番情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link FukukitaruItemModel} instance
     * @return {@link FukukitaruItemModel} instance
     */
    //@PostMapping
    public FukukitaruItemModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final FukukitaruItemModel item) {
        return createService.call(CreateServiceParameter.<FukukitaruItemModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * フクキタル品番情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fItemId フクキタル品番ID
     * @return {@link FukukitaruItemModel} instance
     */
    //@GetMapping("/{fItemId}")
    public FukukitaruItemModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fItemId") final BigInteger fItemId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(fItemId).build()).getItem();
    }

    /**
     * フクキタル品番情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fItemId フクキタル品番ID
     * @param item {@link FukukitaruItemModel} instance
     * @return {@link FukukitaruItemModel} instance
     */
    //@PutMapping("/{fItemId}")
    public FukukitaruItemModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fItemId") final BigInteger fItemId,
            @RequestBody @Validated(UpdateValidationGroup.class) final FukukitaruItemModel item) {
        item.setId(fItemId);
        return updateService.call(UpdateServiceParameter.<FukukitaruItemModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }


    /**
     * フクキタル品番情報を削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fItemId フクキタル品番ID
     */
    //@DeleteMapping("/{fItemId}")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fItemId") final BigInteger fItemId) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(fItemId).build());
        return;
    }



}
