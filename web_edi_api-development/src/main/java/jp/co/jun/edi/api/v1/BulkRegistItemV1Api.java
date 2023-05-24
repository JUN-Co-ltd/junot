package jp.co.jun.edi.api.v1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jp.co.jun.edi.component.bulkregist.BulkRegistItemComponent;
import jp.co.jun.edi.model.BulkRegistItemModel;
import jp.co.jun.edi.model.MultipartFileModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.bulkregist.item.BulkRegistItemPreRegistValidateService;
import jp.co.jun.edi.service.bulkregist.item.BulkRegistItemRegistValidateService;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;

/**
 * 品番・商品一括登録API.
 */
@RestController
@RequestMapping("/api/v1/bulkRegistItems")
@PreAuthorize("hasRole('ROLE_EDI')")
public class BulkRegistItemV1Api {
    @Autowired
    private BulkRegistItemPreRegistValidateService preRegistValidateService;

    @Autowired
    private BulkRegistItemRegistValidateService registValidateService;

    @Autowired
    private BulkRegistItemComponent bulkRegistItemComponent;

    /**
     * 仮登録（商品登録）可能か検証する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param file {@link MultipartFile} instance
     * @return {@link BulkRegistItemModel} instance
     */
    @PostMapping("/preRegistValidate")
    public BulkRegistItemModel preRegistValidate(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestParam("file") final MultipartFile file) {

        // validationGroupsは、BulkRegistItemComponent.Validator.toValidationHints()で、データごとに付与するため、ダミーの空のリストを設定する
        final List<Object> validationGroups = new ArrayList<>();

        final BulkRegistItemModel model = preRegistValidateService.call(ValidateServiceParameter.<MultipartFileModel>builder()
                .loginUser(loginUser).item(MultipartFileModel.of(file)).validationGroups(validationGroups).build()).getItem();

        return model;
    }

    /**
     * 仮登録（商品登録）する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param file {@link MultipartFile} instance
     * @return {@link BulkRegistItemModel} instance
     */
    @PostMapping("/preRegist")
    public BulkRegistItemModel preRegist(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestParam("file") final MultipartFile file) {

        // validationGroupsは、BulkRegistItemComponent.Validator.toValidationHints()で、データごとに付与するため、ダミーの空のリストを設定する
        final List<Object> validationGroups = new ArrayList<>();

        final BulkRegistItemModel model = preRegistValidateService.call(ValidateServiceParameter.<MultipartFileModel>builder()
                .loginUser(loginUser).item(MultipartFileModel.of(file)).validationGroups(validationGroups).build()).getItem();

        bulkRegistItemComponent.registAsync(
                SecurityContextHolder.getContext().getAuthentication(),
                loginUser,
                model);

        return model;
    }

    /**
     * 本登録（品番登録）可能か検証する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param file {@link MultipartFile} instance
     * @return {@link BulkRegistItemModel} instance
     */
    @PostMapping("/registValidate")
    public BulkRegistItemModel registValidate(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestParam("file") final MultipartFile file) {

        // validationGroupsは、BulkRegistItemComponent.Validator.toValidationHints()で、データごとに付与するため、ダミーの空のリストを設定する
        final List<Object> validationGroups = new ArrayList<>();

        final BulkRegistItemModel model = registValidateService.call(ValidateServiceParameter.<MultipartFileModel>builder()
                .loginUser(loginUser).item(MultipartFileModel.of(file)).validationGroups(validationGroups).build()).getItem();

        return model;
    }

    /**
     * 本登録（品番登録）する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param file {@link MultipartFile} instance
     * @return {@link BulkRegistItemModel} instance
     */
    @PostMapping("/regist")
    public BulkRegistItemModel regist(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestParam("file") final MultipartFile file) {

        // validationGroupsは、BulkRegistItemComponent.Validator.toValidationHints()で、データごとに付与するため、ダミーの空のリストを設定する
        final List<Object> validationGroups = new ArrayList<>();

        final BulkRegistItemModel model = registValidateService.call(ValidateServiceParameter.<MultipartFileModel>builder()
                .loginUser(loginUser).item(MultipartFileModel.of(file)).validationGroups(validationGroups).build()).getItem();

        bulkRegistItemComponent.registAsync(
                SecurityContextHolder.getContext().getAuthentication(),
                loginUser,
                model);

        return model;
    }
}
