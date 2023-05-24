package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.validation.groups.Default;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.api.ValidateException;
import jp.co.jun.edi.component.ItemComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.ItemSearchConditionModel;
import jp.co.jun.edi.model.ValidateModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.ItemArticleNumberCheckService;
import jp.co.jun.edi.service.ItemCreateService;
import jp.co.jun.edi.service.ItemDeleteService;
import jp.co.jun.edi.service.ItemGetService;
import jp.co.jun.edi.service.ItemListService;
import jp.co.jun.edi.service.ItemUpdateService;
import jp.co.jun.edi.service.item.ItemValidateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.validation.group.CreateValidationGroup;
import jp.co.jun.edi.validation.group.UpdateValidationGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 品番API.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/items")
public class ItemV1Api {
    @Autowired
    private ItemCreateService createService;

    @Autowired
    private ItemGetService getService;

    @Autowired
    private ItemListService listService;

    @Autowired
    private ItemUpdateService updateService;

    @Autowired
    private ItemDeleteService deleteService;

    @Autowired
    private ItemArticleNumberCheckService articleNumberCheckService;

    @Autowired
    private ItemValidateService itemValidateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemComponent itemComponent;

    /**
     * 品番情報を作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link ItemModel} instance
     * @return {@link ItemModel} instance
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public ItemModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated({CreateValidationGroup.class, Default.class}) final ItemModel item) {
        // 対象項目を右トリム
        itemComponent.rtrimToNull(item);

        // バリデーショングループリストの作成
        final List<Object> validationGroups = new ArrayList<>();
        validationGroups.add(CreateValidationGroup.class);
        validationGroups.add(Default.class);

        // バリデーションサービス呼び出し(戻り値：ValidateModel)
        final ValidateModel validate = itemValidateService.call(ValidateServiceParameter.<ItemModel>builder()
                        .loginUser(loginUser).item(item).validationGroups(validationGroups).build()).getItem();

        // エラーがある場合は例外(ValidateException)を投げる
        if (CollectionUtils.isNotEmpty(validate.getErrors())) {
            throw new ValidateException(validate);
        }
        return createService.call(CreateServiceParameter.<ItemModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * 品番情報を削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param itemId 品番ID
     */
    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("itemId") final BigInteger itemId) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(itemId).build());

        return;
    }

    /**
     * 品番情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param itemId 品番ID
     * @return {@link ItemModel} instance
     */
    @GetMapping("/{itemId}")
    public ItemModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("itemId") final BigInteger itemId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(itemId).build()).getItem();
    }

    /**
     * 品番情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link ItemSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<ItemModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final ItemSearchConditionModel searchCondition) {

        final ItemSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<ItemModel> serviceResponse = listService
                .call(ListServiceParameter.<ItemSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<ItemModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * 品番情報を更新します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param itemId 品番ID
     * @param item {@link ItemModel} instance
     * @return {@link ItemModel} instance
     */
    @PutMapping("/{itemId}")
    @PreAuthorize("hasRole('ROLE_EDI') or hasRole('ROLE_MAKER')")
    public ItemModel update(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("itemId") final BigInteger itemId,
            @RequestBody @Validated({UpdateValidationGroup.class, Default.class}) final ItemModel item) {
        // 対象項目を右トリム
        itemComponent.rtrimToNull(item);

        item.setId(itemId);
        // バリデーショングループリストの作成
        final List<Object> validationGroups = new ArrayList<>();
        validationGroups.add(UpdateValidationGroup.class);
        validationGroups.add(Default.class);

        // バリデーションサービス呼び出し(戻り値：ValidateModel)
        final ValidateModel validate = itemValidateService.call(ValidateServiceParameter.<ItemModel>builder()
                        .loginUser(loginUser).item(item).validationGroups(validationGroups).build()).getItem();

        // エラーがある場合は例外(ValidateException)を投げる
        if (CollectionUtils.isNotEmpty(validate.getErrors())) {
            throw new ValidateException(validate);
        }

        return updateService.call(UpdateServiceParameter.<ItemModel>builder()
                .loginUser(loginUser)
                .item(item)
                .preItem(getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(itemId).build()).getItem())
                .build())
                .getItem();
    }

    /**
     * JAN/UPCのバリデーションチェックを行います.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param item {@link ItemModel} instance
     * @return {@link ItemModel} instance
     */
    @PostMapping("/validArticleNumber")
    public ValidateModel validArticleNumber(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated(CreateValidationGroup.class) final ItemModel item) {

        return articleNumberCheckService.call(UpdateServiceParameter.<ItemModel>builder().loginUser(loginUser).item(item).build()).getItem();
    }

    /**
     * モデルを取得します.
     *
     * @param searchCondition {@link ItemSearchConditionModel} instance
     * @return {@link ItemSearchConditionModel} instance
     */
    private ItemSearchConditionModel getSearchCondition(final ItemSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final ItemSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        ItemSearchConditionModel.class);

                log.info(localSearchCondition.toString());

                return localSearchCondition;
            } catch (IOException e) {
                log.warn("IOException.", e);
            }
        }

        return searchCondition;
    }

    /**
     * NextPageTokenを取得します.
     *
     * @param searchCondition {@link ItemSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final ItemSearchConditionModel searchCondition,
            final boolean nextPage) {

        String nextPageToken = "";

        if (!nextPage) {
            return nextPageToken;
        }

        searchCondition.setPageToken(null);
        searchCondition.setPage(searchCondition.getPage() + 1);

        try {
            nextPageToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(searchCondition));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException.", e);
        }

        return nextPageToken;
    }
}
