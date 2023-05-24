package jp.co.jun.edi.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.MisleadingRepresentationFileModel;
import jp.co.jun.edi.model.MisleadingRepresentationFilePostModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.MisleadingRepresentationFileCreateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;

/**
 * 優良誤認検査ファイル情報API.
 */
@RestController
@RequestMapping("/api/v1/misleadingRepresentationFiles")
public class MisleadingRepresentationFileV1Api {

    @Autowired
    private MisleadingRepresentationFileCreateService createService;

    /**
     * 優良誤認検査ファイル情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link MisleadingRepresentationModel} instance
     * @return {@link MisleadingRepresentationModel} instance
     */
    @PostMapping
    public GenericListMobel<MisleadingRepresentationFileModel> create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final MisleadingRepresentationFilePostModel item) {
        final ListServiceResponse<MisleadingRepresentationFileModel> serviceResponse =
                createService.call(CreateServiceParameter.<MisleadingRepresentationFilePostModel>builder().loginUser(loginUser).item(item).build());

        final GenericListMobel<MisleadingRepresentationFileModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());

        return response;
    }

}
