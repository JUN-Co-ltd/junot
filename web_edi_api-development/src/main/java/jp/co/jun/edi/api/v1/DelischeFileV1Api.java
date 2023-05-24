package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.DelischeFileInfoModel;
import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DelischeFileCreateService;
import jp.co.jun.edi.service.DelischeFileInfoCreateService;
import jp.co.jun.edi.service.DelischeFileListService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;

/**
 * デリスケファイルAPI.
 */
@RestController
@RequestMapping("/api/v1/delische-files")
@Secured("ROLE_JUN")
public class DelischeFileV1Api {
    @Autowired
    private DelischeFileInfoCreateService infoCreateService;

    @Autowired
    private DelischeFileCreateService fileCreateService;

    @Autowired
    private DelischeFileListService listService;

    /**
     * デリスケファイルを作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DelischeOrderSearchConditionModel} instance
     */
    @PostMapping
    public void create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final DelischeOrderSearchConditionModel searchCondition) {
        final DelischeFileInfoModel delischeFileInfoModel = infoCreateService.call(CreateServiceParameter.<DelischeOrderSearchConditionModel>builder().
                loginUser(loginUser).item(searchCondition).build()).getItem();
        fileCreateService.callAsync(CreateServiceParameter.<DelischeFileInfoModel>builder().
                loginUser(loginUser).item(delischeFileInfoModel).build());
    }

    /**
     * デリスケファイル情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<DelischeFileInfoModel> list(@AuthenticationPrincipal final CustomLoginUser loginUser) {
        final ListServiceResponse<DelischeFileInfoModel> serviceResponse = listService.execute(loginUser.getUserId());

        final GenericListMobel<DelischeFileInfoModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }
}
